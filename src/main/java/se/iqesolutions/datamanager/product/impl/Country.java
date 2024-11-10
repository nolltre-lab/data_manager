// File: src/main/java/se/iqesolutions/datamanager/product/Country.java

package se.iqesolutions.datamanager.product.impl;

import se.iqesolutions.datamanager.product.DataProduct;

public record Country(String name) implements DataProduct {
}