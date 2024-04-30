package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author sunipa
 * @since 21-12-2023
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributorUploadDto {
    private Long companyId;
    private MultipartFile distributorUploadFile;

}
