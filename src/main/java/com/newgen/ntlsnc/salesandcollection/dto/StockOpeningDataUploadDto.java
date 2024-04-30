package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Tanjela Aaan
 * @since 19th Jun, 23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockOpeningDataUploadDto {

    private Long depotId;
    private Long companyId;
    private MultipartFile stockOpeningFile;

}
