package com.project.core_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.core_service.services.LookupService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lookups")
public class LookupController {

    private final LookupService lookupService;

    @Autowired
    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("lookupName") String lookupName) {
        return ResponseEntity.ok(lookupService.processCsvFile(file, lookupName));
    }

    @GetMapping
    public ResponseEntity<?> getAllLookups() {
        return ResponseEntity.ok(lookupService.getAllLookups());
    }

    @GetMapping("/{lookupName}")
    public ResponseEntity<?> getLookupByName(@PathVariable String lookupName) {
        return ResponseEntity.ok(lookupService.getLookupByName(lookupName));
    }

    @DeleteMapping("/{lookupName}")
    public ResponseEntity<?> deleteLookup(@PathVariable String lookupName) {
        return ResponseEntity.ok(lookupService.deleteLookup(lookupName));
    }
}