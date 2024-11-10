// File: src/main/java/se/iqesolutions/datamanager/product/StockScore.java

package se.iqesolutions.datamanager.product;

public record StockScore(String tickerSymbol, double score) implements DataProduct {
}