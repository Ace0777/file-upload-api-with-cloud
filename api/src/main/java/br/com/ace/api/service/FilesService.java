package br.com.ace.api.service;

import br.com.ace.api.entity.FileMetadata;
import br.com.ace.api.repository.FilesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilesService {

    private final FilesRepository repository;

    public void save(MultipartFile file){

        try {

            String originalName = file.getOriginalFilename();
            String uniqueName = UUID.randomUUID() + "-" + originalName;

            Path uploadPath = getUploadDirectory();
            Path filePath = uploadPath.resolve(uniqueName);

            Files.copy(file.getInputStream(), filePath);

            FileMetadata entity = new FileMetadata();
            entity.setFileName(originalName);
            entity.setSize(file.getSize());
            entity.setPath(filePath.toString());
            entity.setCreatedAt(LocalDateTime.now());

            repository.save(entity);

        } catch (IOException e) {
            throw new RuntimeException("error");
        }

    }


    public List<FileMetadata> findAll(){
        return repository.findAll();
    }


    private Path getUploadDirectory() throws IOException {

        Path uploadPath = Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        return uploadPath;
    }

}
