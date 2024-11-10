package se.iqesolutions.datamanager;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import se.iqesolutions.datamanager.product.DataProduct;
import se.iqesolutions.datamanager.constraints.DataProductConstraint;

import java.util.List;
import java.util.Map;

public record DataManagerRequest(
    @NotNull
    @Valid
    Map<String, @Valid DataProductConstraint> requestedDataProducts,

    @NotNull
    @Valid
    List<@Valid DataProduct> alreadyAvailableDataProducts
) {
}