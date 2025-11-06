package com.project.core_service.dto;

import org.springframework.web.multipart.MultipartFile;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.lookup.Lookup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLookupDTO {
    private String description;
    private MultipartFile lookupFile;
}
