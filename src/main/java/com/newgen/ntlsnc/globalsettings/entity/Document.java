package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Document extends SuperEntity<Long> {

    @Column(nullable = false)
    private String refTable; // DistributorGuarantor

    @Column(nullable = false)
    private String filePath; // file path of file

    @Column(nullable = false)
    private Long refId; //  1

    @Column(nullable = false)
    private String fileName; // employee_photo logo


    private Long fileSize; // bytes

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType; // photo, nid, doc, logo

    @ManyToOne
    @JoinColumn(name = "companyId", nullable = false)
    private Organization company;

}
