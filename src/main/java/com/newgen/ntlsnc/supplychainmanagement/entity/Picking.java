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
 * @date 4/16/22
 * @time 12:50 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Picking extends SuperEntity<Long> {
    @Column(nullable = false,length = 30)
    private String pickingNo;

    private LocalDate pickingDate;

    private String reason;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
