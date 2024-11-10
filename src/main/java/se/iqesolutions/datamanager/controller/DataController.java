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

    @PostMapping("/collect")
    public DataManagerResponse collectData(@RequestBody DataManagerRequest request) {
        return dataManagerService.collectDataProducts(request);
    }
}