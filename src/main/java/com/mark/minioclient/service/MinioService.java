package com.mark.minioclient.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    byte[] getFile(String bucketName, String filePath);

    void uploadFile(MultipartFile file, String bucketName, String filePath);

}
