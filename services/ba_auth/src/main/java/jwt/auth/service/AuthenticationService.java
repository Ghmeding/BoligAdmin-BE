package jwt.auth.service;

import jakarta.mail.MessagingException;
import jwt.auth.dto.LoginUserDto;
import jwt.auth.dto.RegisterUserDto;
import jwt.auth.dto.VerifyUserDto;
import jwt.auth.models.User;
import jwt.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public User signUp(RegisterUserDto input){
        //TODO: Do checks here: valid email, username/email already taken, valid password

        //Do basic checks
        if(input.getUsername().length() < 2 || input.getUsername().length() > 20){
            throw new IllegalArgumentException("Username must be between 2 and 20 characters");
        } else if(input.getPassword().length() < 5 || input.getPassword().length() > 20){
            throw new IllegalArgumentException("Password must be between 5 and 20 characters");
        } else if(!input.getEmail().contains("@")){
            throw new IllegalArgumentException("Email is not valid");
        }

        if(userRepository.findByEmail(input.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email is already taken");
        }

        User user = new User(
                input.getUsername(),
                input.getEmail(),
                passwordEncoder.encode(input.getPassword())
        );

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input){
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify account");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return user;
    }

    public void verifyUser(VerifyUserDto input){
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired.");
            }

            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true); // user verified their email successfully
                user.setVerificationCode(null); // reset the verification code
                user.setVerificationCodeExpiresAt(null); // reset the verification date
                userRepository.save(user); // save the user to the database
            } else {
                throw new RuntimeException("Invalid verification code.");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email){
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1)); // One hour to find the verification email
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found.");
        }
    }

    public void sendVerificationEmail(User user){
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String msg = "verification message: " + verificationCode;

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, msg);
        } catch (MessagingException e){
            e.printStackTrace(); //TODO: replace with better logging
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
