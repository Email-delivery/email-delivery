package az.aladdin.emaildelivery.config.security;

import az.aladdin.emaildelivery.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    @Override
    public AdminUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminUserRepository.findByEmailIgnoreCase(username)
                .map(AdminUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found: " + username));
    }
}
