package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/5/22
 * @time 10:00 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PackSize extends SuperEntity<Long> {

    @Column(nullable = false)
    private Integer packSize; //  40   1 case of beer has 24 bottles. So the pack size would contain 24 bottles when 1 case is purchased.

    private String description;

    @Column(nullable = false)
    private Float height;

    @Column(nullable = false)
    private Float width;

    @Column(nullable = false)
    private Float length;

    @ManyToOne(fetch = FetchType.LAZY)
    private UnitOfMeasurement uom; // ml, kg
}
