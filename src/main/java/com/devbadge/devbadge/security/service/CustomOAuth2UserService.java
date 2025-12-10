package com.devbadge.devbadge.security.service;

import com.devbadge.devbadge.security.entity.AuthProvider;
import com.devbadge.devbadge.security.entity.Role;
import com.devbadge.devbadge.security.entity.User;
import com.devbadge.devbadge.security.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(req);

        String providerName = req.getClientRegistration().getRegistrationId();

        String email = (String) user.getAttributes().get("email");
        String name = (String) user.getAttributes().get("name");

        AuthProvider provider =
                providerName.equalsIgnoreCase("google")
                        ? AuthProvider.GOOGLE
                        : AuthProvider.GITHUB;
        User appUser = userRepository.findByEmail(email).orElseGet(() ->
                User.builder()
                        .email(email)
                        .name(name)
                        .provider(provider)
                        .role(Role.ROLE_USER)
                        .build()
        );

        userRepository.save(appUser);

        return user;
    }
}