package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceSequence;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Year;

/**
 * @author liton
 * Created on 1/1/23 12:06 PM
 */

@Repository
public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Long> {
    InvoiceSequence getInvoiceSequenceByCompanyAndProductCategoryAndInvoiceNatureAndYearAndIsDeletedIsFalseAndIsActiveIsTrue(
            Organization company, ProductCategory productCategory, InvoiceNature invoiceNature, Integer year
    );
}
