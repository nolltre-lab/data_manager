// File: src/main/java/se/iqesolutions/datamanager/constraints/DataProductConstraint.java

package se.iqesolutions.datamanager.constraints;

import java.util.List;

public record DataProductConstraint(
        List<String> acceptableMethods,
        String cacheKey,
        int maxCacheAgeSeconds,
        double maxCost,
        double maxTime,
        double costWeight,
        double timeWeight
) {
    public DataProductConstraint {
        // Validation: Ensure weights are non-negative
        if (costWeight < 0 || timeWeight < 0) {
            throw new IllegalArgumentException("Cost weight and time weight must be non-negative");
        }
    }
}