package com.moto.backmoto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.moto.modelos.Rol;
import com.moto.modelos.Usuario;
import com.moto.repositorios.UsuarioRepository;
import com.moto.repositorios.RolRepository;

@SpringBootApplication(scanBasePackages = "com.moto")
@EnableJpaRepositories(basePackages = "com.moto.repositorios")
@EntityScan(basePackages = {"com.moto.modelos", "com.moto.repositorios", "com.moto.controladores", "com.moto.dtos", "com.moto.backmoto"})
public class BackmotoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackmotoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdminUser(UsuarioRepository usuarioRepository, RolRepository rolRepository, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder) {
		return args -> {
			if (!usuarioRepository.existsByUsername("admin")) {
				Rol rolAdmin = rolRepository.findByNombre("ADMIN");
				if (rolAdmin == null) {
					rolAdmin = new Rol();
					rolAdmin.setNombre("ADMIN");
					rolAdmin = rolRepository.save(rolAdmin);
				}
				Usuario admin = new Usuario();
				admin.setUsername("admin");
				admin.setClave(encoder.encode("123"));
				admin.setRol(rolAdmin);
				usuarioRepository.save(admin);
			}
		};
	}
}
