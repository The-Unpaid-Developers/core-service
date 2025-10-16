package com.project.core_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.core_service.dto.LookupDTO;
import com.project.core_service.services.LookupService;


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

    @GetMapping
    public ResponseEntity<LookupDTO> getAllLookups() {
        System.out.println("HELLO IM INSIDE");
        return ResponseEntity.ok(lookupService.getAllLookups());
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