package com.prophet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan
//@ComponentScan
public class Application extends SpringBootServletInitializer{

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception{
		SpringApplication.run(Application.class, args);
	}
}
