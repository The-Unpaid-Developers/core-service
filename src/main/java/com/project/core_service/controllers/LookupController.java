package com.project.core_service.controllers;

import com.project.core_service.dto.*;
import com.project.core_service.services.LookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public ResponseEntity<List<LookupWODataDTO>> getAllLookups() {
        return ResponseEntity.ok(lookupService.getAllLookups());
    }

    @GetMapping("/{lookupName}")
    public ResponseEntity<LookupDTO> getLookupByName(@PathVariable String lookupName) {
        return ResponseEntity.ok(lookupService.getLookupByName(lookupName));
    }

    @DeleteMapping("/{lookupName}")
    public ResponseEntity<Void> deleteLookup(@PathVariable String lookupName) {
        lookupService.deleteLookup(lookupName);
        return ResponseEntity.noContent().build();
    }
}