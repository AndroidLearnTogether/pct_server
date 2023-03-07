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

    @PostMapping("/ls")
    public String testLi(@RequestParam("name") String name) throws IOException, DataException {
        Resource resource = new PathResource("C:\\Users\\user\\git\\pct_server\\src\\main\\resources\\static\\" + name);
        InputStream inputStream = resource.getInputStream();
        byte[] data = FileCopyUtils.copyToByteArray(inputStream);
        String tester = "";

        int i = 0;
        while (i < 3000) {
            tester = tester + i + " : " + (data[i] & 0xFF) + ' ' + new String(new byte[] {data[i]}) + '\n';
            i++;
        }

        return tester;
    }
    @PostMapping("/l")
    public String testL(@RequestParam("name") String name) throws IOException, DataException {
        Resource resource = new PathResource("C:\\Users\\user\\git\\pct_server\\src\\main\\resources\\static\\" + name);
        InputStream inputStream = resource.getInputStream();
        byte[] data = FileCopyUtils.copyToByteArray(inputStream);

        //시그니처 검증
        if(!Arrays.equals(Arrays.copyOfRange(data, 0, 4), new byte[] {56, 66, 80, 83})) {
            return "Signature not match";
        }
        //버전 확인/검증
        if(!Arrays.equals(Arrays.copyOfRange(data, 4, 6), new byte[] {0, 1})) {
            if(!Arrays.equals(Arrays.copyOfRange(data, 4, 6), new byte[] {0, 2})) {
                return "Version not match";
            }
            return "PSB is not support";
        }
        //헤더 빈공간 확인
        if(!Arrays.equals(Arrays.copyOfRange(data, 6, 12), new byte[] {0, 0, 0, 0, 0, 0})) {
            return "Null detected";
        }
        //채널 추출
        int channels = (data[12] & 0xFF) << 8 | (data[13] & 0xFF) << 0;
        //높이 추출
        int height = ((data[14] & 0xFF) << 24 | (data[15] & 0xFF) << 16 | (data[16] & 0xFF) << 8 | (data[17] & 0xFF));
        //너비 추출
        int wide = ((data[18] & 0xFF) << 24 | (data[19] & 0xFF) << 16 | (data[20] & 0xFF) << 8 | (data[21] & 0xFF));
        //채널 비트심도 추출
        int bitDepth = ((data[22] & 0xFF) << 8 | (data[23] & 0xFF));
        if(bitDepth != 1 && bitDepth != 8 && bitDepth != 16 && bitDepth != 32) {
            return "BitDepth is abnormal";
        }

        //색상모드 추출
        String colorMode;

        switch (((data[24] & 0xFF) << 8 | (data[25] & 0xFF))) {
            case 0:
                colorMode = "Bitmap";
                return "Bitmap is not support";
            case 1:
                colorMode = "GrayScale";
                return "GrayScale is not support";
            case 2:
                colorMode = "Indexed";
                return "Indexed is not support";
            case 3:
                colorMode = "RGB";
                break;
            case 4:
                colorMode = "CMYK";
                break;
            case 7:
                colorMode = "MultiChannel";
                return "MultiChannel is not support";
            case 8:
                colorMode = "Duotone";
                return "Duotone is not support";
            case 9:
                colorMode = "Lab";
                return "Lab is not support";
            default:
                return "colorMode is abnormal";
        }

        if (((data[26] & 0xFF) << 24 | (data[27] & 0xFF) << 16 | (data[28] & 0xFF) << 8 | (data[29] & 0xFF)) != 0) {
            return "Color mode is not 0 " + data[28] + data[29] + data[30] + data[31];
        }
        int head = 30;
        int resources_length = ((data[head++] & 0xFF) << 24 | (data[head++] & 0xFF) << 16 | (data[head++] & 0xFF) << 8 | (data[head++] & 0xFF));
        log.info(resources_length);
        head += resources_length;

        // 리소스 검증자 (미 구현 계획됨)
        /*
        while (resources > i++) {
            if(!Arrays.equals(Arrays.copyOfRange(data, head, head + 4), new byte[] {56, 66, 73, 77})) {
                return "image resources is not 8BIM " + head + ' ' + i + ' ' + data[head++] + data[head++] + data[head++] + data[head++];
            }
            head += 4;
            // 리소스 식별자(무시하기 - 헤더 건너뜀) 만약 1045 식별 코드를 보유시 Unicode 정책에 따라 마지막 2개 null,
            System.out.println(head);
            if(Arrays.equals(Arrays.copyOfRange(data, head, head + 2), new byte[] {4, 21})) {
                head += 2;
            } head += 2;
            do {
                head++;
            } while (!(data[head - 1] == 0 && data[head] == 0));
            head++;
            int resource_len = (data[head++] & 0xFF) << 24 | (data[head++] & 0xFF) << 16 | (data[head++] & 0xFF) << 8 | (data[head++] & 0xFF);
            head += resource_len; // 리소스 실질데이터(무시하기 - 헤더 건너뜀)
        }
        */

        int layerMask_length = ((data[head++] & 0xFF) << 24 | (data[head++] & 0xFF) << 16 | (data[head++] & 0xFF) << 8 | (data[head++] & 0xFF));
        int layer_length = ((data[head++] & 0xFF) << 24 | (data[head++] & 0xFF) << 16 | (data[head++] & 0xFF) << 8 | (data[head++] & 0xFF));


        return Integer.toString(channels) + ' '
                + height + ' ' +
                wide + ' ' +
                bitDepth + ' ' +
                colorMode + ' ' +
                head + ' ' +
                (head + layerMask_length) + ' ' +
                data.length;
    }
}
