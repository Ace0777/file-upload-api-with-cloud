package br.com.ace.api.service;

import br.com.ace.api.config.S3Config;
import br.com.ace.api.entity.FileMetadata;
import br.com.ace.api.repository.FilesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilesService {

    private final FilesRepository repository;
    private final S3Client s3Client;
    private final S3Config s3Config;

    public void save(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String uniqueName = "uploads/" + UUID.randomUUID() + "-" + originalName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(uniqueName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String fileUrl = "https://" + s3Config.getBucketName()
                    + ".s3.sa-east-1.amazonaws.com/" + uniqueName;

            FileMetadata entity = new FileMetadata();
            entity.setFileName(originalName);
            entity.setSize(file.getSize());
            entity.setPath(fileUrl);
            entity.setCreatedAt(LocalDateTime.now());

            repository.save(entity);

        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    public List<FileMetadata> findAll() {
        return repository.findAll();
    }
}