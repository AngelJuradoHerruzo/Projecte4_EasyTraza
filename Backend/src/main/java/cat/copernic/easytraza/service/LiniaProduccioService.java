package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.repository.LiniaProduccioRepository;

/**
 * SERVEI DE LÍNIES DE PRODUCCIÓ.
 *
 * Gestionades les consultes bàsiques de les línies de producció registrades als albarans de client.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class LiniaProduccioService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final LiniaProduccioRepository liniaProduccioRepository;

    public LiniaProduccioService(LiniaProduccioRepository liniaProduccioRepository) {
        this.liniaProduccioRepository = liniaProduccioRepository;
    }


    /**
     * OBTENCIÓ DEL LLISTAT.
     *
     * Obtingut el conjunt de dades sol·licitat pel servei, aplicant
     * els filtres o l'ordenació corresponents quan és necessari.
     *
     * @return llista de resultats obtinguda
     */
    public List<LiniaProduccio> getAllLiniesProduccio() {
        return liniaProduccioRepository.findAll();
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param id identificador utilitzat en l'operació
     * @return resultat obtingut pel mètode
     */
    public LiniaProduccio getLiniaProduccioById(Long id) {
        Optional<LiniaProduccio> liniaProduccio = liniaProduccioRepository.findById(id);
        return liniaProduccio.orElse(null);
    }
}