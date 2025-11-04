package com.project.core_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.dto.LookupContextDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
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

    @PostMapping("/upload")
    public ResponseEntity<LookupDTO> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("lookupName") String lookupName) {
        return ResponseEntity.ok(lookupService.processCsvFile(file, lookupName));
    }

    @PostMapping("/{lookupName}/add-lookup-context")
    public ResponseEntity<LookupContextDTO> addLookupContext(@PathVariable String lookupName,
                                                              @RequestBody LookupContextDTO lookupContextDTO) {
        return ResponseEntity.ok(lookupService.addLookupContext(lookupName, lookupContextDTO));
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

    @GetMapping("/{lookupName}/get-field-names")
    public ResponseEntity<List<String>> getFieldNames(@PathVariable String lookupName) {
        return ResponseEntity.ok(lookupService.getFieldNames(lookupName));
    }
}