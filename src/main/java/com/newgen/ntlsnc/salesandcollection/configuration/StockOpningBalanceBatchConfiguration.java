
package com.newgen.ntlsnc.salesandcollection.configuration;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.salesandcollection.dto.StockOpeningBalanceExcelDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetDetailsCrudRepository;
import com.newgen.ntlsnc.salesandcollection.service.StockOpeningDataItemProcessor;
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

import java.util.List;


/**
 * @author Tanjela Aman
 * @@since 19th Jun, 23
 */


@Configuration
@EnableBatchProcessing
public class StockOpningBalanceBatchConfiguration {

    @Autowired
    SalesBudgetDetailsCrudRepository stockOpeningBalanceDetailsRepository;
    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    JobBuilderFactory jobBuilderFactory;
    private StepExecution stepExecution;
    private String fileName;

    private RowMapper<StockOpeningBalanceExcelDto> excelRowMapper() {
        BeanWrapperRowMapper<StockOpeningBalanceExcelDto> rowMapper = new BeanWrapperRowMapper<>();
        try {
            rowMapper.setTargetType(StockOpeningBalanceExcelDto.class);
            return rowMapper;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rowMapper;
    }

    @StepScope
    @Bean
    ItemReader<StockOpeningBalanceExcelDto> stockOpeningBalanceExcelFileReader(@Value("#{jobParameters['fileName']}") String fileName, @Value("#{jobParameters['companyId']}") Long companyId) {
        System.out.println("companyId : "+companyId);
        PoiItemReader<StockOpeningBalanceExcelDto> reader = new PoiItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource(CommonConstant.STOCK_OPENING_FILE_UPLOAD_PATH+fileName));
        reader.setRowMapper(excelRowMapper());
        reader.setStrict(false);
        reader.open(new ExecutionContext());

       return reader;
    }

    @Bean
    public StockOpeningDataItemProcessor stockOpeningBalanceProcessor() {
        return new StockOpeningDataItemProcessor();
    }

    @Bean
    ItemWriter<List<SalesBudgetDetails>> stockOpeningBalanceWriter() {
        RepositoryListItemWriter<List<SalesBudgetDetails>> writer = new RepositoryListItemWriter<>();
        writer.setRepository(stockOpeningBalanceDetailsRepository);
        writer.setMethodName("saveAll");
        return writer;
    }

    @Bean
    Step stockOpeningBalanceExcelFileToDatabaseStep() {

        return stepBuilderFactory.get("stockOpeningData")
                .<StockOpeningBalanceExcelDto, List<SalesBudgetDetails>>chunk(2)
                .reader(stockOpeningBalanceExcelFileReader(null, null))
                .processor(stockOpeningBalanceProcessor())
                .writer(stockOpeningBalanceWriter())
                //.taskExecutor(taskExecutor())
                .build();
    }

    @Bean(name = "stockOpeningData")
    Job excelFileToDatabaseJob() {
        return jobBuilderFactory.get("stockOpeningData")
                .incrementer(new RunIdIncrementer())
                .flow(stockOpeningBalanceExcelFileToDatabaseStep())
                .end()
                .build();
    }
}

