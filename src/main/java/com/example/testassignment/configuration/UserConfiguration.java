package com.example.testassignment.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class UserConfiguration {

    @Value("${user.minAge}")
    private int minAge;
}
