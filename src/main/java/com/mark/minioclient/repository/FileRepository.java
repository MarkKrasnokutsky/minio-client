package com.mark.minioclient.repository;

import com.mark.minioclient.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity,Long> {


    @Query(value = "SELECT * FROM files WHERE file_path LIKE CONCAT(:filePathWithoutExtension, '.%')",
            nativeQuery = true)
    List<FileEntity> findByFilePath(@Param("filePathWithoutExtension") String filePathWithoutExtension);

}
