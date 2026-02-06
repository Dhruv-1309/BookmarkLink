package com.bookmarklink.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookmarkLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookmarkLinkApplication.class, args);
    }
}
