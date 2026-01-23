package com.saoodahmad.cacheforge;

import com.saoodahmad.cacheforge.cache.config.CacheForgeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(CacheForgeProperties.class)
public class CacheforgeApplication {

	public static void main(String[] args)  {
		SpringApplication.run(CacheforgeApplication.class, args);
	}

}
