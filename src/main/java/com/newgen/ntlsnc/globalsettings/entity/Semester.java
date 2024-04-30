package com.newgen.ntlsnc.globalsettings.entity;

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
public class Semester extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String   semesterName; // June- December

    @Column(nullable = false)
    private LocalDate startDate; // 01-06-2022

    @Column(nullable = false)
    private LocalDate endDate; // 31-12-2022

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_year_id")
    private AccountingYear  accountingYear;
}
