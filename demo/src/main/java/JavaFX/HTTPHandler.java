package JavaFX;

import SpringBoot.PortHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class HTTPHandler {
    public int port = PortHandler.getCurrentPort();
    public void POST(String path, String JSON) {
        try {
            URL url = new URL("http://localhost:" + port + "/");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode() == 200 ? "Successfully posted." : "Failed to post.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void DELETE(Long id, String path, String archive) {
        String url = "http://localhost:" + port + "/" + path + "/" + id + "?archive=".concat(archive);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Response body: " + response.body());
            } else {System.out.println("Failed to delete.");}
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public <T> List<T> GET (String path, Class<T> objectType){
        try{
            String url = "http://localhost:" + port + "/" + path ;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            JsonNode rootNode = mapper.readTree(json);
            if (rootNode.isArray()) {
                return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, objectType));
            } else if (rootNode.isObject()) {
                T singleObject = mapper.treeToValue(rootNode, objectType);
                return List.of(singleObject);
            }
        }catch(IOException | InterruptedException e){
            System.out.println("HTTP: An issue arose with GETTING (ignorable).");
        }   return Collections.emptyList();
    }
    public boolean GET(String path){
        try {
            String url = "http://localhost:" + port + "/" + path ;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Boolean.parseBoolean(response.body().trim());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("HTTP: Mismatch of inputs.");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("HTTP: Session has been interrupted.");
        }
        return false;
    }
    public void UPDATE(String JSON, String path) {
        try {
            String url = "http://localhost:" + port + "/" + path;
            HttpRequest request = HttpRequest.newBuilder() //Building the HTTP request
                    .uri(URI.create(url)).header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(JSON)).build(); //Attaches json as the body
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
