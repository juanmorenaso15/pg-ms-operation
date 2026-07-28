package com.pulse_gym.ms_operation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@SpringBootApplication(scanBasePackages = {
    "com.pulse_gym.ms_operation",  
    "com.pulse_gym.lb_common",
	"com.pulse_gym.lb_common.services"      
})
@EntityScan("com.pulse_gym.lb_common.entity.operation")

@EnableJpaRepositories(basePackages = {
    "com.pulse_gym.ms_operation.repository",  
})
@EnableFeignClients(basePackages = {
    "com.pulse_gym.lb_common.client",
    "com.pulse_gym.ms_operation.client"
})

public class MsOperationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsOperationApplication.class, args);
	}

}
