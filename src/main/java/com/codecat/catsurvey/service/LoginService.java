package com.codecat.catsurvey.service;

import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.models.User;
import com.codecat.catsurvey.models.UserRole;
import com.codecat.catsurvey.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoginService {
    @Autowired
    private UserRepository userRepository;

    public User login(String userName, String password){
        User user = userRepository.findByUserName(userName).orElseThrow(() ->
                new ValidationException("非法用户名: " + userName)
        );
        return user;
    }

    public List<Role> getRoleList(Integer userId) {
        if (userId == null)
            return null;

        User user = userRepository.findById(userId).orElseThrow(() ->
                new ValidationException("非法userId: " + userId)
        );

        List<UserRole> userRoleList = user.getUserRoleList();
        if (userRoleList == null)
            return new ArrayList<>();

        return userRoleList.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());
    }
}
