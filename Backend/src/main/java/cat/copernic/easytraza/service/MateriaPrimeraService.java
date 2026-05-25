package cat.copernic.easytraza.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.repository.LotProveidorRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;

@Service
@Transactional
public class MateriaPrimeraService {

    // ---------------------------- REPOSITORIS I CONSTRUCTOR ----------------------------
    private final MateriaPrimeraRepository materiaPrimeraRepository;
    private final LotProveidorRepository lotProveidorRepository;

    public MateriaPrimeraService(MateriaPrimeraRepository materiaPrimeraRepository,
                                 LotProveidorRepository lotProveidorRepository) {
        this.materiaPrimeraRepository = materiaPrimeraRepository;
        this.lotProveidorRepository = lotProveidorRepository;
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


    // PREPARAR LLISTAT WEB DE MATÈRIES PRIMERES AMB FILTRE I ORDENACIÓ OPCIONALS
    public List<MateriaPrimera> getMateriesPrimeresLlistat(String nomMateria,
                                                            String ordre,
                                                            String direccio) {

        List<MateriaPrimera> materiesPrimeres = new ArrayList<>(materiaPrimeraRepository.findAll());

        if (nomMateria != null && !nomMateria.isBlank()) {
            materiesPrimeres.removeIf(materia -> !conteText(materia.getNomMateria(), nomMateria));
        }

        ordenarMateriesPrimeres(materiesPrimeres, ordre, direccio);

        return materiesPrimeres;
    }


    // OBTENIR ELS IDENTIFICADORS DE LES MATÈRIES PRIMERES QUE NO ES PODEN ELIMINAR
    public Set<Long> getIdsMateriesPrimeresEnUs(List<MateriaPrimera> materiesPrimeres) {

        Set<Long> materiesEnUs = new HashSet<>();

        for (MateriaPrimera materiaPrimera : materiesPrimeres) {
            if (lotProveidorRepository.existsByMateriaPrimeraId(materiaPrimera.getId())) {
                materiesEnUs.add(materiaPrimera.getId());
            }
        }

        return materiesEnUs;
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
            materiaPrimeraActual.setDescripcio(
                materiaPrimera.getDescripcio() != null ? materiaPrimera.getDescripcio().trim() : null
            );

            return materiaPrimeraRepository.save(materiaPrimeraActual);
        }

        return null;
    }


    // ELIMINAR MATÈRIA PRIMERA SI NO ESTÀ ASSOCIADA A CAP LOT
    public void deleteMateriaPrimera(Long id) {

        if (!materiaPrimeraRepository.existsById(id)) {
            throw new RuntimeException("No s'ha trobat la matèria primera que vols eliminar.");
        }

        if (lotProveidorRepository.existsByMateriaPrimeraId(id)) {
            throw new RuntimeException("No es pot eliminar la matèria primera perquè està associada a un o més lots.");
        }

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


    // ORDENAR MATÈRIES PRIMERES PEL CAMP SELECCIONAT
    private void ordenarMateriesPrimeres(List<MateriaPrimera> materiesPrimeres,
                                          String ordre,
                                          String direccio) {

        String campOrdre = ordre != null && !ordre.isBlank() ? ordre : "nomMateria";

        Comparator<MateriaPrimera> comparator;

        switch (campOrdre) {
            case "id":
                comparator = Comparator.comparing(MateriaPrimera::getId);
                break;

            case "descripcio":
                comparator = Comparator.comparing(
                    materia -> valorText(materia.getDescripcio()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "nomMateria":
            default:
                comparator = Comparator.comparing(
                    materia -> valorText(materia.getNomMateria()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;
        }

        if ("desc".equalsIgnoreCase(direccio)) {
            comparator = comparator.reversed();
        }

        materiesPrimeres.sort(comparator.thenComparing(MateriaPrimera::getId));
    }


    // RETORNAR TEXT SEGUR PER A L'ORDENACIÓ
    private String valorText(String valor) {
        return valor != null ? valor : "";
    }


    // COMPROVAR SI UN TEXT CONTÉ UN FILTRE IGNORANT MAJÚSCULES I MINÚSCULES
    private boolean conteText(String valor, String filtre) {

        if (filtre == null || filtre.isBlank()) {
            return true;
        }

        if (valor == null) {
            return false;
        }

        return valor.toLowerCase().contains(filtre.trim().toLowerCase());
    }
}
