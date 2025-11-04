package com.project.core_service.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.dto.LookupContextDTO;
import com.project.core_service.exceptions.CsvProcessingException;
import com.project.core_service.exceptions.InvalidFileException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.lookup.Lookup;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LookupService {

    private final MongoDatabase mongoDatabase;

    @Value("${mongodb.collection.lookups.name}")
    private String collectionName;

    // MongoDB document field names
    private static final String LOOKUP_NAME_FIELD = "lookupName";
    private static final String ID_FIELD = "_id";
    private static final String DATA_FIELD = "data";
    private static final String UPLOADED_AT_FIELD = "uploadedAt";
    private static final String RECORD_COUNT_FIELD = "recordCount";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String FIELDS_DESCRIPTION_FIELD = "fieldsDescription";

    // Lookup collection names
    private static final String BUSINESS_CAPABILITIES_LOOKUP = "business-capabilities";
    private static final String TECH_EOL_LOOKUP = "tech_eol";

    // Error messages
    private static final String BUSINESS_CAPABILITIES_NOT_FOUND_MSG = "Business capabilities lookup not found";
    private static final String TECH_COMPONENTS_NOT_FOUND_MSG = "Tech components lookup not found";

    // CSV field names for business capabilities
    private static final String L1_FIELD = "L1";
    private static final String L2_FIELD = "L2";
    private static final String L3_FIELD = "L3";

    // CSV field names for tech components
    private static final String PRODUCT_NAME_FIELD = "Product Name";
    private static final String PRODUCT_VERSION_FIELD = "Product Version";

    @Autowired
    public LookupService(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public LookupDTO processCsvFile(MultipartFile file, String lookupName) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("file parameter is required and cannot be empty");
        }

        if (lookupName == null || lookupName.trim().isEmpty()) {
            throw new InvalidFileException("lookupName parameter is required and cannot be empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        if (!fileName.toLowerCase().endsWith(".csv") &&
            !"text/csv".equals(contentType)) {
            throw new InvalidFileException("File must be a CSV file");
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
        } catch (InvalidFileException | CsvProcessingException e) {
            // Re-throw dedicated exceptions
            throw e;
        } catch (Exception e) {
            // Wrap unexpected exceptions
            throw new CsvProcessingException("Failed to process CSV file: " + e.getMessage(), e);
        }
    }

    private List<Map<String, String>> parseCsvToJson(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            CSVParser csvParser = createCsvParser(reader)) {

            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            validateHeaders(headerMap);

            Map<String, String> cleanHeaderMap = buildCleanHeaderMap(headerMap);
            List<Map<String, String>> records = parseRecords(csvParser, headerMap, cleanHeaderMap);

            validateRecords(records);
            return records;

        } catch (CsvProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvProcessingException("Error parsing CSV file: " + e.getMessage(), e);
        }
    }

    private CSVParser createCsvParser(BufferedReader reader) throws IOException {
        return new CSVParser(reader, CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build());
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        if (headerMap.isEmpty()) {
            throw new CsvProcessingException("CSV file must contain headers");
        }
    }

    private Map<String, String> buildCleanHeaderMap(Map<String, Integer> headerMap) {
        Map<String, String> cleanHeaderMap = new HashMap<>();
        for (String header : headerMap.keySet()) {
            String cleanHeader = cleanHeader(header);
            cleanHeaderMap.put(header, cleanHeader);
        }
        return cleanHeaderMap;
    }

    private String cleanHeader(String header) {
        return header.replaceAll("^\uFEFF", "") 
                    .replaceAll("[\u200B-\u200D\uFEFF]", "")
                    .trim();
    }

    private List<Map<String, String>> parseRecords(CSVParser csvParser, 
                                                    Map<String, Integer> headerMap, 
                                                    Map<String, String> cleanHeaderMap) {
        List<Map<String, String>> records = new ArrayList<>();
        
        for (CSVRecord csvRecord : csvParser) {
            Map<String, String> parsedRecord = parseRecord(csvRecord, headerMap, cleanHeaderMap);
            records.add(parsedRecord);
        }
        
        return records;
    }

    private Map<String, String> parseRecord(CSVRecord csvRecord, 
                                        Map<String, Integer> headerMap, 
                                        Map<String, String> cleanHeaderMap) {
        Map<String, String> headerRecord = new HashMap<>();
        
        for (Map.Entry<String, Integer> headerEntry : headerMap.entrySet()) {
            String originalHeader = headerEntry.getKey();
            String cleanHeader = cleanHeaderMap.get(originalHeader);
            int index = headerEntry.getValue();
            
            String value = extractValue(csvRecord, index);
            headerRecord.put(cleanHeader, value);
        }
        
        return headerRecord;
    }

    private String extractValue(CSVRecord csvRecord, int index) {
        if (index < csvRecord.size()) {
            String value = csvRecord.get(index);
            return value != null ? value.trim() : "";
        }
        return "";
    }

    private void validateRecords(List<Map<String, String>> records) {
        if (records.isEmpty()) {
            throw new CsvProcessingException("CSV file contains no data rows");
        }
    }

    private void saveToMongoDB(Lookup lookup) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        // Convert Lookup to BSON Document
        Map<String, Object> lookupMap = new HashMap<>();
        lookupMap.put(ID_FIELD, lookup.getId());
        lookupMap.put(LOOKUP_NAME_FIELD, lookup.getLookupName());
        lookupMap.put(DATA_FIELD, lookup.getData());
        lookupMap.put(UPLOADED_AT_FIELD, lookup.getUploadedAt());
        lookupMap.put(RECORD_COUNT_FIELD, lookup.getRecordCount());

        // Add description and fieldsDescription if they exist
        if (lookup.getDescription() != null) {
            lookupMap.put(DESCRIPTION_FIELD, lookup.getDescription());
        }
        if (lookup.getFieldsDescription() != null) {
            lookupMap.put(FIELDS_DESCRIPTION_FIELD, lookup.getFieldsDescription());
        }

        Document document = new Document(lookupMap);

        // Use replaceOne with upsert to update if exists or insert if not
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        collection.replaceOne(
            Filters.eq(ID_FIELD, lookup.getId()),
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
                .success(true)
                .totalLookups(lookups.size())
                .lookups(lookups)
                .build();
    }

    public LookupDTO getLookupByName(String lookupName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        Document doc = collection.find(Filters.eq(ID_FIELD, lookupName)).first();

        if (doc == null) {
            throw new NotFoundException("Lookup with name '" + lookupName + "' not found");
        }

        Lookup lookup = documentToLookup(doc);

        return LookupDTO.builder()
                .success(true)
                .lookups(new ArrayList<>(List.of(lookup)))
                .build();
    }

    public LookupDTO deleteLookup(String lookupName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        long deletedCount = collection.deleteOne(Filters.eq(ID_FIELD, lookupName)).getDeletedCount();

        if (deletedCount == 0) {
            throw new NotFoundException("Lookup with name '" + lookupName + "' not found");
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
            String id = doc.getString(ID_FIELD);
            String lookupName = doc.getString(LOOKUP_NAME_FIELD);
            Date uploadedAt = doc.getDate(UPLOADED_AT_FIELD);
            Integer recordCount = doc.getInteger(RECORD_COUNT_FIELD);
            String description = doc.getString(DESCRIPTION_FIELD);

            // Handle the data field (List of Maps)
            Object dataObj = doc.get(DATA_FIELD);
            List<Map<String, String>> data = extractDataList(dataObj, "lookup document");

            // Handle fieldsDescription (Map<String, String>)
            Object fieldsDescObj = doc.get(FIELDS_DESCRIPTION_FIELD);
            Map<String, String> fieldsDescription = null;
            if (fieldsDescObj instanceof Map) {
                fieldsDescription = new HashMap<>();
                Map<?, ?> rawMap = (Map<?, ?>) fieldsDescObj;
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    String key = entry.getKey() != null ? entry.getKey().toString() : "";
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    fieldsDescription.put(key, value);
                }
            }

            return Lookup.builder()
                    .id(id)
                    .lookupName(lookupName)
                    .data(data)
                    .uploadedAt(uploadedAt)
                    .recordCount(recordCount != null ? recordCount : 0)
                    .description(description)
                    .fieldsDescription(fieldsDescription)
                    .build();

        } catch (Exception e) {
            // Log the actual error for debugging
            log.error("Error converting document to Lookup", e);
            log.error("Document JSON: {}", doc.toJson());
            throw new CsvProcessingException("Failed to convert document to Lookup: " + e.getMessage(), e);
        }
    }

    /**
     * Get business capabilities from the "business-capabilities" lookup.
     * Transforms the lookup data into a list of BusinessCapabilityLookupDTO objects.
     * 
     * @return List of BusinessCapabilityLookupDTO objects
     * @throws NotFoundException if business-capabilities lookup not found
     */
    public List<BusinessCapabilityLookupDTO> getBusinessCapabilities() {
        log.info("Getting business capabilities from lookup");
        
        List<Map<String, String>> data = getLookupData(BUSINESS_CAPABILITIES_LOOKUP, BUSINESS_CAPABILITIES_NOT_FOUND_MSG, "business capabilities");
        return transformDataToBusinessCapabilities(data);
    }

    /**
     * Transforms raw data from MongoDB document into BusinessCapabilityLookupDTO objects.
     * This method provides a reusable way to convert business capability data regardless of source.
     * 
     * @param data List of maps containing business capability data with L1, L2, L3 keys
     * @return List of BusinessCapabilityLookupDTO objects
     * @throws IllegalArgumentException if data contains invalid structure
     */
    private List<BusinessCapabilityLookupDTO> transformDataToBusinessCapabilities(List<Map<String, String>> data) {
        if (data == null) {
            return new ArrayList<>();
        }
        
        List<BusinessCapabilityLookupDTO> businessCapabilities = new ArrayList<>();
        for (Map<String, String> dataRow : data) {
            BusinessCapabilityLookupDTO capability = new BusinessCapabilityLookupDTO(
                dataRow.get(L1_FIELD),
                dataRow.get(L2_FIELD), 
                dataRow.get(L3_FIELD)
            );
            businessCapabilities.add(capability);
        }
        
        return businessCapabilities;
    }

    /**
     * Get tech components from the "tech_eol" lookup.
     * Transforms the lookup data into a list of TechComponentLookupDTO objects.
     * 
     * @return List of TechComponentLookupDTO objects
     * @throws NotFoundException if tech_eol lookup not found
     */
    public List<TechComponentLookupDTO> getTechComponents() {
        log.info("Getting tech components from lookup");
        
        List<Map<String, String>> data = getLookupData(TECH_EOL_LOOKUP, TECH_COMPONENTS_NOT_FOUND_MSG, "tech components");
        return transformDataToTechComponents(data);
    }

    /**
     * Transforms raw data from MongoDB document into TechComponentLookupDTO objects.
     * This method provides a reusable way to convert tech component data regardless of source.
     * 
     * @param data List of maps containing tech component data with Product Name and Product Version keys
     * @return List of TechComponentLookupDTO objects
     * @throws IllegalArgumentException if data contains invalid structure
     */
    private List<TechComponentLookupDTO> transformDataToTechComponents(List<Map<String, String>> data) {
        if (data == null) {
            return new ArrayList<>();
        }
        
        List<TechComponentLookupDTO> techComponents = new ArrayList<>();
        for (Map<String, String> dataRow : data) {
            String productName = dataRow.get(PRODUCT_NAME_FIELD);
            String productVersion = dataRow.get(PRODUCT_VERSION_FIELD);
            
            // Skip entries with null or empty product names as they are invalid
            if (productName == null || productName.trim().isEmpty()) {
                log.warn("Tech component found with null or empty product name in row: {}, skipping entry", dataRow);
                continue;
            }
            
            // Log warning for missing version but still include the component
            if (productVersion == null || productVersion.trim().isEmpty()) {
                log.warn("Tech component found with null or empty product version for product: {}", productName);
                productVersion = ""; // Ensure we have a non-null value
            }
            
            TechComponentLookupDTO component = new TechComponentLookupDTO(productName, productVersion);
            techComponents.add(component);
        }
        
        return techComponents;
    }

    /**
     * Generic method to retrieve lookup data from MongoDB collection.
     * This method consolidates the common logic for fetching and processing lookup data.
     * 
     * @param lookupName The name of the lookup collection to retrieve
     * @param notFoundMessage The error message to use if lookup is not found
     * @param logContext Context for logging (e.g., "business capabilities", "tech components")
     * @return List of maps containing the lookup data
     * @throws NotFoundException if the specified lookup is not found
     * @throws CsvProcessingException if data processing fails
     */
    private List<Map<String, String>> getLookupData(String lookupName, String notFoundMessage, String logContext) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document doc = collection.find(Filters.eq(LOOKUP_NAME_FIELD, lookupName)).first();
        
        if (doc == null) {
            throw new NotFoundException(notFoundMessage);
        }
        
        try {
            Object dataObj = doc.get(DATA_FIELD);
            List<Map<String, String>> data = extractDataList(dataObj, logContext);
            
            if (data.isEmpty()) {
                log.warn("{} lookup found but contains no data", logContext);
                return new ArrayList<>();
            }
            
            log.info("Successfully retrieved {} {} records", data.size(), logContext);
            return data;
            
        } catch (Exception e) {
            log.error("Error processing {} lookup", logContext, e);
            throw new CsvProcessingException("Failed to process " + logContext + ": " + e.getMessage(), e);
        }
    }

    /**
     * Safely extracts and validates a List<Map<String, String>> from a MongoDB document field.
     * This method provides type-safe extraction without using unchecked casts.
     * 
     * @param dataObj The object retrieved from the MongoDB document
     * @param contextName A descriptive name for error messages (e.g., "business capabilities")
     * @return A validated List<Map<String, String>> or null if the data is null
     * @throws CsvProcessingException if the data structure is invalid
     */
    private List<Map<String, String>> extractDataList(Object dataObj, String contextName) {
        if (dataObj == null) {
            return new ArrayList<>();
        }

        // Check if it's a List
        if (!(dataObj instanceof List)) {
            throw new CsvProcessingException(
                String.format("Invalid data structure in %s: expected List but got %s", 
                    contextName, dataObj.getClass().getSimpleName())
            );
        }

        List<?> rawList = (List<?>) dataObj;
        List<Map<String, String>> result = new ArrayList<>();

        // Validate each element in the list
        for (int i = 0; i < rawList.size(); i++) {
            Object item = rawList.get(i);
            
            if (item == null) {
                log.warn("Found null item at index {} in {} data, skipping", i, contextName);
                continue;
            }

            // Check if it's a Map
            if (!(item instanceof Map)) {
                throw new CsvProcessingException(
                    String.format("Invalid item at index %d in %s: expected Map but got %s", 
                        i, contextName, item.getClass().getSimpleName())
                );
            }

            Map<?, ?> rawMap = (Map<?, ?>) item;
            Map<String, String> stringMap = new HashMap<>();

            // Validate and convert map entries
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                // Convert key to string
                String stringKey = (key != null) ? key.toString() : "";
                
                // Convert value to string (null values become empty strings)
                String stringValue = (value != null) ? value.toString() : "";
                
                stringMap.put(stringKey, stringValue);
            }

            result.add(stringMap);
        }

        return result;
    }

    /**
     * Updates the context (description and fields description) for a lookup.
     *
     * @param lookupName The name of the lookup to update
     * @param lookupContextDTO DTO containing the description and fieldsDescription
     * @return The updated LookupContextDTO
     * @throws NotFoundException if the lookup with the given name is not found
     */
    public LookupContextDTO addLookupContext(String lookupName, LookupContextDTO lookupContextDTO) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        // Find the existing lookup
        Document doc = collection.find(Filters.eq(ID_FIELD, lookupName)).first();

        if (doc == null) {
            throw new NotFoundException("Lookup with name '" + lookupName + "' not found");
        }

        // Convert existing document to Lookup object
        Lookup existingLookup = documentToLookup(doc);

        // Update the description and fieldsDescription
        existingLookup.setDescription(lookupContextDTO.getDescription());
        existingLookup.setFieldsDescription(lookupContextDTO.getFieldsDescription());

        // Save the updated lookup back to MongoDB
        saveToMongoDB(existingLookup);

        log.info("Successfully updated context for lookup: {}", lookupName);

        return lookupContextDTO;
    }

    public List<String> getFieldNames(String lookupName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        Document doc = collection.find(Filters.eq(ID_FIELD, lookupName)).first();

        if (doc == null) {
            throw new NotFoundException("Lookup with name '" + lookupName + "' not found");
        }

        Lookup lookup = documentToLookup(doc);

        if (lookup.getData() == null || lookup.getData().isEmpty()) {
            return new ArrayList<>();
        }

        // Extract field names from the first record
        Map<String, String> firstRecord = lookup.getData().get(0);
        return new ArrayList<>(firstRecord.keySet());
    }
}