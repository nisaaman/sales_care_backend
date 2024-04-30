package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.TicketStatus;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/11/22
 * @time 9:52 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MaterialReceivePlanHistory extends SuperEntity<Long> {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    private LocalDate ticketStatusDate;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private MaterialReceivePlan materialReceivePlan;
}
