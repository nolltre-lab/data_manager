// File: src/main/java/se/iqesolutions/datamanager/product/TickerSymbol.java

package se.iqesolutions.datamanager.product.impl;

import se.iqesolutions.datamanager.product.DataProduct;

public record TickerSymbol(String symbol) implements DataProduct {
}