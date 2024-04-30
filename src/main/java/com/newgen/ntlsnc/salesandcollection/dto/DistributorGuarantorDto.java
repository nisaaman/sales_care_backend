package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author marziah
 * Created on 5/4/22 11:00 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributorGuarantorDto {
    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;
    @NotNull(message = "NID field is required.")
    private String nid;

    private String fatherName;
    private String motherName;

    @NotNull(message = "Contact No. field is required.")
    private String contactNo;

    private String address;

    //    @NotNull(message = "Distributor field is required.")
//    private Long distributorId;
//
//    @NotNull(message = "Organization field is required.")
//    private Long organizationId;
    private String fileName;
    private String filePath;

}
