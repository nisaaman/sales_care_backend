package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author Newaz Sharif
 * @since 26th July,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CollectionBudget extends SuperEntity<Long> {

    @Column(nullable = false)
    private LocalDate budgetDate;

    @OneToOne
    @JoinColumn(nullable = false)
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
}
