package com.ali.antelaka.exception;


import lombok.Data;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.naming.AuthenticationException;

public class CustomAuthenticationException extends RuntimeException {


    public CustomAuthenticationException(String msg ) {
        super(msg);

    }


}