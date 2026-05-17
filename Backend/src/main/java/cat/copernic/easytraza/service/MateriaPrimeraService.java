package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;

@Service
@Transactional
public class MateriaPrimeraService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final MateriaPrimeraRepository materiaPrimeraRepository;

    public MateriaPrimeraService(MateriaPrimeraRepository materiaPrimeraRepository) {
        this.materiaPrimeraRepository = materiaPrimeraRepository;
    }


    // OBTENIR TOTES LES MATÈRIES PRIMERES
    public List<MateriaPrimera> getAllMateriesPrimeres() {
        return materiaPrimeraRepository.findAll();
    }


    // OBTENIR MATÈRIA PRIMERA PER ID
    public MateriaPrimera getMateriaPrimeraById(Long id) {
        Optional<MateriaPrimera> materiaPrimera = materiaPrimeraRepository.findById(id);
        return materiaPrimera.orElse(null);
    }


    // PREPARAR LLISTAT WEB DE MATÈRIES PRIMERES AMB ORDENACIÓ OPCIONAL
    public List<MateriaPrimera> getMateriesPrimeresLlistat(String sort, String dir) {

        List<MateriaPrimera> materiesPrimeres = new java.util.ArrayList<>(materiaPrimeraRepository.findAll());

        if (sort == null || sort.isBlank() || dir == null || dir.isBlank()) {
            return materiesPrimeres;
        }

        java.util.Comparator<MateriaPrimera> comparador = switch (sort) {
            case "nomMateria" -> java.util.Comparator.comparing(
                    MateriaPrimera::getNomMateria,
                    String.CASE_INSENSITIVE_ORDER
            );

            case "descripcio" -> java.util.Comparator.comparing(
                    materia -> materia.getDescripcio() != null ? materia.getDescripcio() : "",
                    String.CASE_INSENSITIVE_ORDER
            );

            default -> null;
        };

        if (comparador == null) {
            return materiesPrimeres;
        }

        if ("desc".equalsIgnoreCase(dir)) {
            comparador = comparador.reversed();
        }

        materiesPrimeres.sort(comparador);

        return materiesPrimeres;
    }


    // CREAR MATÈRIA PRIMERA
    public MateriaPrimera createMateriaPrimera(MateriaPrimera materiaPrimera) {
        validarDadesMateriesPrimeres(materiaPrimera);
        return materiaPrimeraRepository.save(materiaPrimera);
    }


    // ACTUALITZAR MATÈRIA PRIMERA
    public MateriaPrimera updateMateriaPrimera(Long id, MateriaPrimera materiaPrimera) {

        Optional<MateriaPrimera> materiaPrimeraOpt = materiaPrimeraRepository.findById(id);

        if (materiaPrimeraOpt.isPresent()) {
            materiaPrimera.setId(id);
            validarDadesMateriesPrimeres(materiaPrimera);

            MateriaPrimera materiaPrimeraActual = materiaPrimeraOpt.get();

            materiaPrimeraActual.setNomMateria(materiaPrimera.getNomMateria().trim());
            materiaPrimeraActual.setDescripcio(materiaPrimera.getDescripcio() != null ? materiaPrimera.getDescripcio().trim() : null);

            return materiaPrimeraRepository.save(materiaPrimeraActual);
        }

        return null;
    }


    // ELIMINAR MATÈRIA PRIMERA
    public void deleteMateriaPrimera(Long id) {
        materiaPrimeraRepository.deleteById(id);
    }


    // VALIDAR DADES DE LA MATÈRIA PRIMERA
    private void validarDadesMateriesPrimeres(MateriaPrimera materiaPrimera) {

        if (materiaPrimera.getNomMateria() != null) {
            materiaPrimera.setNomMateria(materiaPrimera.getNomMateria().trim());
        }

        if (materiaPrimera.getDescripcio() != null) {
            materiaPrimera.setDescripcio(materiaPrimera.getDescripcio().trim());
        }

        if (materiaPrimera.getNomMateria() == null || materiaPrimera.getNomMateria().isBlank()) {
            throw new RuntimeException("El nom de la matèria primera és obligatori.");
        }

        Optional<MateriaPrimera> materiaPrimeraExistent = materiaPrimeraRepository.findByNomMateria(materiaPrimera.getNomMateria());

        if (materiaPrimeraExistent.isPresent()
                && !materiaPrimeraExistent.get().getId().equals(materiaPrimera.getId())) {
            throw new RuntimeException("Ja existeix una matèria primera amb aquest nom.");
        }

        if (materiaPrimera.getDescripcio() != null && materiaPrimera.getDescripcio().length() > 50) {
            throw new RuntimeException("La descripció no pot superar els 50 caràcters.");
        }
    }
}