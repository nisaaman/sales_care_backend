package com.newgen.ntlsnc.supplychainmanagement.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Newaz Sharif
 * @since 30th Oct, 22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityInspectionDto {

    private Long id;
    @NotNull(message = "Company is Required")
    private Long companyId;
    @NotNull(message = "Depot is Required")
    private Long depotId;
    @NotNull(message = "Store is Required")
    private Long storeId;
    private Long qaBy;
    private String qaDate;
    private MultipartFile file;
    List<QualityInspectionDetailsDto> qualityInspectionDetails;
}
