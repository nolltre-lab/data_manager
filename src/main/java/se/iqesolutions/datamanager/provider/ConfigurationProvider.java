// File: src/main/java/se/iqesolutions/datamanager/provider/ConfigurationProvider.java

package se.iqesolutions.datamanager.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.iqesolutions.datamanager.product.*;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;

import java.util.*;

@Component
public class ConfigurationProvider implements DataProvider {

    @Value("${stock.score.threshold:5.0}")
    private double stockScoreThreshold;

    @Override
    public List<Class<? extends DataProduct>> getSupportedDataProducts() {
        return Arrays.asList(Configuration.class);
    }

    @Override
    public List<Class<? extends DataProduct>> getDependencies(Class<? extends DataProduct> dataProduct) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getMethods(Class<? extends DataProduct> dataProduct) {
        return Arrays.asList("CONFIGURATION");
    }

    @Override
    public double getExpectedTime(Class<? extends DataProduct> dataProduct) {
        return 0.0;
    }

    @Override
    public double getExpectedCost(Class<? extends DataProduct> dataProduct) {
        return 0.0;
    }

    @Override
    public DataProduct collectDataProduct(
            Class<? extends DataProduct> dataProduct,
            Map<Class<? extends DataProduct>, DataProduct> availableDataProducts,
            DataProductConstraint constraint
    ) {
        if (dataProduct.equals(Configuration.class)) {
            Map<String, Object> settings = new HashMap<>();
            settings.put("stockScoreThreshold", stockScoreThreshold);
            return new Configuration(settings);
        }
        return null;
    }
}