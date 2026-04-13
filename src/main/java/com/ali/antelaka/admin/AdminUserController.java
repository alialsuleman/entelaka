package com.ali.antelaka.admin;


import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.auth.RegisterRequest;
import com.ali.antelaka.user.dto.ManagerResponse;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.ali.antelaka.user.entity.Role.MANAGER;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANGER')")
public class AdminUserController {

    private final AdminUserService adminUserService;


    private final AuthenticationService service ;




    @PostMapping("/addmanger")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> addmanger(
            @Valid @RequestBody RegisterRequest request
    ) {
        request.setRole(MANAGER);


        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("register successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data (service.register(request))
                .build();

        return ResponseEntity.ok(response);

    }

    @GetMapping("/getallmangeres")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getallmangeres() {
        List<ManagerResponse> managers = service.getAllManagers();

        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("Managers retrieved successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(managers)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletemanger")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deletemanger(@RequestParam Integer managerId) {
        service.deleteManager(managerId);

        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("Manager deleted successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }








    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminUserResponseDTO>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        try {
            AdminUserSearchRequest request = AdminUserSearchRequest.builder()
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();

            Page<AdminUserResponseDTO> users = adminUserService.getAllUsers(request);

            ApiResponse<Page<AdminUserResponseDTO>> response = ApiResponse
                    .<Page<AdminUserResponseDTO>>builder()
                    .success(true)
                    .message("Users retrieved successfully")
                    .data(users)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<AdminUserResponseDTO>> response = ApiResponse
                    .<Page<AdminUserResponseDTO>>builder()
                    .success(false)
                    .message("Error retrieving users: " + e.getMessage())
                    .data(null)
                    .errors(Collections.singletonList(e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AdminUserResponseDTO>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<AdminUserResponseDTO> users = adminUserService.searchUsersByKeyword(keyword, page, size);

            ApiResponse<Page<AdminUserResponseDTO>> response = ApiResponse
                    .<Page<AdminUserResponseDTO>>builder()
                    .success(true)
                    .message("Users searched successfully")
                    .data(users)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<AdminUserResponseDTO>> response = ApiResponse
                    .<Page<AdminUserResponseDTO>>builder()
                    .success(false)
                    .message("Error searching users: " + e.getMessage())
                    .data(null)
                    .errors(Collections.singletonList(e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResponseDTO>> getUserDetails(@PathVariable Integer userId) {
        try {
            AdminUserResponseDTO user = adminUserService.getUserDetails(userId);

            ApiResponse<AdminUserResponseDTO> response = ApiResponse
                    .<AdminUserResponseDTO>builder()
                    .success(true)
                    .message("User details retrieved successfully")
                    .data(user)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<AdminUserResponseDTO> response = ApiResponse
                    .<AdminUserResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .errors(Collections.singletonList(e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ApiResponse<AdminUserResponseDTO> response = ApiResponse
                    .<AdminUserResponseDTO>builder()
                    .success(false)
                    .message("Error retrieving user details: " + e.getMessage())
                    .data(null)
                    .errors(Collections.singletonList(e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // حظر مستخدم
    @PutMapping("/{userId}/ban")
    public ResponseEntity<ApiResponse<User>> banUser(@PathVariable Integer userId) {
        try {
            User bannedUser = adminUserService.banUser(userId);

            ApiResponse<User> response = ApiResponse
                    .<User>builder()
                    .success(true)
                    .message("User banned successfully")
                    .data(bannedUser)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<User> response = ApiResponse
                    .<User>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .errors(Collections.singletonList(e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // إلغاء حظر مستخدم
    @PutMapping("/{userId}/unban")
    public ResponseEntity<ApiResponse<User>> unbanUser(@PathVariable Integer userId) {
        try {
            User unbannedUser = adminUserService.unbanUser(userId);

            ApiResponse<User> response = ApiResponse
                    .<User>builder()
                    .success(true)
                    .message("User unbanned successfully")
                    .data(unbannedUser)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<User> response = ApiResponse
                    .<User>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .errors(Collections.singletonList(e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // أو طريقة واحدة مع باراميتر (الأفضل)
    @PutMapping("/{userId}/ban-status")
    public ResponseEntity<ApiResponse<User>> updateBanStatus(
            @PathVariable Integer userId,
            @RequestParam boolean banned) {
        try {
            User updatedUser = adminUserService.toggleUserBan(userId, banned);
            String message = banned ? "User banned successfully" : "User unbanned successfully";

            ApiResponse<User> response = ApiResponse
                    .<User>builder()
                    .success(true)
                    .message(message)
                    .data(updatedUser)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<User> response = ApiResponse
                    .<User>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .errors(Collections.singletonList(e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}