package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/11/22
 * @time 2:33 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BatchDetails extends SuperEntity<Long> {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Product product;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ApplicationUser supervisor;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Batch batch;
}
