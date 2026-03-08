package com.ali.antelaka.notification.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChatNotificationRequest {
    private Integer senderId;      // id المرسل
    private Integer receiverId;    // id المستقبل
    private String text;        // نص الرسالة
    private String timestamp;
}
