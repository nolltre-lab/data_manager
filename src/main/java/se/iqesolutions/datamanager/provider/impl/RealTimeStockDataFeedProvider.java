// File: src/main/java/se/iqesolutions/datamanager/provider/RealTimeStockDataFeedProvider.java

package se.iqesolutions.datamanager.provider.impl;

import org.springframework.stereotype.Component;
import se.iqesolutions.datamanager.product.*;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;
import se.iqesolutions.datamanager.product.impl.RealTimeSharePrice;
import se.iqesolutions.datamanager.product.impl.TickerSymbol;
import se.iqesolutions.datamanager.provider.DataProvider;

import java.math.BigDecimal;
import java.util.*;

@Component
public class RealTimeStockDataFeedProvider implements DataProvider {

    @Override
    public List<Class<? extends DataProduct>> getSupportedDataProducts() {
        return Arrays.asList(RealTimeSharePrice.class);
    }

    @Override
    public List<Class<? extends DataProduct>> getDependencies(Class<? extends DataProduct> dataProduct) {
        if (dataProduct.equals(RealTimeSharePrice.class)) {
            return Arrays.asList(TickerSymbol.class);
        }
        return Collections.emptyList();
    }

    @Override
    public double getExpectedTime(Class<? extends DataProduct> dataProduct) {
        return 1.0; // Expected time in seconds
    }

    @Override
    public double getExpectedCost(Class<? extends DataProduct> dataProduct) {
        return 0.5; // Expected cost
    }

    @Override
    public DataProduct collectDataProduct(
        Class<? extends DataProduct> dataProduct,
        Map<Class<? extends DataProduct>, DataProduct> availableDataProducts,
        DataProductConstraint constraint
    ) throws Exception {
        if (dataProduct.equals(RealTimeSharePrice.class)) {
            TickerSymbol tickerSymbol = (TickerSymbol) availableDataProducts.get(TickerSymbol.class);
            if (tickerSymbol == null) {
                throw new Exception("TickerSymbol dependency not satisfied");
            }

            // Simulate data collection
            BigDecimal price = fetchRealTimePrice(tickerSymbol.symbol());
            return new RealTimeSharePrice(tickerSymbol.symbol(), price);
        }
        return null;
    }

    private BigDecimal fetchRealTimePrice(String symbol) {
        // Simulate fetching real-time price
        return BigDecimal.valueOf(100.0); // Placeholder value
    }

    @Override
    public List<String> getMethods(Class<? extends DataProduct> dataProduct) {
        if (dataProduct.equals(RealTimeSharePrice.class)) {
            return Arrays.asList("REAL_TIME_FEED");
        }
        return Collections.emptyList();
    }
}