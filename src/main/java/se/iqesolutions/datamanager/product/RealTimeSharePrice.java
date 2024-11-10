// File: src/main/java/se/iqesolutions/datamanager/product/RealTimeSharePrice.java

package se.iqesolutions.datamanager.product;

import java.math.BigDecimal;

public record RealTimeSharePrice(String tickerSymbol, BigDecimal price) implements DataProduct {
}