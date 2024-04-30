package com.newgen.ntlsnc.common;

import com.newgen.ntlsnc.common.sftp.FileTransferService;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.globalsettings.service.FileStorageService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Newaz Sharif
 * @since 21st June, 22
 */

@Service
public class FileUploadService {

    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    DocumentService documentService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    FileTransferService fileTransferService;

    public String fileUpload(MultipartFile file, String fileType, String refTable,
                             Long organizationId, Long companyId) throws RuntimeException{
        String fileRootPath = CommonConstant.SFTP_SERVER_ROOT_DIR_NAME;
        String orgShortName = organizationService.getShortName(organizationId);
        String companyShortName = organizationService.getShortName(companyId);

        List<String> pathList = Arrays.asList(fileRootPath,orgShortName+"-"+String.valueOf(organizationId),
                        companyShortName+"-"+String.valueOf(companyId),refTable,fileType);

        String filePath = new ClassPathResource(getFileFullPath(pathList)).getPath();

        String fileName = getFileNameWithoutExtension(file.getOriginalFilename())+"_"+companyId
                + "." +getFileExtension(file.getOriginalFilename());

        boolean fileTransferStatus = fileTransferService.uploadFileToServer(file,filePath,fileName);

        if(!fileTransferStatus) {
            fileStorageService.upload(file, filePath, fileName);
        }

        return filePath+File.separator+fileName;

    }

    public String getFileFullPath(List<String> pathList) {

        return pathList.stream()
                       .map(path -> path.toLowerCase())
                       .collect(Collectors.joining("/"));
    }

    public String getFileNameWithoutExtension(String fileNameWithExt) {

        return FilenameUtils.removeExtension(fileNameWithExt);
    }

    public String getFileExtension(String fileNameWithExt) {
        return FilenameUtils.getExtension(fileNameWithExt);
    }

    public String getFileNameFromFilePath(String filePath) {
        return FilenameUtils.getName(filePath);
    }

    public String getFileMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentType = fileNameMap.getContentTypeFor(fileName);

        if (contentType == null) {
            contentType = FIleUtility.fileExtensionMap.get(getFileExtension(fileName));
        }
        return contentType;
    }
}
