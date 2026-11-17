package com.usta.reparacionautomotriz.models.daos;

import com.usta.reparacionautomotriz.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDao extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    // Mecánicos activos para el formulario de reparación (RN-10)
    List<UserEntity> findByRol_RolAndEstadoUsuTrue(String rol);
}
