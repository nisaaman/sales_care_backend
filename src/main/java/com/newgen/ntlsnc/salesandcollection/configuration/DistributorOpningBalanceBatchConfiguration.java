
package com.newgen.ntlsnc.salesandcollection.configuration;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorOpeningBalanceExcelDto;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorBalance;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorBalanceRepository;
import com.newgen.ntlsnc.salesandcollection.service.DistributorOpeningBalanceDataItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
 * @since 8th Aug,23
 */


@Configuration
@EnableBatchProcessing
public class DistributorOpningBalanceBatchConfiguration {

    @Autowired
    DistributorBalanceRepository distributorBalanceRepository;
    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    JobBuilderFactory jobBuilderFactory;

    private RowMapper<DistributorOpeningBalanceExcelDto> excelRowMapper() {
        BeanWrapperRowMapper<DistributorOpeningBalanceExcelDto> rowMapper = new BeanWrapperRowMapper<>();
        try {
            rowMapper.setTargetType(DistributorOpeningBalanceExcelDto.class);
            return rowMapper;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rowMapper;
    }

    @StepScope
    @Bean
    ItemReader<DistributorOpeningBalanceExcelDto> distributorOpeningBalanceExcelFileReader(@Value("#{jobParameters['fileName']}") String fileName) {
        PoiItemReader<DistributorOpeningBalanceExcelDto> reader = new PoiItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource(CommonConstant.DISTRIBUTOR_OPENING_BALANCE_FILE_UPLOAD_PATH+fileName));
        reader.setRowMapper(excelRowMapper());
        reader.setStrict(false);
        reader.open(new ExecutionContext());

       return reader;
    }

    @Bean
    public DistributorOpeningBalanceDataItemProcessor distributorOpeningBalanceProcessor() {
        return new DistributorOpeningBalanceDataItemProcessor();
    }

    @Bean
    ItemWriter<List<DistributorBalance>> distributorOpeningBalanceWriter() {
        RepositoryListItemWriter<List<DistributorBalance>> writer = new RepositoryListItemWriter<>();
        writer.setRepository(distributorBalanceRepository);
        writer.setMethodName("saveAll");
        return writer;
    }

    @Bean
    Step distributorOpeningBalanceExcelFileToDatabaseStep() {

        return stepBuilderFactory.get("distributorOpeningBalance")
                .<DistributorOpeningBalanceExcelDto, List<DistributorBalance>>chunk(2)
                .reader(distributorOpeningBalanceExcelFileReader(null))
                .processor(distributorOpeningBalanceProcessor())
                .writer(distributorOpeningBalanceWriter())
                .build();
    }

    @Bean(name = "distributorOpeningBalance")
    Job excelFileToDatabaseJob() {
        return jobBuilderFactory.get("distributorOpeningBalance")
                .incrementer(new RunIdIncrementer())
                .flow(distributorOpeningBalanceExcelFileToDatabaseStep())
                .end()
                .build();
    }
}

