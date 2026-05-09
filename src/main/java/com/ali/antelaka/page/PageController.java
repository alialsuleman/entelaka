package com.ali.antelaka.page;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.page.request.CreatePageBody;
import com.ali.antelaka.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RequestMapping("/users")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class PageController {


}
