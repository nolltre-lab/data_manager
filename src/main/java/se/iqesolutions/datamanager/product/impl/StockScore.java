// File: src/main/java/se/iqesolutions/datamanager/product/StockScore.java

package se.iqesolutions.datamanager.product.impl;

import se.iqesolutions.datamanager.product.DataProduct;

public record StockScore(String tickerSymbol, double score) implements DataProduct {
}