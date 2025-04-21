package org.example.misischatbot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Data
@Entity
public class Mailing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private LocalDateTime createDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private MailingType type = MailingType.ONCE;

    private String cronExpression;

    private LocalDateTime sendTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mailing_user_stats", joinColumns = @JoinColumn(name = "mailing_id"))
    @MapKeyColumn(name = "user_id")
    private Map<String, UserStats> userStats = new HashMap<>();

    private boolean sent = false;

    public enum MailingType {
        ONCE, REPEATING
    }
}

