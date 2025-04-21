package org.example.misischatbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    private boolean isSent;
    private boolean isRead;
    private LocalDateTime sentTime;
    private LocalDateTime readTime;
    private long msgId;
}
