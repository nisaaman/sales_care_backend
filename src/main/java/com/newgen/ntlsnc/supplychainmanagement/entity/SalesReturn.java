package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author liton
 * Created on 4/24/22 11:52 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesReturn extends SuperEntity<Long> {

    @Column(length = 50)
    private String returnNo;  // auto generated

    @Column(nullable = false)
    private LocalDateTime returnDate;

    private String returnNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Depot depot;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvTransaction invTransaction;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesReturnProposal salesReturnProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    // TODO need to consider status to know ending of the process
}
