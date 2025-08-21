package com.gltkorea.icebang.domain.user.service;

import com.gltkorea.icebang.domain.user.model.UserAccountPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserAuthServiceImpl implements UserAuthService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return UserAccountPrincipal.builder().build();
    }
}
