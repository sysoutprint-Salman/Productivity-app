package SpringBoot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class HTTPHandler {
    public static int port = PortHandler.getCurrentPort();
    public static void POST(String path, String JSON) {
        try {
            URL url = new URL("http://localhost:" + port + "/");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode() == 200 ?
                    "HTTP: Successfully posted." : "HTTP: Failed to post.");
        } catch (Exception ex) {
            System.out.println("HTTP: An issue arose with POST request.");
            ex.printStackTrace();
        }
    } //Old
    public static void POST(String path, Object object) {
        try {
            String json = Json.MAPPER.writeValueAsString(object);

            URL url = new URL("http://localhost:" + port + "/");

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println(response.statusCode() == 200
                    ? "HTTP: Successfully posted."
                    : "HTTP: Failed to post.");

        } catch (Exception ex) {
            System.out.println("HTTP: An issue arose with POST request.");
            ex.printStackTrace();
        }
    }
    public static <T, R> R POST(String path, T object, Class<R> objectType) {
        try {
            String JSON = Json.MAPPER.writeValueAsString(object);
            URI uri = URI.create("http://localhost:" + port + "/" + path);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("HTTP: POST failed: "  + response.statusCode());
                System.out.println("Response Body: " + response.body());
                return null;
            }
            if (response.body()==null || response.body().isBlank()) {
                System.out.println("HTTP: Empty response received. Returning null.");
                return null;
            }
            return Json.MAPPER.readValue(response.body(), objectType);

        } catch (Exception ex) {
            System.out.println("HTTP: An issue arose with POST request.");
            ex.printStackTrace();
        }
        return null;
    }
    public static void DELETE(String path) {
        String url = "http://localhost:" + port + "/" + path;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Response body: " + response.body());
            } else {System.out.println("Failed to delete.");}
        } catch (IOException | InterruptedException e) {
            System.out.println("HTTP: An issue arose with DELETE request.");
            e.printStackTrace();
        }
    }
    public static <T> List<T> GET (String path, Class<T> objectType){
        try{
            String url = "http://localhost:" + port + "/" + path ;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            JsonNode rootNode = Json.MAPPER.readTree(json);
            if (rootNode.isArray()) {
                return Json.MAPPER.readValue(json, Json.MAPPER.getTypeFactory().constructCollectionType(List.class, objectType));
            } else if (rootNode.isObject()) {
                T singleObject = Json.MAPPER.treeToValue(rootNode, objectType);
                return List.of(singleObject);
            }
        }catch(Exception e){
            System.out.println("HTTP: An issue arose with GET request. (ignorable).");
            e.printStackTrace();
        }   return Collections.emptyList();
    }
    public static boolean GET(String path){
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
    public static void PUT(String JSON, String path) {
        try {
            String url = "http://localhost:" + port + "/" + path;
            HttpRequest request = HttpRequest.newBuilder() //Building the HTTP request
                    .uri(URI.create(url)).header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(JSON)).build(); //Attaches json as the body
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            System.out.println("HTTP: PUT failed.");
            e.printStackTrace();
        }
    } //Old
    public static void PUT(String path, Object object) {
        try {
            String json = Json.MAPPER.writeValueAsString(object);

            String url = "http://localhost:" + port + "/" + path;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

        } catch (InterruptedException | IOException e) {
            System.out.println("HTTP: PUT failed.");
            e.printStackTrace();
        }
    }
    public static void PATCH(String JSON, String path) {
        try {
            String url = "http://localhost:" + port + "/" + path;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(JSON))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            System.out.println("HTTP: PATCH failed");
            e.printStackTrace();
        }
    } //Old
    public static void PATCH(String path) {
        try {
            String url = "http://localhost:" + port + "/" + path;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("PATCH", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (InterruptedException | IOException e) {
            System.out.println("HTTP: PATCH failed");
            e.printStackTrace();
        }
    }
    public static void PATCH(List<Map<String, Object>> jsonList, String path) {
        try {
            String json = Json.MAPPER.writeValueAsString(jsonList);
            PATCH(json, path);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build batch PATCH JSON", e);
        }
    }

}
