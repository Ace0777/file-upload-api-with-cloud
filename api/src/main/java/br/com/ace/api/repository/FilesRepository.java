package br.com.ace.api.repository;

import br.com.ace.api.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilesRepository extends JpaRepository<FileMetadata, Long> {
}
