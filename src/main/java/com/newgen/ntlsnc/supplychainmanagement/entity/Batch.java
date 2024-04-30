package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/11/22
 * @time 11:43 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Batch extends SuperEntity<Long> {

    private String consignmentNo;

    @Column(nullable = false)
    private String batchNo;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate productionDate;

    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
