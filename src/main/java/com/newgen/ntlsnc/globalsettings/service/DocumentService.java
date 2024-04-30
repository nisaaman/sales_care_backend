package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.globalsettings.entity.Document;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.DocumentRepository;
import com.newgen.ntlsnc.globalsettings.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author sagor
 * @date ১৭/৫/২২
 */

@Service
public class DocumentService {
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    FileUploadService fileUploadService;

    private static final String CLASS_NAME = "Document";
    private static final String DELETE_MESSAGE = " deleted successful";
    private static final String CANT_DELETE = "can't be deleted.";

    public Document save(String tableName, String filePath, Long refColumnId, String fileName, String fileType,
                         Organization parent, Long companyId, Long fileSize) {
        try {
            Optional<Document> optionalDocument =
                    documentRepository.findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefId(tableName, refColumnId);
            if(optionalDocument.isPresent()){
                Document document = optionalDocument.get();
                document.setIsDeleted(true);
                documentRepository.save(document);
            }
            Document document = new Document();
            document.setRefTable(tableName);
            document.setFilePath(filePath);
            document.setRefId(refColumnId);
            document.setFileName(fileName);
            document.setFileType(FileType.valueOf(fileType));
            document.setOrganization(parent);
            document.setFileSize(fileSize);
            if (companyId != null) {
                document.setCompany(organizationRepository.getById(companyId));
            }
            document = documentRepository.save(document);
            return document;

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ApiResponse delete(Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            Optional<Document> documentOpt = documentRepository.findById(id);
            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();
                document.setIsDeleted(true);
                documentRepository.save(document);

                response.setSuccess(true);
                response.setMessage(CLASS_NAME + DELETE_MESSAGE);
            } else {
                response.setSuccess(false);
                response.setMessage(CLASS_NAME + CANT_DELETE);
            }
        } catch (Exception ex) {
            response.setMessage(CLASS_NAME + DELETE_MESSAGE);
        }

        return response;
    }

    public ResponseEntity<?> getById(Long id) {
        Optional<Document> documentOpt = documentRepository.findByIdAndIsDeletedFalse(id);
        if (documentOpt.isPresent()) {
            return ResponseEntity.ok(documentOpt.get());
        } else {
            return ResponseEntity.ok(HttpStatus.NOT_FOUND);
        }
    }

    public String getDocumentFileType(String refTable, Long refId) {
        Optional<Document> document = documentRepository.
                findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefId(refTable, refId);
        return document.isPresent() == true ? document.get().getFileType().getCode() : "";
    }

    public String getDocumentFileExtension(String refTable, Long refId) {
        Optional<Document> document = documentRepository.
                findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefId(refTable, refId);
        return document.isPresent() == true ? fileUploadService.getFileExtension(document.get().getFileName()) : "";
    }

    public String getDocumentMimeType(String refTable, Long refId) {
        Optional<Document> document = documentRepository.
                findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefId(refTable, refId);
        return document.isPresent() == true ? fileUploadService.getFileMimeType(document.get().getFileName()) : "";
    }

    public String getDocumentFilePath(String refTable, Long refId, FileType fileType) {
        Optional<Document> document = documentRepository.
                findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefIdAndFileType(refTable, refId, fileType);
        return document.isPresent() == true ? document.get().getFilePath() : "";
    }

    public String getDocumentFileName(String refTable, Long refId, FileType fileType) {
        Optional<Document> document = documentRepository
                .findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefIdAndFileType(refTable, refId, fileType);
        return document.isPresent() == true ? document.get().getFileName() : "";
    }

    public String getDocumentFileName(String refTable, Long refId) {
        Optional<Document> document = documentRepository.findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefId(refTable, refId);
        return document.isPresent() == true ? document.get().getFileName() : "";
    }

    public String getFileType(Long refId, String tableName) {
        return getDocumentFileType(tableName, refId);
    }

    public String getFileMimeType(Long id, String tableName) {
        return getDocumentMimeType(tableName, id);
    }

    public String getFileName(Long refId, String tableName) {
        return getDocumentFileName(tableName, refId, FileType.valueOf(getFileType(refId, tableName)));
    }

    public String getFilePath(Long refId, String tableName) {
        String fileType = getFileType(refId, tableName);

        return fileType.equals("") ? fileType : getDocumentFilePath(tableName, refId, FileType.valueOf(fileType));
    }

    public Document getDocumentInfoByRefIdAndRefTable(Long refId, String refTable) {
        Optional<Document> document = documentRepository.findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefId(refTable, refId);
        return document.isPresent() == true ? document.get() : null;
    }

    public List<Map<String, Object>> getDocumentFileByDistributorIdAndCompanyId(List<Long> salesInvoiceList){
        List<Map<String, Object>> document = documentRepository.getAcknowledgedInvoiceReportByDistributorId(salesInvoiceList);
        return document;
    }
}
