package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Tanjela Aaan
 * @since 8th Aug, 23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributorOpeningBalanceDto {
    private Long companyId;
    private MultipartFile openingBalanceFile;

}
