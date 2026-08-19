package org.example.pathpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PathPilotApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PathPilotApplication.class);
        app.setHeadless(false);
        app.run(args);
    }
}
