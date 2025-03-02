package com.assessment.speernotes.service;

import com.assessment.speernotes.exceptions.AuthenticationException;
import com.assessment.speernotes.exceptions.UserException;
import com.assessment.speernotes.model.User;
import com.assessment.speernotes.model.dto.UserAuthDto;
import com.assessment.speernotes.repository.UsersRepository;
import com.assessment.speernotes.utils.JwtUtil;
import com.assessment.speernotes.utils.ConvertorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UsersService {
    private final UsersRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertorUtil convertorUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository userRepository, JwtUtil jwtUtil, ConvertorUtil convertorUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.convertorUtil = convertorUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * This method is used to return the authenticated user.
     *
     * @return User user
     */
    public User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email).orElseThrow(() -> new UserException(email));
        } else {
            throw new AuthenticationException();
        }
    }

    /**
     * This method is used to find the user by email.
     * It returns the Optional<User>, so if the email is present it will contain the user otherwise it will be null
     *
     * @param email
     * @return Optional<User> user
     */
    public User findUserByEmail(String email) {
        log.info("Find the user by the email {}", email);
        return userRepository.findByEmail(email).orElseThrow(() -> new UserException(email));
    }

    /**
     * This method is used to verify whether the user is present or not.
     * It will search by the email and if it is present it will return true, otherwise false is returned.
     *
     * @param email
     * @return boolean
     */
    public boolean isUserPresent(String email) {
        log.info("Verify whether the email {} associated with an existing user", email);
        try {
            this.findUserByEmail(email);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method is used to verify whether the user entered password is matching or not.
     * If the password is matching it will return true, otherwise false is returned.
     *
     * @param user
     * @param existingUser
     * @return boolean
     */
    boolean isPasswordMatching(User user, User existingUser) {
        log.info("Verify whether the password is matching for the email {}", user.getEmail());
        return passwordEncoder.matches(user.getPassword(), existingUser.getPassword());
    }

    /**
     * This method is used to save the user into the mongo DB
     * @param user
     */
    public void saveUser(User user) {
        userRepository.save(user);
    }

    /**
     * This method is used to create the user into the DB.
     *
     * @param userAuthDto
     */
    public void createUser(UserAuthDto userAuthDto) {
        log.info("Create a new user for the email {}", userAuthDto.getEmail());
        User user = convertorUtil.convertUserAuthDtoToUser(userAuthDto);
        log.info(String.valueOf(user));
        user.setPassword(passwordEncoder.encode(userAuthDto.getPassword()));
        saveUser(user);
    }

    /**
     * This method is used to generate the JWT token for the user and return the same upon successful validation.
     *
     * @param userAuthDto
     * @return String
     */
    public String getUserJWT(UserAuthDto userAuthDto) {
        log.info("Retrieve the JWT token for the email {}", userAuthDto.getEmail());
        User user = convertorUtil.convertUserAuthDtoToUser(userAuthDto);
        User existingUser = this.findUserByEmail(user.getEmail());
        if (this.isPasswordMatching(user, existingUser)) {
            return jwtUtil.generateToken(user.getEmail());
        }
        return "";
    }

    /**
     * This method is used to delete the created user if present
     *
     * @param email
     */
    public void deleteUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresent(userRepository::delete);
    }
}
