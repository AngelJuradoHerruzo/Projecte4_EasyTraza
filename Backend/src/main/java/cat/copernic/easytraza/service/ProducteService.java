package cat.copernic.easytraza.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Producte;
import cat.copernic.easytraza.repository.ProducteRepository;

/**
 * SERVEI DE PRODUCTES.
 *
 * Gestionades les operacions de consulta, creació, modificació i eliminació dels productes.
 * També aplicades les validacions i ordenacions necessàries sobre les dades introduïdes.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class ProducteService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final ProducteRepository producteRepository;
    private final MessageSource messageSource;

    public ProducteService(ProducteRepository producteRepository, MessageSource messageSource) {
        this.producteRepository = producteRepository;
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
    public List<Producte> getAllProductes() {
        return getAllProductes(null);
    }


    /**
     * OBTENCIÓ DEL LLISTAT.
     *
     * Obtingut el conjunt de dades sol·licitat pel servei, aplicant
     * els filtres o l'ordenació corresponents quan és necessari.
     *
     * @param nomProducte valor de nomProducte utilitzat pel mètode
     * @return llista de resultats obtinguda
     */
    public List<Producte> getAllProductes(String nomProducte) {

        List<Producte> productes = new ArrayList<>(producteRepository.findAll());

        if (nomProducte != null && !nomProducte.isBlank()) {
            productes.removeIf(producte -> !conteText(producte.getNomProducte(), nomProducte));
        }

        ordenarProductesPerNom(productes);

        return productes;
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
    public Producte getProducteById(Long id) {
        Optional<Producte> producte = producteRepository.findById(id);
        return producte.orElse(null);
    }


    /**
     * CREACIÓ DEL REGISTRE.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param producte valor de producte utilitzat pel mètode
     * @return registre resultant de l'operació
     */
    public Producte createProducte(Producte producte) {
        validarDadesProducte(producte);
        return producteRepository.save(producte);
    }


    /**
     * ACTUALITZACIÓ DEL REGISTRE.
     *
     * Actualitzat el registre indicat amb les dades rebudes, mantenint
     * les validacions pròpies del servei.
     *
     * @param id identificador utilitzat en l'operació
     * @param producte valor de producte utilitzat pel mètode
     * @return registre resultant de l'operació
     */
    public Producte updateProducte(Long id, Producte producte) {

        Optional<Producte> producteOpt = producteRepository.findById(id);

        if (producteOpt.isPresent()) {
            producte.setId(id);
            validarDadesProducte(producte);

            Producte producteActual = producteOpt.get();

            producteActual.setNomProducte(producte.getNomProducte().trim());
            producteActual.setDescripcio(
                producte.getDescripcio() != null ? producte.getDescripcio().trim() : null
            );

            return producteRepository.save(producteActual);
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
    public void deleteProducte(Long id) {
        producteRepository.deleteById(id);
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param producte valor de producte utilitzat pel mètode
     */
    private void validarDadesProducte(Producte producte) {

        if (producte.getNomProducte() != null) {
            producte.setNomProducte(producte.getNomProducte().trim());
        }

        if (producte.getDescripcio() != null) {
            producte.setDescripcio(producte.getDescripcio().trim());
        }

        if (producte.getNomProducte() == null || producte.getNomProducte().isBlank()) {
            throw new RuntimeException(missatge("service.producte.nomObligatori"));
        }

        Optional<Producte> producteExistent = producteRepository.findByNomProducte(producte.getNomProducte());

        if (producteExistent.isPresent()
                && !producteExistent.get().getId().equals(producte.getId())) {
            throw new RuntimeException(missatge("service.producte.nomDuplicat"));
        }

        if (producte.getDescripcio() != null && producte.getDescripcio().length() > 50) {
            throw new RuntimeException(missatge("service.producte.descripcioMax"));
        }
    }


    /**
     * ORDENACIÓ DE DADES.
     *
     * Ordenada la llista rebuda segons el criteri indicat o segons
     * l'ordre propi del servei.
     *
     * @param productes valor de productes utilitzat pel mètode
     */
    private void ordenarProductesPerNom(List<Producte> productes) {

        productes.sort(
            Comparator.comparing(
                producte -> producte.getNomProducte() != null ? producte.getNomProducte() : "",
                String.CASE_INSENSITIVE_ORDER
            )
        );
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