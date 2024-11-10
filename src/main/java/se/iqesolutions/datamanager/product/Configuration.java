// File: src/main/java/se/iqesolutions/datamanager/product/Configuration.java

package se.iqesolutions.datamanager.product;

import java.util.Map;

public record Configuration(Map<String, Object> settings) implements DataProduct {
}