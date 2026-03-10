package br.com.ace.api.controller;

import br.com.ace.api.entity.FileMetadata;
import br.com.ace.api.service.FilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping("/api/files")
@RequiredArgsConstructor
@RestController
public class FilesController {

    private final FilesService service;

    @PostMapping
    ResponseEntity<Void> upload(@RequestParam final MultipartFile file){
        service.save(file);
        return ResponseEntity.status(CREATED.value()).build();
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }
}
