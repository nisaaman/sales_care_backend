package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Bank;
import com.newgen.ntlsnc.globalsettings.entity.BankBranch;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Distributor extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String distributorName;

    @Column(nullable = false, length = 20)
    private String contactNo;

    private String email;
    @Column(length = 50)
    private String tradeLicenseNo;
    @Column(length = 50)
    private String pesticideLicenseNo;
    @Column(length = 50)
    private String seedLicenseNo;
    @Column(length = 50)
    private String vatRegistrationNo;
    @Column(length = 50)
    private String tinRegistrationNo;

    @Column(nullable = false)
    private String shipToAddress;
    @Column(nullable = false)
    private String billToAddress;

//    @Column(nullable = false)
//    private String branchName;

    @Column(nullable = false, length = 50)
    private String geoLatitude;
    @Column(nullable = false, length = 50)
    private String geoLongitude;
    @Column(nullable = false, length = 50)
    private String radius;

    @Column(nullable = false, length = 50)
    private String chequeNo;
    @Column(nullable = false, length = 50)
    private String chequeType;

    private Double chequeAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_type_id")
    private DistributorType distributorType;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "location_id", nullable = false)
//    private Location location;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "bank_id")
//    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_branch_id")
    private BankBranch bankBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private Location location; // distributor physical location
}
