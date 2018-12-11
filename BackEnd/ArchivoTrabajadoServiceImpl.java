package com.logismic.iscam.service.impl;

import com.logismic.iscam.domain.RegistroProcesoInformante;
import com.logismic.iscam.repository.ArchivoTrabajadoRepository;
import com.logismic.iscam.repository.BitacoraEventosRepository;
import com.logismic.iscam.repository.StatusRepository;
import com.logismic.iscam.service.*;
import com.logismic.iscam.service.dto.InformanteDTO;
import com.logismic.iscam.service.dto.PeriodoOperativoDTO;
import com.logismic.iscam.service.filesmanager.ArchivoTrabajadoFileService;
import com.logismic.iscam.service.util.TipoCargaEnum;
import com.logismic.iscam.web.rest.errors.IscamExcelReaderException;
import com.logismic.iscam.web.rest.errors.IscamFilesException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArchivoTrabajadoServiceImpl implements ArchivoTrabajadoService{
    private static final Integer DEPURACION = 6;
    private final Logger log = LoggerFactory.getLogger(ArchivoTrabajadoServiceImpl.class);
    private final String FILE_FORMAT = ".xlsx";

    private final ArchivoTrabajadoRepository archivoTrabajadoRepository;
    private final ArchivoTotalesService archivoTotalesService;
    private final InformanteService informanteService;
    private final PeriodoOperativoService periodoOperativoService;
    private final ArchivoTrabajadoReaderService workedReader;
    private final ArchivoTrabajadoFileService archivoTrabajadoFileService;
    private final BitacoraEventosRepository bitacoraEventosRepository;
    private final RegistroProcesosInformantesService registroProcesosInformantesService;
    private final StatusRepository statusRepository;

    public ArchivoTrabajadoServiceImpl(ArchivoTrabajadoRepository archivoTrabajadoRepository,
                                       ArchivoTotalesService archivoTotalesService,
                                       InformanteService informanteService,
                                       PeriodoOperativoService periodoOperativoService,
                                       ArchivoTrabajadoReaderService workedReader,
                                       ArchivoTrabajadoFileService archivoTrabajadoFileService,
                                       BitacoraEventosRepository bitacoraEventosRepository,
                                       RegistroProcesosInformantesService registroProcesosInformantesService,
                                       StatusRepository statusRepository) {
        this.archivoTrabajadoRepository = archivoTrabajadoRepository;
        this.archivoTotalesService = archivoTotalesService;
        this.informanteService = informanteService;
        this.periodoOperativoService = periodoOperativoService;
        this.workedReader = workedReader;
        this.archivoTrabajadoFileService = archivoTrabajadoFileService;
        this.bitacoraEventosRepository = bitacoraEventosRepository;
        this.registroProcesosInformantesService = registroProcesosInformantesService;
        this.statusRepository = statusRepository;
    }

    @Override
    public void process(ArrayList<MultipartFile> workedFiles, MultipartFile totalFile,
                        Long idInformante, Long idPeriodo, Integer uploadMethod, Integer replacement)
        throws Exception {

        //Recuperamos entidades que nos servirán para ciertos procesos (metodos)
        InformanteDTO informante = informanteService.findOne(idInformante);
        PeriodoOperativoDTO periodo = periodoOperativoService.findOne(idPeriodo);

        //Validamos que si el metodo de carga es "Nuevo" no existan archivos
        if (uploadMethod == TipoCargaEnum.NUEVO.getNumber()) {
            if (archivoTrabajadoFileService.alreadyExist(informante, periodo))
                throw new IscamFilesException(IscamFilesException.METHOD_NEW_BUT_ALREADY_EXIST);
        }
        //Primero validamos que los archivos no sean corruptos
         if (totalFile.isEmpty())
            throw new IscamExcelReaderException("ERROR(archivo totales): \n" + IscamExcelReaderException.EMPTY_FILE, totalFile.getName());
        if (workedFiles.size() < 1)
            throw new IscamExcelReaderException("ERROR(archivo trabajado): \n" + IscamExcelReaderException.FILES_NOT_PROVIDED);
        for (MultipartFile file : workedFiles) {
            if (file.isEmpty())
                throw new IscamExcelReaderException("ERROR(archivo trabajado): \nEl archivo " + file.getOriginalFilename() + " está vacío");
        }
        //Validamos y almacenamos los totales en un csv
        File totalCsv = new File("temp_" + totalFile.getOriginalFilename() + ".csv");
        PrintWriter writer = new PrintWriter(totalCsv);
        try {
            log.debug("creando CSV de carga de totales");
            archivoTotalesService.write(totalFile, writer, informante.getClaveIscam().trim(), periodo.getAcronimo());
        } catch (Exception e) {
            clean(writer,totalCsv);
            throw e;
        }
        writer.close();
        //Validamos y alamacenamos los archivos trabajados en csv's
        List<File> workedCsvFiles = new ArrayList<>();
        log.debug("Iniciando carga de trabajados");
        for (MultipartFile file : workedFiles) {
            File workedFile = new File("temp_" + file.getOriginalFilename() + ".csv");
            writer = new PrintWriter(workedFile);
            try {
                log.debug("Archivo: ", file.getName());
                write(file, writer, informante.getClaveIscam().trim(), periodo.getAcronimo());
            } catch (Exception e) {
                clean(writer, workedFile);
                throw e;
            }
            writer.close();
            workedCsvFiles.add(workedFile);
        }
        //Si llegamos hasta acá sin IscamExceptions, entonces procedemos a guardar los excel's
        //y a realizar la inserción de datos a TArchivosTrabajados.
        log.debug("limpiando base de datos para totales y trabajados");
        archivoTrabajadoRepository.deleteAllByClaveInformanteAndPeriodoAcronimo(informante.getClaveIscam(), periodo.getAcronimo());
        archivoTotalesService.deleteByClaveInformanteAndPeriodoAcronimo(informante.getClaveIscam(), periodo.getAcronimo());

       ///Realizamos la inserción masiva
        try {
            log.debug("realizando carga masiva");
            archivoTotalesService.storeData(totalCsv);
            storeData(workedCsvFiles);
        } finally {
            //Eliminamos los archivos temporales
            totalCsv.delete();
            for (File workedCsvFile : workedCsvFiles) {
                workedCsvFile.delete();
            }
        }

        //Almacenamos los archivos
        log.debug("almacenando los archivos en el direcotrio");
        saveFilesIntoDirectory(totalFile, workedFiles, informante, periodo);
        log.debug("fin. Carga completa");
    }

    private void storeData(List<File> workedCsvFiles) throws Exception {
        for (File file : workedCsvFiles) {
            archivoTrabajadoRepository.bulkInsert(file);
        }
    }

    private void clean(PrintWriter writer, File file) {
        writer.close();
        file.delete();
    }

    @Override
    public void write(MultipartFile file, PrintWriter temp_csv, String claveIscam, String periodoAcronimo)
        throws IscamExcelReaderException, IOException {
        if (file.getOriginalFilename().toLowerCase().endsWith(FILE_FORMAT)) {
            try {
                OPCPackage pkg = OPCPackage.open(file.getInputStream());
                workedReader.process(file.getName(), claveIscam, periodoAcronimo, pkg, temp_csv, 0);
            } catch (OpenXML4JException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                throw new IscamExcelReaderException(e.getMessage());
            }
        } else {
            throw new IscamExcelReaderException(IscamExcelReaderException.INVALID_FORMAT);
        }
    }

    @Override
    public Boolean validate(ArrayList<MultipartFile> workedFiles) {
        return null;
    }

    @Override
    public void saveFilesIntoDirectory(MultipartFile totalsFile, ArrayList<MultipartFile> workedFiles,
                                       InformanteDTO informante, PeriodoOperativoDTO periodo) throws IOException, IscamFilesException {
         //verificamos y asignamos directorio
        File directory = archivoTrabajadoFileService.createDirectory(informante, periodo);
        archivoTrabajadoFileService.setDirectory(directory);
        //versionamos archivos si es necesario
        archivoTrabajadoFileService.versionFiles();

        //Almacenamos los archivos excel
        archivoTrabajadoFileService.storeFile(totalsFile);
        archivoTrabajadoFileService.storeFiles(workedFiles);
    }

    @Override
    public File compressWorkedFiles(Long idInformante, Long idPeriodo, String compressName) throws IscamFilesException {
        InformanteDTO informante = informanteService.findOne(idInformante);
        PeriodoOperativoDTO periodo = periodoOperativoService.findOne(idPeriodo);
        File directory = archivoTrabajadoFileService.createDirectory(informante, periodo);
        archivoTrabajadoFileService.setDirectory(directory);
        File zipOut;
        try {
            zipOut = archivoTrabajadoFileService.zipDirectory(compressName);
        } catch (IOException e) {
            throw new IscamFilesException(IscamFilesException.CANNOT_ZIP, compressName);
        }
        return zipOut;
    }

    @Override
    @Async
    public void depuracion(Integer uploadMethod, Long idInformante, Long idPeriodo, Integer replacement, Long secuenciaProceso, Long idMercado, Integer secuencia) {
        //Actualizamos a ejecutando: Depuracion de datos
        //Integer idProceso = registroProcesosInformantesService.getIdRegistroProceso(idPeriodo, RegistroProcesoInformante.DEPURACION);

            registroProcesosInformantesService.setEnProceso(secuencia, secuenciaProceso, idPeriodo, idInformante, "Inicio del proceso de depuracion", 1, idMercado);
        try {
            //Finalmente invocamos el llamado al store procedure general de iscam
            Integer code;
            if (uploadMethod == TipoCargaEnum.REEMPLAZAR.getNumber())
                code = archivoTrabajadoRepository.procesoGeneral(idInformante.intValue(), idPeriodo.intValue(), uploadMethod, replacement);
            else
                code = archivoTrabajadoRepository.procesoGeneral(idInformante.intValue(), idPeriodo.intValue(), uploadMethod, 0);
            String message = statusRepository.getMessageById(new Long(code));
            registroProcesosInformantesService.updateStatus(secuencia, secuenciaProceso, idPeriodo, idInformante, message, 1, idMercado, code);
        } catch (Exception e) {
            registroProcesosInformantesService.setConError(secuencia, secuenciaProceso, idPeriodo, idInformante, "Error inesperado", 1, idMercado);
            return;
        }
        validarSucursalesPendiente(idInformante, idPeriodo, RegistroProcesoInformante.VALIDACION_SUCURSALES, idMercado, secuencia);
    }

    private void validarSucursalesPendiente(Long idInformante, Long idPeriodo, Long secuenciaProceso, Long idMercado, Integer secuencia) {
        //Integer idProceso = registroProcesosInformantesService.getIdRegistroProceso(idPeriodo, secuenciaProceso);
        registroProcesosInformantesService.setPendiente(secuencia, secuenciaProceso, idPeriodo, idInformante, "", 1, idMercado);
    }
}
