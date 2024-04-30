package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.enums.NoteType;
import com.newgen.ntlsnc.globalsettings.entity.DocumentSequence;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceSequence;
import com.newgen.ntlsnc.globalsettings.entity.ProductCategory;
import com.newgen.ntlsnc.globalsettings.repository.InvoiceSequenceRepository;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.service.SalesInvoiceChallanMapService;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransactionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;

/**
 * @author liton
 * Created on 1/1/23 11:56 AM
 */

@Service
public class InvoiceSequenceService {

    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    InvoiceSequenceRepository invoiceSequenceRepository;
    @Autowired
    SalesInvoiceChallanMapService salesInvoiceChallanMapService;
    @Autowired
    InvTransactionDetailsRepository invTransactionDetailsRepository;

    public String getInvoiceSequence(int documentId, SalesInvoice invoice, ProductCategory productCategory){

        try {
            //Verify and update document sequence
            DocumentSequence documentSequence = documentSequenceService.getDocumentSequence(documentId);
            documentSequenceService.getSequenceByDocumentId(documentId);

            //Find and update or create invoice sequence
            InvoiceSequence invoiceSequence =
                invoiceSequenceRepository.getInvoiceSequenceByCompanyAndProductCategoryAndInvoiceNatureAndYearAndIsDeletedIsFalseAndIsActiveIsTrue(
                    invoice.getCompany(),
                    productCategory,
                    invoice.getInvoiceNature(),
                    LocalDate.now().getYear()
                );

            if (invoiceSequence == null) {
                invoiceSequence = new InvoiceSequence();
                invoiceSequence.setOrganization(invoice.getOrganization());
                invoiceSequence.setCompany(invoice.getCompany());
                invoiceSequence.setProductCategory(productCategory);
                invoiceSequence.setInvoiceNature(invoice.getInvoiceNature());
                invoiceSequence.setYear(LocalDate.now().getYear());
                invoiceSequence.setMaxSequence(1);
                invoiceSequence.setSequenceLength(documentSequence.getSequenceLength());
                invoiceSequence.setPrefix(documentSequence.getPrefix());
                invoiceSequence.setPostfix(documentSequence.getPostfix());
                invoiceSequence.setDescription(productCategory.getName() + " Initial setup.");
            } else {
                invoiceSequence.setMaxSequence(invoiceSequence.getMaxSequence() + 1);
            }

            invoiceSequence = invoiceSequenceRepository.save(invoiceSequence);

            String invoiceType = invoiceSequence.getInvoiceNature().getName().equalsIgnoreCase(NoteType.CREDIT.getCode()) ? "CR" : "CS";
            String format = "%0"+invoiceSequence.getSequenceLength()+"d";
            String sequence = String.format(format, invoiceSequence.getMaxSequence());

            sequence = invoiceSequence.getPrefix() + "-" + invoiceSequence.getCompany().getShortName() + "-"
                    + invoiceSequence.getProductCategory().getPrefix().toUpperCase() + "-" + invoiceType + "-"
                    + invoiceSequence.getYear() + "-" + sequence;
            sequence = invoiceSequence.getPostfix() != null ? sequence + invoiceSequence.getPostfix() : sequence;

            return sequence;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
