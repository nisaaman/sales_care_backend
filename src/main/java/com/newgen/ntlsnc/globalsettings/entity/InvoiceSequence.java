package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.time.Year;

/**
 * @author liton
 * Created on 12/28/22 11:39 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvoiceSequence extends SuperEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductCategory productCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    private InvoiceNature invoiceNature;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer maxSequence;

    @Column(nullable = false)
    private Integer sequenceLength;

    @Column(nullable = false, length = 10)
    private String prefix;

    @Column(length = 10)
    private String postfix;

    private String description;
}
