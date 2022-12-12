package com.badasstechie.infinitychat.Chat;

import com.badasstechie.infinitychat.AppUser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findAllByOrderByModifiedDesc();
    Optional<Chat> findByCreatorAndMember(AppUser sender, AppUser recipient);
    List<Chat> findAllByCreator(AppUser creator);
    List<Chat> findAllByMember(AppUser member);
}
