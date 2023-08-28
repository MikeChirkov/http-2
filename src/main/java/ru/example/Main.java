package ru.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=";
    public static final String NASA_KEY = "DEMO_KEY";

    public static void main(String[] args) {
        try {
            CloseableHttpResponse response = execute(REMOTE_SERVICE_URI + NASA_KEY);
            ObjectMapper mapper = new ObjectMapper();
            Nasa nasaResponse = mapper.readValue(
                    response.getEntity().getContent(),
                    new TypeReference<>() {
                    });

            System.out.println(nasaResponse.toString());
            getImage(nasaResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getImage(Nasa nasa) {
        String[] splitFileName = nasa.getUrl().split("/");
        String fileName = splitFileName[splitFileName.length - 1];
        try (FileOutputStream fos = new FileOutputStream(fileName);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            CloseableHttpResponse response = execute(nasa.getUrl());
            byte[] image = response.getEntity().getContent().readAllBytes();
            bos.write(image);
            bos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static CloseableHttpResponse execute(String url) {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setUserAgent("My Test Service")
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}