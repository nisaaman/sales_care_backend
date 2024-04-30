package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UnitOfMeasurement extends SuperEntity<Long> {

    @Column(nullable = false, length = 20)
    private String abbreviation;

    @Column(length = 80)
    private String description;
}
