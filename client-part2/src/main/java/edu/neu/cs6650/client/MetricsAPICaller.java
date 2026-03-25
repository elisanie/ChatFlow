package edu.neu.cs6650.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MetricsAPICaller {
    //for switch between local and aws ec2
//    private static final String METRICS_URL = "http://localhost:8080/metrics";

    //alb
    private static final String METRICS_URL = "http://chatflow-alb-308077375.us-west-2.elb.amazonaws.com/metrics";


    public static void call() {
        try {

            //to match api design (time window)
            String url = METRICS_URL + "?startTime=0&endTime=9999999999999";

            // create HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // GET request to the metrics endpoint
            HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

            // send request synchronously and receive res as String
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("=== Metrics API Response ===");

            // status code
            System.out.println("Status Code: " + response.statusCode());

            // only print if request succeeded
            if (response.statusCode() == 200) {
                System.out.println(response.body());
            } else {
                System.err.println("Request failed: " + response.body());
            }

            System.out.println("============================");

        } catch (Exception e) {

            System.err.println("Metrics API call failed: " + e.getMessage());
        }
    }
}


