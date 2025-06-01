package com.example.fashionshopbackend.controller.open;

import com.example.fashionshopbackend.dto.common.ContactRequest;
import com.example.fashionshopbackend.dto.common.ContactResponse;
import com.example.fashionshopbackend.service.open.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> sendContact(@Valid @RequestBody ContactRequest request) {
        try {
            ContactResponse response = contactService.sendContact(request);
            logger.info("Contact request sent from: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to send contact request: {}", e.getMessage());
            ContactResponse response = new ContactResponse();
            response.setMessage("Failed to send contact request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}