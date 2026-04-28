package com.mark.minioclient.domain.dto;

import com.mark.minioclient.domain.enumeration.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private FileStatus fileStatus;
    private byte[] fileData;

}
