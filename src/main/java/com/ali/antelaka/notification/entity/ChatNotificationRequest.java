package com.ali.antelaka.notification.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class ChatNotificationRequest {
    private Integer id;              // id الرسالة
    private String uuid;             // uuid الرسالة
    private Integer senderId;        // id المرسل
    private Integer receiverId;      // id المستقبل
    private String text;             // نص الرسالة
    private Integer isRead;          // حالة القراءة
    private String createdAt;        // وقت الإرسال
    private Integer isDelivered;     // حالة التسليم
    private Integer unreadCount;     // عدد الرسائل غير المقروءة
    private Integer partnerId;       // id الشريك
    private Boolean isMine;          // هل الرسالة مني
    private PartnerInfo partnerInfo; // معلومات الشريك

    @Data
    @Getter
    @Setter
    public static class PartnerInfo {
        private Integer id;
        private String firstname;
        private String lastname;
        private String imagePath;
    }
}