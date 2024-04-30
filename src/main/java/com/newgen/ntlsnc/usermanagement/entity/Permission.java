package com.newgen.ntlsnc.usermanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ActivityFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 6/9/22
 * @time 1:49 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Permission extends SuperEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;

    @Enumerated(EnumType.STRING)
    private ActivityFeature activityFeature;

    private Boolean isCreate = false;
    private Boolean isUpdate = false;
    private Boolean isDelete = false;
    private Boolean isView = false;
    private Boolean isAllView = false;
}
