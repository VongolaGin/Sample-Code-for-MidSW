package com.logismic.iscam.service;

import com.logismic.iscam.service.dto.InformanteDTO;
import com.logismic.iscam.service.dto.PeriodoOperativoDTO;
import com.logismic.iscam.web.rest.errors.IscamExcelReaderException;
import com.logismic.iscam.web.rest.errors.IscamFilesException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;

/**
 * Service Interface for managing ArchivoTrabajados.
 */
public interface ArchivoTrabajadoService {

    /**
     * Validate worked files
     */
    Boolean validate(ArrayList<MultipartFile> workedFiles);

    /**
     * Service for write csv
     */
    void write(MultipartFile file, PrintWriter temp_csv, String claveIscam, String periodoAcronimo)
        throws IscamExcelReaderException, IOException;

    /**
     * Save files into archivos trabajados directory
     * @param totalsFile
     * @param workedFiles
     * @param informante
     * @param periodo
     */
    public void saveFilesIntoDirectory(MultipartFile totalsFile, ArrayList<MultipartFile> workedFiles,
                                       InformanteDTO informante, PeriodoOperativoDTO periodo) throws IOException, IscamFilesException;


    void process(ArrayList<MultipartFile> workedFiles, MultipartFile totalFile,
                 Long idInformante, Long idPeriodo, Integer uploadMethod, Integer replacemente)
        throws Exception;

    File compressWorkedFiles(Long idInformante, Long idPeriodo, String compressName) throws IscamFilesException, IOException;

    void depuracion(Integer uploadMethod, Long idInformante, Long idPeriodo, Integer replacement, Long secuenciaProceso, Long idMercado, Integer idSecuencia);
}
