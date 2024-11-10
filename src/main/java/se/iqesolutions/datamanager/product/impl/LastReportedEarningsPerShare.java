// File: src/main/java/se/iqesolutions/datamanager/product/LastReportedEarningsPerShare.java

package se.iqesolutions.datamanager.product.impl;

import se.iqesolutions.datamanager.product.DataProduct;

import java.math.BigDecimal;

public record LastReportedEarningsPerShare(String tickerSymbol, BigDecimal earningsPerShare) implements DataProduct {
}