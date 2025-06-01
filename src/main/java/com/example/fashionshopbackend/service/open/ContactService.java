package com.example.fashionshopbackend.service.open;

import com.example.fashionshopbackend.dto.common.ContactRequest;
import com.example.fashionshopbackend.dto.common.ContactResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    @Autowired
    private JavaMailSender mailSender;

    public ContactResponse sendContact(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@fashionshop.com"); // Email của admin
        message.setSubject(request.getSubject());
        message.setText("From: " + request.getName() + " (" + request.getEmail() + ")\n\n" + request.getMessage());
        mailSender.send(message);

        ContactResponse response = new ContactResponse();
        response.setMessage("Contact request sent successfully");
        return response;
    }
}