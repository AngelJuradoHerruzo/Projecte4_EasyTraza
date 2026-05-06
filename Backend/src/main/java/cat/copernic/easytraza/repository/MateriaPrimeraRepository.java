package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.MateriaPrimera;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateriaPrimeraRepository extends JpaRepository<MateriaPrimera, Long> {

    Optional<MateriaPrimera> findByNomMateria(String nomMateria);
}
