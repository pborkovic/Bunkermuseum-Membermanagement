package com.bunkermuseum.membermanagement.lib.helper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Tracks login attempts for a user and determines whether the user should be locked out
 * after a certain number of failed attempts.
 *
 * <p>This class is designed to help prevent brute-force login attacks by:
 * <ul>
 *   <li>Counting consecutive failed login attempts</li>
 *   <li>Locking the user out after exceeding a maximum number of attempts</li>
 *   <li>Automatically unlocking after a defined lockout duration</li>
 * </ul>
 *
 * <p>The lockout state is stored in-memory, so the lifetime of the tracking
 * depends on the lifetime of the instance managing it.</p>
 *
 * <p>This class is thread-safe and uses synchronized methods to ensure
 * correct behavior in concurrent environments.</p>
 */
public final class LoginAttemptTracker {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private volatile int failedAttempts = 0;
    private volatile Instant lockedOutUntil = null;
    private volatile Instant lastAttempt = Instant.now();

    /**
     * Resets all tracking values including failed attempts and lockout state.
     * <p>Call this after a successful login or after a lockout period expires.</p>
     *
     * @author Philipp Borkovic
     */
    public synchronized void reset() {
        failedAttempts = 0;
        lockedOutUntil = null;
        lastAttempt = Instant.now();
    }

    /**
     * Returns the timestamp of the most recent login attempt.
     *
     * @return the {@link Instant} representing the last login attempt time
     *
     * @author Philipp Borkovic
     */
    public synchronized Instant getLastAttempt() {
        return lastAttempt;
    }

    /**
     * Increments the failed login attempt counter and updates the last attempt timestamp.
     *
     * <p>If the number of failed attempts reaches or exceeds {@link #MAX_LOGIN_ATTEMPTS},
     * this method will set the user into a locked state for {@link #LOCKOUT_DURATION_MINUTES}
     * minutes.</p>
     *
     * @author Philipp Borkovic
     */
    public synchronized void incrementFailedAttempts() {
        failedAttempts++;
        lastAttempt = Instant.now();

        if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
            lockedOutUntil = Instant.now().plus(LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES);
        }
    }

    /**
     * Determines whether the user is currently locked out due to too many failed login attempts.
     *
     * <p>If the lockout period has expired, this method will automatically reset the state,
     * clearing the lockout and resetting the failed attempt counter.</p>
     *
     * @return {@code true} if the user is still within the lockout period,
     *         {@code false} otherwise
     *
     * @author Philipp Borkovic
     */
    public synchronized boolean isLocked() {
        if (lockedOutUntil != null && Instant.now().isBefore(lockedOutUntil)) {
            return true;
        }

        if (lockedOutUntil != null && Instant.now().isAfter(lockedOutUntil)) {
            reset();
        }

        return false;
    }

}
