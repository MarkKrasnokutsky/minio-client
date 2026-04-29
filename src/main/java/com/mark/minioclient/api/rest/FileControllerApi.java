package com.mark.minioclient.api.rest;

import com.mark.minioclient.domain.enumeration.FileStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/api/file")
public interface FileControllerApi {

    @PostMapping("/upload")
    ResponseEntity<Void> upload(@RequestParam("file") MultipartFile file) throws IOException;

    @GetMapping("/{id}/status")
    ResponseEntity<FileStatus> getFileStatus(@PathVariable Long id);

    @GetMapping("/{id}/data")
    ResponseEntity<byte[]> getFileData(@PathVariable Long id);

}
