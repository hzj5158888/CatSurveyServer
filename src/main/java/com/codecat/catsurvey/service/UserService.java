package com.codecat.catsurvey.service;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.codecat.catsurvey.commcon.models.Permission;
import com.codecat.catsurvey.commcon.models.Role;
import com.codecat.catsurvey.commcon.models.User;
import com.codecat.catsurvey.commcon.models.UserRole;
import com.codecat.catsurvey.commcon.repository.RoleRepository;
import com.codecat.catsurvey.commcon.repository.UserRepository;
import com.codecat.catsurvey.commcon.repository.UserRoleRepository;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleSerivce roleSerivce;

    public static Map<String, Object> filter(User user) {
        Map<String, Object> ans = Util.objectToMap(user);
        ans.remove("userRoleList");
        ans.remove("password");
        ans.remove("id");
        ans.put("userId", user.getId());

        return ans;
    }

    public static List<Map<String, Object>> filter(List<User> users) {
        List<Map<String, Object>> ans = new ArrayList<>();
        for (User user : users) {
            ans.add(filter(user));
        }

        return ans;
    }

    public SaTokenInfo getToken(String userName, String password) {
        User user = userRepository.findByUserName(userName).orElseThrow(() ->
                new ValidationException("非法userName: " + userName)
        );

        if (!user.getUserName().equals(userName)) // 区分大小写
            return null;
        if (!user.getPassword().equals(password))
            return null;

        StpUtil.login(user.getId());
        return StpUtil.getTokenInfo();
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

    public boolean isLoginId(Integer userId) {
        if (userId == null || !StpUtil.isLogin())
            return false;

        Integer loginId = Integer.parseInt((String) StpUtil.getLoginId());
        return userId.equals(loginId);
    }

    public Integer getLoginId() {
        if (!StpUtil.isLogin())
            return null;

        return Integer.parseInt((String) StpUtil.getLoginId());
    }

    public boolean containsPermissionName(String permissionName) {
        if (permissionName == null || !StpUtil.isLogin())
            return false;

        Set<String> permissionNameSet = new HashSet<>(StpUtil.getPermissionList());
        return permissionNameSet.contains(permissionName);
    }

    public boolean containsRoleName(String roleName) {
        if (roleName == null || !StpUtil.isLogin())
            return false;

        Set<String> roleNameSet = new HashSet<>(StpUtil.getRoleList());
        return roleNameSet.contains(roleName);
    }

    public Set<Permission> getAllPermission(Integer userId) {
        if (userId == null)
            return null;

        List<Role> roleList = getRoleList(userId);
        Set<Permission> permissionSet = new HashSet<>();
        for (Role role : roleList) {
            permissionSet.addAll(
                    roleSerivce.getPermissionList(role.getId())
            );
        }

        return permissionSet;
    }

    public Set<String> getAllPermissionName(Integer userId) {
        Set<Permission> permissionSet = getAllPermission(userId);

        Set<String> permissionName = new HashSet<>();
        for (Permission permission : permissionSet) {
            permissionName.add(permission.getName());
        }

        return permissionName;
    }

    public boolean addRole(Integer userId, String roleName) {
        if (userId == null || !userRepository.existsById(userId)) {
            System.out.println("UserService addRole: unavailable userId: " + userId);
            return false;
        }

        Set<Integer> roleSet = new HashSet<>();
        List<Role> curRole = getRoleList(userId);
        for (Role role : curRole) {
            roleSet.add(role.getId());
        }

        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            System.out.println("UserService setRoleAll: unavailable roleName:" + roleName);
            return false;
        } else if (roleSet.contains(roleOpt.get().getId()))
            return true;

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleOpt.get().getId());
        userRoleRepository.saveAndFlush(userRole);

        return true;
    }

    private List<UserRole> toUserRoleList(List<Integer> roleIdList, Integer userId) {
        List<UserRole> ans = new ArrayList<>();
        for (Integer roleId : roleIdList) {
            UserRole cur = new UserRole();
            cur.setUserId(userId);
            cur.setRoleId(roleId);

            ans.add(cur);
        }

        return ans;
    }

    public boolean addRoleAll(Integer userId, List<String> roleNameList) {
        if (userId == null || !userRepository.existsById(userId)) {
            System.out.println("UserService addRoleAll: unavailable userId: " + userId);
            return false;
        }

        Set<Integer> roleSet = new HashSet<>();
        List<Role> curRole = getRoleList(userId);
        for (Role role : curRole) {
            roleSet.add(role.getId());
        }

        userRoleRepository.deleteAllByUserId(userId);

        for (String roleName : roleNameList) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("UserService addRoleAll: unavailable roleName:" + roleName);
                return false;
            }

            roleSet.add(roleOpt.get().getId());
        }

        List<UserRole> userRoleList = toUserRoleList(roleSet.stream().toList(), userId);
        userRoleRepository.saveAllAndFlush(userRoleList);

        return true;
    }

    public boolean setRoleAll(Integer userId, List<String> roleNameList) {
        if (userId == null || !userRepository.existsById(userId)) {
            System.out.println("UserService setRoleAll: unavailable userId: " + userId);
            return false;
        }

        userRoleRepository.deleteAllByUserId(userId);

        Set<Integer> roleSet = new HashSet<>();
        for (String roleName : roleNameList) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("UserService setRoleAll: unavailable roleName:" + roleName);
                return false;
            }

            roleSet.add(roleOpt.get().getId());
        }

        List<UserRole> userRoleList = toUserRoleList(roleSet.stream().toList(), userId);
        userRoleRepository.saveAllAndFlush(userRoleList);

        return true;
    }

    public boolean delRoleAll(Integer userId, List<String> roleNameList) {
        if (userId == null || !userRepository.existsById(userId)) {
            System.out.println("UserService delRoleAll: unavailable userId: " + userId);
            return false;
        }

        Set<Integer> roleSet = new HashSet<>();
        List<Role> curRole = getRoleList(userId);
        for (Role role : curRole) {
            roleSet.add(role.getId());
        }

        userRoleRepository.deleteAllByUserId(userId);

        for (String roleName : roleNameList) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("UserService setRoleAll: unavailable roleName:" + roleName);
                return false;
            } else if (!roleSet.contains(roleOpt.get().getId())) {
                System.out.println("UserService delRoleAll: unable to del role:" + roleName + ", not exits in this user");
                return false;
            } else if (roleName.equals("User")) {
                System.out.println("UserService delRoleAll: unable to del role: User, This is base role");
                return false;
            }

            roleSet.remove(roleOpt.get().getId());
        }

        List<UserRole> userRoleList = toUserRoleList(roleSet.stream().toList(), userId);
        userRoleRepository.saveAllAndFlush(userRoleList);

        return true;
    }

    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid User user) {}
}
