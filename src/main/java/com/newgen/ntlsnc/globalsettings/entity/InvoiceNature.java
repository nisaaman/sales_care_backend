package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author nisa
 * @date 4/7/22
 * @time 11:53 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvoiceNature extends SuperEntity<Long> {

    @Column(nullable = false, length = 10)
    private String name; // Advance or Cash or Credit

    @Column(length = 80)
    private String description; // Advance or Cash or Credit
}
