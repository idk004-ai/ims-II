package com.khoilnm.ims.repository;

import com.khoilnm.ims.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @Modifying
    @Query("""
                DELETE FROM RefreshToken rt
                    WHERE rt.email = :email
            """)
    int deleteByEmail(@Param("email") String email);

    @Query("""
                SELECT rt
                    FROM RefreshToken rt
                    WHERE rt.email = :email
            """)
    List<RefreshToken> findAllByEmail(String email);

    @Query("""
                SELECT rt
                    FROM RefreshToken rt
                    WHERE rt.token = :token
            """)
    Optional<RefreshToken> findByToken(String token);

    @Query("""
                SELECT rt
                    FROM RefreshToken rt
                    WHERE rt.email = :email
                    AND rt.isRevoke = false
                    ORDER BY
                    CASE WHEN :orderCreatedDateDesc = true THEN rt.createdDate END DESC,
                    CASE WHEN :orderCreatedDateDesc = false THEN rt.createdDate END ASC
            """)
    List<RefreshToken> findAllByEmailAndRevokeFalseOrderByCreatedDateDesc(String email, boolean orderCreatedDateDesc);

    @Modifying
    @Query("""
                DELETE FROM RefreshToken rt
                    WHERE rt.isRevoke = true
                    AND rt.revokedAt < :thirtyDaysAgo
            """)
    int deleteByRevokeTrueAndRevokedAtBefore(Date thirtyDaysAgo);

    @Modifying
    @Query("""
                DELETE FROM RefreshToken rt
                    WHERE rt.expiryDate < :date
            """)
    int deleteByExpiryDateBefore(Date date);
}
