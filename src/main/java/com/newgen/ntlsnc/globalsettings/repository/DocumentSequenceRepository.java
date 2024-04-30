package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.DocumentSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author kamal
 * @Date ১৫/৫/২২
 */

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    DocumentSequence getDocumentSequenceByOrganizationIdAndDocumentIdAndIsDeletedFalseAndIsActiveTrue(Long organizationId, int documentId);
}
