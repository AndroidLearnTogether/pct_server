package com.example.pct.controller;

import com.example.pct.exception.DataException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

@RestController
@RequestMapping("/test")
@Log4j2
@RequiredArgsConstructor
public class RestTestController {
    @PostMapping("/w")
    public String testW(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();

        // PSD 파일 내용을 이진수로 추출하는 부분
        byte[] fileContent = inputStream.readAllBytes();

        // 추출한 PSD 파일 내용을 파일로 저장하는 부분
        File targetFile = new File(
                "C:\\Users\\user\\git\\pct_server\\src\\main\\resources\\static\\"
                +multipartFile.getOriginalFilename());
        OutputStream outputStream = new FileOutputStream(targetFile);
        outputStream.write(fileContent);
        outputStream.close();

        return "파일 업로드가 완료되었습니다.";
    }
    @PostMapping("/l")
    public String testL(@RequestParam("name") String name) throws IOException, DataException {
        Resource resource = new PathResource("C:\\Users\\user\\git\\pct_server\\src\\main\\resources\\static\\" + name);
        InputStream inputStream = resource.getInputStream();
        byte[] data = FileCopyUtils.copyToByteArray(inputStream);

        //시그니처 검증
        if(!Arrays.equals(Arrays.copyOfRange(data, 0, 4), new byte[] {56, 66, 80, 83})) {
            throw new DataException("Signature not match");
        }
        //버전 확인/검증
        if(!Arrays.equals(Arrays.copyOfRange(data, 4, 6), new byte[] {0, 1})) {
            if(!Arrays.equals(Arrays.copyOfRange(data, 4, 6), new byte[] {0, 2})) {
                throw new DataException("Version not match");
            }
            throw new DataException("PSB is not support");
        }
        //헤더 빈공간 확인
        if(!Arrays.equals(Arrays.copyOfRange(data, 6, 12), new byte[] {0, 0, 0, 0, 0, 0})) {
            throw new DataException("Null detected");
        }
        //채널 추출
        int channels = (data[12] & 0xFF) << 8 | (data[13] & 0xFF) << 0;
        int height = ((data[14] & 0xFF) << 24 | (data[15] & 0xFF) << 16 | (data[16] & 0xFF) << 8 | (data[17] & 0xFF));
        int wide = ((data[18] & 0xFF) << 24 | (data[19] & 0xFF) << 16 | (data[20] & 0xFF) << 8 | (data[21] & 0xFF));

        System.out.println(Integer.toString(data[14] & 0xFF) + (data[15] & 0xFF) + (data[16] & 0xFF) + (data[17] & 0xFF));
        System.out.println(Integer.toString(data[18] & 0xFF) + (data[19] & 0xFF) + (data[20] & 0xFF) + (data[21] & 0xFF));

        return Integer.toString(channels) + ' ' + height + ' ' + wide;
    }
}
