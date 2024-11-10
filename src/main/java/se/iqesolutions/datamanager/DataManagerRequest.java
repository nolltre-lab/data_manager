// File: src/main/java/se/iqesolutions/datamanager/DataManagerRequest.java

package se.iqesolutions.datamanager;

import se.iqesolutions.datamanager.product.DataProduct;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;

import java.util.List;
import java.util.Map;

public record DataManagerRequest(
    Map<String, DataProductConstraint> requestedDataProducts,
    List<DataProduct> alreadyAvailableDataProducts
) {
}