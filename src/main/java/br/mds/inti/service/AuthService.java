package br.mds.inti.service;

import br.mds.inti.model.dto.auth.LoginRequest;
import br.mds.inti.model.dto.auth.ProfileCreationResponse;
import br.mds.inti.model.dto.auth.RegisterRequest;
import br.mds.inti.model.dto.auth.LoginResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.ProfileAlreadyExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProfileCreationResponse register(RegisterRequest request) {

        try {
            Profile user = new Profile();
            user.setEmail(request.email());
            user.setName(request.name());
            user.setUsername(request.username());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setType(request.type() != null ? request.type() : ProfileType.user);
            user.setFollowersCount(0);
            user.setFollowingCount(0);
            user.setCreatedAt(Instant.now());
            Profile savedUser = profileRepository.save(user);

            String jwt = jwtService.generateToken(user);

            return new ProfileCreationResponse(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    jwt,
                    savedUser.getType(),
                    savedUser.getCreatedAt());

        } catch (DataIntegrityViolationException e) {

            if (isUniqueConstraintViolation(e)) {
                throw new ProfileAlreadyExistsException("Username already in use");
            }

            throw e;
        }
    }

    public LoginResponse login(LoginRequest request) {
        Profile user = profileRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Usuario ou senha invalidos");
        }

        String jwt = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                jwt,
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getType());
    }

    private boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();

        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {

                return cve.getConstraintName() != null &&
                        cve.getConstraintName().equalsIgnoreCase("profiles_username_key");
            }
            cause = cause.getCause();
        }
        return false;
    }

}
