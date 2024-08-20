package com.roa.security_redis.Controller;

import com.roa.security_redis.DTO.LoginDTO;
import com.roa.security_redis.DTO.RegisterDTO;
import com.roa.security_redis.DTO.ResponseDTO;
import com.roa.security_redis.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody RegisterDTO registerDTO){
        return new ResponseEntity<>(authService.register(registerDTO), HttpStatus.CREATED);
    }
    @GetMapping("/login")
    public ResponseEntity<ResponseDTO> register(@RequestBody LoginDTO loginDTO){
        return new ResponseEntity<>(authService.login(loginDTO), HttpStatus.OK);
    }


    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request){

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.contains("Bearer ")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("please login again ");
        }
        String access_token = authHeader.substring(7);
        ResponseDTO refresh = authService.refresh(access_token);
        return ResponseEntity.status(HttpStatus.OK).body(refresh);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication){
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.contains("Bearer ")){
            return new ResponseEntity<>("please login",HttpStatus.UNAUTHORIZED);
        }
        String access_token = authHeader.substring(7);
        authService.logout(access_token);
        logoutHandler.logout(request,response,authentication);
        return new ResponseEntity<>("logout successful",HttpStatus.OK);
    }

}
