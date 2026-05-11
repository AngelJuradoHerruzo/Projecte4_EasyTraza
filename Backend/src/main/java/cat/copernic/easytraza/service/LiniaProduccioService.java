package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.repository.LiniaProduccioRepository;

@Service
@Transactional
public class LiniaProduccioService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final LiniaProduccioRepository liniaProduccioRepository;

    public LiniaProduccioService(LiniaProduccioRepository liniaProduccioRepository) {
        this.liniaProduccioRepository = liniaProduccioRepository;
    }


    // OBTENIR TOTES LES LÍNIES DE PRODUCCIÓ
    public List<LiniaProduccio> getAllLiniesProduccio() {
        return liniaProduccioRepository.findAll();
    }


    // OBTENIR LÍNIA DE PRODUCCIÓ PER ID
    public LiniaProduccio getLiniaProduccioById(Long id) {
        Optional<LiniaProduccio> liniaProduccio = liniaProduccioRepository.findById(id);
        return liniaProduccio.orElse(null);
    }
}