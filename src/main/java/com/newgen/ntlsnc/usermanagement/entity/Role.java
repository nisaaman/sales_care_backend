package com.newgen.ntlsnc.usermanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author liton
 * Created on 6/5/22 10:44 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role extends SuperEntity<Long> {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
