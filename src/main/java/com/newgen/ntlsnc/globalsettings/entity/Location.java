package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/4/22
 * @time 10:00 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Location extends SuperEntity<Long> {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 250)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private LocationType locationType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location parent;
}
