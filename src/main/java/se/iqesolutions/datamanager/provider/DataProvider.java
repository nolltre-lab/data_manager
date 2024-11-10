// File: src/main/java/se/iqesolutions/datamanager/provider/DataProvider.java

package se.iqesolutions.datamanager.provider;

import se.iqesolutions.datamanager.product.DataProduct;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;
import java.util.List;
import java.util.Map;

public interface DataProvider {
    /**
     * Returns a list of DataProduct classes that this provider can serve.
     */
    List<Class<? extends DataProduct>> getSupportedDataProducts();

    /**
     * Returns a list of dependencies required to serve the given DataProduct.
     */
    List<Class<? extends DataProduct>> getDependencies(Class<? extends DataProduct> dataProduct);

    /**
     * Returns a list of acceptable methods this provider uses to collect the given DataProduct.
     */
    List<String> getMethods(Class<? extends DataProduct> dataProduct);

    /**
     * Returns expected time and cost to serve the DataProduct.
     */
    double getExpectedTime(Class<? extends DataProduct> dataProduct);

    double getExpectedCost(Class<? extends DataProduct> dataProduct);

    /**
     * Attempts to collect the DataProduct.
     */
    DataProduct collectDataProduct(
        Class<? extends DataProduct> dataProduct,
        Map<Class<? extends DataProduct>, DataProduct> availableDataProducts,
        DataProductConstraint constraint
    ) throws Exception;
}