package com.nmvk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

/**
 * Launcher of the application.
 *
 * @author raghav
 */
@EnableAutoConfiguration
@SpringBootApplication
public class Launcher {
    public static void main(String[] args) {
        try {
           SpringApplication.run(Launcher.class, args);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
