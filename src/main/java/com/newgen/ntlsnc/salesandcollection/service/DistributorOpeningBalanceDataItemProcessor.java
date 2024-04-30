package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.PaymentNature;
import com.newgen.ntlsnc.common.enums.PaymentType;
import com.newgen.ntlsnc.globalsettings.dto.PaymentBookDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.PaymentBook;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.globalsettings.service.InvoiceNatureService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.PaymentBookService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorOpeningBalanceExcelDto;
import com.newgen.ntlsnc.salesandcollection.dto.PaymentCollectionDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorBalance;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author nisa
 * @Date ৮/৮/২৩ , ১১:৩৫ AM
 */
@Service
public class DistributorOpeningBalanceDataItemProcessor implements ItemProcessor<DistributorOpeningBalanceExcelDto, List<DistributorBalance>>,
        StepExecutionListener {

    @Autowired
    DistributorService distributorService;
    @Autowired
    InvoiceNatureService invoiceNatureService;

    @Autowired
    PaymentCollectionService paymentCollectionService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    DepotService depotService;

    @Autowired
    PaymentBookService paymentBookService;

    @Autowired
    DistributorBalanceService distributorBalanceService;

    private StepExecution stepExecution;


    private static final Logger LOGGER = LoggerFactory.getLogger(DistributorOpeningBalanceDataItemProcessor.class);

    @Override
    public void beforeStep(@NotNull StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    @Override
    public List<DistributorBalance> process(
            final DistributorOpeningBalanceExcelDto distributorOpeningBalanceExcelDto) {
        List<DistributorBalance> distributorBalanceList = new ArrayList<>();
        JobParameters jobParameters = stepExecution.getJobParameters();
        Long companyId = jobParameters.getLong("companyId");
        Organization company = organizationService.findById(companyId);
        Distributor distributor = null;

        if(distributorOpeningBalanceExcelDto.getDistributor_Id() == null && distributorOpeningBalanceExcelDto.getDistributor_Name() !=null && (distributorOpeningBalanceExcelDto.getCredit() != null || distributorOpeningBalanceExcelDto.getCash() != null || distributorOpeningBalanceExcelDto.getAdvance() != null)) {
            Long distributorId = distributorService.findByNameAndCompany(company, distributorOpeningBalanceExcelDto.getDistributor_Name());
            if(distributorId != null) {
                distributor = distributorService.findById(distributorId);
            }

            if(distributor == null) {
                    Optional<Distributor> distributorOptional= distributorService.findByName(company.getParent(), distributorOpeningBalanceExcelDto.getDistributor_Name());
                    distributor = distributorOptional.orElse(null);
            }
            if(distributor == null) {
                System.out.println("Not insert Distributor Name : " + distributorOpeningBalanceExcelDto.getDistributor_Name());
                System.out.println("Not insert Distributor Cash Balance : " + distributorOpeningBalanceExcelDto.getCash());
                System.out.println("Not insert Distributor Credit Balance : " + distributorOpeningBalanceExcelDto.getCredit());
                System.out.println("Not insert Distributor Advance Balance : " + distributorOpeningBalanceExcelDto.getAdvance());
                return distributorBalanceList;
            }
        }
        if (distributorOpeningBalanceExcelDto.getDistributor_Id() == null && distributor==null && (distributorOpeningBalanceExcelDto.getCredit() == null || distributorOpeningBalanceExcelDto.getCash() == null || distributorOpeningBalanceExcelDto.getAdvance() == null)) {
            if (distributorOpeningBalanceExcelDto.getDistributor_Id() == null) {
                LOGGER.info("Distributor ID not found. " + distributorOpeningBalanceExcelDto.getDistributor_Name());
            } else if (distributorOpeningBalanceExcelDto.getCredit() == null) {
                LOGGER.info("Distributor Credit Balance not found. " + distributorOpeningBalanceExcelDto.getDistributor_Name());
            } else if (distributorOpeningBalanceExcelDto.getCash() == null) {
                LOGGER.info("Distributor Cash Balance not found. " + distributorOpeningBalanceExcelDto.getDistributor_Name());
            } else if (distributorOpeningBalanceExcelDto.getAdvance() == null) {
                LOGGER.info("Distributor Advance Balance not found. " + distributorOpeningBalanceExcelDto.getDistributor_Name());
            } else if (distributorOpeningBalanceExcelDto.getDistributor_Name() == null) {
                LOGGER.info("Distributor Name Not not found." + distributorOpeningBalanceExcelDto.getDistributor_Name());
            }
            return distributorBalanceList;
        }
        if (distributorOpeningBalanceExcelDto.getDistributor_Id() != null) {
            distributor = distributorService.findById(distributorOpeningBalanceExcelDto.getDistributor_Id());
        }
        List<Long> distributorBalanceIds = distributorBalanceService.getNotUsedListByDistributorIdAndCompanyId(distributor.getId(), companyId);
        List<Long> paymentCollectionIds = paymentCollectionService.getNotUsedListByDistributorIdAndCompanyId(distributor.getId(), companyId);
        if (!distributorBalanceIds.isEmpty() && !paymentCollectionIds.isEmpty()) {
            distributorBalanceService.deleteAll(distributorBalanceIds);
            paymentCollectionService.deleteAll(paymentCollectionIds);
        }

        if (distributorBalanceService.existsByDistributorAndCompany(distributor, company)) {
            LOGGER.info("Distributor Already Exits in Distributor Balance: " + distributor.getDistributorName());
            return new ArrayList<>();
        }

        if (paymentCollectionService.existsByDistributorAndCompany(distributor, company)) {
            LOGGER.info("Distributor Already Exits in Payment Collection: " + distributor.getDistributorName());
            return new ArrayList<>();
        }
        Organization organization = organizationService.getOrganizationFromLoginUser();
        String distributorCode = "_" + distributor.getDistributorName().split(" ")[0];

        if (distributorOpeningBalanceExcelDto.getCredit() != null) {
            DistributorBalance distributorBalance = new DistributorBalance();
            distributorBalance.setDistributor(distributor);
            distributorBalance.setBalance(distributorOpeningBalanceExcelDto.getCredit());
            distributorBalance.setRemainingBalance(distributorOpeningBalanceExcelDto.getCredit());
            distributorBalance.setInvoiceNature(invoiceNatureService.findById(CommonConstant.CREDIT_INVOICE_NATURE));
            distributorBalance.setReferenceNo(distributorOpeningBalanceExcelDto.getReference_No() + distributorCode);
            distributorBalance.setCompany(company);
            distributorBalance.setOrganization(organization);
            distributorBalance.setTransactionDate(LocalDate.now());
            distributorBalanceList.add(distributorBalance);
            LOGGER.info("Credit Distributor Balance : " + distributorBalance);
        }

//        if (distributorOpeningBalanceExcelDto.getCash() != null) {
//            DistributorBalance distributorBalance = new DistributorBalance();
//            distributorBalance.setDistributor(distributor);
//            distributorBalance.setBalance(distributorOpeningBalanceExcelDto.getCash());
//            distributorBalance.setRemainingBalance(distributorOpeningBalanceExcelDto.getCash());
//           // distributorBalance.setInvoiceNature(invoiceNatureService.findById(CommonConstant.CASH_INVOICE_NATURE));
//            distributorBalance.setReferenceNo(distributorOpeningBalanceExcelDto.getReference_No() + distributorCode);
//            distributorBalance.setCompany(company);
//            distributorBalance.setOrganization(organization);
//            distributorBalance.setTransactionDate(LocalDate.now());
//            distributorBalanceList.add(distributorBalance);
//            LOGGER.info("Cash Distributor Balance : " + distributorBalance);
//
//        }
        if (distributorOpeningBalanceExcelDto.getAdvance() != null) {
            Map locationMap = depotService.getDepotAndTerritoryLocationByCompanyIdAndDistributorId(companyId, distributor.getId());
            if (!locationMap.isEmpty()) {
                PaymentCollectionDto paymentCollectionDto = new PaymentCollectionDto();
                paymentCollectionDto.setCompanyId(companyId);
                paymentCollectionDto.setDistributorId(distributor.getId());
                paymentCollectionDto.setReferenceNo(distributorOpeningBalanceExcelDto.getReference_No() + distributorCode);
                paymentCollectionDto.setCollectionAmount(distributorOpeningBalanceExcelDto.getAdvance().doubleValue());
                paymentCollectionDto.setPaymentNature(PaymentNature.ADVANCE.getCode());
                paymentCollectionDto.setApprovalStatus(ApprovalStatus.APPROVED.getCode());
                paymentCollectionDto.setApprovalStatusForAuthorization(ApprovalStatus.APPROVED.getCode());
                paymentCollectionDto.setPaymentType(PaymentType.CASH.getCode());
                Long locationId = Long.parseLong(locationMap.get("location_id").toString());
                PaymentBook paymentBook = paymentBookService.findPaymentBookByCompanyAndLocation(companyId, locationId);
                if (paymentBook != null) {
                    paymentCollectionDto.setPaymentBookId(paymentBook.getId());
                    Map<String, Object> moneyReceiptNoMap = paymentCollectionService.getLastMoneyReceiptNo(paymentBook.getId());
                    if (!moneyReceiptNoMap.isEmpty()) {
                        Long lastMoneyReceiptNo = Long.parseLong(moneyReceiptNoMap.get("moneyReceiptNo").toString());
                        Long toMrNo = Long.parseLong(moneyReceiptNoMap.get("toMrNo").toString());
                        if (lastMoneyReceiptNo < toMrNo) {
                            paymentCollectionDto.setMoneyReceiptNo(lastMoneyReceiptNo + 1L);
                        } else if (lastMoneyReceiptNo.equals(toMrNo)) {
                            paymentBook.setToMrNo(paymentBook.getToMrNo() + 1L);
                            paymentBookService.updateToMrNo(paymentBook);
                            paymentCollectionDto.setMoneyReceiptNo(paymentBook.getToMrNo());
                        }
                    } else {
                        paymentCollectionDto.setMoneyReceiptNo(paymentBook.getFromMrNo());
                    }

                } else {
                    PaymentBookDto paymentBookDto = new PaymentBookDto();
                    paymentBookDto.setBookNumber("Payment_Book" + "_" + locationMap.get("location_name").toString());
                    paymentBookDto.setCompanyId(companyId);
                    paymentBookDto.setFromMrNo(1L);
                    paymentBookDto.setToMrNo(1L);
                    paymentBookDto.setPaymentBookLocationId(locationId);
                    paymentBookDto.setStatus(true);
                    paymentBook = paymentBookService.create(paymentBookDto);
                    paymentCollectionDto.setPaymentBookId(paymentBook.getId());
                    paymentCollectionDto.setMoneyReceiptNo(paymentBookDto.getFromMrNo());
                }
                LOGGER.info("Advance Distributor Balance : " + paymentCollectionDto);
                paymentCollectionService.create(paymentCollectionDto);
            }

        }

        return distributorBalanceList;
    }

}