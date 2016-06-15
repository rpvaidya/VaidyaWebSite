package com.vaidya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.*;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@EnableAutoConfiguration
@RestController
public class VaidyaWebSiteApplication {


	public static void main(String[] args) {
		SpringApplication.run(VaidyaWebSiteApplication.class, args);
	
	}

}
