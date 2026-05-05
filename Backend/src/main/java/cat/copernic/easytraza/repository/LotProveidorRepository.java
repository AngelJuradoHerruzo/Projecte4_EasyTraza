package cat.copernic.easytraza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.LotProveidor;

public interface LotProveidorRepository extends JpaRepository<LotProveidor, Long> {

    boolean existsByIdentificadorLotAndAlbaraProveidor_Proveidor_Id(String identificadorLot, Long proveidorId);
    boolean existsByIdentificadorLotAndAlbaraProveidor_Proveidor_IdAndIdNot(String identificadorLot, Long proveidorId, Long id);
}
