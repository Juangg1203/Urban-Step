package com.tiendaropa.security;

import java.util.List;
import com.tiendaropa.model.Usuario;
import com.tiendaropa.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class DetalleUsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public DetalleUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByNombreUsuario(usuario)
                .or(() -> usuarioRepository.findByCorreo(usuario))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o clave incorrectos"));

        return User.withUsername(u.getNombreUsuario())
                .password(u.getClave())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())))
                .disabled(!u.isActivo())
                .build();
    }
}
