package dev.luisoliveira.roteiro.controller;

import dev.luisoliveira.roteiro.config.security.UserPrincipal;
import dev.luisoliveira.roteiro.model.PrayerContent;
import dev.luisoliveira.roteiro.repository.PrayerContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserContentController {

    @Autowired
    private PrayerContentRepository prayerContentRepository;

    @GetMapping("/prayers")
    public ResponseEntity<List<PrayerContent>> getUserPrayers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String userId = String.valueOf(userPrincipal.getId());

        List<PrayerContent> userPrayers = prayerContentRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return ResponseEntity.ok(userPrayers);
    }
}