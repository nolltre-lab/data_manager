// File: src/main/java/se/iqesolutions/datamanager/DataManagerResponse.java

package se.iqesolutions.datamanager;

import se.iqesolutions.datamanager.product.DataProduct;

import java.util.List;
import java.util.Map;

public record DataManagerResponse(
    List<DataProduct> collectedDataProducts,
    Map<String, String> failedDataProducts
) {
}