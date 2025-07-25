package com.olivin.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.olivin.app.**.mapper")
public class OlivinApplication {

	public static void main(String[] args) {
		SpringApplication.run(OlivinApplication.class, args);
	}

}
