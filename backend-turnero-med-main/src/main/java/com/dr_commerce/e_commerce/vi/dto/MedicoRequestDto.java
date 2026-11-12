package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.Especialidad;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicoRequestDto {

    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String matricula;
    private Especialidad especialidad;
}
