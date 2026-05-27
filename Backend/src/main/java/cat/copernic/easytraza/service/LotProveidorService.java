package cat.copernic.easytraza.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.LotProveidorRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;

/**
 * SERVEI DE LOTS DE PROVEÏDOR.
 *
 * Gestionades les operacions de consulta, inici i finalització dels lots de proveïdor.
 * També controlades les validacions d'estat i de matèria primera associada.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class LotProveidorService {

    // ---------------------------- REPOSITORIS I CONSTRUCTOR ----------------------------
    private final LotProveidorRepository lotProveidorRepository;
    private final MateriaPrimeraRepository materiaPrimeraRepository;
    private final MessageSource messageSource;

    public LotProveidorService(LotProveidorRepository lotProveidorRepository,
                               MateriaPrimeraRepository materiaPrimeraRepository,
                               MessageSource messageSource) {
        this.lotProveidorRepository = lotProveidorRepository;
        this.materiaPrimeraRepository = materiaPrimeraRepository;
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
    public List<LotProveidor> getAllLotsProveidor() {
        return lotProveidorRepository.findAll();
    }


    // PREPARAR LLISTAT WEB DE LOTS AMB FILTRES OPCIONALS
    public List<LotProveidor> getLotsProveidorLlistat(Long materiaId,
                                                       String identificadorLot,
                                                       LocalDate dataCaducitat,
                                                       LocalDate dataObertura,
                                                       LocalDate dataAcabament) {

        List<LotProveidor> lots = new java.util.ArrayList<>(lotProveidorRepository.findAll());

        if (materiaId != null) {
            lots.removeIf(lot -> lot.getMateriaPrimera() == null || !materiaId.equals(lot.getMateriaPrimera().getId()));
        }

        if (identificadorLot != null && !identificadorLot.isBlank()) {
            lots.removeIf(lot -> !conteText(lot.getIdentificadorLot(), identificadorLot));
        }

        if (dataCaducitat != null) {
            lots.removeIf(lot -> lot.getDataCaducitat() == null || !dataCaducitat.equals(lot.getDataCaducitat()));
        }

        if (dataObertura != null) {
            lots.removeIf(lot -> lot.getDataObertura() == null || !dataObertura.equals(lot.getDataObertura()));
        }

        if (dataAcabament != null) {
            lots.removeIf(lot -> lot.getDataAcabament() == null || !dataAcabament.equals(lot.getDataAcabament()));
        }

        return lots;
    }


    /**
     * OBTENCIÓ DEL LLISTAT.
     *
     * Obtingut el conjunt de dades sol·licitat pel servei, aplicant
     * els filtres o l'ordenació corresponents quan és necessari.
     *
     * @return llista de resultats obtinguda
     */
    public List<MateriaPrimera> getAllMateriesPrimeresOrdenades() {
        return materiaPrimeraRepository.findAll(Sort.by("nomMateria").ascending());
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
    public LotProveidor getLotProveidorById(Long id) {
        return obtenirLotValidat(id);
    }


    /**
     * COMPROVACIÓ D'EXISTÈNCIA.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param id identificador utilitzat en l'operació
     * @return cert si es compleix la condició indicada
     */
    public boolean existeixLotObertMateixaMateria(Long id) {

        LotProveidor lotProveidor = obtenirLotValidat(id);

        validarLotPerIniciar(lotProveidor);

        Optional<LotProveidor> lotObertOpt = lotProveidorRepository
                .findFirstByMateriaPrimeraIdAndEstatAndIdNot(
                        lotProveidor.getMateriaPrimera().getId(),
                        EstatLot.OBERT,
                        lotProveidor.getId()
                );

        return lotObertOpt.isPresent();
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param id identificador utilitzat en l'operació
     * @param confirmarFinalitzacioAnterior valor de confirmarFinalitzacioAnterior utilitzat pel mètode
     * @return registre resultant de l'operació
     */
    public LotProveidor iniciarLot(Long id, boolean confirmarFinalitzacioAnterior) {

        LotProveidor lotProveidorActual = obtenirLotValidat(id);

        validarLotPerIniciar(lotProveidorActual);

        Optional<LotProveidor> lotObertOpt = lotProveidorRepository
                .findFirstByMateriaPrimeraIdAndEstatAndIdNot(
                        lotProveidorActual.getMateriaPrimera().getId(),
                        EstatLot.OBERT,
                        lotProveidorActual.getId()
                );

        if (lotObertOpt.isPresent()) {
            if (!confirmarFinalitzacioAnterior) {
                throw new RuntimeException(missatge("service.lot.duplicatObert"));
            }

            LotProveidor lotObertAnterior = lotObertOpt.get();
            lotObertAnterior.setEstat(EstatLot.ACABAT);
            lotObertAnterior.setDataAcabament(LocalDate.now());
            lotProveidorRepository.save(lotObertAnterior);
        }

        lotProveidorActual.setEstat(EstatLot.OBERT);
        lotProveidorActual.setDataObertura(LocalDate.now());

        return lotProveidorRepository.save(lotProveidorActual);
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param id identificador utilitzat en l'operació
     * @return registre resultant de l'operació
     */
    public LotProveidor finalitzarLot(Long id) {

        LotProveidor lotProveidor = obtenirLotValidat(id);

        validarLotPerFinalitzar(lotProveidor);

        lotProveidor.setEstat(EstatLot.ACABAT);
        lotProveidor.setDataAcabament(LocalDate.now());

        return lotProveidorRepository.save(lotProveidor);
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
    private LotProveidor obtenirLotValidat(Long id) {

        Optional<LotProveidor> lotProveidorOpt = lotProveidorRepository.findById(id);

        if (lotProveidorOpt.isEmpty()) {
            throw new RuntimeException(missatge("service.lot.noTrobat"));
        }

        return lotProveidorOpt.get();
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param lotProveidor valor de lotProveidor utilitzat pel mètode
     */
    private void validarLotPerIniciar(LotProveidor lotProveidor) {

        if (lotProveidor.getMateriaPrimera() == null) {
            throw new RuntimeException(missatge("service.lot.materiaObligatoria"));
        }

        if (lotProveidor.getEstat() == null) {
            throw new RuntimeException(missatge("service.lot.estatObligatori"));
        }

        if (lotProveidor.getEstat() != EstatLot.EN_ESTOC) {
            throw new RuntimeException(missatge("service.lot.iniciarEstoc"));
        }
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param lotProveidor valor de lotProveidor utilitzat pel mètode
     */
    private void validarLotPerFinalitzar(LotProveidor lotProveidor) {

        if (lotProveidor.getEstat() == null) {
            throw new RuntimeException(missatge("service.lot.estatObligatori"));
        }

        if (lotProveidor.getEstat() != EstatLot.OBERT) {
            throw new RuntimeException(missatge("service.lot.finalitzarObert"));
        }
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