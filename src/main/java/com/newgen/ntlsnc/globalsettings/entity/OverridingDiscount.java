package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CalculationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/7/22
 * @time 12:10 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OverridingDiscount extends SuperEntity<Long> {

    @Column(nullable = false)
    private Integer fromDay;
    @Column(nullable = false)
    private Integer toDay;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CalculationType calculationType; //  Percentage/ Amount

    @Column(nullable = false)
    private Double ord = 0.0d;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Semester  semester;

    @ManyToOne(fetch = FetchType.LAZY)
    private InvoiceNature  invoiceNature;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organization company;
}
