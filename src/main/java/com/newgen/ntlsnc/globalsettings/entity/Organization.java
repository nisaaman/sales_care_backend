package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.BaseEntity;
import com.newgen.ntlsnc.subscriptions.entity.SubscriptionPackage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
//@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id", "shortName"}))
public class Organization extends BaseEntity<Long> {

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false, length = 250)
    private String address;

    @Column(nullable = false, length = 10)
    private String shortName;

    @Column(nullable = false, length = 100, unique = true)
    @Email
    private String email;

    @Column(length = 100, unique = true)
    private String webAddress;

    @Column(nullable = false, length = 20, unique = true)
    private String contactNumber;

    @Column(nullable = false, length = 50)
    private String contactPerson;

    @Column(length = 250)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_tree_id")
    private LocationTree locationTree; /*location tree assigned to child company*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Organization parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_package_id")
    private SubscriptionPackage subscriptionPackage;

}
