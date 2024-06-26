package com.example.login_administrator.service;

import com.example.login_administrator.config.security.JwtUtils;
import com.example.login_administrator.constan.ERole;
import com.example.login_administrator.dto.LoginDto;
import com.example.login_administrator.dto.TokenDto;
import com.example.login_administrator.dto.UserDto;
import com.example.login_administrator.model.Role;
import com.example.login_administrator.model.User;
import com.example.login_administrator.repository.RoleRepository;
import com.example.login_administrator.repository.UserRepository;
import com.example.login_administrator.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AuthService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ModelMapper modelMapper;

    @Value("${upload.directory}")
    private String uploadDirectory;

    public ResponseEntity<Object> registerUser(UserDto userDto, MultipartFile image) {
        if (Boolean.TRUE.equals(userRepository.existsByEmail(userDto.getEmail()))) {
            return Response.build(Response.exist("User", "email", userDto.getEmail()), null, null, HttpStatus.BAD_REQUEST);
        }

        if (Boolean.TRUE.equals(userRepository.existsByPhone(userDto.getPhone()))) {
            return Response.build(Response.exist("User", "phone", userDto.getPhone()), null, null, HttpStatus.BAD_REQUEST);
        }

        User user = modelMapper.map(userDto, User.class);
        user.setPassword(encoder.encode(userDto.getPassword()));

        Set<Role> roles = new HashSet<>();

        Optional<Role> userRole = roleRepository.findByName(ERole.USER);
        if(userRole.isEmpty()){
            log.info("Role user not found");
            return Response.build("Internal server error", null, null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        roles.add(userRole.get());

        user.setRoles(roles);

        if (image != null && !image.isEmpty()) {
            try {
                byte[] bytes = image.getBytes();
                Path path = Paths.get(uploadDirectory + File.separator + image.getOriginalFilename());
                Files.write(path, bytes);
                user.setImagePath(path.toString());
            } catch (IOException e) {
                log.error("Failed to upload image", e);
                return Response.build("Failed to upload image", null, null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        userRepository.save(user);

        UserDto userNoPasswordDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();

        return Response.build("Register success", userNoPasswordDto, null, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> loginUser(LoginDto loginDto) {
        if (Boolean.FALSE.equals(userRepository.existsByEmail(loginDto.getEmail()))) {
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findByEmail(loginDto.getEmail());
        Boolean isPasswordCorrect = encoder.matches(loginDto.getPassword(), user.get().getPassword());
        if (Boolean.FALSE.equals(isPasswordCorrect)) {
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        TokenDto token = TokenDto.builder().token(jwt).build();

        return Response.build("Login success", token, null, HttpStatus.OK);
    }

    public ResponseEntity<Object> loginAdmin(LoginDto loginDto) {
        if (Boolean.FALSE.equals(userRepository.existsByEmail(loginDto.getEmail()))) {
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findByEmail(loginDto.getEmail());
        Optional<Role> adminRole = roleRepository.findByName(ERole.ADMIN);
        if(adminRole.isEmpty()){
            log.info("Role admin not found");
            return Response.build("Internal server error", null, null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(!user.get().getRoles().contains(adminRole.get())){
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Boolean isPasswordCorrect = encoder.matches(loginDto.getPassword(), user.get().getPassword());
        if (Boolean.FALSE.equals(isPasswordCorrect)) {
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        TokenDto token = TokenDto.builder().token(jwt).build();

        return Response.build("Login success", token, null, HttpStatus.OK);
    }

    public ResponseEntity<Object> loginSuperAdmin(LoginDto loginDto) {
        if (Boolean.FALSE.equals(userRepository.existsByEmail(loginDto.getEmail()))) {
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findByEmail(loginDto.getEmail());
        Optional<Role> superAdminRole = roleRepository.findByName(ERole.SUPER_ADMIN);
        if(superAdminRole.isEmpty()){
            log.info("Role super admin not found");
            return Response.build("Internal server error", null, null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(!user.get().getRoles().contains(superAdminRole.get())){
            log.info("user don't have super admin role");
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Boolean isPasswordCorrect = encoder.matches(loginDto.getPassword(), user.get().getPassword());
        if (Boolean.FALSE.equals(isPasswordCorrect)) {
            return Response.build("Email or password incorrect", null, null, HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        TokenDto token = TokenDto.builder().token(jwt).build();

        return Response.build("Login success", token, null, HttpStatus.OK);
    }
}
