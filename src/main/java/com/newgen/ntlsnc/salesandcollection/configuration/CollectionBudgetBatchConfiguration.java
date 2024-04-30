package com.newgen.ntlsnc.salesandcollection.configuration;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.salesandcollection.dto.CollectionBudgetExcelDistributorWiseDto;
import com.newgen.ntlsnc.salesandcollection.entity.CollectionBudgetDetails;
import com.newgen.ntlsnc.salesandcollection.repository.CollectionBudgetDetailsCrudRepository;
import com.newgen.ntlsnc.salesandcollection.service.DistributorWiseCollectionBudgetItemProcessor;
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
 * @author Newaz Sharif
 * @since 25th Aug, 22
 */
@Configuration
@EnableBatchProcessing
public class CollectionBudgetBatchConfiguration {

    @Autowired
    CollectionBudgetDetailsCrudRepository collectionBudgetDetailsCrudRepository;
    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    JobBuilderFactory jobBuilderFactory;

    private RowMapper<CollectionBudgetExcelDistributorWiseDto> excelRowMapper() {
        BeanWrapperRowMapper<CollectionBudgetExcelDistributorWiseDto> rowMapper = new BeanWrapperRowMapper<>();
        try {
            rowMapper.setTargetType(CollectionBudgetExcelDistributorWiseDto.class);
            return rowMapper;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rowMapper;
    }

    @StepScope
    @Bean
    ItemReader<CollectionBudgetExcelDistributorWiseDto> collectionBudgetExcelFileReader(
                @Value("#{jobParameters['fileName']}") String fileName) {
        PoiItemReader<CollectionBudgetExcelDistributorWiseDto> reader = new PoiItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource(CommonConstant.COLLECTION_BUDGET_FILE_UPLOAD_PATH+fileName));
        reader.setRowMapper(excelRowMapper());
        reader.setStrict(false);
        reader.open(new ExecutionContext());

        return reader;
    }

    @StepScope
    @Bean
    public DistributorWiseCollectionBudgetItemProcessor collectionBudgetProcessor() {
        return new DistributorWiseCollectionBudgetItemProcessor();
    }

    @Bean
    ItemWriter<List<CollectionBudgetDetails>> collectionBudgetWriter() {
        RepositoryListItemWriter<List<CollectionBudgetDetails>> writer = new RepositoryListItemWriter<>();
        writer.setRepository(collectionBudgetDetailsCrudRepository);
        writer.setMethodName("saveAll");
        return writer;
    }

    @Bean
    Step collectionBudgetExcelFileToDatabaseStep() {
        return stepBuilderFactory.get("collectionBudget")
                .<CollectionBudgetExcelDistributorWiseDto,
                        List<CollectionBudgetDetails>>chunk(1000)
                .reader(collectionBudgetExcelFileReader(null))
                .processor(collectionBudgetProcessor())
                .writer(collectionBudgetWriter())
                .build();
    }

    @Bean(name = "collectionBudget")
    Job excelFileToDatabaseJob() {
        return jobBuilderFactory.get("collectionBudget")
                .incrementer(new RunIdIncrementer())
                .flow(collectionBudgetExcelFileToDatabaseStep())
                .end()
                .build();
    }

    /*@Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }*/

}
