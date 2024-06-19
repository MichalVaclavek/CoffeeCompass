package cz.fungisoft.coffeecompass.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Data
@Table(name="refresh_token", schema = "coffeecompass")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name="expiry_date", nullable = false)
    private Instant expiryDate;
}
