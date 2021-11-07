package com.penta.centralauth.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.security.Signature;
import java.security.cert.X509Certificate;

/*
 * central로 데이터 전송 전 인증 >> 이후 central 전송
 * scenario
 *          device -> edge1 -> central            >> sendDataToCentralFromEdge(datafile, certfile, metadata, hashtable을 central)
 *          device -> edge1 -> edge2 -> central   >> sendItemToCentralFromEdge(hashtable)
 * */

@RestController
@RequestMapping("/api/central/add")
@Slf4j
public class AuthController {

    @PostMapping(value = "/data/auth")
    @SneakyThrows
    public void sendDataToCentralFromEdge(@RequestParam("file") MultipartFile[] files,
                                          @RequestParam("signature") MultipartFile[] signatures,
                                          HttpServletRequest request) {

        /*
         * files[0], signatures[0] : datafile
         * files[1], signatures[1] : certfile
         * files[2], signatures[2] : metadata
         * files[3], signatures[3] : hashtable
         * */

        // Request의 인증서 추출
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        X509Certificate certificate = certs[0];

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate.getPublicKey());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (int i = 0; i < files.length; i++) {

            MultipartFile file = files[i];
            MultipartFile signatureBytes = signatures[i];
            signature.update(file.getBytes());

            // 전자서명 binary to hex (전자서명의 hex값이 필요한 경우 사용)
            // String signatureValue = fileManager.getHex(signatureBytes.getBytes());

            if (signature.verify(signatureBytes.getBytes())) {
                log.info("VERIFIED SIGNATURE"); // 전자서명 검증 성공
                switch (i) {
                    case 0: // datafile
                        body.add("datafile", file.getResource());
                        break;
                    case 1: // certfile
                        body.add("certfile", file.getResource());
                        break;
                    case 2: // metadata
                        body.add("metadata", Base64.encodeBase64URLSafeString(file.getBytes()));
                        break;
                    case 3: // hashtable
                        body.add("hashtable", Base64.encodeBase64URLSafeString(file.getBytes()));
                        break;
                }
            } else {
                log.error("서명 검증 실패");
            }
        } // end of for

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);
        String centralUrl = "http://20.196.220.98:80/api/tracking/add/data";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

        log.info("response :: {} ", response);

    }

    @PostMapping(value = "/item/auth")
    @SneakyThrows
    public void sendItemToCentralFromEdge(@RequestParam("file") MultipartFile[] files,
                                          @RequestParam("signature") MultipartFile[] signatures,
                                          HttpServletRequest request) {

        // Request의 인증서 추출
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        X509Certificate certificate = certs[0];

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate.getPublicKey());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 현재는 hashtable 1개만 받고 있음 (size = 1)
        for (int i = 0; i < files.length; i++) {
            log.info("hashtable(files) size :: {} ", files.length);
            MultipartFile file = files[i];
            MultipartFile signatureBytes = signatures[i];
            signature.update(file.getBytes());

            // 전자서명 binary to hex (전자서명의 hex값이 필요한 경우 사용)
            // String signatureValue = fileManager.getHex(signatureBytes.getBytes());

            if (signature.verify(signatureBytes.getBytes())) {
                log.info("VERIFIED SIGNATURE"); // 전자서명 검증 성공
                body.add("hashtable", Base64.encodeBase64URLSafeString(file.getBytes()));
            } else {
                log.error("서명 검증 실패");
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);
            String centralUrl = "http://20.196.220.98:80/api/tracking/add/item";
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

            log.info("response :: {}", response);
        }

    }

}
