package com.newgen.ntlsnc.multilayerapproval.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author nisa
 * @date 9/10/22
 * @time 2:49 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ApprovalStep extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String name;

    private String description;
}
