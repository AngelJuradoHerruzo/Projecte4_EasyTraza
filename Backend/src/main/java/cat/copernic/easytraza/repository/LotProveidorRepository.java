package cat.copernic.easytraza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.enums.EstatLot;

public interface LotProveidorRepository extends JpaRepository<LotProveidor, Long> {

    Optional<LotProveidor> findFirstByMateriaPrimeraIdAndEstatAndIdNot(Long materiaPrimeraId, EstatLot estat, Long id);

    List<LotProveidor> findByEstat(EstatLot estat);

    // COMPROVAR SI UNA MATÈRIA PRIMERA ESTÀ ASSOCIADA A ALGUN LOT
    boolean existsByMateriaPrimeraId(Long materiaPrimeraId);
}
