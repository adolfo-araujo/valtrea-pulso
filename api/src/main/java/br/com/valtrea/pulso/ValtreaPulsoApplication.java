package br.com.valtrea.pulso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ValtreaPulsoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValtreaPulsoApplication.class, args);
	}

}
