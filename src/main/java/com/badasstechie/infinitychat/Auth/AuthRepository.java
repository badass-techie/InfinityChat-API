package com.badasstechie.infinitychat.Auth;

import com.badasstechie.infinitychat.AppUser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<AppUser, Long> {

}
