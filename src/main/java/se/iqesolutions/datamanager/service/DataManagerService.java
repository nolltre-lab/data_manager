// File: src/main/java/se/iqesolutions/datamanager/service/DataManagerService.java

package se.iqesolutions.datamanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.iqesolutions.datamanager.DataManagerRequest;
import se.iqesolutions.datamanager.DataManagerResponse;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;
import se.iqesolutions.datamanager.product.DataProduct;
import se.iqesolutions.datamanager.provider.DataProvider;

import java.util.*;

@Service
public class DataManagerService {
    private static final Logger logger = LoggerFactory.getLogger(DataManagerService.class);

    private final List<DataProvider> dataProviders;

    // Use constructor injection to inject all DataProvider beans
    @Autowired
    public DataManagerService(List<DataProvider> dataProviders) {
        this.dataProviders = dataProviders;
    }

    // Plan and execute data collection
    public DataManagerResponse collectDataProducts(DataManagerRequest request) {
        Map<String, DataProductConstraint> requestedDataProductsStr = request.requestedDataProducts();
        List<DataProduct> alreadyAvailableDataProducts = request.alreadyAvailableDataProducts();

        Map<Class<? extends DataProduct>, DataProductConstraint> requestedDataProducts = new HashMap<>();
        Map<Class<? extends DataProduct>, DataProduct> collectedDataProductsMap = new LinkedHashMap<>();
        List<DataProduct> collectedDataProducts = new ArrayList<>();
        Map<String, String> failedDataProducts = new HashMap<>();

        // Initialize with already available data products
        for (DataProduct dataProduct : alreadyAvailableDataProducts) {
            collectedDataProductsMap.put(dataProduct.getClass(), dataProduct);
            collectedDataProducts.add(dataProduct);
            logger.info("Already available data product: {}", dataProduct.getClass().getName());
        }

        // Convert String class names to Class objects
        for (Map.Entry<String, DataProductConstraint> entry : requestedDataProductsStr.entrySet()) {
            String className = entry.getKey();
            DataProductConstraint constraint = entry.getValue();

            try {
                Class<?> clazz = Class.forName(className);
                if (DataProduct.class.isAssignableFrom(clazz)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends DataProduct> dataProductClass = (Class<? extends DataProduct>) clazz;
                    requestedDataProducts.put(dataProductClass, constraint);
                } else {
                    String message = "Class " + className + " does not implement DataProduct";
                    logger.error(message);
                    failedDataProducts.put(className, message);
                }
            } catch (ClassNotFoundException e) {
                String message = "Class " + className + " not found";
                logger.error(message);
                failedDataProducts.put(className, message);
            }
        }

        // Now attempt to collect each requested data product recursively
        for (Map.Entry<Class<? extends DataProduct>, DataProductConstraint> entry : requestedDataProducts.entrySet()) {
            Class<? extends DataProduct> dataProductClass = entry.getKey();
            DataProductConstraint constraint = entry.getValue();

            collectDataProductRecursive(
                    dataProductClass,
                    collectedDataProductsMap,
                    collectedDataProducts,
                    failedDataProducts,
                    constraint,
                    new HashSet<>()
            );
        }

        // Prepare response
        return new DataManagerResponse(
                collectedDataProducts,
                failedDataProducts
        );
    }

    private void collectDataProductRecursive(
            Class<? extends DataProduct> dataProductClass,
            Map<Class<? extends DataProduct>, DataProduct> collectedDataProductsMap,
            List<DataProduct> collectedDataProducts,
            Map<String, String> failedDataProducts,
            DataProductConstraint constraint,
            Set<Class<? extends DataProduct>> inProgressDataProducts
    ) {
        String className = dataProductClass.getName();

        if (collectedDataProductsMap.containsKey(dataProductClass) || failedDataProducts.containsKey(className)) {
            return;
        }

        if (inProgressDataProducts.contains(dataProductClass)) {
            String message = "Cycle detected involving " + className;
            logger.error(message);
            failedDataProducts.put(className, message);
            return;
        }

        inProgressDataProducts.add(dataProductClass);

        // Find providers that can provide this data product and meet the constraints
        boolean provided = false;

        // Collect providers that support the data product
        List<DataProvider> supportingProviders = new ArrayList<>();
        for (DataProvider provider : dataProviders) {
            if (provider.getSupportedDataProducts().contains(dataProductClass)) {
                supportingProviders.add(provider);
            }
        }

        // Sort providers based on weighted score
        supportingProviders.sort(Comparator.comparingDouble(p -> {
            double cost = p.getExpectedCost(dataProductClass);
            double time = p.getExpectedTime(dataProductClass);
            double costWeight = (constraint != null) ? constraint.costWeight() : 1.0;
            double timeWeight = (constraint != null) ? constraint.timeWeight() : 1.0;
            // Normalize weights if both are zero to avoid division by zero
            if (costWeight == 0 && timeWeight == 0) {
                costWeight = 1.0;
                timeWeight = 1.0;
            }
            // Calculate weighted score
            return (cost * costWeight) + (time * timeWeight);
        }));

        for (DataProvider provider : supportingProviders) {
            // Check if provider's methods align with acceptable methods in constraints
            List<String> providerMethods = provider.getMethods(dataProductClass);
            if (constraint != null && constraint.acceptableMethods() != null) {
                boolean methodAccepted = false;
                for (String method : providerMethods) {
                    if (constraint.acceptableMethods().contains(method)) {
                        methodAccepted = true;
                        break;
                    }
                }
                if (!methodAccepted) {
                    // Provider's methods are not acceptable; skip to next provider
                    continue;
                }
            }

            // **Enhancements: Check cost and time constraints**
            if (constraint != null) {
                // Check cost
                if (provider.getExpectedCost(dataProductClass) > constraint.maxCost()) {
                    continue; // Skip provider
                }
                // Check time
                if (provider.getExpectedTime(dataProductClass) > constraint.maxTime()) {
                    continue; // Skip provider
                }
            }

            // Check dependencies
            List<Class<? extends DataProduct>> dependencies = provider.getDependencies(dataProductClass);
            boolean dependenciesSatisfied = true;
            for (Class<? extends DataProduct> dependency : dependencies) {
                if (!collectedDataProductsMap.containsKey(dependency)) {
                    // Collect dependency recursively
                    collectDataProductRecursive(
                            dependency,
                            collectedDataProductsMap,
                            collectedDataProducts,
                            failedDataProducts,
                            null, // No specific constraint for dependencies
                            inProgressDataProducts
                    );
                    if (!collectedDataProductsMap.containsKey(dependency)) {
                        dependenciesSatisfied = false;
                        break;
                    }
                }
            }

            if (dependenciesSatisfied) {
                try {
                    DataProduct dataProduct = provider.collectDataProduct(
                            dataProductClass,
                            collectedDataProductsMap,
                            constraint
                    );
                    collectedDataProductsMap.put(dataProductClass, dataProduct);
                    collectedDataProducts.add(dataProduct);
                    logger.info("Collected data product: {} using provider: {}", className, provider.getClass().getSimpleName());
                    provided = true;
                    break;
                } catch (Exception e) {
                    String message = e.getMessage();
                    logger.error("Failed to collect data product {} using provider {}: {}", className, provider.getClass().getSimpleName(), message);
                    // Continue to next provider
                }
            }
        }

        if (!provided && !collectedDataProductsMap.containsKey(dataProductClass)) {
            String reason = "No provider available or dependencies not satisfied";
            failedDataProducts.put(className, reason);
        }

        inProgressDataProducts.remove(dataProductClass);
    }
}