package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author kamal
 * @Date ১০/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesOrder extends SuperEntity<Long> {

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private LocalDate deliveryDate; // SalesBooking -> tentativeDeliveryDate

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesBooking salesBooking;

    @Column(nullable = false, length = 20)  //For Authorization process
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDate approvalDate;

}
