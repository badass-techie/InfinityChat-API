package com.badasstechie.infinitychat.Chat;

import com.badasstechie.infinitychat.AppUser.AppUser;
import lombok.*;

import javax.persistence.*;
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
public class Chat {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long chatId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "creator_app_user_id")
    @ToString.Exclude
    private AppUser creator;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_app_user_id")
    @ToString.Exclude
    private AppUser member;

    @PastOrPresent(message = "Created date must be in the past or present")
    private Instant created;

    @PastOrPresent(message = "Modified date must be in the past or present")
    private Instant modified;

    public boolean isMember(AppUser appUser) {
        return appUser.getAppUserId().equals(creator.getAppUserId()) || appUser.getAppUserId().equals(member.getAppUserId());
    }

    public AppUser otherMember(AppUser appUser) {
        return appUser.getAppUserId().equals(creator.getAppUserId())? member : creator;
    }
}
