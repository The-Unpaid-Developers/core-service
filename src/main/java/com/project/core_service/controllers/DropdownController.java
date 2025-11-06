package com.project.core_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.services.DropdownService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dropdowns")
public class DropdownController {

    private final DropdownService dropdownService;

    @Autowired
    public DropdownController(DropdownService dropdownService) {
        this.dropdownService = dropdownService;
    }

    @GetMapping("/business-capabilities")
    public ResponseEntity<List<BusinessCapabilityLookupDTO>> getBusinessCapabilities() {
        return ResponseEntity.ok(dropdownService.getBusinessCapabilities());
    }

    @GetMapping("/tech-components")
    public ResponseEntity<List<TechComponentLookupDTO>> getTechComponents() {
        return ResponseEntity.ok(dropdownService.getTechComponents());
    }
}
