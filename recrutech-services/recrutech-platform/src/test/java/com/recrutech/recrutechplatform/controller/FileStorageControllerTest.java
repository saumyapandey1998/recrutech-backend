package com.recrutech.recrutechplatform.controller;

import com.recrutech.recrutechplatform.dto.file.FileMetadataResponse;
import com.recrutech.recrutechplatform.model.FileMetadata;
import com.recrutech.recrutechplatform.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileStorageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileStorageController fileStorageController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fileStorageController).build();
    }

    @Test
    public void testUploadFile() throws Exception {
        // Create a mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF content".getBytes()
        );

        // Create a mock file metadata
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId("123e4567-e89b-12d3-a456-426614174000");
        fileMetadata.setFileName("test.pdf");
        fileMetadata.setContentType(MediaType.APPLICATION_PDF_VALUE);
        fileMetadata.setSize(12L);
        fileMetadata.setFilePath("test.pdf");

        // Mock the service response
        when(fileStorageService.storeFile(any())).thenReturn(fileMetadata);

        // Perform the request and verify the response
        mockMvc.perform(multipart("/storage/files")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.fileName").value("test.pdf"))
                .andExpect(jsonPath("$.contentType").value(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(jsonPath("$.size").value(12));
    }

    @Test
    public void testDownloadFile() throws Exception {
        // Create a mock file metadata
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId("123e4567-e89b-12d3-a456-426614174000");
        fileMetadata.setFileName("test.pdf");
        fileMetadata.setContentType(MediaType.APPLICATION_PDF_VALUE);
        fileMetadata.setSize(12L);
        fileMetadata.setFilePath("test.pdf");

        // Create a mock resource with test content
        byte[] pdfContent = "PDF test content".getBytes();
        Resource resource = new InputStreamResource(new ByteArrayInputStream(pdfContent));

        // Mock the service responses
        when(fileStorageService.loadFileAsResource("123e4567-e89b-12d3-a456-426614174000")).thenReturn(resource);
        when(fileStorageService.getFileMetadata("123e4567-e89b-12d3-a456-426614174000")).thenReturn(fileMetadata);

        // Perform the request and verify the response
        mockMvc.perform(get("/storage/files/123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""));
    }
}
