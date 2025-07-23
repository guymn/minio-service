package com.pccth.minio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MinioServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinioServiceApplication.class, args);
	}

}
