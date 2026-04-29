package com.mark.minioclient.service;

import com.mark.minioclient.domain.dto.FileResponse;
import com.mark.minioclient.domain.enumeration.FileStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    void upload(MultipartFile file) throws IOException;

    FileStatus getFileStatusById(Long id);

    byte[] getFileData(Long id);

    FileResponse getFileInfo(Long id);

}
