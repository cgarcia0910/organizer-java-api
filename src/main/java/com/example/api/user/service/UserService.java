package com.example.api.user.service;

import com.example.api.auth.TenantContext;
import com.example.api.user.entity.User;
import com.example.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        Long tenantId = TenantContext.getTenantId();
        return userRepository.findAllByTenantId(tenantId);
    }
}