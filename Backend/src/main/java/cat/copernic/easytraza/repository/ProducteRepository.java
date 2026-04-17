package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.Producte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProducteRepository extends JpaRepository<Producte, Long> {}