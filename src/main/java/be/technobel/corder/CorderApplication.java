package be.technobel.corder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.speedment.jpastreamer"})
public class CorderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorderApplication.class, args);
    }

}
