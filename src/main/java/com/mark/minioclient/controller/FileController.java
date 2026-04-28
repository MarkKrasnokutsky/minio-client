package com.mark.minioclient.controller;

import com.mark.minioclient.api.rest.FileControllerApi;
import com.mark.minioclient.domain.enumeration.FileStatus;
import com.mark.minioclient.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileController implements FileControllerApi {

    private final FileService fileService;

    @Override
    public ResponseEntity<String> upload(MultipartFile file) {
        return ResponseEntity.ok(fileService.upload(file));
    }

    @Override
    public ResponseEntity<FileStatus> getFileStatus(Long id) {
        return ResponseEntity.ok(fileService.getFileStatusById(id));
    }

    @Override
    public ResponseEntity<byte[]> getFileData(Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileService.getFileInfo(id).getFileName() + "\"")
                .body(fileService.getFileData(id));
    }

}
