package cat.copernic.easytraza.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.repository.LotProveidorRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;

/**
 * SERVEI DE MATÈRIES PRIMERES.
 *
 * Gestionades les operacions de consulta, creació, modificació i eliminació de matèries primeres.
 * També aplicats els filtres i validacions necessaris abans de guardar-les.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class MateriaPrimeraService {

    // ---------------------------- REPOSITORIS I CONSTRUCTOR ----------------------------
    private final MateriaPrimeraRepository materiaPrimeraRepository;
    private final LotProveidorRepository lotProveidorRepository;
    private final MessageSource messageSource;

    public MateriaPrimeraService(MateriaPrimeraRepository materiaPrimeraRepository,
                                 LotProveidorRepository lotProveidorRepository,
                                 MessageSource messageSource) {
        this.materiaPrimeraRepository = materiaPrimeraRepository;
        this.lotProveidorRepository = lotProveidorRepository;
        this.messageSource = messageSource;
    }


    /**
     * OBTENCIÓ DEL LLISTAT.
     *
     * Obtingut el conjunt de dades sol·licitat pel servei, aplicant
     * els filtres o l'ordenació corresponents quan és necessari.
     *
     * @return llista de resultats obtinguda
     */
    public List<MateriaPrimera> getAllMateriesPrimeres() {
        return materiaPrimeraRepository.findAll();
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


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param materiesPrimeres valor de materiesPrimeres utilitzat pel mètode
     * @return llista de resultats obtinguda
     */
    public Set<Long> getIdsMateriesPrimeresEnUs(List<MateriaPrimera> materiesPrimeres) {

        Set<Long> materiesEnUs = new HashSet<>();

        for (MateriaPrimera materiaPrimera : materiesPrimeres) {
            if (lotProveidorRepository.existsByMateriaPrimeraId(materiaPrimera.getId())) {
                materiesEnUs.add(materiaPrimera.getId());
            }
        }

        return materiesEnUs;
    }


    /**
     * CREACIÓ DEL REGISTRE.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param materiaPrimera valor de materiaPrimera utilitzat pel mètode
     * @return registre resultant de l'operació
     */
    public MateriaPrimera createMateriaPrimera(MateriaPrimera materiaPrimera) {
        validarDadesMateriesPrimeres(materiaPrimera);
        return materiaPrimeraRepository.save(materiaPrimera);
    }


    /**
     * ACTUALITZACIÓ DEL REGISTRE.
     *
     * Actualitzat el registre indicat amb les dades rebudes, mantenint
     * les validacions pròpies del servei.
     *
     * @param id identificador utilitzat en l'operació
     * @param materiaPrimera valor de materiaPrimera utilitzat pel mètode
     * @return registre resultant de l'operació
     */
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


    /**
     * ELIMINACIÓ DEL REGISTRE.
     *
     * Eliminat el registre identificat quan el servei permet completar
     * l'operació sol·licitada.
     *
     * @param id identificador utilitzat en l'operació
     */
    public void deleteMateriaPrimera(Long id) {

        if (!materiaPrimeraRepository.existsById(id)) {
            throw new RuntimeException(missatge("service.materia.noTrobadaEliminar"));
        }

        if (lotProveidorRepository.existsByMateriaPrimeraId(id)) {
            throw new RuntimeException(missatge("service.materia.eliminarLots"));
        }

        materiaPrimeraRepository.deleteById(id);
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param materiaPrimera valor de materiaPrimera utilitzat pel mètode
     */
    private void validarDadesMateriesPrimeres(MateriaPrimera materiaPrimera) {

        if (materiaPrimera.getNomMateria() != null) {
            materiaPrimera.setNomMateria(materiaPrimera.getNomMateria().trim());
        }

        if (materiaPrimera.getDescripcio() != null) {
            materiaPrimera.setDescripcio(materiaPrimera.getDescripcio().trim());
        }

        if (materiaPrimera.getNomMateria() == null || materiaPrimera.getNomMateria().isBlank()) {
            throw new RuntimeException(missatge("service.materia.nomObligatori"));
        }

        Optional<MateriaPrimera> materiaPrimeraExistent = materiaPrimeraRepository.findByNomMateria(materiaPrimera.getNomMateria());

        if (materiaPrimeraExistent.isPresent()
                && !materiaPrimeraExistent.get().getId().equals(materiaPrimera.getId())) {
            throw new RuntimeException(missatge("service.materia.nomDuplicat"));
        }

        if (materiaPrimera.getDescripcio() != null && materiaPrimera.getDescripcio().length() > 50) {
            throw new RuntimeException(missatge("service.materia.descripcioMax"));
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


    /**
     * NORMALITZACIÓ DEL VALOR.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String valorText(String valor) {
        return valor != null ? valor : "";
    }


    /**
     * COMPROVACIÓ DE CONTINGUT.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param valor valor que s'ha de processar
     * @param filtre valor utilitzat per filtrar les dades
     * @return cert si es compleix la condició indicada
     */
    private boolean conteText(String valor, String filtre) {

        if (filtre == null || filtre.isBlank()) {
            return true;
        }

        if (valor == null) {
            return false;
        }

        return valor.toLowerCase().contains(filtre.trim().toLowerCase());
    }


    /**
     * OBTENCIÓ DEL MISSATGE.
     *
     * Obtingut el text internacionalitzat corresponent al codi rebut
     * i als arguments indicats.
     *
     * @param codi codi del missatge que s'ha d'obtenir
     * @param arguments arguments aplicats al missatge
     * @return text obtingut pel mètode
     */
    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
