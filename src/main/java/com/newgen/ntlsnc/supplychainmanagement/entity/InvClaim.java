package com.newgen.ntlsnc.supplychainmanagement.entity;
import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author sunipa
 * @date ২১/৯/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvClaim extends SuperEntity<Long> {

    @Column(nullable = false)
    private LocalDate claimDate;
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvTransaction invTransaction; /*reference transaction*/

    @ManyToOne(fetch = FetchType.LAZY)
    private InvTransfer invTransfer; // reference transfer receive no

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
