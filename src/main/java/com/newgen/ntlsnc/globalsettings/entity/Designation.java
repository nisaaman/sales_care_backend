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
public class Designation extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 250)
    private String description;

    // done discussation
}
