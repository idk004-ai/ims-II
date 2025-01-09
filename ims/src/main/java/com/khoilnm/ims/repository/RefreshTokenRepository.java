package com.khoilnm.ims.repository;

import com.khoilnm.ims.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    boolean existsByUserEmail(String email);

    @Modifying
    @Query("""
                DELETE FROM RefreshToken rt
                    WHERE rt.user.email = :email
            """)
    int deleteByEmail(@Param("email") String email);

    @Query("SELECT rt FROM RefreshToken rt " +
            "WHERE rt.user.email = :email " +
            "AND rt.token = :token " +
            "AND rt.series = :series")
    Optional<RefreshToken> findByEmailAndTokenAndSeries(
            @Param("email") String email,
            @Param("token") String token,
            @Param("series") String series);

}
