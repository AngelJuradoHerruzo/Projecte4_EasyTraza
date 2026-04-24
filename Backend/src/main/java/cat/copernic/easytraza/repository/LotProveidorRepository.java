package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.LotProveidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotProveidorRepository extends JpaRepository<LotProveidor, Long> {}