package com.newgen.ntlsnc.salesandcollection.service;


import com.newgen.ntlsnc.common.enums.Month;
import com.newgen.ntlsnc.common.enums.MonthCode;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetExcelDistributorWiseDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudget;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DistributorWiseSalesBudgetItemProcessor
        implements ItemProcessor<SalesBudgetExcelDistributorWiseDto, List<SalesBudgetDetails>>,
                        StepExecutionListener {

    @Autowired
    ProductService productService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    SalesBudgetRepository salesBudgetRepository;

    private StepExecution stepExecution;

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
            final SalesBudgetExcelDistributorWiseDto salesBudgetExcelDto) {

        List<SalesBudgetDetails> salesBudgetDetailsList = new ArrayList<>();
        for (Month m : Month.values()) {
            SalesBudgetDetails salesBudgetDetails = new SalesBudgetDetails();
            salesBudgetDetails.setProduct(productService.findById(
                                Long.parseLong(salesBudgetExcelDto.getProduct_Id())));

            salesBudgetDetails.setDistributor(distributorService.findById(
                    Long.parseLong(salesBudgetExcelDto.getDistributor_Id())));

            if(salesBudgetExcelDto.getManufacturing_Cost() != ""
                    && salesBudgetExcelDto.getManufacturing_Cost() != null) {
                salesBudgetDetails.setManufacturingPrice(
                        Float.valueOf(salesBudgetExcelDto.getManufacturing_Cost()));
            }
            salesBudgetDetails.setProductTradePrice(Double.parseDouble(salesBudgetExcelDto.getProduct_Trade_price()));
            salesBudgetDetails.setMonth(MonthCode.valueOf(m.getCode()).getName());

            if (m.getName().equalsIgnoreCase("Jan"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getJan());
            if (m.getName().equalsIgnoreCase("Feb"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getFeb());
            if (m.getName().equalsIgnoreCase("Mar"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getMar());
            if (m.getName().equalsIgnoreCase("Apr"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getApr());
            if (m.getName().equalsIgnoreCase("May"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getMay());
            if (m.getName().equalsIgnoreCase("Jun"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getJun());
            if (m.getName().equalsIgnoreCase("Jul"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getJul());
            if (m.getName().equalsIgnoreCase("Aug"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getAug());
            if (m.getName().equalsIgnoreCase("Sep"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getSep());
            if (m.getName().equalsIgnoreCase("Oct"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getOct());
            if (m.getName().equalsIgnoreCase("Nov"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getNov());
            if (m.getName().equalsIgnoreCase("Dec"))
                salesBudgetDetails.setQuantity(salesBudgetExcelDto.getDec());

            Long salesBudgetId = (Long)stepExecution.getJobExecution()
                                                                .getJobParameters()
                                                                .getParameters()
                                                                .get("salesBudgetId")
                                                                .getValue();

            Optional<SalesBudget> salesBudget = salesBudgetRepository.findById(salesBudgetId);
            salesBudgetDetails.setOrganization(salesBudget.get().getOrganization());
            salesBudgetDetails.setSalesBudget(salesBudget.get());

            salesBudgetDetailsList.add(salesBudgetDetails);
        }

        return salesBudgetDetailsList;
    }

}

