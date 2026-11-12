package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.model.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccesoService {
    public Usuario usuarioOpcional() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Usuario usuario ? usuario : null;
    }
    public Usuario usuarioActual() {
        Usuario usuario = usuarioOpcional();
        if (usuario == null) throw new AuthenticationCredentialsNotFoundException("Iniciá sesión");
        return usuario;
    }
    public Long pacienteActual() {
        Usuario usuario = usuarioActual();
        if (!(usuario instanceof Paciente)) throw new AccessDeniedException("Se requiere un paciente");
        return usuario.getId();
    }
    public void exigirPaciente(Long id) {
        if (!pacienteActual().equals(id)) throw new AccessDeniedException("El paciente no corresponde a tu cuenta");
    }
    public void exigirGestionMedico(Long medicoId) {
        Usuario usuario = usuarioActual();
        if (usuario.getRol() != Rol.ADMIN && !(usuario.getRol() == Rol.MEDICO && usuario.getId().equals(medicoId)))
            throw new AccessDeniedException("No podés gestionar turnos de ese médico");
    }
    public void exigirPacienteOAdmin(Long id) {
        Usuario usuario = usuarioActual();
        if (usuario.getRol() != Rol.ADMIN && !usuario.getId().equals(id))
            throw new AccessDeniedException("No podés acceder a otro paciente");
    }
}
