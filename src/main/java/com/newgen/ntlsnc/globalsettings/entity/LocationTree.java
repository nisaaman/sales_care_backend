package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author kamal
 * @Date ২৫/৫/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LocationTree extends SuperEntity<Long> {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 250)
    private String description;
}
