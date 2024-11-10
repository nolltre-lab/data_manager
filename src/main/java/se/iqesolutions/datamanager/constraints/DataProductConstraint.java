// File: src/main/java/se/iqesolutions/datamanager/constraints/DataProductConstraint.java

package se.iqesolutions.datamanager.constraints;

import java.util.List;

public record DataProductConstraint(
    List<String> acceptableMethods,
    String cacheKey,
    long maxCacheAgeSeconds,
    double maxCost,
    double maxTime
) {
}