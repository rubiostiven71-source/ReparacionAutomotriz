package com.usta.reparacionautomotriz.models.daos;

import com.usta.reparacionautomotriz.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleDao extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRol(String rol);
}
