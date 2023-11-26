package com.codecat.catsurvey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CatSurveyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatSurveyApplication.class, args);
    }

}



