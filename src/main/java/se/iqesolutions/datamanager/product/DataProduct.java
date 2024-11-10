// File: src/main/java/se/iqesolutions/datamanager/product/DataProduct.java

package se.iqesolutions.datamanager.product;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

// This annotation helps with polymorphic deserialization
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface DataProduct extends Serializable {
    // Marker interface; can add common methods if needed
}