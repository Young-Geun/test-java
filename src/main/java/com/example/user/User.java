package com.example.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String userId;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "failed_attempt")
    private int failedAttempt = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void increaseFailedAttempts() {
        this.failedAttempt++;
    }

    public void resetFailedAttempts() {
        this.failedAttempt = 0;
    }

    public void lock() {
        this.accountNonLocked = false;
        this.lockTime = LocalDateTime.now();
    }

    public void unlock() {
        this.accountNonLocked = true;
        this.lockTime = null;
        this.failedAttempt = 0;
    }

    public boolean isAccountLocked() {
        return !this.accountNonLocked;
    }
}
