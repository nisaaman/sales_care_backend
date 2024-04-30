package com.newgen.ntlsnc.usermanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/5/22
 * @time 01:00 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReportingManager extends SuperEntity<Long> {
    private LocalDate fromDate;
    private LocalDate toDate;

    @ManyToOne
    private ApplicationUser applicationUser;

    @OneToOne
    private ApplicationUser reportingTo;
}
