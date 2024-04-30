package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.PaymentBook;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author kamal
 * @Date ২০/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCollectionDto {

    private Long id;

//    @NotNull(message = "Payment No. field is required.")
//    private String paymentNo;

    //@NotNull(message = "Reference No. field is required.")
    private String referenceNo;

    @NotNull(message = "Payment Date field is required.")
    private String paymentDate;
    @NotNull(message = "Collection Amount field is required.")
    private Double collectionAmount;

    @NotNull(message = "Money Receipt No field is required")
    private Long moneyReceiptNo;
    private String rejectReason;
    private String actionTakenDate;

    private String remarks;
    @NotNull(message = "Payment Book field is required.")
    private Long paymentBookId;
    private Long salesBookingId;
    private String approvalStatus;
    //@NotNull(message = "Action Taken By field is required.")
    private Long actionTakenById;

    @NotNull(message = "Payment Nature field is required.")
    private String paymentNature;

    @NotNull(message = "Payment Type field is required.")
    private String paymentType;

    @NotNull(message = "Distributor field is required.")
    private Long distributorId;

    @NotNull(message = "Company field is required.")
    private Long companyId;
    private Long bankBranchId;
    private  MultipartFile file;

    private String approvalStatusForAuthorization;
}
