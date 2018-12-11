package com.logismic.iscam.service.filesmanager;

import com.logismic.iscam.service.dto.InformanteDTO;
import com.logismic.iscam.service.dto.PeriodoOperativoDTO;
import com.logismic.iscam.web.rest.errors.IscamFilesException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public interface ArchivoTrabajadoFileService {

    Boolean storeFile(MultipartFile file) throws IscamFilesException, IOException;

    Boolean storeFiles(List<MultipartFile> files) throws IscamFilesException, IOException;

    Boolean versionFiles() throws IOException, IscamFilesException;

    List<File> getFiles();

    File createDirectory(InformanteDTO informante, PeriodoOperativoDTO periodo) throws IscamFilesException;

    void setDirectory(File file);

    File zipDirectory(String zipName) throws IOException, IscamFilesException;

    boolean alreadyExist(InformanteDTO informante, PeriodoOperativoDTO periodo);
}
