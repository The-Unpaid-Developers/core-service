package com.project.core_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.models.lookup.Lookup;
import com.project.core_service.services.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LookupController.class)
@AutoConfigureMockMvc(addFilters = false)
class LookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LookupService lookupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadCsvFile_ValidRequest_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            "name,age\nJohn,30".getBytes()
        );

        LookupDTO expectedResponse = LookupDTO.builder()
            .success(true)
            .lookupName("employees")
            .recordsProcessed(1)
            .message("CSV file processed and stored successfully")
            .build();

        when(lookupService.processCsvFile(any(), eq("employees")))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/lookups/upload")
                .file(file)
                .param("lookupName", "employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.lookupName").value("employees"))
            .andExpect(jsonPath("$.recordsProcessed").value(1));
    }

    @Test
    void uploadCsvFile_MissingFile_BadRequest() throws Exception {
        mockMvc.perform(multipart("/api/lookups/upload")
                .param("lookupName", "test"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAllLookups_Success() throws Exception {
        // Arrange
        LookupDTO expectedResponse = LookupDTO.builder()
            .totalLookups(2)
            .lookups(Arrays.asList(
                createMockLookup("lookup1", "Lookup 1"),
                createMockLookup("lookup2", "Lookup 2")
            ))
            .build();

        when(lookupService.getAllLookups()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/lookups"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLookups").value(2))
            .andExpect(jsonPath("$.lookups").isArray())
            .andExpect(jsonPath("$.lookups[0].id").value("lookup1"));
    }

    @Test
    void getLookupByName_ExistingLookup_Success() throws Exception {
        // Arrange
        String lookupName = "employees";
        LookupDTO expectedResponse = LookupDTO.builder()
            .lookups(Arrays.asList(createMockLookup(lookupName, "Employee Data")))
            .build();

        when(lookupService.getLookupByName(lookupName)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/lookups/{lookupName}", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].id").value(lookupName))
            .andExpect(jsonPath("$.lookups[0].lookupName").value("Employee Data"));
    }

    @Test
    void deleteLookup_Success() throws Exception {
        // Arrange
        String lookupName = "employees";
        LookupDTO expectedResponse = LookupDTO.builder()
            .success(true)
            .lookupName(lookupName)
            .message("Lookup deleted successfully")
            .build();

        when(lookupService.deleteLookup(lookupName)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/lookups/{lookupName}", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.lookupName").value(lookupName));
    }

    private Lookup createMockLookup(String id, String name) {
        return Lookup.builder()
            .id(id)
            .lookupName(name)
            .recordCount(10)
            .uploadedAt(new Date())
            .build();
    }
}