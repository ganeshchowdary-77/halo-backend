package com.thehalo.halobackend.service.auth;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.thehalo.halobackend.dto.auth.request.LoginRequest;
import com.thehalo.halobackend.dto.auth.request.RefreshTokenRequest;
import com.thehalo.halobackend.dto.auth.request.RegisterRequest;
import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.exception.domain.auth.EmailAlreadyRegisteredException;
import com.thehalo.halobackend.exception.domain.auth.UserNotFoundException;
import com.thehalo.halobackend.exception.security.InvalidCredentialsException;
import com.thehalo.halobackend.model.user.AppRole;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.AppRoleRepository;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.security.config.JwtProperties;
import com.thehalo.halobackend.security.service.JwtService;
import com.thehalo.halobackend.security.service.JwtService.TokenValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private AppRoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private AppUser testUser;
    private AppRole influencerRole;

    @BeforeEach
    void setUp() {
        influencerRole = new AppRole(1L, RoleName.INFLUENCER);
        
        testUser = AppUser.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded-password")
                .fullName("John Doe")
                .role(influencerRole)
                .build();
    }

    @Test
    void register_ShouldCreateUser_WhenValidRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .fullName("John Doe")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.INFLUENCER)).thenReturn(Optional.of(influencerRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.getOrCreateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .fullName("John Doe")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void login_ShouldReturnTokens_WhenValidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmailWithRole(request.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.getOrCreateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenInvalidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("WrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        
        TokenValidationResult validationResult = mock(TokenValidationResult.class);
        when(validationResult.isValid()).thenReturn(true);
        
        when(jwtService.validateToken(request.getRefreshToken())).thenReturn(validationResult);
        when(jwtService.isRefreshTokenRevoked(request.getRefreshToken())).thenReturn(false);
        when(jwtService.extractEmail(request.getRefreshToken())).thenReturn(testUser.getEmail());
        when(userRepository.findByEmailWithRole(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(jwtService).revokeRefreshToken(request.getRefreshToken());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenUserNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-token");

        TokenValidationResult validationResult = mock(TokenValidationResult.class);
        when(validationResult.isValid()).thenReturn(true);
        
        when(jwtService.validateToken(request.getRefreshToken())).thenReturn(validationResult);
        when(jwtService.isRefreshTokenRevoked(request.getRefreshToken())).thenReturn(false);
        when(jwtService.extractEmail(request.getRefreshToken())).thenReturn("unknown@example.com");
        when(userRepository.findByEmailWithRole("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UserNotFoundException.class);
    }
}
