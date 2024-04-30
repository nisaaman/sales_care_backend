package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.CommonConstant;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * @author sagor
 * @date ১৭/৫/২২
 */
@Service
public class FileStorageService {

    private final String rootPath = "uploads";
    private final Path root = Paths.get(rootPath);

    public void init() {
        try {
            if (!Files.exists(root))
                Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public void save(MultipartFile file, String fileName) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(fileName));
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }

    }

    public void upload(MultipartFile file, String filePath, String fileName) {

        try {
            Path path = Paths.get(this.rootPath + File.separator + filePath);

            if(Files.notExists(path)){
                createSubDirectory(this.rootPath+File.separator+filePath);
            }

            Files.copy(file.getInputStream(), path.resolve(fileName),
                                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }

    }

    public void uploadInResourceDirectory(MultipartFile file, String filePath, String fileName) {

        try {
            Path path = Paths.get(filePath);

            if(Files.notExists(path)){
                createSubDirectory(filePath);
            }

            Files.copy(file.getInputStream(), path.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }

    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            File directory = new File(rootPath + "/" + filePath);
            FileUtils.forceDelete(directory);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    public Path loadPath(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            Path p = Paths.get(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return p;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public boolean isFileAvailable(String directory, String fileNameSubString) {

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            return paths
                    .filter(Files::isRegularFile)
                    .anyMatch(file -> String.valueOf(
                            file.getFileName()).contains(fileNameSubString));

        } catch (Exception ex) {

        }
        return false;
    }

    public Resource downloadFile(String fileName){
        Path path = Paths.get(this.root + File.separator)
                .toAbsolutePath().resolve(fileName);

        Resource resource;

        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("The file can't be reading.");
        }

        if (resource.exists() && resource.isReadable()){
            return resource;
        }else{
            throw new RuntimeException("The file doesn't exist.");
        }
    }

    public void move(String sourcePath , String destinationPath) {
        try {
            Path srcPath = Paths.get(this.root + File.separator + sourcePath);
            Path destPath = Paths.get(this.root + File.separator + destinationPath);
            if(Files.notExists(destPath)){
                Files.createDirectory(destPath);
            }
            Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new RuntimeException("Could not move the file. Error: " + e.getMessage());
        }

    }

    public boolean createSubDirectory(String filePath) {
        File file = new File( filePath );
        file.mkdirs();
        return true;
    }

    public void deleteFileFromDirectory(String filePath, String fileName) {

        try{
            org.apache.commons.io.FileUtils.forceDelete(
                    new File(filePath+fileName));
        } catch (Exception ex) {

        }
    }
}
