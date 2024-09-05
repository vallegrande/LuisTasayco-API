package pe.edu.vallegrande.traductor.rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.traductor.model.TranslateRequestBody;
import pe.edu.vallegrande.traductor.service.TraductorService;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:5153")
@RequestMapping("/api")
public class traductorRest {

    private final TraductorService traductorService;

    @Autowired
    public traductorRest(TraductorService traductorService) {
        this.traductorService = traductorService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> translateText(@RequestBody TranslateRequestBody requestBody) {
        String text = requestBody.getText();
        String from = requestBody.getFrom();
        String to = requestBody.getTo();
        
        return traductorService.translateText(text, from, to)
            .map(translatedText -> ResponseEntity.status(HttpStatus.OK).body(translatedText))
            .onErrorResume(error -> {
                log.error("Error translating text: {}", error.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error translating text"));
            });
    }
}
