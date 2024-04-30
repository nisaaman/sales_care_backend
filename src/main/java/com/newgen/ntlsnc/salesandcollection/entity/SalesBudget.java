package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * @author nisa
 * @date 4/9/22
 * @time 9:41 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesBudget extends SuperEntity<Long> {

    @Column(nullable = false)
    private LocalDate budgetDate;

    /*@Column(nullable = false)
    private Integer month;*/
   /* @Column(nullable = false)
    private Integer year;*/

    //private Double targetValue;
    @OneToOne
    @JoinColumn(name = "accounting_year_id", nullable = false)
    private AccountingYear accountingYear;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDate approvalDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BudgetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organization company;

    // need discussion
}
