// File: src/main/java/se/iqesolutions/datamanager/provider/FinnishStockDataProvider.java

package se.iqesolutions.datamanager.provider.impl;

import org.springframework.stereotype.Component;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;
import se.iqesolutions.datamanager.product.*;
import se.iqesolutions.datamanager.product.impl.Country;
import se.iqesolutions.datamanager.product.impl.RealTimeSharePrice;
import se.iqesolutions.datamanager.product.impl.TickerSymbol;
import se.iqesolutions.datamanager.provider.DataProvider;

import java.math.BigDecimal;
import java.util.*;

@Component
public class FinnishStockDataProvider implements DataProvider {

    @Override
    public List<Class<? extends DataProduct>> getSupportedDataProducts() {
        return Arrays.asList(RealTimeSharePrice.class);
    }

    @Override
    public List<Class<? extends DataProduct>> getDependencies(Class<? extends DataProduct> dataProduct) {
        if (dataProduct.equals(RealTimeSharePrice.class)) {
            return Arrays.asList(TickerSymbol.class, Country.class);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getMethods(Class<? extends DataProduct> dataProduct) {
        return Arrays.asList("REAL_TIME_FEED");
    }

    @Override
    public double getExpectedTime(Class<? extends DataProduct> dataProduct) {
        return 2.0; // Same or similar to existing provider
    }

    @Override
    public double getExpectedCost(Class<? extends DataProduct> dataProduct) {
        return 0.001; // Cheaper than existing provider
    }

    @Override
    public DataProduct collectDataProduct(
            Class<? extends DataProduct> dataProduct,
            Map<Class<? extends DataProduct>, DataProduct> availableDataProducts,
            DataProductConstraint constraint
    ) throws Exception {
        if (dataProduct.equals(RealTimeSharePrice.class)) {
            TickerSymbol tickerSymbol = (TickerSymbol) availableDataProducts.get(TickerSymbol.class);
            Country country = (Country) availableDataProducts.get(Country.class);
            if (tickerSymbol == null || country == null) {
                throw new Exception("Dependencies not satisfied for RealTimeSharePrice");
            }
            // Only supports Finnish shares
            if (!"Finland".equalsIgnoreCase(country.name())) {
                throw new Exception("FinnishStockDataProvider only supports Finnish shares");
            }
            // Simulate fetching real-time share price
            BigDecimal price = fetchFinnishSharePrice(tickerSymbol.symbol());
            return new RealTimeSharePrice(tickerSymbol.symbol(), price);
        }
        return null;
    }

    private BigDecimal fetchFinnishSharePrice(String symbol) {
        // Placeholder data
        return BigDecimal.valueOf(50.0); // Placeholder value for Finnish shares
    }
}