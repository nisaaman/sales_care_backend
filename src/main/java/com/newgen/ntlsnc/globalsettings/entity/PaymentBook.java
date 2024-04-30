package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentBook extends SuperEntity<Long> {


    @Column(nullable = false)
    private String bookNumber;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private Long fromMrNo;

    private String remarks;

    @Column(nullable = false)
    private Long toMrNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id",nullable = false)
    private Location paymentBookLocation; // Territory

}
