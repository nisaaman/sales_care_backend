package com.newgen.ntlsnc;

import com.newgen.ntlsnc.common.AuditorAwareImpl;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.io.InputStream;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "NTL-SNC API", version = "1.0", description = "NTL Product Sales and Channel"))
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class NtlSncApplication {

	@Autowired
	DataSource dataSource;

	public static void main(String[] args) {
		SpringApplication.run(NtlSncApplication.class, args);
	}

	@Bean
	public void loadStoredProcedureScript() throws Exception {
		ResourceDatabasePopulator resourceDatabasePopulator=new ResourceDatabasePopulator();
		resourceDatabasePopulator.setSeparator("DELIMITER");
		InputStream inputStream = new ClassPathResource("/sql/StoredProcedure").getInputStream();
		resourceDatabasePopulator.addScript(new InputStreamResource(inputStream));
		resourceDatabasePopulator.execute(dataSource);
	}

	@Bean
	public void loadViewTableScript() throws Exception {
		ResourceDatabasePopulator resourceDatabasePopulator=new ResourceDatabasePopulator();
		resourceDatabasePopulator.setSeparator("DELIMITER");
		InputStream inputStream = new ClassPathResource("/sql/ViewTable").getInputStream();
		resourceDatabasePopulator.addScript(new InputStreamResource(inputStream));
		resourceDatabasePopulator.execute(dataSource);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public AuditorAware<Long> auditorAware() {
		return new AuditorAwareImpl();
	}

}
