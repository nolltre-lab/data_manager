// File: src/main/java/se/iqesolutions/datamanager/provider/EarningsDataProvider.java

package se.iqesolutions.datamanager.provider.impl;

import org.springframework.stereotype.Component;
import se.iqesolutions.datamanager.product.*;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;
import se.iqesolutions.datamanager.product.impl.LastReportedEarningsPerShare;
import se.iqesolutions.datamanager.product.impl.TickerSymbol;
import se.iqesolutions.datamanager.provider.DataProvider;

import java.math.BigDecimal;
import java.util.*;

@Component
public class EarningsDataProvider implements DataProvider {

    @Override
    public List<Class<? extends DataProduct>> getSupportedDataProducts() {
        return Arrays.asList(LastReportedEarningsPerShare.class);
    }

    @Override
    public List<Class<? extends DataProduct>> getDependencies(Class<? extends DataProduct> dataProduct) {
        if (dataProduct.equals(LastReportedEarningsPerShare.class)) {
            return Arrays.asList(TickerSymbol.class);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getMethods(Class<? extends DataProduct> dataProduct) {
        return Arrays.asList("FINANCIAL_REPORT");
    }

    @Override
    public double getExpectedTime(Class<? extends DataProduct> dataProduct) {
        return 2.0;
    }

    @Override
    public double getExpectedCost(Class<? extends DataProduct> dataProduct) {
        return 0.05;
    }

    @Override
    public DataProduct collectDataProduct(
            Class<? extends DataProduct> dataProduct,
            Map<Class<? extends DataProduct>, DataProduct> availableDataProducts,
            DataProductConstraint constraint
    ) throws Exception {
        if (dataProduct.equals(LastReportedEarningsPerShare.class)) {
            TickerSymbol tickerSymbol = (TickerSymbol) availableDataProducts.get(TickerSymbol.class);
            if (tickerSymbol == null) {
                throw new Exception("TickerSymbol dependency not satisfied");
            }
            // Simulate fetching earnings per share
            BigDecimal eps = fetchEarningsPerShare(tickerSymbol.symbol());
            return new LastReportedEarningsPerShare(tickerSymbol.symbol(), eps);
        }
        return null;
    }

    private BigDecimal fetchEarningsPerShare(String symbol) {
        // Placeholder data
        return BigDecimal.valueOf(3.50); // Placeholder value
    }
}