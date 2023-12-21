package com.codecat.catsurvey.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Permission;
import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.models.User;
import com.codecat.catsurvey.models.UserRole;
import com.codecat.catsurvey.repository.RoleRepository;
import com.codecat.catsurvey.repository.UserRepository;
import com.codecat.catsurvey.repository.UserRoleRepository;
import com.codecat.catsurvey.utils.MD5Util;
import com.codecat.catsurvey.utils.Util;
import com.codecat.catsurvey.common.valid.group.validationTime;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                new CatValidationException("非法userName: " + userName)
        );

        if (!user.getUserName().equals(userName)) // 区分大小写
            return null;
        if (!user.getPassword().equals(password))
            return null;

        StpUtil.login(user.getId());
        return StpUtil.getTokenInfo();
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

    @Transactional
    public void add(User user) {
        userRepository.saveAndFlush(user);
        addRole(user.getId(), "User");
    }

    public Boolean existsByUserName(String userName){
        return userRepository.existsByUserName(userName);
    }

    public User update(User user){
        return userRepository.saveAndFlush(user);
    }

    @Transactional
    public void del(Integer userId) {
        if (userId == null || !userRepository.existsById(userId))
            throw new CatValidationException("用户不存在");

        userRepository.deleteById(userId);
    }

    @Transactional
    public void modify(Integer userId, JSONObject user) {
        User userOld = userRepository.findById(userId).orElseThrow(() ->
                new CatValidationException("非法userId: " + userId)
        );

        boolean needLogout = false;
        Set<String> continueItem = new HashSet<>() {{
            add("id");
            add("userRoleList");
            add("oldPassword");
        }};
        Set<String> userFiled = Util.getObjectFiledName(userOld);
        Map<String, Object> userMap = Util.objectToMap(userOld);
        for (Map.Entry<String, Object> entry : user.entrySet()) {
            if (entry.getValue() == null
                    || continueItem.contains(entry.getKey())
                    || !userFiled.contains(entry.getKey()))
                continue;

            if (entry.getKey().equals("password")) {
                String password = (String) user.get("password");
                if (password == null || password.length() < 6 || password.length() > 16)
                    throw new CatValidationException("密码长度为6-16位");
                if (!password.matches("^[A-Za-z0-9.]+$"))
                    throw new CatValidationException("密码只能由数字英文以及'.'组成");

                entry.setValue(MD5Util.getMD5(password));
                needLogout = true;
            }

            userMap.put(entry.getKey(), entry.getValue());
        }

        User userFinal = Util.mapToObject(userMap, User.class);
        checkFullUpdate(userFinal);
        userRepository.saveAndFlush(userFinal);
        if (needLogout)
            StpUtil.logout();
    }

    @Transactional
    public User getById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new CatValidationException("用户不存在")
        );
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    public boolean existsById(Integer userId) {
        return userRepository.existsById(userId);
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

        List<Role> roleList = getAllRole(userId);
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

    @Transactional
    public void addRole(Integer userId, String roleName) {
        if (userId == null || !userRepository.existsById(userId))
            throw new CatValidationException("用户不存在");

        Set<Integer> roleSet = new HashSet<>();
        List<Role> curRole = getAllRole(userId);
        for (Role role : curRole) {
            roleSet.add(role.getId());
        }

        Role role = roleRepository.findByName(roleName).orElseThrow(() ->
                new CatValidationException("角色名错误: " + roleName)
        );
        if (roleSet.contains(role.getId()))
            return;

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleRepository.saveAndFlush(userRole);
    }

    public List<Role> getAllRole(Integer userId) {
        if (userId == null)
            return null;

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CatValidationException("非法userId: " + userId)
        );

        List<UserRole> userRoleList = user.getUserRoleList();
        if (userRoleList == null)
            return new ArrayList<>();

        return userRoleList.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());
    }

    public List<String> getAllRoleName(Integer userId) {
        return getAllRole(userId).stream()
                .map(Role::getName)
                .collect(Collectors.toList());
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

    @Transactional
    public void addRoleAll(Integer userId, List<String> roleNameList) {
        if (userId == null || !userRepository.existsById(userId))
            throw new CatValidationException("用户不存在");

        Set<Integer> roleSet = new HashSet<>();
        List<Role> curRole = getAllRole(userId);
        for (Role role : curRole) {
            roleSet.add(role.getId());
        }

        userRoleRepository.deleteAllByUserId(userId);

        for (String roleName : roleNameList) {
            Role role = roleRepository.findByName(roleName).orElseThrow(() ->
                    new CatValidationException("角色名错误: " + roleName)
            );

            roleSet.add(role.getId());
        }

        List<UserRole> userRoleList = toUserRoleList(roleSet.stream().toList(), userId);
        userRoleRepository.saveAllAndFlush(userRoleList);
    }

    @Transactional
    public void setRoleAll(Integer userId, List<String> roleNameList) {
        if (userId == null || !userRepository.existsById(userId))
            throw new CatValidationException("用户不存在");


        userRoleRepository.deleteAllByUserId(userId);

        roleNameList.add("User");
        Set<Integer> roleSet = new HashSet<>();
        for (String roleName : roleNameList) {
            Role role = roleRepository.findByName(roleName).orElseThrow(() ->
                    new CatValidationException("角色名错误: " + roleName)
            );

            roleSet.add(role.getId());
        }

        List<UserRole> userRoleList = toUserRoleList(roleSet.stream().toList(), userId);
        userRoleRepository.saveAllAndFlush(userRoleList);
    }

    @Transactional
    public void delRoleAll(Integer userId, List<String> roleNameList) {
        if (userId == null || !userRepository.existsById(userId))
            throw new CatValidationException("用户不存在");

        Set<Integer> roleSet = new HashSet<>();
        List<Role> curRole = getAllRole(userId);
        for (Role role : curRole) {
            roleSet.add(role.getId());
        }

        userRoleRepository.deleteAllByUserId(userId);

        for (String roleName : roleNameList) {
            Role role = roleRepository.findByName(roleName).orElseThrow(() ->
                    new CatValidationException("角色名错误: " + roleName)
            );

            if (!roleSet.contains(role.getId()))
                throw new CatValidationException("无法删除角色: " + roleName + "、该用户不存在该角色");
            if (roleName.equals("User"))
                throw new CatValidationException("无法删除User角色，此为基本角色");

            roleSet.remove(role.getId());
        }

        List<UserRole> userRoleList = toUserRoleList(roleSet.stream().toList(), userId);
        userRoleRepository.saveAllAndFlush(userRoleList);
    }

    public boolean isAdmin(Integer userId) {
        List<String> roleList = getAllRoleName(userId);
        for (String roleName : roleList)
            if (roleName.contains("Admin"))
                return true;

        return false;
    }

    public String getPassword(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new CatValidationException("用户不存在")
        );

        return user.getPassword();
    }

    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid User user) {}
}
