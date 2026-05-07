package cat.copernic.easytraza.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByNif(String nif);
    Optional<Client> findByTelefon(String telefon);
    Optional<Client> findByEmail(String email);
}
