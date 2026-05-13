package cat.copernic.easytraza.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.AlbaraProveidor;

public interface AlbaraProveidorRepository extends JpaRepository<AlbaraProveidor, Long> {

    List<AlbaraProveidor> findAllByOrderByDataRecepcioDescIdDesc();
}
