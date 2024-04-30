package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

/**
 * @author nisa
 * @date 4/7/22
 * @time 11:30 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Depot extends SuperEntity<Long> {

    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String depotName; //

    @Column(nullable = false)
    private Boolean isCentralWarehouse = false;

    private String address;

    @Column(nullable = false, length = 20)
    private String contactNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApplicationUser depotManager;


}
