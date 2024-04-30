package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "Address field is required.")
    private String address;

    @NotNull(message = "ShortName field is required.")
    private String shortName;

    @NotNull(message = "Email field is required.")
    private String email;

    private String webAddress;

    @NotNull(message = "ContactNumber field is required.")
    private String contactNumber;

    @NotNull(message = "ContactPerson field is required.")
    private String contactPerson;

    private String remarks;

    @NotNull(message = "Organization field is required.")
    private Long parentId;

    @NotNull(message = "SubscriptionPackage field is required.")
    private Long subscriptionPackageId;

    MultipartFile files;

    private String imageString;

    private Long documentId;

    private Long locationTreeId;

    private Boolean isNewLocation; // true= new location need to be created, false = existed location

    private LocationTreeDto locationTreeDto;
}
