package com.badasstechie.infinitychat.Message;

import com.badasstechie.infinitychat.AppUser.AppUser;
import com.badasstechie.infinitychat.Chat.Chat;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.Instant;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long messageId;

    @NotBlank(message = "Message cannot be empty or Null")
    @Lob    // Large object
    private String text;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "chat_id")
    @ToString.Exclude
    private Chat chat;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "sender_app_user_id")
    @ToString.Exclude
    private AppUser sender;

    @PastOrPresent(message = "Created date must be in the past or present")
    private Instant created;
}

