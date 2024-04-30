package com.newgen.ntlsnc.globalsettings.entity;


import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProductCategory extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String  name; // Agriculture Equipment FG Stock

    @Column(nullable = false, length = 10)
    private String  prefix; //  AE

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductCategoryType productCategoryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Organization company;
}
