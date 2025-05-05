package com.example.fashionshopbackend.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    private Integer categoryId;
    @NotBlank
    private String name;
    private String description;
}