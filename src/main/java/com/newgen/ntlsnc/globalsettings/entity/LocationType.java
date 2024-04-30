package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/4/22
 * @time 09:30 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LocationType extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String name;  // Zone,

    @Column(nullable = false)
    private Integer level;  //1, 2

    @Column(nullable = false)
    private Boolean isDepotLevel = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private LocationTree locationTree;
}
