package com.innowise.paymentservice.api.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class RandomNumberClient {

    private final RestClient restClient = RestClient.create();

    @Value("${random.api.url}")
    private String externalApiUrl;

    public Integer getRandomNumber() {
        try {
            String response = restClient.get()
                .uri(externalApiUrl)
                .header("User-Agent", "PaymentService/1.0")
                .retrieve()
                .body(String.class);

            if (response != null) {
                return Integer.parseInt(response.trim());
            }
        } catch (Exception e) {
            log.error("External API failed, using fallback random: {}", e.getMessage());
        }

        return 0;
    }
}
