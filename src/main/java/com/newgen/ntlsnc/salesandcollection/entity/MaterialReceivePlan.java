package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.TicketStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author kamal
 * @Date ৯/৪/২২
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MaterialReceivePlan extends SuperEntity<Long> {

    //@Column(nullable = false)
    //private String ticketNo;

    @Column(nullable = false)
    private LocalDate ticketDate;

    @Column(nullable = false)
    private Float quantity;

    private LocalDate requireDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    private LocalDate ticketStatusDate;

    private LocalDate commitmentDate;

    private String notes;

    private Long confirmQuantity;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesBookingDetails salesBookingDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organization company;
}
