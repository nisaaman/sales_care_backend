package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.CreditLimitTerm;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/7/22
 * @time 3:32 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CreditLimit extends SuperEntity<Long> {

    @Column(nullable = false)
    private Float creditLimit = 0.0f;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditLimitTerm creditLimitTerm;

    private LocalDate startDate; // only for short term

    private LocalDate endDate; // only for short term

    @ManyToOne(fetch = FetchType.LAZY)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    private CreditLimitProposal creditLimitProposal;  // will be null when admin create long-term credit limit

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesBooking salesBooking;  // only for a specific booking

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

//    @Column(columnDefinition = "DECIMAL(19,4)") // test purpuse
//    private BigDecimal testNumber;
}
