package cat.copernic.easytraza.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.UnitatMesura;
import cat.copernic.easytraza.repository.UnitatMesuraRepository;

/**
 * SERVEI D'UNITATS DE MESURA.
 *
 * Gestionades les operacions de consulta, creació, modificació i eliminació de les unitats de mesura.
 * També aplicades les validacions perquè no es dupliquin valors.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class UnitatMesuraService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final UnitatMesuraRepository unitatMesuraRepository;
    private final MessageSource messageSource;

    public UnitatMesuraService(UnitatMesuraRepository unitatMesuraRepository, MessageSource messageSource) {
        this.unitatMesuraRepository = unitatMesuraRepository;
        this.messageSource = messageSource;
    }


    // OBTENIR TOTES LES UNITATS DE MESURA ORDENADES
    public List<UnitatMesura> getAllUnitatsMesura() {
        return unitatMesuraRepository.findAllByOrderByNomAsc();
    }


    // OBTENIR UNITAT DE MESURA PER ID
    public UnitatMesura getUnitatMesuraById(Long id) {
        Optional<UnitatMesura> unitatMesura = unitatMesuraRepository.findById(id);
        return unitatMesura.orElse(null);
    }


    /**
     * CREACIÓ DEL REGISTRE.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param nom valor de nom utilitzat pel mètode
     * @return registre resultant de l'operació
     */
    public UnitatMesura createUnitatMesura(String nom) {

        String nomNormalitzat = normalitzarNom(nom);

        validarNomUnitatMesura(nomNormalitzat);

        UnitatMesura unitatMesura = new UnitatMesura();
        unitatMesura.setNom(nomNormalitzat);

        return unitatMesuraRepository.save(unitatMesura);
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param nom valor de nom utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    public boolean existsByNom(String nom) {

        String nomNormalitzat = normalitzarNom(nom);

        if (nomNormalitzat == null || nomNormalitzat.isBlank()) {
            return false;
        }

        return unitatMesuraRepository.existsByNom(nomNormalitzat);
    }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param nom valor de nom utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    public String normalitzarNom(String nom) {

        if (nom == null) {
            return null;
        }

        return nom.trim().toLowerCase();
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param nom valor de nom utilitzat pel mètode
     */
    private void validarNomUnitatMesura(String nom) {

        if (nom == null || nom.isBlank()) {
            throw new RuntimeException(missatge("service.unitat.obligatoria"));
        }

        if (nom.length() > 4) {
            throw new RuntimeException(missatge("service.unitat.longitudMax"));
        }

        if (!nom.matches("^[a-z0-9]+$")) {
            throw new RuntimeException(missatge("service.unitat.caracters"));
        }

        if (unitatMesuraRepository.existsByNom(nom)) {
            throw new RuntimeException(missatge("service.unitat.duplicada"));
        }

        List<String> unitatsSemblants = obtenirUnitatsSemblants(nom);

        if (!unitatsSemblants.isEmpty()) {
            throw new RuntimeException(missatge("service.unitat.semblant", String.join(", ", unitatsSemblants)));
        }
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param nom valor de nom utilitzat pel mètode
     * @return llista de resultats obtinguda
     */
    private List<String> obtenirUnitatsSemblants(String nom) {

        List<String> unitatsSemblants = new ArrayList<>();

        for (UnitatMesura unitatMesura : unitatMesuraRepository.findAll()) {

            String nomExistent = unitatMesura.getNom();

            if (nomExistent != null
                    && !nomExistent.equals(nom)
                    && (nomExistent.contains(nom) || nom.contains(nomExistent))) {
                unitatsSemblants.add(nomExistent);
            }
        }

        return unitatsSemblants;
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
