package com.newgen.ntlsnc.globalsettings.repository;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.globalsettings.entity.Document;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<Document> findByIdAndIsDeletedFalse(Long id);

    Optional<Document> findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefIdAndFileType(
            String refTable, Long refId, FileType fileType);

    Optional<Document> findByIsActiveTrueAndIsDeletedFalseAndRefTableAndRefId(
            String refTable, Long refId);


    Optional<Document> findByRefIdAndRefTableAndIsActiveTrueAndIsDeletedFalse(Long refColumnId, String refTable);

    @Query(value = "select d.id, d.ref_id, d.file_path from document d where d.ref_table = \"SalesInvoice\" \n" +
            "and (COALESCE(:salesInvoiceList) is null or d.ref_id in(:salesInvoiceList))\n" +
            "and d.is_active is true and d.is_deleted is false;", nativeQuery = true)
    List<Map<String, Object>> getAcknowledgedInvoiceReportByDistributorId( List<Long> salesInvoiceList);

}
