package br.mds.inti.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class GeocodingControllerTest {

    private GeocodingController geocodingController;
    private final AtomicReference<URI> capturedUri = new AtomicReference<>();
    private String responseBody;

    @BeforeEach
    void setUp() {
        responseBody = "{}";
        ExchangeFunction exchangeFunction = request -> {
            capturedUri.set(request.url());
            ClientResponse response = ClientResponse
                    .create(HttpStatus.OK)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .body(responseBody)
                    .build();
            return Mono.just(response);
        };

        WebClient webClient = WebClient.builder()
                .baseUrl("https://nominatim.test")
                .exchangeFunction(exchangeFunction)
                .build();

        geocodingController = new GeocodingController(webClient, "pt-BR");
    }

    @Test
    void reverse_shouldReturnBodyFromExternalService() {
        responseBody = "{\"mock\":true}";

        ResponseEntity<String> response = geocodingController.reverse(-15.8, -47.9, null).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("mock");
    }

    @Test
    void reverse_withLanguageOverride_shouldIncludeCustomLanguageInQuery() {
        geocodingController.reverse(10.0, 20.0, "en-US").block();

        URI builtUri = capturedUri.get();
        assertThat(builtUri).isNotNull();
        assertThat(builtUri.getQuery()).contains("accept-language=en-US");
        assertThat(builtUri.getQuery()).contains("lat=10.0");
        assertThat(builtUri.getQuery()).contains("lon=20.0");
    }
}
