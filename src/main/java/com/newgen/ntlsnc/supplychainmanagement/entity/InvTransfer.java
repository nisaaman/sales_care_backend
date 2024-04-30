package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.InvReturnType;
import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/16/22
 * @time 10:54 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvTransfer extends SuperEntity<Long> {

    @Column(nullable = false)
    private LocalDate transferDate;
    private String transferNo;
    private String driverName;
    private String driverContactNo;
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Depot fromDepot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Depot toDepot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvTransaction invTransaction;

    // Start: Stock Transfer Receive Return

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private InvReturnType invReturnType;

    private String returnReason;

    @ManyToOne(fetch = FetchType.LAZY)
    private InvTransfer invTransfer; // Against First Transfer

    // End: Stock Transfer Receive Return
}
