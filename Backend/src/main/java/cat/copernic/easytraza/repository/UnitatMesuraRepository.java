package cat.copernic.easytraza.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.UnitatMesura;

public interface UnitatMesuraRepository extends JpaRepository<UnitatMesura, Long> {

    Optional<UnitatMesura> findByNom(String nom);
    boolean existsByNom(String nom);
    List<UnitatMesura> findAllByOrderByNomAsc();
}
