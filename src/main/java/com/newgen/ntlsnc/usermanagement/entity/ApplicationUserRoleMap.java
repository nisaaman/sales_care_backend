package com.newgen.ntlsnc.usermanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author liton
 * Created on 6/5/22 10:45 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ApplicationUserRoleMap extends SuperEntity<Long> {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser applicationUser;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
