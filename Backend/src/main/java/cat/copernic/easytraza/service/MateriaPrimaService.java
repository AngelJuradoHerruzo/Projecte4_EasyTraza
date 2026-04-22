package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.MateriaPrima;
import cat.copernic.easytraza.repository.MateriaPrimaRepository;

@Service
@Transactional
public class MateriaPrimaService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final MateriaPrimaRepository materiaPrimaRepository;

    public MateriaPrimaService(MateriaPrimaRepository materiaPrimaRepository) {
        this.materiaPrimaRepository = materiaPrimaRepository;
    }


    // OBTENIR TOTES LES MATÈRIES PRIMERES
    public List<MateriaPrima> getAllMateriesPrimeres() {
        return materiaPrimaRepository.findAll();
    }


    // OBTENIR MATÈRIA PRIMERA PER ID
    public MateriaPrima getMateriaPrimaById(Long id) {
        Optional<MateriaPrima> materiaPrima = materiaPrimaRepository.findById(id);
        return materiaPrima.orElse(null);
    }


    // CREAR MATÈRIA PRIMERA
    public MateriaPrima createMateriaPrima(MateriaPrima materiaPrima) {
        validarDadesMateriesPrimeres(materiaPrima);
        return materiaPrimaRepository.save(materiaPrima);
    }


    // ACTUALITZAR MATÈRIA PRIMERA
    public MateriaPrima updateMateriaPrima(Long id, MateriaPrima materiaPrima) {

        Optional<MateriaPrima> materiaPrimaOpt = materiaPrimaRepository.findById(id);

        if (materiaPrimaOpt.isPresent()) {
            validarDadesMateriesPrimeres(materiaPrima);

            MateriaPrima materiaPrimaActual = materiaPrimaOpt.get();

            materiaPrimaActual.setNomMateria(materiaPrima.getNomMateria().trim());
            materiaPrimaActual.setDescripcio(materiaPrima.getDescripcio() != null ? materiaPrima.getDescripcio().trim() : null);

            return materiaPrimaRepository.save(materiaPrimaActual);
        }

        return null;
    }


    // ELIMINAR MATÈRIA PRIMERA
    public void deleteMateriaPrima(Long id) {
        materiaPrimaRepository.deleteById(id);
    }


    // VALIDAR DADES DE LA MATÈRIA PRIMERA
    private void validarDadesMateriesPrimeres(MateriaPrima materiaPrima) {

        if (materiaPrima.getNomMateria() != null) {
            materiaPrima.setNomMateria(materiaPrima.getNomMateria().trim());
        }

        if (materiaPrima.getDescripcio() != null) {
            materiaPrima.setDescripcio(materiaPrima.getDescripcio().trim());
        }

        if (materiaPrima.getNomMateria() == null || materiaPrima.getNomMateria().isBlank()) {
            throw new RuntimeException("El nom de la matèria primera és obligatori.");
        }

        if (materiaPrima.getDescripcio() != null && materiaPrima.getDescripcio().length() > 50) {
            throw new RuntimeException("La descripció no pot superar els 50 caràcters.");
        }
    }
}