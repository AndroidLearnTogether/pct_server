package com.example.pct.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

@RestController
@RequestMapping("/test")
@Log4j2
public class RestTestController {
    @PostMapping("/l")
    public void test(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(
                new File("C:\\Users\\user\\git\\pct_server\\src\\main\\resources\\static\\"
                + multipartFile.getName()+".txt"));

        int data = inputStream.read();
        while (data != -1) {
            data = inputStream.read();
            outputStream.write(Integer.toBinaryString(data).getBytes());
        }
        log.info("complete");

        inputStream.close();
        outputStream.close();
    }
}
