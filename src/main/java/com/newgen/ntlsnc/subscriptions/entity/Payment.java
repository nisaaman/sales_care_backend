package com.newgen.ntlsnc.subscriptions.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payment extends SuperEntity<Long>{

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private String paymentMethod; //BKash,Nogod

    @ManyToOne
    @JoinColumn(name = "subscription_package_id")
    private SubscriptionPackage subscriptionPackage;
}
