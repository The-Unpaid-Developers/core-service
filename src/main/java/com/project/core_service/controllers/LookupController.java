package com.project.core_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.dto.LookupFieldDescriptionsDTO;
import com.project.core_service.dto.CreateLookupDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.dto.UpdateLookupDTO;
import com.project.core_service.services.LookupService;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/v1/lookups")
public class LookupController {

    private final LookupService lookupService;

    @Autowired
    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @PostMapping
    public ResponseEntity<LookupDTO> createLookup(@ModelAttribute CreateLookupDTO createLookupDTO) {
        return ResponseEntity.ok(lookupService.createLookup(createLookupDTO));
    }

    @GetMapping("/{lookupName}/field-descriptions")
    public ResponseEntity<LookupFieldDescriptionsDTO> getFieldDescriptions(@PathVariable String lookupName) {
        return ResponseEntity.ok(lookupService.getFieldDescriptionsDTO(lookupName));
    }

    @PutMapping("/{lookupName}/field-descriptions")
    public ResponseEntity<LookupFieldDescriptionsDTO> updateFieldDescriptions(@PathVariable String lookupName,
                                                              @RequestBody LookupFieldDescriptionsDTO lookupContextDTO) {
        return ResponseEntity.ok(lookupService.updateFieldDescriptions(lookupName, lookupContextDTO));
    }

    @PutMapping("/{lookupName}")
    public ResponseEntity<LookupDTO> updateLookup(@PathVariable String lookupName, @ModelAttribute UpdateLookupDTO updateLookupDTO) {
        return ResponseEntity.ok(lookupService.updateLookup(lookupName, updateLookupDTO));
    }

    @GetMapping
    public ResponseEntity<LookupDTO> getAllLookups() {
        return ResponseEntity.ok(lookupService.getAllLookups());
    }

    @GetMapping("/business-capabilities")
    public ResponseEntity<List<BusinessCapabilityLookupDTO>> getBusinessCapabilities() {
        return ResponseEntity.ok(lookupService.getBusinessCapabilities());
    }

    @GetMapping("/tech-components")
    public ResponseEntity<List<TechComponentLookupDTO>> getTechComponents() {
        return ResponseEntity.ok(lookupService.getTechComponents());
    }

    @GetMapping("/{lookupName}")
    public ResponseEntity<LookupDTO> getLookupByName(@PathVariable String lookupName) {
        return ResponseEntity.ok(lookupService.getLookupByName(lookupName));
    }

    @DeleteMapping("/{lookupName}")
    public ResponseEntity<LookupDTO> deleteLookup(@PathVariable String lookupName) {
        return ResponseEntity.ok(lookupService.deleteLookup(lookupName));
    }
}