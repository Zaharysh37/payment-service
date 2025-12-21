package com.innowise.paymentservice.api.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class RandomNumberClient {

    private final RestClient restClient = RestClient.create();

    private static final String EXTERNAL_API_URL =
        "https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new";

    public Integer getRandomNumber() {
        try {
            String response = restClient.get()
                .uri(EXTERNAL_API_URL)
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
