package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankBranch extends SuperEntity<Long> {

    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, length = 20, unique = true)
    private String contactNumber;
    @Column(length = 100, unique = true)
    @Email
    private String email;
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;


}
