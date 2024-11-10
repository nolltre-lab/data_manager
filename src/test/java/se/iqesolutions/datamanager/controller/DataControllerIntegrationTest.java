// File: src/test/java/se/iqesolutions/datamanager/controller/DataControllerIntegrationTest.java

package se.iqesolutions.datamanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.iqesolutions.datamanager.DataManagerApplication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import se.iqesolutions.datamanager.DataManagerResponse;
import se.iqesolutions.datamanager.product.DataProduct;
import se.iqesolutions.datamanager.product.impl.RealTimeSharePrice;
import se.iqesolutions.datamanager.product.impl.TickerSymbol;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = DataManagerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCollectData_Successful() {
        String requestJson = """
        {
          "requestedDataProducts": {
            "se.iqesolutions.datamanager.product.impl.RealTimeSharePrice": {
              "acceptableMethods": ["REAL_TIME_FEED"],
              "cacheKey": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
              "maxCacheAgeSeconds": 0,
              "maxCost": 1.0,
              "maxTime": 5.0
            }
          },
          "alreadyAvailableDataProducts": [
            {
              "@class": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
              "symbol": "AAPL"
            }
          ]
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Simple checks to ensure response contains expected data
        String responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"tickerSymbol\":\"AAPL\""));
        assertTrue(responseBody.contains("\"price\":100.0"));
        assertTrue(responseBody.contains("\"failedDataProducts\":{}"));
    }

    @Test
    public void testCollectData_MissingDependency() {
        String requestJson = """
        {
          "requestedDataProducts": {
            "se.iqesolutions.datamanager.product.impl.RealTimeSharePrice": {
              "acceptableMethods": ["REAL_TIME_FEED"],
              "cacheKey": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
              "maxCacheAgeSeconds": 0,
              "maxCost": 1.0,
              "maxTime": 5.0
            }
          },
          "alreadyAvailableDataProducts": []
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);
        // Both TickerSymbol and RealTimeSharePrice should fail
        assertTrue(responseBody.contains("se.iqesolutions.datamanager.product.impl.TickerSymbol"));
        assertTrue(responseBody.contains("No provider available or dependencies not satisfied"));
        assertTrue(responseBody.contains("se.iqesolutions.datamanager.product.impl.RealTimeSharePrice"));
    }

    @Test
    public void testCollectData_UnsupportedDataProduct() {
        String requestJson = """
        {
          "requestedDataProducts": {
            "se.iqesolutions.datamanager.product.impl.UnsupportedDataProduct": {
              "acceptableMethods": ["REAL_TIME_FEED"],
              "cacheKey": "",
              "maxCacheAgeSeconds": 0,
              "maxCost": 1.0,
              "maxTime": 5.0
            }
          },
          "alreadyAvailableDataProducts": []
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);
        // UnsupportedDataProduct should fail
        assertTrue(responseBody.contains("se.iqesolutions.datamanager.product.impl.UnsupportedDataProduct"));
        assertTrue(responseBody.contains("No provider available or dependencies not satisfied"));
    }

    @Test
    public void testCollectData_MissingDataProduct() throws Exception {
        String requestJson = """
        {
          "requestedDataProducts": {
            "se.iqesolutions.datamanager.product.MissingDataProduct": {
              "acceptableMethods": ["REAL_TIME_FEED"],
              "cacheKey": "",
              "maxCacheAgeSeconds": 0,
              "maxCost": 1.0,
              "maxTime": 5.0
            }
          },
          "alreadyAvailableDataProducts": []
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);

        // Parse the response
        ObjectMapper objectMapper = new ObjectMapper();
        DataManagerResponse dataManagerResponse = objectMapper.readValue(responseBody, DataManagerResponse.class);

        // Assert that no data products were collected
        assertTrue(dataManagerResponse.collectedDataProducts().isEmpty());

        // Assert that the failedDataProducts contains the expected key and message
        Map<String, String> failedDataProducts = dataManagerResponse.failedDataProducts();
        assertEquals(1, failedDataProducts.size());
        assertTrue(failedDataProducts.containsKey("se.iqesolutions.datamanager.product.MissingDataProduct"));
        assertEquals(
                "Class se.iqesolutions.datamanager.product.MissingDataProduct not found",
                failedDataProducts.get("se.iqesolutions.datamanager.product.MissingDataProduct")
        );
    }


    @Test
    public void testCollectData_ConstraintViolation() {
        String requestJson = """
        {
          "requestedDataProducts": {
            "se.iqesolutions.datamanager.product.impl.RealTimeSharePrice": {
              "acceptableMethods": ["USER_INPUT"],
              "cacheKey": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
              "maxCacheAgeSeconds": 0,
              "maxCost": 0.001,
              "maxTime": 0.001
            }
          },
          "alreadyAvailableDataProducts": [
            {
              "@class": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
              "symbol": "AAPL"
            }
          ]
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);
        // Constraint violation should cause RealTimeSharePrice to fail
        assertTrue(responseBody.contains("se.iqesolutions.datamanager.product.impl.RealTimeSharePrice"));
        assertTrue(responseBody.contains("No provider available or dependencies not satisfied"));
    }

    @Test
    public void testCollectData_StockScore_Successful() throws Exception {
        String requestJson = """
    {
      "requestedDataProducts": {
        "se.iqesolutions.datamanager.product.impl.StockScore": {
          "acceptableMethods": ["CALCULATION"],
          "cacheKey": "",
          "maxCacheAgeSeconds": 0,
          "maxCost": 1.0,
          "maxTime": 10.0
        }
      },
      "alreadyAvailableDataProducts": [
        {
          "@class": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
          "symbol": "AAPL"
        }
      ]
    }
    """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);

        // Parse the response
        ObjectMapper objectMapper = new ObjectMapper();
        DataManagerResponse dataManagerResponse = objectMapper.readValue(responseBody, DataManagerResponse.class);

        // Collect class names in the order they were collected
        List<String> collectedClassNames = dataManagerResponse.collectedDataProducts().stream()
                .map(dataProduct -> dataProduct.getClass().getName())
                .collect(Collectors.toList());

        // Map data products to their dependencies
        Map<String, List<String>> dependenciesMap = new HashMap<>();
        dependenciesMap.put("se.iqesolutions.datamanager.product.impl.StockScore", Arrays.asList(
                "se.iqesolutions.datamanager.product.impl.RealTimeSharePrice",
                "se.iqesolutions.datamanager.product.impl.LastReportedEarningsPerShare",
                "se.iqesolutions.datamanager.product.impl.Configuration",
                "se.iqesolutions.datamanager.product.impl.Country"
        ));
        dependenciesMap.put("se.iqesolutions.datamanager.product.impl.RealTimeSharePrice", Collections.emptyList());
        dependenciesMap.put("se.iqesolutions.datamanager.product.impl.LastReportedEarningsPerShare", Collections.emptyList());
        dependenciesMap.put("se.iqesolutions.datamanager.product.impl.Configuration", Collections.emptyList());
        dependenciesMap.put("se.iqesolutions.datamanager.product.impl.Country", Collections.emptyList());
        // Add other data products if necessary

        // Build a map of data product to its index in the collected list
        Map<String, Integer> collectedIndexMap = new HashMap<>();
        for (int i = 0; i < collectedClassNames.size(); i++) {
            collectedIndexMap.put(collectedClassNames.get(i), i);
        }

        // Verify that each data product's dependencies are collected before it
        for (Map.Entry<String, List<String>> entry : dependenciesMap.entrySet()) {
            String dataProduct = entry.getKey();
            List<String> dependencies = entry.getValue();

            if (!collectedIndexMap.containsKey(dataProduct)) {
                fail("Data product not collected: " + dataProduct);
            }

            int dataProductIndex = collectedIndexMap.get(dataProduct);
            for (String dependency : dependencies) {
                if (!collectedIndexMap.containsKey(dependency)) {
                    fail("Dependency not collected: " + dependency);
                }
                int dependencyIndex = collectedIndexMap.get(dependency);
                assertTrue(dependencyIndex < dataProductIndex,
                        "Dependency " + dependency + " should be collected before " + dataProduct);
            }
        }

        // Ensure there are no failed data products
        assertTrue(dataManagerResponse.failedDataProducts().isEmpty());
    }

    @Test
    public void testCollectData_EarningsData_ConstraintViolation() throws Exception {
        String requestJson = """
    {
      "requestedDataProducts": {
        "se.iqesolutions.datamanager.product.impl.LastReportedEarningsPerShare": {
          "acceptableMethods": ["REAL_TIME_FEED"],
          "cacheKey": "",
          "maxCacheAgeSeconds": 0,
          "maxCost": 0.001,
          "maxTime": 5.0
        }
      },
      "alreadyAvailableDataProducts": [
        {
          "@class": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
          "symbol": "AAPL"
        }
      ]
    }
    """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);

        // Parse the response
        ObjectMapper objectMapper = new ObjectMapper();
        DataManagerResponse dataManagerResponse = objectMapper.readValue(responseBody, DataManagerResponse.class);

        // The collectedDataProducts should contain only the TickerSymbol
        List<DataProduct> collectedDataProducts = dataManagerResponse.collectedDataProducts();
        assertEquals(1, collectedDataProducts.size());
        assertTrue(collectedDataProducts.get(0) instanceof TickerSymbol);
        TickerSymbol tickerSymbol = (TickerSymbol) collectedDataProducts.get(0);
        assertEquals("AAPL", tickerSymbol.symbol());

        // LastReportedEarningsPerShare should fail due to cost constraint violation
        Map<String, String> failedDataProducts = dataManagerResponse.failedDataProducts();
        assertEquals(1, failedDataProducts.size());
        assertTrue(failedDataProducts.containsKey("se.iqesolutions.datamanager.product.impl.LastReportedEarningsPerShare"));
        assertTrue(failedDataProducts.get("se.iqesolutions.datamanager.product.impl.LastReportedEarningsPerShare").contains("No provider available or dependencies not satisfied"));
    }

    @Test
    public void testCollectData_FinnishSharePrice_Successful() throws Exception {
        String requestJson = """
    {
      "requestedDataProducts": {
        "se.iqesolutions.datamanager.product.impl.RealTimeSharePrice": {
          "acceptableMethods": ["REAL_TIME_FEED"],
          "cacheKey": "",
          "maxCacheAgeSeconds": 0,
          "maxCost": 0.05,
          "maxTime": 5.0
        }
      },
      "alreadyAvailableDataProducts": [
        {
          "@class": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
          "symbol": "NOK"
        }
      ]
    }
    """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);

        // Parse the response
        ObjectMapper objectMapper = new ObjectMapper();
        DataManagerResponse dataManagerResponse = objectMapper.readValue(responseBody, DataManagerResponse.class);

        // Assert that RealTimeSharePrice is collected
        boolean sharePriceCollected = dataManagerResponse.collectedDataProducts().stream()
                .anyMatch(dp -> dp instanceof RealTimeSharePrice);
        assertTrue(sharePriceCollected);

        // Verify that the price matches the Finnish provider's value
        dataManagerResponse.collectedDataProducts().stream()
                .filter(dp -> dp instanceof RealTimeSharePrice)
                .map(dp -> (RealTimeSharePrice) dp)
                .forEach(sharePrice -> {
                    assertEquals("NOK", sharePrice.tickerSymbol());
                    assertEquals(BigDecimal.valueOf(50.0), sharePrice.price());
                });

        // Ensure there are no failed data products
        assertTrue(dataManagerResponse.failedDataProducts().isEmpty());
    }

    @Test
    public void testCollectData_PrioritizeCost() throws Exception {
        String requestJson = """
        {
          "requestedDataProducts": {
            "se.iqesolutions.datamanager.product.impl.RealTimeSharePrice": {
              "acceptableMethods": ["REAL_TIME_FEED"],
              "cacheKey": "",
              "maxCacheAgeSeconds": 0,
              "maxCost": 1.0,
              "maxTime": 10.0,
              "costWeight": 0.9,
              "timeWeight": 0.1
            }
          },
          "alreadyAvailableDataProducts": [
            {
              "@class": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
              "symbol": "NOK"
            },
            {
              "@class": "se.iqesolutions.datamanager.product.impl.Country",
              "name": "Finland"
            }
          ]
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);

        // Parse the response
        ObjectMapper objectMapper = new ObjectMapper();
        DataManagerResponse dataManagerResponse = objectMapper.readValue(responseBody, DataManagerResponse.class);

        // Assert that RealTimeSharePrice is collected
        boolean sharePriceCollected = dataManagerResponse.collectedDataProducts().stream()
                .anyMatch(dp -> dp instanceof RealTimeSharePrice);
        assertTrue(sharePriceCollected);

        // Verify that the price matches the Finnish provider's value
        dataManagerResponse.collectedDataProducts().stream()
                .filter(dp -> dp instanceof RealTimeSharePrice)
                .map(dp -> (RealTimeSharePrice) dp)
                .forEach(sharePrice -> {
                    assertEquals("NOK", sharePrice.tickerSymbol());
                    assertEquals(BigDecimal.valueOf(50.0), sharePrice.price());
                });

        // Ensure there are no failed data products
        assertTrue(dataManagerResponse.failedDataProducts().isEmpty());
    }

    @Test
    public void testCollectData_PrioritizeTime() throws Exception {
        String requestJson = """
        {
          "requestedDataProducts": {
            "se.iqesolutions.datamanager.product.impl.RealTimeSharePrice": {
              "acceptableMethods": ["REAL_TIME_FEED"],
              "cacheKey": "",
              "maxCacheAgeSeconds": 0,
              "maxCost": 1.0,
              "maxTime": 10.0,
              "costWeight": 0.1,
              "timeWeight": 0.9
            }
          },
          "alreadyAvailableDataProducts": [
            {
              "@class": "se.iqesolutions.datamanager.product.impl.TickerSymbol",
              "symbol": "NOK"
            },
            {
              "@class": "se.iqesolutions.datamanager.product.impl.Country",
              "name": "Finland"
            }
          ]
        }
        """;

        // Assuming RealTimeStockDataFeedProvider has lower expectedTime than FinnishStockDataProvider
        // Adjust providers' expected times accordingly in your provider implementations

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/data/collect", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);

        // Parse the response
        ObjectMapper objectMapper = new ObjectMapper();
        DataManagerResponse dataManagerResponse = objectMapper.readValue(responseBody, DataManagerResponse.class);

        // Assert that RealTimeSharePrice is collected
        boolean sharePriceCollected = dataManagerResponse.collectedDataProducts().stream()
                .anyMatch(dp -> dp instanceof RealTimeSharePrice);
        assertTrue(sharePriceCollected);

        // Verify that the price matches the provider with lower time (assuming it's 100.0 from RealTimeStockDataFeedProvider)
        dataManagerResponse.collectedDataProducts().stream()
                .filter(dp -> dp instanceof RealTimeSharePrice)
                .map(dp -> (RealTimeSharePrice) dp)
                .forEach(sharePrice -> {
                    assertEquals("NOK", sharePrice.tickerSymbol());
                    assertEquals(BigDecimal.valueOf(100.0), sharePrice.price());
                });

        // Ensure there are no failed data products
        assertTrue(dataManagerResponse.failedDataProducts().isEmpty());
    }

}