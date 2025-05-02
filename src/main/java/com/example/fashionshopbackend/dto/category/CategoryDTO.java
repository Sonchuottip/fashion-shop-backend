package com.example.fashionshopbackend.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    @NotBlank
    private Integer categoryId;
    private String name;
    private String description;
}