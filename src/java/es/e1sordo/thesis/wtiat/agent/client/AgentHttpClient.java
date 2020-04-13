package es.e1sordo.thesis.wtiat.agent.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.e1sordo.thesis.wtiat.agent.configuration.GatewayProperties;
import es.e1sordo.thesis.wtiat.agent.dto.AgentGetDto;
import es.e1sordo.thesis.wtiat.agent.dto.AgentPostDto;
import es.e1sordo.thesis.wtiat.agent.model.GatewayInfo;
import es.e1sordo.thesis.wtiat.agent.model.TimestampMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class AgentHttpClient {

    final Logger logger = LoggerFactory.getLogger(AgentHttpClient.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private final Gson gson = new Gson();


    public void ping() throws IOException, InterruptedException {

        var gateway = GatewayProperties.getActualData();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(String.format("%s://%s:%s/rest/devices/ping", gateway.getProtocol(), gateway.getHost(), gateway.getPort())))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public List<AgentGetDto> sendGet(GatewayInfo gateway) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(String.format("%s://%s:%s/rest/agents", gateway.getProtocol(), gateway.getHost(), gateway.getPort())))
                .build();


        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // print response headers
        HttpHeaders headers = response.headers();
        headers.map().forEach((k, v) -> System.out.println(k + ":" + v));

        // print status code
        System.out.println(response.statusCode());

        // print response jsonBody
        String jsonBody = response.body();
        System.out.println(jsonBody);


        Type type = new TypeToken<List<AgentGetDto>>() {}.getType();
        return gson.fromJson(jsonBody, type);

    }

    public AgentGetDto pingAgent(AgentPostDto body) {
        var gatewayInfo = GatewayProperties.getActualData();

        logger.debug("Protocol: " + gatewayInfo.getProtocol());
        logger.debug("Host: " + gatewayInfo.getHost());
        logger.debug("Port: " + gatewayInfo.getPort());

        String jsonBody = gson.toJson(body);
        logger.info("Request: \n{}", jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .uri(URI.create(String.format("%s://%s:%s/rest/agents",
                        gatewayInfo.getProtocol(), gatewayInfo.getHost(), gatewayInfo.getPort())))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            logger.info("Response: \n{}", responseBody);
            return gson.fromJson(responseBody, AgentGetDto.class);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to send request", e);
            return null;
        }
    }

    public boolean loadMetrics(Collection<TimestampMetric> body, String deviceId) {
        var gatewayInfo = GatewayProperties.getActualData();

        logger.debug("Protocol: " + gatewayInfo.getProtocol());
        logger.debug("Host: " + gatewayInfo.getHost());
        logger.debug("Port: " + gatewayInfo.getPort());

        String jsonBody = gson.toJson(body);
        logger.info("Request: \n{}", jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .uri(URI.create(String.format("%s://%s:%s/rest/metrics/load?deviceId=%s",
                        gatewayInfo.getProtocol(), gatewayInfo.getHost(), gatewayInfo.getPort(), deviceId)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 400;
        } catch (IOException | InterruptedException e) {
            logger.error("Load metrics failed", e);
            return false;
        }
    }

    public File downloadConnector(String currentLoadedConnectorName) {
        var gatewayInfo = GatewayProperties.getActualData();

        logger.debug("Protocol: " + gatewayInfo.getProtocol());
        logger.debug("Host: " + gatewayInfo.getHost());
        logger.debug("Port: " + gatewayInfo.getPort());

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("Content-Type", "application/octet-stream")
                .uri(URI.create(String.format("%s://%s:%s/rest/connectors/%s.jar",
                        gatewayInfo.getProtocol(), gatewayInfo.getHost(), gatewayInfo.getPort(), currentLoadedConnectorName)))
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] responseBody = response.body();

            File tempFile = File.createTempFile("WTiAT-Connector-", ".jar");
            tempFile.deleteOnExit();

            final var path = Paths.get(tempFile.toURI());
            Files.write(path, responseBody);

            return tempFile;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null; // todo: нельзя возвращать null
        }
    }
}
