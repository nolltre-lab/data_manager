// File: src/main/java/se/iqesolutions/datamanager/controller/DataController.java

package se.iqesolutions.datamanager.controller;

import se.iqesolutions.datamanager.*;
import se.iqesolutions.datamanager.provider.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;
import se.iqesolutions.datamanager.service.DataManagerService;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataManagerService dataManagerService;

    @PostConstruct
    public void init() {
        // Register data providers
        dataManagerService.registerDataProvider(new RealTimeStockDataFeedProvider());
        dataManagerService.registerDataProvider(new EarningsDataProvider());
        dataManagerService.registerDataProvider(new CountryProvider());
        dataManagerService.registerDataProvider(new ConfigurationProvider());
        dataManagerService.registerDataProvider(new StockScoringProvider());
        dataManagerService.registerDataProvider(new FinnishStockDataProvider());
        // You can register more data providers here
    }

    @PostMapping("/collect")
    public DataManagerResponse collectData(@RequestBody DataManagerRequest request) {
        return dataManagerService.collectDataProducts(request);
    }
}