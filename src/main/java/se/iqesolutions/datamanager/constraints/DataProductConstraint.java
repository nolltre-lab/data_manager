package se.iqesolutions.datamanager.constraints;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DataProductConstraint(
        @NotNull @Size(min = 1)
        List<String> acceptableMethods,

        String cacheKey,

        @Min(0)
        int maxCacheAgeSeconds,

        @Min(0)
        double maxCost,

        @Min(0)
        double maxTime,

        @Min(0)
        double costWeight,

        @Min(0)
        double timeWeight
) {
}