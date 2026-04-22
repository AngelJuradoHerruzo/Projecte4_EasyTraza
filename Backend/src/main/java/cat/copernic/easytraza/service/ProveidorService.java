package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.repository.ProveidorRepository;

@Service
@Transactional
public class ProveidorService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final ProveidorRepository proveidorRepository;

    public ProveidorService(ProveidorRepository proveidorRepository) {
        this.proveidorRepository = proveidorRepository;
    }


    // OBTENIR TOTS ELS PROVEÏDORS
    public List<Proveidor> getAllProveidors() {
        return proveidorRepository.findAll();
    }


    // OBTENIR PROVEÏDOR PER ID
    public Proveidor getProveidorById(Long id) {
        Optional<Proveidor> proveidor = proveidorRepository.findById(id);
        return proveidor.orElse(null);
    }


    // CREAR PROVEÏDOR
    public Proveidor createProveidor(Proveidor proveidor) {
        validarDadesProveidor(proveidor);

        Optional<Proveidor> proveidorExistent = proveidorRepository.findByCif(proveidor.getCif());
        if (proveidorExistent.isPresent()) {
            throw new RuntimeException("Ja existeix un proveïdor amb aquest CIF o DNI.");
        }

        return proveidorRepository.save(proveidor);
    }


    // ACTUALITZAR PROVEÏDOR
    public Proveidor updateProveidor(Long id, Proveidor proveidor) {

        Optional<Proveidor> proveidorOpt = proveidorRepository.findById(id);

        if (proveidorOpt.isPresent()) {
            validarDadesProveidor(proveidor);

            Optional<Proveidor> proveidorExistent = proveidorRepository.findByCif(proveidor.getCif());
            if (proveidorExistent.isPresent() && !proveidorExistent.get().getId().equals(id)) {
                throw new RuntimeException("Ja existeix un proveïdor amb aquest CIF o DNI.");
            }

            Proveidor proveidorActual = proveidorOpt.get();

            proveidorActual.setCif(proveidor.getCif().trim().toUpperCase());
            proveidorActual.setNomProveidor(proveidor.getNomProveidor().trim());
            proveidorActual.setAdreca(proveidor.getAdreca() != null ? proveidor.getAdreca().trim() : null);
            proveidorActual.setDescripcio(proveidor.getDescripcio() != null ? proveidor.getDescripcio().trim() : null);

            return proveidorRepository.save(proveidorActual);
        }

        return null;
    }


    // ELIMINAR PROVEÏDOR
    public void deleteProveidor(Long id) {
        proveidorRepository.deleteById(id);
    }


    // VALIDAR DADES DEL PROVEÏDOR
    private void validarDadesProveidor(Proveidor proveidor) {

        if (proveidor.getCif() != null) {
            proveidor.setCif(proveidor.getCif().trim().toUpperCase());
        }

        if (proveidor.getNomProveidor() != null) {
            proveidor.setNomProveidor(proveidor.getNomProveidor().trim());
        }

        if (proveidor.getAdreca() != null) {
            proveidor.setAdreca(proveidor.getAdreca().trim());
        }

        if (proveidor.getDescripcio() != null) {
            proveidor.setDescripcio(proveidor.getDescripcio().trim());
        }

        if (proveidor.getCif() == null || proveidor.getCif().isBlank()) {
            throw new RuntimeException("El CIF o DNI és obligatori.");
        }

        if (proveidor.getNomProveidor() == null || proveidor.getNomProveidor().isBlank()) {
            throw new RuntimeException("El nom del proveïdor és obligatori.");
        }

        if (proveidor.getDescripcio() != null && proveidor.getDescripcio().length() > 50) {
            throw new RuntimeException("La descripció no pot superar els 50 caràcters.");
        }
    }
}