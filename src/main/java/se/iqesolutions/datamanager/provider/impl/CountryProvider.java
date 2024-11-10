// File: src/main/java/se/iqesolutions/datamanager/provider/CountryProvider.java

package se.iqesolutions.datamanager.provider.impl;

import org.springframework.stereotype.Component;
import se.iqesolutions.datamanager.product.*;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;
import se.iqesolutions.datamanager.product.impl.Country;
import se.iqesolutions.datamanager.product.impl.TickerSymbol;
import se.iqesolutions.datamanager.provider.DataProvider;

import java.util.*;

@Component
public class CountryProvider implements DataProvider {

    @Override
    public List<Class<? extends DataProduct>> getSupportedDataProducts() {
        return Arrays.asList(Country.class);
    }

    @Override
    public List<Class<? extends DataProduct>> getDependencies(Class<? extends DataProduct> dataProduct) {
        // Assuming Country requires TickerSymbol
        if (dataProduct.equals(Country.class)) {
            return Arrays.asList(TickerSymbol.class);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getMethods(Class<? extends DataProduct> dataProduct) {
        return Arrays.asList("CONFIGURATION");
    }

    @Override
    public double getExpectedTime(Class<? extends DataProduct> dataProduct) {
        return 0.1;
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
        if (dataProduct.equals(Country.class)) {
            TickerSymbol tickerSymbol = (TickerSymbol) availableDataProducts.get(TickerSymbol.class);
            if (tickerSymbol == null) {
                throw new Exception("TickerSymbol dependency not satisfied");
            }
            // Simulate fetching country information based on ticker symbol
            String countryName = getCountryByTicker(tickerSymbol.symbol());
            return new Country(countryName);
        }
        return null;
    }

    private String getCountryByTicker(String tickerSymbol) {
        // Placeholder mapping
        Map<String, String> tickerCountryMap = Map.of(
                "AAPL", "USA",
                "TSLA", "USA",
                "NOK", "Finland"
        );
        return tickerCountryMap.getOrDefault(tickerSymbol, "Unknown");
    }
}