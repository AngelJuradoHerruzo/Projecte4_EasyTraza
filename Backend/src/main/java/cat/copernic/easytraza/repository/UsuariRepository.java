package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.Usuari;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariRepository extends JpaRepository<Usuari, Long> {}