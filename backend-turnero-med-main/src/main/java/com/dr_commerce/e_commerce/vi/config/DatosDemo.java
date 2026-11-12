package com.dr_commerce.e_commerce.vi.config;

import com.dr_commerce.e_commerce.vi.model.*;
import com.dr_commerce.e_commerce.vi.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.math.BigDecimal;

// Datos ficticios únicamente para probar el perfil local.
@Component
@Profile("local")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DatosDemo implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final MedicoRepository medicos;
    private final TurnoRepository turnos;
    private final PasswordEncoder encoder;
    public DatosDemo(UsuarioRepository usuarios, MedicoRepository medicos, TurnoRepository turnos, PasswordEncoder encoder) {
        this.usuarios = usuarios; this.medicos = medicos; this.turnos = turnos; this.encoder = encoder;
    }
    @Override
    @Transactional
    public void run(String... args) {
        if (!usuarios.existsByEmail("admin@turnero.local")) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador"); admin.setApellido("Demo");
            admin.setEmail("admin@turnero.local"); admin.setPassword(encoder.encode("AdminDemo123!"));
            admin.setRol(Rol.ADMIN); usuarios.save(admin);
        }
        if (!usuarios.existsByEmail("medico@turnero.local")) {
            Medico medico = new Medico();
            medico.setNombre("Ana"); medico.setApellido("Demo");
            medico.setEmail("medico@turnero.local"); medico.setPassword(encoder.encode("MedicoDemo123!"));
            medico.setRol(Rol.MEDICO); medico.setMatricula("DEMO-001");
            medico.setEspecialidad(Especialidad.CARDIOLOGIA); medicos.save(medico);
            for (int hora = 9; hora <= 11; hora++) {
                Turno turno = new Turno();
                turno.setMedico(medico);
                turno.setFecha(new Fecha(LocalDate.now().plusDays(1), LocalTime.of(hora, 0)));
                turno.setDireccion(new Direccion("Consultorio de ejemplo", 123, "Buenos Aires", "1000"));
                turno.setEstado(EstadoTurno.DISPONIBLE); turno.setPrecio(new BigDecimal("25000.00"));
                turnos.save(turno);
            }
        }
    }
}
