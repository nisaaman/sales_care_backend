package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.globalsettings.entity.DocumentSequence;
import com.newgen.ntlsnc.globalsettings.repository.DocumentSequenceRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author kamal
 * @Date ১৫/৫/২২
 */

@Service
public class DocumentSequenceService {

    @Autowired
    DocumentSequenceRepository documentSequenceRepository;
    @Autowired
    ApplicationUserService applicationUserService;

    @Transactional
    public String getSequenceByDocumentId(int documentId) {

        DocumentSequence documentSequence = this.getDocumentSequence(documentId);

        try {
            if (documentSequence == null) {
                throw new Exception("Document Sequence is not exist with document id=" + documentId);
            }
            documentSequence.setMaxSequence(documentSequence.getMaxSequence() + 1);
            documentSequence = documentSequenceRepository.save(documentSequence);

            String format = "%0"+documentSequence.getSequenceLength()+"d";
            String sequence= String.format(format, documentSequence.getMaxSequence());

            sequence = documentSequence.getPrefix() != null ? documentSequence.getPrefix() + sequence : sequence;
            sequence = documentSequence.getPostfix() != null ? sequence + documentSequence.getPostfix() : sequence;

            return sequence;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public DocumentSequence getDocumentSequence(int documentId){
        return documentSequenceRepository.getDocumentSequenceByOrganizationIdAndDocumentIdAndIsDeletedFalseAndIsActiveTrue(
                applicationUserService.getOrganizationIdFromLoginUser(), documentId);
    }
}
