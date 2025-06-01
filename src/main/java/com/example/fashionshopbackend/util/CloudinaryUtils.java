package com.example.fashionshopbackend.util;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CloudinaryUtils {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        cloudinary = new Cloudinary(config);
    }

    public String uploadImage(byte[] imageData, String publicId) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("public_id", publicId);
        params.put("resource_type", "image");
        Map uploadResult = cloudinary.uploader().upload(imageData, params);
        return uploadResult.get("url").toString();
    }

    public void deleteImage(String publicId) throws IOException {
        Map<String, String> params = new HashMap<>();
        cloudinary.uploader().destroy(publicId, params);
    }

    public Cloudinary getCloudinary() {
        return cloudinary;
    }
}