package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Newaz Sharif
 * @since 25th Aug,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionBudgetUploadDto {
    private Long accountingYearId;
    private Long companyId;
    private MultipartFile collectionBudgetFile;
}
