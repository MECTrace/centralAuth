package com.penta.centralauth.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<?> sendDataToCentralFromEdge(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("metadata") String metaJson,
            @RequestParam("hashtable") String hashJson,
            @RequestParam("signature") MultipartFile[] signatures,
            HttpServletRequest request) {

        log.info("***************** device -> edge -> central(direct) *****************");

        // Request의 인증서 추출
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        X509Certificate certificate = certs[0];

        String errMsg = "서명 검증 실패";

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate.getPublicKey());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        /*
         * signatures[0] : datafile(files[0])
         * signatures[1] : certfile(files[1])
         * signatures[2] : metadata
         * signatures[3] : hashtable
         * */

        for (int i = 0; i < signatures.length; i++) {

            if (i == 0 || i == 1) {
                String key = i == 0 ? "datafile" : "certfile";
                MultipartFile file = files[i];
                MultipartFile signatureBytes = signatures[i];
                signature.update(file.getBytes());

                if (signature.verify(signatureBytes.getBytes())) {
                    log.info("VERIFIED SIGNATURE"); // 전자서명 검증 성공
                    body.add(key, file.getResource());
                } else {
                    log.error(errMsg);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errMsg);
                }
            } else if (i == 2) {
                MultipartFile signatureBytes = signatures[i];
                signature.update(metaJson.getBytes(StandardCharsets.UTF_8));
                if (signature.verify(signatureBytes.getBytes())) {
                    log.info("VERIFIED SIGNATURE"); // 전자서명 검증 성공
                    body.add("metadata", metaJson);
                } else {
                    log.error(errMsg);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errMsg);
                }
            } else {
                MultipartFile signatureBytes = signatures[i];
                signature.update(hashJson.getBytes(StandardCharsets.UTF_8));
                if (signature.verify(signatureBytes.getBytes())) {
                    log.info("VERIFIED SIGNATURE"); // 전자서명 검증 성공
                    body.add("hashtable", hashJson);
                } else {
                    log.error(errMsg);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errMsg);
                }
            }
        } // end of for

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

        log.info("----------------------------------------------");
        log.info("      HTTP REQUEST BODY(without files)        ");
        log.info("----------------------------------------------");

        log.info("metadata : {}", body.get("metadata"));
        log.info("hashtable : {}", body.get("hashtable"));


        String centralUrl = "http://localhost:80/api/tracking/add/data";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

        log.info("response :: {} ", response);

        return ResponseEntity.status(HttpStatus.OK).body(response.getBody());

    }

    @PostMapping(value = "/item/auth")
    @SneakyThrows
    public ResponseEntity<?> sendItemToCentralFromEdge(@RequestParam("hashtable") String hashJson,
                                                       @RequestParam("signature") MultipartFile[] signatures,
                                                       HttpServletRequest request) {

        log.info("***************** device -> edge1 -> edge2 -> central *****************");

        // Request의 인증서 추출
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        X509Certificate certificate = certs[0];

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate.getPublicKey());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        MultipartFile signatureBytes = signatures[0];
        signature.update(hashJson.getBytes());

        if (signature.verify(signatureBytes.getBytes())) {
            log.info("VERIFIED SIGNATURE"); // 전자서명 검증 성공
            body.add("hashtable", hashJson);
        } else {
            log.error("서명 검증 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("서명 검증 실패");
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);
        String centralUrl = "http://localhost:80/api/tracking/add/item";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

        log.info("response :: {}", response);

        return ResponseEntity.status(HttpStatus.OK).body(response.getBody());

    }


}
