package kr.bi.greenmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class GreenMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenMateApplication.class, args);
    }

}
