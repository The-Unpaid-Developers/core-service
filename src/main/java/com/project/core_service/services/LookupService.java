package com.project.core_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.models.lookup.Lookup;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;

@Service
public class LookupService {

    private final MongoDatabase mongoDatabase;
    private final ObjectMapper objectMapper;

    @Value("${mongodb.collection.lookups.name}")
    private String collectionName;

    @Autowired
    public LookupService(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        this.objectMapper = new ObjectMapper();
    }

    public LookupDTO processCsvFile(MultipartFile file, String lookupName) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file parameter is required and cannot be empty");
        }
        
        if (lookupName == null || lookupName.trim().isEmpty()) {
            throw new IllegalArgumentException("lookupName parameter is required and cannot be empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        if (!fileName.toLowerCase().endsWith(".csv") && 
            !"text/csv".equals(contentType)) {
            throw new IllegalArgumentException("File must be a CSV file");
        }

        try {
            // Parse CSV to List of Maps
            List<Map<String, String>> csvData = parseCsvToJson(file);
            
            // Create the lookup object
            Lookup lookup = Lookup.builder()
                    .id(lookupName)
                    .lookupName(lookupName)
                    .data(csvData)
                    .uploadedAt(new Date())
                    .recordCount(csvData.size())
                    .build();

            // Store in MongoDB
            saveToMongoDB(lookup);

            // Return response
            return LookupDTO.builder()
                    .success(true)
                    .lookupName(lookupName)
                    .recordsProcessed(csvData.size())
                    .message("CSV file processed and stored successfully")
                    .build();
        } catch (Exception e) {
            // Convert generic exceptions to more specific ones
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage(), e);
        }
    }

    private List<Map<String, String>> parseCsvToJson(MultipartFile file) {
        List<Map<String, String>> records = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build())) {
            
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            
            if (headerMap.isEmpty()) {
                throw new IllegalArgumentException("CSV file must contain headers");
            }
            
            // Clean headers to remove BOM and other invisible characters
            Map<String, String> cleanHeaderMap = new HashMap<>();
            for (String header : headerMap.keySet()) {
                String cleanHeader = header.replaceAll("^\uFEFF", "") 
                                        .replaceAll("[\u200B-\u200D\uFEFF]", "")
                                        .trim();
                cleanHeaderMap.put(header, cleanHeader);
            }
            
            for (CSVRecord csvRecord : csvParser) {
                Map<String, String> record = new HashMap<>();
                for (Map.Entry<String, Integer> headerEntry : headerMap.entrySet()) {
                    String originalHeader = headerEntry.getKey();
                    String cleanHeader = cleanHeaderMap.get(originalHeader);
                    int index = headerEntry.getValue();
                    
                    if (index < csvRecord.size()) {
                        String value = csvRecord.get(index);
                        record.put(cleanHeader, value != null ? value.trim() : "");
                    }
                }
                records.add(record);
            }
            
            if (records.isEmpty()) {
                throw new IllegalArgumentException("CSV file contains no data rows");
            }
            
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new RuntimeException("Error parsing CSV file: " + e.getMessage(), e);
        }
        
        return records;
    }

    private void saveToMongoDB(Lookup lookup) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        
        // Convert Lookup to BSON Document
        Map<String, Object> lookupMap = new HashMap<>();
        lookupMap.put("_id", lookup.getId());
        lookupMap.put("lookupName", lookup.getLookupName());
        lookupMap.put("data", lookup.getData());
        lookupMap.put("uploadedAt", lookup.getUploadedAt());
        lookupMap.put("recordCount", lookup.getRecordCount());
        
        Document document = new Document(lookupMap);
        
        // Use replaceOne with upsert to update if exists or insert if not
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        collection.replaceOne(
            Filters.eq("_id", lookup.getId()),
            document,
            options
        );
    }

    public LookupDTO getAllLookups() {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        
        List<Lookup> lookups = new ArrayList<>();
        
        // Fetch all documents and convert to Lookup objects
        for (Document doc : collection.find()) {
            Lookup lookup = documentToLookup(doc);
            lookups.add(lookup);
        }
        
        return LookupDTO.builder()
                .totalLookups(lookups.size())
                .lookups(lookups)
                .build();
    }

    public LookupDTO getLookupByName(String lookupName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        
        Document doc = collection.find(Filters.eq("_id", lookupName)).first();
        
        if (doc == null) {
            throw new NoSuchElementException("Lookup with name '" + lookupName + "' not found");
        }
        
        Lookup lookup = documentToLookup(doc);
        
        return LookupDTO.builder()
                .lookups(new ArrayList<>(List.of(lookup)))
                .build();
    }

    public LookupDTO deleteLookup(String lookupName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        
        long deletedCount = collection.deleteOne(Filters.eq("_id", lookupName)).getDeletedCount();

        if (deletedCount == 0) {
            throw new NoSuchElementException("Lookup with name '" + lookupName + "' not found");
        }
        
        return LookupDTO.builder()
                .success(true)
                .lookupName(lookupName)
                .message("Lookup deleted successfully")
                .build();
    }

    private Lookup documentToLookup(Document doc) {
        try {
            // Manual mapping to avoid Jackson issues with MongoDB date format
            String id = doc.getString("_id");
            String lookupName = doc.getString("lookupName");
            Date uploadedAt = doc.getDate("uploadedAt");
            Integer recordCount = doc.getInteger("recordCount");
            
            // Handle the data field (List of Maps)
            @SuppressWarnings("unchecked")
            List<Map<String, String>> data = (List<Map<String, String>>) doc.get("data");
            
            return Lookup.builder()
                    .id(id)
                    .lookupName(lookupName)
                    .data(data != null ? data : new ArrayList<>())
                    .uploadedAt(uploadedAt)
                    .recordCount(recordCount != null ? recordCount : 0)
                    .build();
                    
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error converting document: " + e.getMessage());
            System.err.println("Document: " + doc.toJson());
            throw new RuntimeException("Failed to convert document to Lookup: " + e.getMessage(), e);
        }
    }
}