package com.ali.antelaka.post.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentHistoryDTO {
    private Integer myCommentId; // ID التعليق الذي كتبه المستخدم
    private String myCommentText; // نص تعليق المستخدم
    private LocalDateTime myCommentCreatedAt; // وقت تعليق المستخدم
    private LocalDateTime myCommentUpdatedAt; // وقت تحديث تعليق المستخدم
    private Integer myCommentLikes; // عدد الإعجابات على تعليق المستخدم
    private Integer myCommentReplies; // عدد الردود على تعليق المستخدم

    // الفروع الثلاثة
    private CommentLevelDTO commentLevel1; // التعليق الأساسي (الجذر)
    private CommentLevelDTO commentLevel2; // الرد الأول (إن وجد)
    private CommentLevelDTO commentLevel3; // الرد الثاني أو تعليق المستخدم

    private PostSummaryDTO postSummaryDTO;

    // معلومات إضافية
    private Integer totalLevels; // إجمالي عدد المستويات في الفرع
    private boolean isRootComment; // هل تعليق المستخدم هو الجذر؟
}