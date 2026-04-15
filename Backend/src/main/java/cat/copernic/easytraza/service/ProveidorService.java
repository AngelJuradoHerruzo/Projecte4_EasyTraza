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
        return proveidorRepository.save(proveidor);
    }


    // ACTUALITZAR PROVEÏDOR
    public Proveidor updateProveidor(Long id, Proveidor proveidor) {

        Optional<Proveidor> proveidorOpt = proveidorRepository.findById(id);

        if (proveidorOpt.isPresent()) {
            Proveidor proveidorActual = proveidorOpt.get();

            proveidorActual.setCif(proveidor.getCif());
            proveidorActual.setNomProveidor(proveidor.getNomProveidor());
            proveidorActual.setAdreca(proveidor.getAdreca());
            proveidorActual.setDescripcio(proveidor.getDescripcio());

            return proveidorRepository.save(proveidorActual);
        }

        return null;
    }


    // ELIMINAR PROVEÏDOR
    public void deleteProveidor(Long id) {
        proveidorRepository.deleteById(id);
    }
}