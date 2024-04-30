package com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ২০/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCollectionAdjustedDto {

    @NotNull(message = "Payment is required.")
    private Long id;
    @NotNull(message = "Payment number is required.")
    private String paymentNo;
    @NotNull(message = "Money receipt number is required.")
    private String moneyReceiptNo;
    @NotNull(message = "Payment Date field is required.")
    private String paymentDate;
    @NotNull(message = "Remaining amount is required.")
    private Float remainingAmount;
    @NotNull(message = "Collection amount amount is required.")
    private Float collectionAmount;

    private String adjustType;

}
