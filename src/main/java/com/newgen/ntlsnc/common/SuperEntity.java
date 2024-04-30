package com.newgen.ntlsnc.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * @author nisa
 * @Created 08/04/2021 - 11:33 PM
 */
@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public abstract class SuperEntity<U> extends BaseEntity<U> {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
}
