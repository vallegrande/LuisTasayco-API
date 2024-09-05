package pe.edu.vallegrande.traductor.service;

import java.io.IOException;
import org.springframework.stereotype.Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TraductorService {
     private static final String TRANSLATOR_KEY = "7d7ae4478ce24198981736ff43dfa0e8"; 
    private static final String TRANSLATOR_LOCATION = "westus";
    private static final String TRANSLATOR_ENDPOINT = "https://api.cognitive.microsofttranslator.com/";
    private static final String TRANSLATOR_ROUTE = "/translate?api-version=3.0";
    private static final String TRANSLATOR_URL = TRANSLATOR_ENDPOINT.concat(TRANSLATOR_ROUTE);

    private final OkHttpClient client;

    public TraductorService(){
        this.client = new OkHttpClient();
    }

     public Mono<String> translateText(String text, String from, String to) {
        return Mono.fromCallable(() -> {
            MediaType mediaType = MediaType.parse("application/json");
            String requestBody = "[{\"Text\": \"" + text + "\"}]";
            String urlWithParams = TRANSLATOR_URL + "&from=" + from + "&to=" + to;
            RequestBody body = RequestBody.create(mediaType, requestBody);
            Request request = new Request.Builder()
                .url(urlWithParams)
                .post(body)
                .addHeader("Ocp-Apim-Subscription-Key", TRANSLATOR_KEY)
                .addHeader("Ocp-Apim-Subscription-Region", TRANSLATOR_LOCATION)
                .addHeader("Content-type", "application/json")
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return extractTranslatedText(responseBody);
                } else {
                    throw new IOException("Unexpected response code: " + response);
                }
            }
        }).onErrorMap(error -> {
            log.error("Error translating text: {}", error.getMessage());
            return new IOException("Error translating text", error);
        });
    }

    private String extractTranslatedText(String responseBody) {
        JsonParser parser = new JsonParser();
        JsonArray translationsArray = parser.parse(responseBody).getAsJsonArray();
        if (translationsArray.size() > 0) {
            JsonArray translations = translationsArray.get(0).getAsJsonObject().getAsJsonArray("translations");
            if (translations.size() > 0) {
                JsonElement translation = translations.get(0);
                return translation.getAsJsonObject().get("text").getAsString();
            }
        }
        return null;
    }

}
