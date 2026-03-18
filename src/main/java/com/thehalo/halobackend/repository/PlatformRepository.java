package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.platform.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
}
