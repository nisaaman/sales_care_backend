package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author kamal
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Bank extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String bankShortName;
    private String description;

}
