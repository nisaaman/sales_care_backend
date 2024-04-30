package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sagor
 * Created on 5/4/22 10:29 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributorDto {

    private Long id;

    @NotNull(message = "Distributor name field is required.")
    private String distributorName;

    @NotNull(message = "Contact No field is required.")
    private String contactNo;

    private String email;

    private String tradeLicenseNo;
    private String pesticideLicenseNo;
    private String seedLicenseNo;
    private String vatRegistrationNo;
    private String tinRegistrationNo;

    @NotNull(message = "Ship To Address field is required.")
    private String shipToAddress;
    @NotNull(message = "Bill To Address field is required.")
    private String billToAddress;

    @NotNull(message = "Bank Branch field is required.")
    private Long branchId;

    private String branchName;

    @NotNull(message = "Geo Latitude field is required.")
    private String geoLatitude;
    @NotNull(message = "Geo Longitude field is required.")
    private String geoLongitude;
    @NotNull(message = "Radius field is required.")
    private String radius;

    @NotNull(message = "Cheque No field is required.")
    private String chequeNo;
    @NotNull(message = "Cheque Type field is required.")
    private String chequeType;

    private Double chequeAmount;

    @NotNull(message = "Distributor Type field is required.")
    private Long distributorTypeId;

//     @NotNull(message = "Location field is required.")
//     private Long locationId;

    private Long bankId;
    private String bankName;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;

    private List<ProprietorDto> proprietorDtoList;
    private List<DistributorGuarantorDto> distributorGuarantorDtoList;
    private List<DistributorSalesOfficerMapDto> distributorSalesOfficerMapDtoList;

    private MultipartFile distributorLogo;
    private MultipartFile[] proprietorLogoList;
    private MultipartFile[] distributorGuarantorLogoList;
    private String filePath;
    private Boolean isActive;
    private Long locationId;
}
