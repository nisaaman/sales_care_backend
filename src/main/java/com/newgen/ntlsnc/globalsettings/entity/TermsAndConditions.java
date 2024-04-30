package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/16/22
 * @time 3:49 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TermsAndConditions extends SuperEntity<Long> {
    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    // TODO it should be feature wise
}
