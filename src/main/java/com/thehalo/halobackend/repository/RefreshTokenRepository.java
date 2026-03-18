package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.system.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    boolean existsByTokenAndRevokedFalse(String token);

    /**
     * Returns all sessions that are still active:
     * - not revoked (user hasn't logged out)
     * - expiry date is in the future
     */
    List<RefreshToken> findAllByRevokedFalseAndExpiresAtAfter(LocalDateTime now);

    Optional<RefreshToken> findFirstByUserIdAndRevokedFalseAndExpiresAtAfterOrderByExpiresAtDesc(Long userId, LocalDateTime now);

    // Modifying annotation says that this query is update / delete
    // normally @Query assumes that it is select statement
    @Modifying

    // JPQL Java Persistence Query Language we use entity names instead of table names and :var
    // should be provided in the @Param("var")
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
    void revokeAllUserTokens(@Param("userId") Long userId);
}
