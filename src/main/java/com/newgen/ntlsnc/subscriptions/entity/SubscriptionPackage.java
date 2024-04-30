package com.newgen.ntlsnc.subscriptions.entity;

import com.newgen.ntlsnc.common.BaseEntity;
import com.newgen.ntlsnc.common.enums.SubscriptionDurationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SubscriptionPackage extends BaseEntity<Long> {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Float duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionDurationType durationType;

    @Size(min = 1)
    @Column(nullable = false)
    private Integer numberOfUnits;

    @Size(min = 1)
    @Column(nullable = false)
    private Integer numberOfUsers;
}
