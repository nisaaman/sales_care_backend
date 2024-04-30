package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/5/22
 * @time 10:00 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LocationManagerMap extends SuperEntity<Long> {

    private LocalDate fromDate;
    private LocalDate toDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser applicationUser; // TM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
