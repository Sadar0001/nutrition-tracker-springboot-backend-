package com.nutriflow.diet_tracker.controller;

import com.nutriflow.diet_tracker.entity.User;
import com.nutriflow.diet_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sync")

    public User syncProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody User profileData) {

        String clerkId = jwt.getSubject();
        User user = userRepository.findByClerkId(clerkId).orElse(new User());

        user.setClerkId(clerkId);
        user.setAge(profileData.getAge());
        user.setGender(profileData.getGender());
        user.setWeight(profileData.getWeight());
        user.setHeight(profileData.getHeight());
        user.setBmi(profileData.getBmi());

        return userRepository.save(user);
    }
}