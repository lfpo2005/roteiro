package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.model.User;
import dev.luisoliveira.roteiro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço para processar usuários OAuth2 do Google
 * Este serviço é usado quando o login é feito pelo navegador web
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        // Extract user information
        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String pictureUrl = oauth2User.getAttribute("picture");

        log.info("Usuário OAuth2 autenticado: {}, {}", name, email);

        // Procurar usuário ou criar novo
        User user = userRepository.findByGoogleId(googleId)
                .orElse(new User());

        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }

        // Atualizar informações
        user.setGoogleId(googleId);
        user.setEmail(email);
        user.setName(name);
        user.setPictureUrl(pictureUrl);

        // Salvar usuário
        userRepository.save(user);

        // Criar atributos para o OAuth2User
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("userId", user.getId().toString());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email");
    }
}