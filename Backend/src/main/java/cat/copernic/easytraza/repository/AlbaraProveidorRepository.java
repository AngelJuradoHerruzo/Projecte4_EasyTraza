package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbaraProveidorRepository extends JpaRepository<AlbaraProveidor, Long> {}