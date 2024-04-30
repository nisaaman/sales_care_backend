package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author kamal
 * @Date ১০/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DepotLocationMap extends SuperEntity<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Location location;
}
