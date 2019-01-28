package com.sunshine.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

/**
 * @author oyyl
 * @since 2018/08/29
 *
 */
@SpringBootApplication
@Configuration
@ImportResource("classpath:/spring/applicationContext.xml")
@EnableAutoConfiguration
@ComponentScan
public class VehicleDeckBootApplication {

	private static final Logger log = LoggerFactory.getLogger(VehicleDeckBootApplication.class);
	
	@Autowired
	private Environment environment;
	
	@Bean
	public RestTemplate rest(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	public static void main(String[] args) {
		log.info("服务开始启动了");
		SpringApplication.run(VehicleDeckBootApplication.class, args);
	}
}
