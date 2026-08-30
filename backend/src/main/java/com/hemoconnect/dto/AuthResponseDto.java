package com.hemoconnect.dto;

/**
 * What /api/auth/signup and /api/auth/login return: a JWT plus the user's
 * safe profile data, exactly the `{ token, user }` shape AuthContext.jsx
 * already expects.
 */
public class AuthResponseDto {

    private String token;
    private UserResponseDto user;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, UserResponseDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}
