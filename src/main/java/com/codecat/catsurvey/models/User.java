package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.common.valid.function.user.UserNameUnique;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", schema = "codecat")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @Null(message = "id为只读", groups = {validationTime.FullAdd.class, validationTime.Add.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "用户名只能由数字英文组成")
    @UserNameUnique(message = "用户名已存在哦", groups = validationTime.FullAdd.class)
    @Column(name = "user_name", nullable = false)
    private String userName;

    @NotNull(message = "密码不能为空")
    @Length(min = 6, max = 16, message = "密码长度为6-16位", groups = validationTime.FullAdd.class) // 仅在添加时校验
    @Pattern(regexp = "^[A-Za-z0-9.]+$", message = "密码只能由数字英文以及'.'组成", groups = validationTime.FullAdd.class)
    @Column(name = "password", nullable = false)
    private String password;

    @Email(message = "邮件格式错误")
    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "wechat")
    private String wechat;

    @JSONField(serialize = false)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<UserRole> userRoleList = new ArrayList<>();
}
