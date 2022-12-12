package com.badasstechie.infinitychat.AppUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@PreAuthorize("hasAuthority('SCOPE_USER')")
public class AppUserController {
    private final AppUserService appUserService;

    @Autowired
    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // read
    @GetMapping()
    public List<String> getAllUsers() {
        return appUserService.getAllUsers();
    }

    @GetMapping("/{username}")
    public AppUserResponse getUser(@PathVariable String username) {
        return appUserService.getUser(username);
    }

    // delete
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        return appUserService.deleteUser(username);
    }
}
