package com.dr_commerce.e_commerce.vi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    // ---- UserDetails ----

    // Devuelve el rol del usuario como autoridad para Spring Security.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    // Usa el email del usuario como nombre de usuario para autenticarse.
    @Override
    public String getUsername() {
        return email;
    }

    // Indica que la cuenta no está expirada.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Indica que la cuenta no está bloqueada.
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Indica que las credenciales no están expiradas.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Indica que la cuenta está habilitada.
    @Override
    public boolean isEnabled() {
        return true;
    }
}
