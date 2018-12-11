package com.logismic.iscam.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.logismic.iscam.domain.RegistroProcesoInformante;
import com.logismic.iscam.service.ArchivoTrabajadoService;
import com.logismic.iscam.service.BitacoraEventosService;
import com.logismic.iscam.service.RegistroProcesosInformantesService;
import com.logismic.iscam.web.rest.errors.IscamExcelReaderException;
import com.logismic.iscam.web.rest.errors.IscamFilesException;
import net.logstash.logback.encoder.org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/archivo-trabajado")
public class ArchivoTrabajadoResource {

    private final Logger log = LoggerFactory.getLogger(AgrupadorResource.class);

    private static final String ENTITY_NAME = "agrupador";

    private final ArchivoTrabajadoService archivoTrabajadoService;
    private final RegistroProcesosInformantesService registroProcesosInformantesService;
    private final BitacoraEventosService bitacoraEventosService;
    public ArchivoTrabajadoResource(ArchivoTrabajadoService archivoTrabajadoService, RegistroProcesosInformantesService registroProcesosInformantesService, BitacoraEventosService bitacoraEventosService) {
        this.archivoTrabajadoService = archivoTrabajadoService;
        this.registroProcesosInformantesService = registroProcesosInformantesService;
        this.bitacoraEventosService = bitacoraEventosService;
    }

    @PostMapping("/upload")
    @Timed
    @ResponseBody
    public ResponseEntity<Map> upload(@RequestParam("archivosTrabajados") ArrayList<MultipartFile> workedFiles,
                                      @RequestParam("totales") MultipartFile totalFile,
                                      @RequestParam("uploadMethod") Integer uploadMethod,
                                      @RequestParam("idPeriodo") Long idPeriodo,
                                      @RequestParam("idInformante") Long idInformante,
                                      @RequestParam("replacement") Integer replacement,
                                      @RequestParam("secuenciaProceso") Long secuenciaProceso,
                                      @RequestParam("nivelDetalle") Integer nivelDetalle,
                                      @RequestParam("secuencia") Integer secuencia,
                                      @RequestParam("idMercado") Long idMercado
    ) {
        Map response = new HashMap<>();
        //Actualizamos a estado: En proceso
        Integer idProceso = registroProcesosInformantesService.getIdRegistroProceso(idPeriodo, secuenciaProceso);
            registroProcesosInformantesService.setEnProceso(
                secuencia, secuenciaProceso, idPeriodo, idInformante, "Inicio del proceso de carga", 1, idMercado);
        try {
            //Realizamos la carga
            archivoTrabajadoService.process(workedFiles, totalFile, idInformante, idPeriodo, uploadMethod, replacement);
            //Actualizamo a estado: Finalizado
            registroProcesosInformantesService.setFinalizado(secuencia, secuenciaProceso, idPeriodo, idInformante, "Fin de la carga", 1, idMercado);
        } catch (Exception e) {
            //Marcamos la carga con error
            //Actualizamos a estado: Con Error
            registroProcesosInformantesService.setConError(secuencia, secuenciaProceso, idPeriodo, idInformante, "Inicio del proceso de carga", 1, idMercado);
            response = getExceptionResponse(e);
            return ResponseEntity.status(500).body(response);
        }

        //Lanzamos el proceso de depuracion en segundo plano
        archivoTrabajadoService.depuracion(uploadMethod, idInformante, idPeriodo, replacement, RegistroProcesoInformante.DEPURACION, idMercado, secuencia);
        //validarSucursalesPendiente(idInformante, idPeriodo, secuenciaProceso + 1, nivelDetalle);
        response.put("code", 1);
        response.put("message", "Success");
        return ResponseEntity.ok().body(response);

    }



    @GetMapping("/downloadPeriod/{idInformante}/{idPeriodo}")
    @Timed
    @ResponseBody
    public ResponseEntity<?> downloadPeriod(@PathVariable Long idInformante, @PathVariable Long idPeriodo, HttpServletResponse response) throws IOException {
        try {
            String compressName = "ArchivosTrabajados_" + idInformante + "_" + idPeriodo;
            File file = archivoTrabajadoService.compressWorkedFiles(idInformante, idPeriodo, compressName);
            FileSystemResource resource = new FileSystemResource(file);
            response.setHeader("Content-Disposition", "attachment; filename=" + compressName + ".zip");
            response.setHeader("Content-Length", String.valueOf(file.length()));
            return ResponseEntity.status(200).body(resource);
        } catch (IOException | IscamFilesException e) {
            return ResponseEntity.status(500).body(e.getMessage().getBytes());
        }
    }


    public Map getExceptionResponse(Exception e) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("code", 0);
        if (e.getClass() == IscamFilesException.class || e.getClass() == IscamExcelReaderException.class) {
            response.put("message", e.getMessage());
            response.put("error", ExceptionUtils.getStackTrace(e));
        }
        else {
            response.put("message", "Error inesperado");
            response.put("error", ExceptionUtils.getStackTrace(e));
        }
        return response;
    }
}
