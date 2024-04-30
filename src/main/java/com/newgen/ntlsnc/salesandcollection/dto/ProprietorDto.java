package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProprietorDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "NID field is required.")
    private String nid;

    private String fatherName;
    private String motherName;

    @NotNull(message = "Contact field is required.")
    private String contactNo;
    private String address;
    //    private Long distributorId;
//    private Long organizationId;
//    private String file;
    private String fileName;
    private String filePath;


}
