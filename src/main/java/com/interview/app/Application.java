package com.interview.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // spring.main.headless=false è impostato in application.properties
        // SwingAppRunner (ApplicationRunner bean) lancia la Swing UI
        // dopo che il context Spring è completamente pronto
        SpringApplication.run(Application.class, args);
    }
}
