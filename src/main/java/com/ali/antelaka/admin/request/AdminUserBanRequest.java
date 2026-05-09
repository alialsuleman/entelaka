package com.ali.antelaka.admin.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserBanRequest {
    private Integer userId;
    private boolean banned;
}