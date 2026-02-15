package com.jam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "com.jam.**.mapper")
@SpringBootApplication
public class JamApplication {

	public static void main(String[] args) {
		SpringApplication.run(JamApplication.class, args);
	}

}
