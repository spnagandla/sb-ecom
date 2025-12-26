package com.buyology.backend.utils;


import com.buyology.backend.Controller.CategoryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class SupabaseStorageClient {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String bucket;
    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageClient.class);

    public SupabaseStorageClient(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.serviceKey}") String serviceKey,
            @Value("${supabase.bucket}") String bucket
    ){
            this.supabaseUrl = supabaseUrl;
            this.bucket = bucket;

            this.webClient = WebClient.builder()
                    .baseUrl(supabaseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceKey)
                    .defaultHeader("apiKey", serviceKey) //supabase has two layers of security (Supabase Storage needs apikey to identify the project and Authorization to authorize the action)
                    .build();
        log.info("SupabaseStorageClient initialized successfully");
    }


    public void upload(String objectPath, byte[] bytes, String contentType) {
        try{
        webClient.put()
                .uri("/storage/v1/object/{bucket}/{path}", bucket, objectPath)
                .contentType(MediaType.parseMediaType(contentType))
                .bodyValue(bytes)
                .retrieve()
                .toBodilessEntity()
                .block();
        log.info("Upload completed successfully for objectPath={}", objectPath);
    } catch(Exception e){
            log.error("Supabase upload failed for objectPath={}", objectPath, e);
            throw e;
        }
    }

    // For private buckets you won't use this for viewing, but you can still store path in DB.
    public String publicUrl(String objectPath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

}
