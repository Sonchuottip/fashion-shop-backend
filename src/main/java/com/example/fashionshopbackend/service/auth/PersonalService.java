package com.example.fashionshopbackend.service.auth;


import com.example.fashionshopbackend.dto.auth.UserUpdateRequest;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.entity.auth.UserProfile;
import com.example.fashionshopbackend.repository.UserProfileRepository;
import com.example.fashionshopbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonalService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // ... (các phương thức khác như login, register, logout, v.v.)

    public UserProfile getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ cho người dùng: " + email));
        return profile;
    }

    public UserProfile updateProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ cho người dùng: " + email));

        // Cập nhật các trường thông tin từ UserUpdateRequest
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        // Lưu và trả về hồ sơ đã cập nhật
        return userProfileRepository.save(profile);
    }
}
