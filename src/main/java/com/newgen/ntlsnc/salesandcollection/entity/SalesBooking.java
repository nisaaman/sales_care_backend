package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author kamal
 * @Date ৯/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesBooking extends SuperEntity<Long> {

    @Column(nullable = false)
    private String bookingNo;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    private LocalDate tentativeDeliveryDate;

    private String notes;

    @Column(nullable = true, length = 1)
    private String isBookingStockConfirmed;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDate approvalDate;

    @Column(nullable = false)
    private Boolean isHold = false;

    private String holdReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvoiceNature invoiceNature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApplicationUser salesOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Depot depot; //warehouse

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private Location location; // booking location

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
