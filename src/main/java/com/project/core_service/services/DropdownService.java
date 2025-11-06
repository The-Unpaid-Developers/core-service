package com.project.core_service.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.exceptions.CsvProcessingException;
import com.project.core_service.exceptions.NotFoundException;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DropdownService {

    private final MongoDatabase mongoDatabase;

    @Value("${mongodb.collection.lookups.name}")
    private String collectionName;

    // MongoDB document field names
    private static final String LOOKUP_NAME_FIELD = "lookupName";
    private static final String DATA_FIELD = "data";

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
    public DropdownService(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
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
}
