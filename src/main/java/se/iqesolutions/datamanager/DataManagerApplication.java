// File: src/main/java/se/iqesolutions/datamanager/DataManagerApplication.java

package se.iqesolutions.datamanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import se.iqesolutions.datamanager.service.DataManagerService;

@SpringBootApplication
public class DataManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataManagerApplication.class, args);
    }
}