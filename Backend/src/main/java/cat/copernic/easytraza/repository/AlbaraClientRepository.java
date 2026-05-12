package cat.copernic.easytraza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.AlbaraClient;

public interface AlbaraClientRepository extends JpaRepository<AlbaraClient, Long> {

    boolean existsByClientId(Long clientId);
}
