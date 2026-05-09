package com.ali.antelaka.auth.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OtpContext {
    private OtpType type;
    private LocalDateTime banTime;
    private LocalDateTime lastSentAt;
    private int numberOfSending;
}