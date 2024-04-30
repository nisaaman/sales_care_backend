package com.newgen.ntlsnc.salesandcollection.service;


import com.newgen.ntlsnc.common.BaseEntity;
import com.newgen.ntlsnc.common.enums.InvTransactionType;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.repository.StoreRepository;
import com.newgen.ntlsnc.globalsettings.service.DepotLocationMapService;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.salesandcollection.dto.StockOpeningBalanceExcelDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import com.newgen.ntlsnc.supplychainmanagement.dto.*;
import com.newgen.ntlsnc.supplychainmanagement.entity.Batch;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransfer;
import com.newgen.ntlsnc.supplychainmanagement.repository.BatchDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.service.BatchService;
import com.newgen.ntlsnc.supplychainmanagement.service.InvReceiveService;
import com.newgen.ntlsnc.supplychainmanagement.service.InvTransactionService;
import com.newgen.ntlsnc.supplychainmanagement.service.InvTransferService;
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
import java.util.Optional;
import java.util.Random;

@Service
public class StockOpeningDataItemProcessor
        implements ItemProcessor<StockOpeningBalanceExcelDto, List<SalesBudgetDetails>>,
        StepExecutionListener {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    DepotService depotService;

    @Autowired
    DepotLocationMapService depotLocationMapService;
    @Autowired
    StoreRepository storeRepository;

    private StepExecution stepExecution;

    @Autowired
    private BatchService batchService;

    @Autowired
    private InvReceiveService invReceiveService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InvTransferService invTransferService;

    @Autowired
    private InvTransactionService invTransactionService;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    @Override
    public List<SalesBudgetDetails> process(
            final StockOpeningBalanceExcelDto stockOpeningBalanceExcelDto) {
        List<SalesBudgetDetails> salesBudgetDetailsList = new ArrayList<>();

        JobParameters jobParameters = stepExecution.getJobParameters();
        Long companyId = jobParameters.getLong("companyId");
        Long depotId = jobParameters.getLong("depotId");

        if (stockOpeningBalanceExcelDto.getOpening_Stock() == null) {
            return salesBudgetDetailsList;
        }

        if (stockOpeningBalanceExcelDto.getProduct_Id() == null) {
            Long productId = productService.getProductIdsByNameAndItemSizeAndCompanyAndUom(stockOpeningBalanceExcelDto.getProduct_Name(), stockOpeningBalanceExcelDto.getPack_Size(), companyId, stockOpeningBalanceExcelDto.getUoM(), organizationService.getOrganizationFromLoginUser());
            if (productId == null) {
                System.out.println("Not insert Product Name: "+  stockOpeningBalanceExcelDto.getProduct_Name());
                System.out.println("Not insert Product Pack Size: "+ stockOpeningBalanceExcelDto.getPack_Size());
                System.out.println("Not insert Product company: "+  companyId);
                return salesBudgetDetailsList;
            }
            else
                stockOpeningBalanceExcelDto.setProduct_Id(productId);
        }
        // batch creation
        BatchDto batchDto = new BatchDto();
        batchDto.setBatchQuantity(stockOpeningBalanceExcelDto.getOpening_Stock());
        batchDto.setProductionDate(LocalDate.now().toString());
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        batchDto.setConsignmentNo(generatedString);
        BatchDetailsDto batchDetailsDto = new BatchDetailsDto();
        batchDetailsDto.setProductId(stockOpeningBalanceExcelDto.getProduct_Id());
        batchDto.setBatchDetailsDto(batchDetailsDto);
        batchDto.setCompanyId(companyId);

        Batch batch = batchService.create(batchDto);

        // production receive
        Depot depot = new Depot();
        InvReceiveDto invReceiveDto = new InvReceiveDto();
        invReceiveDto.setCompanyId(companyId);
        boolean isCentralDepot = depotService.exitsByIdAndIsCentralWarehouse(depotId);
        if (isCentralDepot) {
            invReceiveDto.setDepotId(depotId);
        } else {
            depot = depotLocationMapService.getCentalDepotByCompnayId(companyId);
            invReceiveDto.setDepotId(depot != null ? depot.getId() : null);
        }
        InvTransactionDto invTransactionDto = new InvTransactionDto();
        invTransactionDto.setTransactionType(InvTransactionType.PRODUCTION_RECEIVE.getCode());
        invReceiveDto.setInvTransactionDto(invTransactionDto);

        InvTransactionDetailsDto invTransactionDetailsDto = new InvTransactionDetailsDto();
        invTransactionDetailsDto.setBatchId(batch.getId());
        invTransactionDetailsDto.setProductId(stockOpeningBalanceExcelDto.getProduct_Id());
        invTransactionDetailsDto.setQuantity(Float.valueOf(batch.getQuantity()));
        invTransactionDetailsDto.setRate(stockOpeningBalanceExcelDto.getRate());
        Optional<Store> storeOptional = storeRepository.findByOrganizationAndStoreTypeAndIsDeletedFalse(organizationService.getOrganizationFromLoginUser(), StoreType.REGULAR);
        invTransactionDetailsDto.setToStoreId(storeOptional.map(BaseEntity::getId).orElse(null));
        List<InvTransactionDetailsDto> invTransactionDetailsDtoList = new ArrayList<InvTransactionDetailsDto>() {{
            add(invTransactionDetailsDto);
        }};
        invReceiveDto.setInvTransactionDetails(invTransactionDetailsDtoList);
        invReceiveService.create(invReceiveDto);
        // production receive

        if(!isCentralDepot){
            InvTransferDto invTransferDto = new InvTransferDto();
            invTransactionDto = new InvTransactionDto();
            invTransactionDto.setTransactionType(InvTransactionType.TRANSFER_SENT.getCode());
            invTransferDto.setInvTransactionDto(invTransactionDto);

            invTransferDto.setFromDepotId(depot.getId());
            invTransferDto.setToDepotId(depotId);
            invTransferDto.setCompanyId(companyId);

            InvTransactionDetailsDto invTransactionDetailsDtoForSend = new InvTransactionDetailsDto();
            invTransactionDetailsDtoForSend.setBatchId(batch.getId());
            invTransactionDetailsDtoForSend.setProductId(stockOpeningBalanceExcelDto.getProduct_Id());
            invTransactionDetailsDtoForSend.setQuantity(Float.valueOf(batch.getQuantity()));
            invTransactionDetailsDtoForSend.setRate(stockOpeningBalanceExcelDto.getRate());
            invTransactionDetailsDtoForSend.setToStoreId(storeRepository.findByOrganizationAndStoreTypeAndIsDeletedFalse(organizationService.getOrganizationFromLoginUser(), StoreType.IN_TRANSIT).get().getId());
            List<InvTransactionDetailsDto> invTransactionDetailsDtoListForSend = new ArrayList<InvTransactionDetailsDto>() {{
                add(invTransactionDetailsDtoForSend);
            }};
            invTransferDto.setInvTransactionDetailsDtoList(invTransactionDetailsDtoListForSend);

            InvTransfer invTransfer =  invTransferService.create(invTransferDto);


            invTransactionDto = new InvTransactionDto();
            invTransactionDto.setId(invTransfer.getInvTransaction().getId());
            invTransactionDto.setTransactionType(InvTransactionType.TRANSFER_RECEIVE.getCode());
            invTransactionDto.setCompanyId(companyId);
            invTransactionDto.setStoreType(StoreType.REGULAR.getCode());

            InvTransactionDetailsDto invTransactionDetailsDtoForReceive = new InvTransactionDetailsDto();
            invTransactionDetailsDtoForReceive.setBatchId(batch.getId());
            invTransactionDetailsDtoForReceive.setProductId(stockOpeningBalanceExcelDto.getProduct_Id());
            invTransactionDetailsDtoForReceive.setQuantity(Float.valueOf(batch.getQuantity()));
            invTransactionDetailsDtoForReceive.setRate(stockOpeningBalanceExcelDto.getRate());
            invTransactionDetailsDtoForReceive.setToStoreId(storeRepository.findByOrganizationAndStoreTypeAndIsDeletedFalse(organizationService.getOrganizationFromLoginUser(), StoreType.REGULAR).get().getId());
            List<InvTransactionDetailsDto> invTransactionDetailsDtoListForReceive = new ArrayList<InvTransactionDetailsDto>() {{
                add(invTransactionDetailsDtoForReceive);
            }};
            invTransactionDto.setInvTransactionDetailsDtoList(invTransactionDetailsDtoListForReceive);

            invTransactionService.create(invTransactionDto);


        }
        return salesBudgetDetailsList;
    }

}

