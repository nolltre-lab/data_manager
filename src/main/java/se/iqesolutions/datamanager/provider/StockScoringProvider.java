// File: src/main/java/se/iqesolutions/datamanager/provider/StockScoringProvider.java

package se.iqesolutions.datamanager.provider;

import se.iqesolutions.datamanager.product.*;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;

import java.math.BigDecimal;
import java.util.*;

public class StockScoringProvider implements DataProvider {

    @Override
    public List<Class<? extends DataProduct>> getSupportedDataProducts() {
        return Arrays.asList(StockScore.class);
    }

    @Override
    public List<Class<? extends DataProduct>> getDependencies(Class<? extends DataProduct> dataProduct) {
        if (dataProduct.equals(StockScore.class)) {
            return Arrays.asList(
                    RealTimeSharePrice.class,
                    LastReportedEarningsPerShare.class,
                    Configuration.class,
                    Country.class
            );
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getMethods(Class<? extends DataProduct> dataProduct) {
        return Arrays.asList("CALCULATION");
    }

    @Override
    public double getExpectedTime(Class<? extends DataProduct> dataProduct) {
        return 0.5;
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
    ) throws Exception {
        if (dataProduct.equals(StockScore.class)) {
            RealTimeSharePrice sharePrice = (RealTimeSharePrice) availableDataProducts.get(RealTimeSharePrice.class);
            LastReportedEarningsPerShare earningsPerShare = (LastReportedEarningsPerShare) availableDataProducts.get(LastReportedEarningsPerShare.class);
            Configuration configuration = (Configuration) availableDataProducts.get(Configuration.class);
            Country country = (Country) availableDataProducts.get(Country.class);
            if (sharePrice == null || earningsPerShare == null || configuration == null || country == null) {
                throw new Exception("Dependencies not satisfied for StockScore");
            }

            // Calculate stock score (simple placeholder calculation)
            double price = sharePrice.price().doubleValue();
            double eps = earningsPerShare.earningsPerShare().doubleValue();
            double score = price / eps;

            // Adjust score based on country or configuration if needed
            // For simplicity, we use the threshold from configuration
            double threshold = (double) configuration.settings().getOrDefault("stockScoreThreshold", 5.0);

            // For example, if score is above threshold, we adjust it
            if (score > threshold) {
                score = threshold;
            }

            return new StockScore(sharePrice.tickerSymbol(), score);
        }
        return null;
    }
}