package com.g18.ecommerce.IdentifyService.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
public class LoggingController {
    private static final String LOG_FILE_PATH = "D:/kong_logs.txt";

    @PostMapping("/kong-logs")
    public String receiveKongLog(@RequestBody String body) {
        try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
            writer.write("===== LOG AT " + LocalDateTime.now() + " =====\n");
            writer.write(body + "\n\n");
            System.out.println("✅ Đã nhận và ghi log.");
            return "Log received";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to write log";
        }
    }
}
