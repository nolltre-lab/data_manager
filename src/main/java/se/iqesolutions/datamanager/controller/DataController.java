// File: src/main/java/se/iqesolutions/datamanager/controller/DataController.java

package se.iqesolutions.datamanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import se.iqesolutions.datamanager.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import se.iqesolutions.datamanager.service.DataManagerService;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataManagerService dataManagerService;

    @PostMapping("/collect")
    @Operation(
            summary = "Collect Data",
            description = "Collect data products based on the provided request",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    public DataManagerResponse collectData(@Valid @RequestBody DataManagerRequest request) {
        return dataManagerService.collectDataProducts(request);
    }
}