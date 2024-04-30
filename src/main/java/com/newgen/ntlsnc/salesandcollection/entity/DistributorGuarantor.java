package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DistributorGuarantor extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 30)
    private String nid;

    @Column(length = 100)
    private String fatherName;
    @Column(length = 100)
    private String motherName;
    @Column(nullable = false, length = 20)
    private String contactNo;
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id")
    private Distributor distributor;
}
