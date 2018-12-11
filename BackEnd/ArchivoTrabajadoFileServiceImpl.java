package com.logismic.iscam.service.filesmanager;

import com.logismic.iscam.service.dto.InformanteDTO;
import com.logismic.iscam.service.dto.PeriodoOperativoDTO;
import com.logismic.iscam.web.rest.errors.IscamFilesException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ArchivoTrabajadoFileServiceImpl implements ArchivoTrabajadoFileService {
    @Value("${worked-files.files.directory}")
    private String FILE_ROOT_PATH;

    private final String VERSION_FOLDER_PREFIX = "Version ";
    private final Logger log = LoggerFactory.getLogger(ArchivoTrabajadoFileServiceImpl.class);
    private File directory;

    @Override
    public Boolean storeFile(MultipartFile file) throws IscamFilesException, IOException {
        File total = new File(directory, file.getOriginalFilename());
        FileUtils.copyInputStreamToFile(file.getInputStream(), total);
        return true;
    }

    @Override
    public Boolean storeFiles(List<MultipartFile> files) throws IscamFilesException, IOException {
        for (MultipartFile workedFile : files) {
            File file = new File(directory, workedFile.getOriginalFilename());
            FileUtils.copyInputStreamToFile(workedFile.getInputStream(), file);
        }
        return true;
    }


    @Override
    public Boolean versionFiles() throws IOException, IscamFilesException {
        List<File> filesForVersion = getFiles();
        if (filesForVersion.size() > 0) {
            List<File> versionFolders = getSubdirectories();
            int version = versionFolders.size() + 1;
            //Creating directory
            File newVersionFolder = new File(directory, VERSION_FOLDER_PREFIX + version);
            Boolean success = true;
            if (!newVersionFolder.exists() || newVersionFolder.isFile())
                success = newVersionFolder.mkdir();
            if (!success)
                throw new IscamFilesException(IscamFilesException.VERSION_FOLDER_CANT_CREATED, newVersionFolder.getAbsolutePath());
            for (File file : filesForVersion) {
                //ignoramos DS_Store de los archivos de mac
                if (file.getName().contains(".DS_Store"))
                    continue;
                FileUtils.copyFileToDirectory(file, newVersionFolder);
            }
        }
        return true;
    }

    private List<File> getSubdirectories() {
        List<File> subdirectories = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                subdirectories.add(file);
        }
        return subdirectories;
    }

    @Override
    public List<File> getFiles() {
        List<File> files = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isFile())
                files.add(file);
        }
        return files;
    }

    @Override
    public void setDirectory(File directory) {
        this.directory = directory;
    }

    @Override
    public File createDirectory(InformanteDTO informante, PeriodoOperativoDTO periodo) throws IscamFilesException {
        String directoryPath = FILE_ROOT_PATH
            .concat("/" + informante.getClaveIscam().trim())
            .concat("/" + periodo.getFecha())
            .concat("/" + periodo.getAcronimo().replace("/", "-"))
            .concat("/");

        File directory = new File(directoryPath);
        if (!directory.exists())
            directory.mkdirs();

        if (!Files.isReadable(directory.toPath()))
            throw new IscamFilesException(IscamFilesException.DIRECTORY_NOT_WRITABLE, directory);

        if (!Files.isWritable(directory.toPath()))
            throw new IscamFilesException(IscamFilesException.DIRECTORY_NOT_WRITABLE, directory);

        return directory;
    }

    @Override
    public File zipDirectory(String zipName) throws IscamFilesException, IOException {
        if (directory.listFiles().length == 0)
            throw new IscamFilesException(IscamFilesException.EMPTY_DIRECTORY);
        File zip = File.createTempFile(zipName,".tmp");
        ZipUtil.pack(directory, zip);
        return zip;
    }

    @Override
    public boolean alreadyExist(InformanteDTO informante, PeriodoOperativoDTO periodo) {
        String directoryPath = FILE_ROOT_PATH
            .concat("/" + informante.getClaveIscam().trim())
            .concat("/" + periodo.getFecha())
            .concat("/" + periodo.getAcronimo().replace("/", "-"))
            .concat("/");
        return Files.exists(Paths.get(directoryPath));
    }
}
