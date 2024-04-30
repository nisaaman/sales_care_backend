package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.enums.Month;
import com.newgen.ntlsnc.common.enums.MonthCode;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.salesandcollection.dto.CollectionBudgetExcelDistributorWiseDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetExcelDistributorWiseDto;
import com.newgen.ntlsnc.salesandcollection.entity.CollectionBudget;
import com.newgen.ntlsnc.salesandcollection.entity.CollectionBudgetDetails;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudget;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import com.newgen.ntlsnc.salesandcollection.repository.CollectionBudgetRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Newaz Sharif
 * @since 27th Aug, 22
 */
public class DistributorWiseCollectionBudgetItemProcessor
        implements ItemProcessor<CollectionBudgetExcelDistributorWiseDto,
                    List<CollectionBudgetDetails>>, StepExecutionListener {

    @Autowired
    DistributorService distributorService;
    @Autowired
    CollectionBudgetRepository collectionBudgetRepository;

    @Autowired
    ProductService productService;

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
    public List<CollectionBudgetDetails> process(
            final CollectionBudgetExcelDistributorWiseDto budgetExcelDistributorWiseDto) {

        List<CollectionBudgetDetails> collectionBudgetDetailsLst = new ArrayList<>();
        for (Month m : Month.values()) {
            CollectionBudgetDetails collectionBudgetDetails = new CollectionBudgetDetails();

            collectionBudgetDetails.setProduct(productService.findById(
                    Long.parseLong(budgetExcelDistributorWiseDto.getProduct_Id())));

            collectionBudgetDetails.setDistributor(distributorService.findById(
                    Long.parseLong(budgetExcelDistributorWiseDto.getDistributor_Id())));

            collectionBudgetDetails.setProductTradePrice(Double.parseDouble(budgetExcelDistributorWiseDto.getProduct_Trade_price()));
            collectionBudgetDetails.setMonth(MonthCode.valueOf(m.getCode()).getName());

            if (m.getName().equalsIgnoreCase("Jan"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getJan());
            if (m.getName().equalsIgnoreCase("Feb"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getFeb());
            if (m.getName().equalsIgnoreCase("Mar"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getMar());
            if (m.getName().equalsIgnoreCase("Apr"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getApr());
            if (m.getName().equalsIgnoreCase("May"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getMay());
            if (m.getName().equalsIgnoreCase("Jun"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getJun());
            if (m.getName().equalsIgnoreCase("Jul"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getJul());
            if (m.getName().equalsIgnoreCase("Aug"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getAug());
            if (m.getName().equalsIgnoreCase("Sep"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getSep());
            if (m.getName().equalsIgnoreCase("Oct"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getOct());
            if (m.getName().equalsIgnoreCase("Nov"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getNov());
            if (m.getName().equalsIgnoreCase("Dec"))
                collectionBudgetDetails.setCollectionBudgetAmount(budgetExcelDistributorWiseDto.getDec());

            Long collectionBudgetId = (Long)stepExecution.getJobExecution()
                    .getJobParameters()
                    .getParameters()
                    .get("collectionBudgetId")
                    .getValue();

            Optional<CollectionBudget> collectionBudget =
                                collectionBudgetRepository.findById(collectionBudgetId);
            collectionBudgetDetails.setOrganization(collectionBudget.get().getOrganization());
            collectionBudgetDetails.setCollectionBudget(collectionBudget.get());

            collectionBudgetDetailsLst.add(collectionBudgetDetails);
        }

        return collectionBudgetDetailsLst;
    }
}
