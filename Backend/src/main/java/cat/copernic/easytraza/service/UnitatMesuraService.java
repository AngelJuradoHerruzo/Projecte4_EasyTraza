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


    // CREAR UNITAT DE MESURA
    public UnitatMesura createUnitatMesura(String nom) {

        String nomNormalitzat = normalitzarNom(nom);

        validarNomUnitatMesura(nomNormalitzat);

        UnitatMesura unitatMesura = new UnitatMesura();
        unitatMesura.setNom(nomNormalitzat);

        return unitatMesuraRepository.save(unitatMesura);
    }


    // COMPROVAR SI EXISTEIX UNA UNITAT DE MESURA
    public boolean existsByNom(String nom) {

        String nomNormalitzat = normalitzarNom(nom);

        if (nomNormalitzat == null || nomNormalitzat.isBlank()) {
            return false;
        }

        return unitatMesuraRepository.existsByNom(nomNormalitzat);
    }


    // NORMALITZAR NOM DE LA UNITAT
    public String normalitzarNom(String nom) {

        if (nom == null) {
            return null;
        }

        return nom.trim().toLowerCase();
    }


    // VALIDAR DADES DE LA UNITAT DE MESURA
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


    // OBTENIR UNITATS AMB NOM SEMBLANT
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

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
