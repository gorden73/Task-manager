package main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final String API_KEY;
    HttpClient client;
    URI uri;

    public KVTaskClient(URI uri) throws IOException, InterruptedException {
        this.uri = uri;
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri + "/register"))
                .build();
        client = HttpClient.newHttpClient();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        API_KEY = client.send(request, handler).body();
    }

    protected void put(String key, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(uri + "/save/" + key + "?" + "API_KEY=" + API_KEY))
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        client.send(request, handler).body();
    }

    protected String load(String key) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri + "/load/" + key + "?" + "API_KEY=" + API_KEY))
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, handler);
        return response.body();
    }
}
