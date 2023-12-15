package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role_permission", schema = "codecat")
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @NotNull
    @Column(name = "role_id", nullable = false)
    private int roleId;

    @NotNull
    @Column(name = "permission_id", nullable = false)
    private int permissionId;

    @JSONField(serialize = false)
    @ToString.Exclude
    @ManyToOne(targetEntity = Role.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Role role;

    @ToString.Exclude
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = Permission.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "permission_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Permission permission;
}
