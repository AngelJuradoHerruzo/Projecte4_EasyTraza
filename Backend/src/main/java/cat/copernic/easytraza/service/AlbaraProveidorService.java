package cat.copernic.easytraza.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.repository.AlbaraProveidorRepository;

@Service
@Transactional
public class AlbaraProveidorService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final AlbaraProveidorRepository albaraProveidorRepository;

    public AlbaraProveidorService(AlbaraProveidorRepository albaraProveidorRepository) {
        this.albaraProveidorRepository = albaraProveidorRepository;
    }
    

    // OBTENIR TOTS ELS ALBARANS DE PROVEÏDOR
    public List<AlbaraProveidor> getAllAlbaransProveidor() {
        return albaraProveidorRepository.findAll();
    }


    // OBTENIR ALBARÀ DE PROVEÏDOR PER ID
    public AlbaraProveidor getAlbaraProveidorById(Long id) {
        Optional<AlbaraProveidor> albaraOpt = albaraProveidorRepository.findById(id);
        return albaraOpt.orElse(null);
    }


    // CREAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor createAlbaraProveidor(AlbaraProveidor albaraProveidor) {
        validarDadesAlbaraProveidor(albaraProveidor);

        if (albaraProveidor.getDataRecepcio() == null) {
            albaraProveidor.setDataRecepcio(LocalDateTime.now());
        }

        if (albaraProveidor.getLots() != null) {
            for (LotProveidor lot : albaraProveidor.getLots()) {
                lot.setAlbaraProveidor(albaraProveidor);
            }
        }

        return albaraProveidorRepository.save(albaraProveidor);
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor updateAlbaraProveidor(Long id, AlbaraProveidor albaraProveidor) {

        Optional<AlbaraProveidor> albaraOpt = albaraProveidorRepository.findById(id);

        if (albaraOpt.isPresent()) {
            validarDadesAlbaraProveidor(albaraProveidor);

            AlbaraProveidor albaraActual = albaraOpt.get();

            albaraActual.setDataRecepcio(albaraProveidor.getDataRecepcio());
            albaraActual.setProveidor(albaraProveidor.getProveidor());
            albaraActual.setUsuariReceptor(albaraProveidor.getUsuariReceptor());

            return albaraProveidorRepository.save(albaraActual);
        }

        return null;
    }


    // ELIMINAR ALBARÀ DE PROVEÏDOR
    public void deleteAlbaraProveidor(Long id) {
        albaraProveidorRepository.deleteById(id);
    }


    // VALIDAR DADES DE L'ALBARÀ DE PROVEÏDOR
    private void validarDadesAlbaraProveidor(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor.getDataRecepcio() == null) {
            throw new RuntimeException("La data de recepció és obligatòria.");
        }

        if (albaraProveidor.getProveidor() == null) {
            throw new RuntimeException("El proveïdor és obligatori.");
        }

        if (albaraProveidor.getUsuariReceptor() == null) {
            throw new RuntimeException("L'usuari receptor és obligatori.");
        }
    }
}