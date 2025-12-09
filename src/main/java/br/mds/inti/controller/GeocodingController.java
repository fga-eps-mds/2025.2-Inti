package br.mds.inti.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/geo")
public class GeocodingController {

    private final WebClient nominatimWebClient;
    private final String defaultAcceptLanguage;

    public GeocodingController(@Qualifier("nominatimWebClient") WebClient nominatimWebClient,
            @Value("${geocoding.accept-language:pt-BR}") String defaultAcceptLanguage) {
        this.nominatimWebClient = nominatimWebClient;
        this.defaultAcceptLanguage = defaultAcceptLanguage;
    }

    @GetMapping("/reverse")
    public Mono<ResponseEntity<String>> reverse(@RequestParam double lat, @RequestParam double lng,
            @RequestParam(name = "lang", required = false) String languageOverride) {
        String language = (languageOverride == null || languageOverride.isBlank())
                ? defaultAcceptLanguage
                : languageOverride;

        return nominatimWebClient
                .get()
                .uri(uriBuilder -> buildReverseUri(uriBuilder, lat, lng, language))
                .retrieve()
                .toEntity(String.class);
    }

    private java.net.URI buildReverseUri(UriBuilder uriBuilder, double lat, double lng, String language) {
        return uriBuilder
                .path("/reverse")
                .queryParam("format", "jsonv2")
                .queryParam("lat", lat)
                .queryParam("lon", lng)
                .queryParam("addressdetails", 1)
                .queryParam("accept-language", language)
                .build();
    }
}
