package br.mds.inti.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.mds.inti.auth.dto.LoginRequest;
import br.mds.inti.auth.dto.ProfileResponse;
import br.mds.inti.auth.dto.RegisterRequest;
import br.mds.inti.models.Profile;
import br.mds.inti.models.ENUM.ProfileType;
import br.mds.inti.repositories.ProfileRepository;

@Service
public class AuthService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProfileResponse register(RegisterRequest request) {
        Profile user = new Profile();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setType(request.type() != null ? request.type() : ProfileType.user);
        user.setCreatedAt(Instant.now());
        profileRepository.save(user);

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getType(),
                user.getCreatedAt());
    }

    public String login(LoginRequest request) {
        Profile user = profileRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Usuario ou senha invalidos");
        }

        return jwtService.generateToken(user.getEmail());
    }

}
