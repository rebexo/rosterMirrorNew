package org.roster.backend.application.port.in;

import org.roster.backend.domain.User;

/**
 * Interface defining the use cases for authentication services.
 *
 * This interface provides methods for user registration and login,
 * operating as the contract between the application core and
 * external systems or adapters responsible for authentication processes.
 *
 * Core Responsibilities:
 * - Managing user registration by creating and persisting new user accounts.
 * - Facilitating user login and generating tokens for authenticated sessions.
 */
public interface iAuthService {
    User register(String username, String password);

    String login(String username, String password);
}
