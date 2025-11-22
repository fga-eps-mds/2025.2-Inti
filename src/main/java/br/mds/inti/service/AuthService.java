package br.mds.inti.service;

import br.mds.inti.model.dto.auth.LoginRequest;
import br.mds.inti.model.dto.auth.ProfileCreationResponse;
import br.mds.inti.model.dto.auth.RegisterRequest;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    }

    public String login(LoginRequest request) {
        Profile user = profileRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Usuario ou senha invalidos");
        }

        return jwtService.generateToken(user);
    }

}
