package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.StoreType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author nisa
 * @date 4/13/22
 * @time 11:16 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Store extends SuperEntity<Long> {

    @Column(nullable = false)
    private String name;

    private String shortName;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StoreType storeType;

    private String description;
}
