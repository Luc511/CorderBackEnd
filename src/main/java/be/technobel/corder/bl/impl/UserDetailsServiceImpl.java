package be.technobel.corder.bl.impl;

import be.technobel.corder.dl.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation of the {@link UserDetailsService} interface.
 * It is responsible for loading user details from the UserRepository based on the given username.
 * <p>
 * The UserDetailsServiceImpl class requires an instance of the UserRepository class
 * to retrieve user details from the underlying data source.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load the user details for the given username.
     *
     * @param username the username for which the user details should be loaded
     * @return the UserDetailsService object that implements the UserDetailsService interface
     * @throws UsernameNotFoundException if the username is not found in the userRepository
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Login not found"));
    }
}
