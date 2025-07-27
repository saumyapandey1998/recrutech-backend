package com.recrutech.recrutechauth.repository;

import com.recrutech.recrutechauth.model.RefreshToken;
import com.recrutech.recrutechauth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for refresh tokens.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its token value.
     *
     * @param token the token value
     * @return the refresh token
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Finds a refresh token by its token ID.
     *
     * @param tokenId the token ID
     * @return the refresh token
     */
    Optional<RefreshToken> findByTokenId(String tokenId);

    /**
     * Finds all refresh tokens for a user.
     *
     * @param user the user
     * @return the list of refresh tokens
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Deletes all expired tokens.
     *
     * @param now the current time
     * @return the number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < ?1")
    int deleteAllExpiredTokens(Instant now);

    /**
     * Deletes all tokens for a user.
     *
     * @param user the user
     * @return the number of deleted tokens
     */
    int deleteByUser(User user);

    /**
     * Finds all expired tokens.
     *
     * @param now the current time
     * @return the list of expired tokens
     */
    List<RefreshToken> findByExpiryDateLessThan(Instant now);

    /**
     * Finds all revoked tokens.
     *
     * @return the list of revoked tokens
     */
    List<RefreshToken> findByRevokedTrue();
}