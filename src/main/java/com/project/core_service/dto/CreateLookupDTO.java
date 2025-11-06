package com.project.core_service.dto;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.lookup.Lookup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLookupDTO {
    @NonNull
    private String lookupName;
    @NonNull
    private String description;
    @NonNull
    private MultipartFile lookupFile;
}
