
package com.newgen.ntlsnc.salesandcollection.configuration;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetExcelDistributorWiseDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetDetailsCrudRepository;
import com.newgen.ntlsnc.salesandcollection.service.DistributorWiseSalesBudgetItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.mapping.BeanWrapperRowMapper;
import org.springframework.batch.extensions.excel.poi.PoiItemReader;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import java.util.List;


/**
 * @author Newaz Sharif
 * @@since 18th Aug, 22
 */


@Configuration
@EnableBatchProcessing
public class SalesBudgetBatchConfiguration {

    @Autowired
    DistributorWiseSalesBudgetItemProcessor distributorWiseSalesBudgetItemProcessor;
    @Autowired
    SalesBudgetDetailsCrudRepository salesBudgetDetailsRepository;
    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    JobBuilderFactory jobBuilderFactory;
    private StepExecution stepExecution;
    private String fileName;
    /*@Bean
    FieldSetMapper<SalesBudgetDetails> fieldSetMapper() {
        BeanWrapperFieldSetMapper<SalesBudgetDetails> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(SalesBudgetDetails.class);
        fieldSetMapper.setConversionService(ApplicationConversionService.getSharedInstance());
        return fieldSetMapper;
    }*/

    private RowMapper<SalesBudgetExcelDistributorWiseDto> excelRowMapper() {
        BeanWrapperRowMapper<SalesBudgetExcelDistributorWiseDto> rowMapper = new BeanWrapperRowMapper<>();
        try {
            rowMapper.setTargetType(SalesBudgetExcelDistributorWiseDto.class);
            return rowMapper;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rowMapper;
    }

    @StepScope
    @Bean
    ItemReader<SalesBudgetExcelDistributorWiseDto> salesBudgetExcelFileReader(@Value("#{jobParameters['fileName']}") String fileName) {
        PoiItemReader<SalesBudgetExcelDistributorWiseDto> reader = new PoiItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource(CommonConstant.SALES_BUDGET_FILE_UPLOAD_PATH+fileName));
        reader.setRowMapper(excelRowMapper());
        reader.setStrict(false);
        reader.open(new ExecutionContext());

       return reader;
    }

    @Bean
    public DistributorWiseSalesBudgetItemProcessor salesBudgetProcessor() {
        return new DistributorWiseSalesBudgetItemProcessor();
    }

    @Bean
    ItemWriter<List<SalesBudgetDetails>> salesBudgetWriter() {
        RepositoryListItemWriter<List<SalesBudgetDetails>> writer = new RepositoryListItemWriter<>();
        writer.setRepository(salesBudgetDetailsRepository);
        writer.setMethodName("saveAll");
        return writer;
    }

    @Bean
    Step salesBudgetExcelFileToDatabaseStep() {

        return stepBuilderFactory.get("salesBudget")
                .<SalesBudgetExcelDistributorWiseDto, List<SalesBudgetDetails>>chunk(1000)
                .reader(salesBudgetExcelFileReader(null))
                .processor(salesBudgetProcessor())
                .writer(salesBudgetWriter())
                //.taskExecutor(taskExecutor())
                .build();
    }

    @Bean(name = "salesBudget")
    Job excelFileToDatabaseJob() {
        return jobBuilderFactory.get("salesBudget")
                .incrementer(new RunIdIncrementer())
                .flow(salesBudgetExcelFileToDatabaseStep())
                .end()
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

}

