package com.mark.minioclient.service;

import com.mark.minioclient.domain.dto.FileResponse;
import com.mark.minioclient.domain.enumeration.FileStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String upload(MultipartFile file);

    FileStatus getFileStatusById(Long id);

    byte[] getFileData(Long id);

    FileResponse getFileInfo(Long id);

}
