package cat.copernic.easytraza.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import cat.copernic.easytraza.dto.OcrLiniaDto;
import cat.copernic.easytraza.dto.OcrResultatAlbaraProveidorDto;
import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.AlbaraProveidorRepository;
import cat.copernic.easytraza.repository.UsuariRepository;
import cat.copernic.easytraza.service.ocr.OcrUtils;
import jakarta.servlet.http.HttpSession;

/**
 * SERVEI D'ALBARANS DE PROVEÏDOR.
 *
 * Gestionades les operacions dels albarans de proveïdor i dels lots rebuts.
 * També preparades les dades procedents del procés OCR i les validacions prèvies al guardat.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class AlbaraProveidorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbaraProveidorService.class);

    // ---------------------------- REPOSITORIS, SERVICES I CONFIGURACIÓ ----------------------------
    private final AlbaraProveidorRepository albaraProveidorRepository;
    private final UsuariRepository usuariRepository;
    private final UnitatMesuraService unitatMesuraService;
    private final ProveidorService proveidorService;
    private final MateriaPrimeraService materiaPrimeraService;
    private final OcrAlbaraProveidorService ocrAlbaraProveidorService;
    private final MessageSource messageSource;

    @Value("${ocr.documents.final-path:backend/uploads/albarans-proveidor}")
    private String directoriFitxers;

    private static final String URL_FITXERS = "/uploads/albarans-proveidor/";

    public AlbaraProveidorService(AlbaraProveidorRepository albaraProveidorRepository,
                                  UsuariRepository usuariRepository,
                                  UnitatMesuraService unitatMesuraService,
                                  ProveidorService proveidorService,
                                  MateriaPrimeraService materiaPrimeraService,
                                  OcrAlbaraProveidorService ocrAlbaraProveidorService,
                                  MessageSource messageSource) {
        this.albaraProveidorRepository = albaraProveidorRepository;
        this.usuariRepository = usuariRepository;
        this.unitatMesuraService = unitatMesuraService;
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
        this.ocrAlbaraProveidorService = ocrAlbaraProveidorService;
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
    public List<AlbaraProveidor> getAllAlbaransProveidor() {

        List<AlbaraProveidor> albarans = albaraProveidorRepository.findAll();

        Comparator<AlbaraProveidor> comparadorProveidor = Comparator.comparing(
                albara -> albara.getProveidor() != null && albara.getProveidor().getNomProveidor() != null
                        ? albara.getProveidor().getNomProveidor().toLowerCase()
                        : ""
        );

        Comparator<AlbaraProveidor> comparadorIdDesc = Comparator.comparing(
                AlbaraProveidor::getId,
                Comparator.nullsLast(Long::compareTo)
        ).reversed();

        albarans.sort(comparadorProveidor.thenComparing(comparadorIdDesc));

        return albarans;
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @return llista de resultats obtinguda
     */
    public Map<String, List<AlbaraProveidor>> getAlbaransAgrupatsPerProveidor() {
        return agruparAlbaransPerProveidor(getAllAlbaransProveidor());
    }


    // OBTENIR ALBARANS AGRUPATS PER PROVEÏDOR AMB FILTRES
    public Map<String, List<AlbaraProveidor>> getAlbaransAgrupatsPerProveidor(String proveidor,
                                                                              String numeroAlbara,
                                                                              String identificadorLot,
                                                                              String dataRecepcio,
                                                                              String receptor) {
        List<AlbaraProveidor> albaransFiltrats = getAllAlbaransProveidor().stream()
                .filter(albara -> coincideixProveidor(albara, proveidor))
                .filter(albara -> coincideixNumeroAlbara(albara, numeroAlbara))
                .filter(albara -> coincideixIdentificadorLot(albara, identificadorLot))
                .filter(albara -> coincideixDataRecepcio(albara, dataRecepcio))
                .filter(albara -> coincideixReceptor(albara, receptor))
                .toList();

        return agruparAlbaransPerProveidor(albaransFiltrats);
    }


    /**
     * AGRUPACIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param albarans valor de albarans utilitzat pel mètode
     * @return llista de resultats obtinguda
     */
    private Map<String, List<AlbaraProveidor>> agruparAlbaransPerProveidor(List<AlbaraProveidor> albarans) {

        Map<String, List<AlbaraProveidor>> albaransPerProveidor = new LinkedHashMap<>();

        for (AlbaraProveidor albara : albarans) {

            String nomProveidor = missatge("service.albaraProveidor.senseProveidor");

            if (albara.getProveidor() != null && albara.getProveidor().getNomProveidor() != null) {
                nomProveidor = albara.getProveidor().getNomProveidor();
            }

            albaransPerProveidor
                    .computeIfAbsent(nomProveidor, key -> new ArrayList<>())
                    .add(albara);
        }

        return albaransPerProveidor;
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
    public AlbaraProveidor getAlbaraProveidorById(Long id) {
        Optional<AlbaraProveidor> albaraProveidor = albaraProveidorRepository.findById(id);
        return albaraProveidor.orElse(null);
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
    public AlbaraProveidor getAlbaraProveidorDetallById(Long id) {

        Optional<AlbaraProveidor> albaraProveidorOpt = albaraProveidorRepository.findById(id);

        if (albaraProveidorOpt.isEmpty()) {
            return null;
        }

        AlbaraProveidor albaraProveidor = albaraProveidorOpt.get();

        if (albaraProveidor.getLots() != null) {
            albaraProveidor.getLots().size();

            for (LotProveidor lot : albaraProveidor.getLots()) {
                if (lot.getMateriaPrimera() != null) {
                    lot.getMateriaPrimera().getId();
                }
            }
        }

        if (albaraProveidor.getFitxers() != null) {
            albaraProveidor.getFitxers().size();
        }

        return albaraProveidor;
    }


    // CREAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor createAlbaraProveidor(AlbaraProveidor albaraProveidor,
                                                 MultipartFile imatgeAlbara,
                                                 String ocrDocumentTemporalId,
                                                 HttpSession session) {

        albaraProveidor.setUsuariReceptor(obtenirUsuariReceptorSessio(session));
        prepararNumeroAlbara(albaraProveidor);
        prepararAlbaraPerGuardar(albaraProveidor);

        AlbaraProveidor albaraGuardat = albaraProveidorRepository.save(albaraProveidor);
        guardarFitxerAlbara(albaraGuardat, imatgeAlbara, ocrDocumentTemporalId);

        return albaraProveidorRepository.save(albaraGuardat);
    }


    // CREAR ALBARÀ DE PROVEÏDOR SENSE DOCUMENT OCR TEMPORAL
    public AlbaraProveidor createAlbaraProveidor(AlbaraProveidor albaraProveidor,
                                                 MultipartFile imatgeAlbara,
                                                 HttpSession session) {
        return createAlbaraProveidor(albaraProveidor, imatgeAlbara, null, session);
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor updateAlbaraProveidor(Long id,
                                                 AlbaraProveidor albaraProveidor,
                                                 MultipartFile imatgeAlbara,
                                                 String ocrDocumentTemporalId,
                                                 HttpSession session) {

        AlbaraProveidor albaraActual = albaraProveidorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Albarà de proveïdor no trobat."));

        validarAlbaraModificable(albaraActual);

        albaraActual.setDataRecepcio(albaraProveidor.getDataRecepcio());
        albaraActual.setNumeroAlbara(albaraProveidor.getNumeroAlbara());
        albaraActual.setProveidor(albaraProveidor.getProveidor());

        if (albaraActual.getUsuariReceptor() == null) {
            albaraActual.setUsuariReceptor(obtenirUsuariReceptorSessio(session));
        }

        prepararNumeroAlbara(albaraActual);
        substituirLots(albaraActual, albaraProveidor.getLots());
        validarDadesAlbaraProveidor(albaraActual);

        guardarFitxerAlbara(albaraActual, imatgeAlbara, ocrDocumentTemporalId);

        return albaraProveidorRepository.save(albaraActual);
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR SENSE DOCUMENT OCR TEMPORAL
    public AlbaraProveidor updateAlbaraProveidor(Long id,
                                                 AlbaraProveidor albaraProveidor,
                                                 MultipartFile imatgeAlbara,
                                                 HttpSession session) {
        return updateAlbaraProveidor(id, albaraProveidor, imatgeAlbara, null, session);
    }


    /**
     * ELIMINACIÓ DEL REGISTRE.
     *
     * Eliminat el registre identificat quan el servei permet completar
     * l'operació sol·licitada.
     *
     * @param id identificador utilitzat en l'operació
     */
    public void deleteAlbaraProveidor(Long id) {

        Optional<AlbaraProveidor> albaraProveidorOpt = albaraProveidorRepository.findById(id);

        if (albaraProveidorOpt.isPresent()) {
            validarAlbaraModificable(albaraProveidorOpt.get());
            albaraProveidorRepository.deleteById(id);
        }
    }


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    public boolean esModificable(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor == null || albaraProveidor.getLots() == null) {
            return true;
        }

        for (LotProveidor lot : albaraProveidor.getLots()) {
            if (lot.getEstat() != EstatLot.EN_ESTOC) {
                return false;
            }
        }

        return true;
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    public String obtenirNumeroAlbaraVisible(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor == null) {
            return "-";
        }

        String numero = albaraProveidor.getNumeroAlbara();

        if (numero == null || numero.isBlank()) {
            return albaraProveidor.getId() != null ? albaraProveidor.getId().toString() : "-";
        }

        return numero.trim()
                .replaceFirst("(?i)^MANUAL-", "")
                .replaceFirst("(?i)^ALBAR[AÀ]-", "")
                .replaceFirst("(?i)^ALBARA-", "")
                .trim();
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     */
    public void validarAlbaraModificable(AlbaraProveidor albaraProveidor) {

        if (!esModificable(albaraProveidor)) {
            throw new RuntimeException(missatge("service.albaraProveidor.lotsNoEstoc"));
        }
    }


    /**
     * NETEJA DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param resultatOcr valor de resultatOcr utilitzat pel mètode
     */
    public void netejarAvisosAssociacioOcr(OcrResultatAlbaraProveidorDto resultatOcr) {
        if (resultatOcr == null || resultatOcr.getAlbaraPendent() == null) {
            return;
        }

        OcrAlbaraPendent pendent = resultatOcr.getAlbaraPendent();
        pendent.getAvisos().removeIf(avis -> avis != null
                && avis.startsWith("Proveïdor detectat per OCR no trobat"));

        if (pendent.getLinies() != null) {
            for (OcrLiniaDto linia : pendent.getLinies()) {
                linia.getAvisos().removeIf(avis -> avis != null
                        && avis.startsWith("Matèria primera no trobada"));
            }
        }
    }


    // APLICAR ASSOCIACIONS NOVES SENSE SOBREESCRIURE CANVIS MANUALS DE L'USUARI
    public void aplicarAssociacionsOcrAlFormulari(AlbaraProveidor albaraProveidor,
                                                    OcrResultatAlbaraProveidorDto resultatOcr) {
        OcrAlbaraPendent pendent = resultatOcr.getAlbaraPendent();

        if ((albaraProveidor.getProveidor() == null || albaraProveidor.getProveidor().getId() == null)
                && pendent.getProveidorId() != null) {
            albaraProveidor.setProveidor(proveidorService.getProveidorById(pendent.getProveidorId()));
        }

        if (albaraProveidor.getLots() == null || pendent.getLinies() == null) {
            return;
        }

        int numeroLinies = Math.min(albaraProveidor.getLots().size(), pendent.getLinies().size());

        for (int index = 0; index < numeroLinies; index++) {
            LotProveidor lotFormulari = albaraProveidor.getLots().get(index);
            OcrLiniaDto liniaOcr = pendent.getLinies().get(index);

            if ((lotFormulari.getMateriaPrimera() == null || lotFormulari.getMateriaPrimera().getId() == null)
                    && liniaOcr.getMateriaPrimeraId() != null) {
                lotFormulari.setMateriaPrimera(
                        materiaPrimeraService.getMateriaPrimeraById(liniaOcr.getMateriaPrimeraId())
                );
            }
        }
    }


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param resultatOcr valor de resultatOcr utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    public AlbaraProveidor convertirResultatOcrAFormulari(OcrResultatAlbaraProveidorDto resultatOcr) {
        AlbaraProveidor albaraProveidor = new AlbaraProveidor();
        OcrAlbaraPendent pendent = resultatOcr.getAlbaraPendent();

        albaraProveidor.setNumeroAlbara(pendent.getNumeroAlbara());
        albaraProveidor.setDataRecepcio(convertirDataOcr(pendent.getDataAlbara()));

        if (pendent.getProveidorId() != null) {
            albaraProveidor.setProveidor(proveidorService.getProveidorById(pendent.getProveidorId()));
        }

        List<LotProveidor> lots = new ArrayList<>();

        if (pendent.getLinies() != null) {
            for (OcrLiniaDto liniaOcr : pendent.getLinies()) {
                lots.add(convertirLiniaOcrAFormulari(liniaOcr));
            }
        }

        if (lots.isEmpty()) {
            lots.add(new LotProveidor());
        }

        albaraProveidor.setLots(lots);

        return albaraProveidor;
    }


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param liniaOcr valor de liniaOcr utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private LotProveidor convertirLiniaOcrAFormulari(OcrLiniaDto liniaOcr) {
        LotProveidor lot = new LotProveidor();
        lot.setIdentificadorLot(liniaOcr.getIdentificadorLot());
        lot.setQuantitat(convertirQuantitatOcr(liniaOcr.getQuantitat()));
        lot.setUnitats(liniaOcr.getUnitat());

        if (liniaOcr.getMateriaPrimeraId() != null) {
            lot.setMateriaPrimera(materiaPrimeraService.getMateriaPrimeraById(liniaOcr.getMateriaPrimeraId()));
        }

        return lot;
    }


    /**
     * COMPLECIÓ DE DADES.
     *
     * Incorporada o completada la informació necessària dins de l'objecte
     * que s'està preparant.
     *
     * @param resultatOcr valor de resultatOcr utilitzat pel mètode
     */
    public void completarAssociacionsOcr(OcrResultatAlbaraProveidorDto resultatOcr) {
        if (resultatOcr == null || resultatOcr.getAlbaraPendent() == null) {
            return;
        }

        OcrAlbaraPendent pendent = resultatOcr.getAlbaraPendent();
        associarProveidorOcr(pendent);
        associarMateriesPrimeresOcr(pendent);
    }


    /**
     * ASSOCIACIÓ DE DADES.
     *
     * Incorporada o completada la informació necessària dins de l'objecte
     * que s'està preparant.
     *
     * @param pendent valor de pendent utilitzat pel mètode
     */
    private void associarProveidorOcr(OcrAlbaraPendent pendent) {
        Proveidor proveidor = buscarProveidor(pendent.getProveidorCifDetectat(), pendent.getProveidorDetectat());

        if (proveidor != null) {
            pendent.setProveidorId(proveidor.getId());
            pendent.setProveidorNomAssociat(proveidor.getNomProveidor());
            pendent.setProveidorTrobat(true);
            return;
        }

        pendent.setProveidorTrobat(false);

        if (pendent.getProveidorDetectat() != null && !pendent.getProveidorDetectat().isBlank()) {
            pendent.afegirAvis(missatge("service.albaraProveidor.ocr.proveidorNoTrobat", pendent.getProveidorDetectat()));
        }
    }


    /**
     * ASSOCIACIÓ DE DADES.
     *
     * Incorporada o completada la informació necessària dins de l'objecte
     * que s'està preparant.
     *
     * @param pendent valor de pendent utilitzat pel mètode
     */
    private void associarMateriesPrimeresOcr(OcrAlbaraPendent pendent) {
        if (pendent.getLinies() == null) {
            return;
        }

        for (OcrLiniaDto linia : pendent.getLinies()) {
            MateriaPrimera materiaPrimera = buscarMateriaPrimera(linia.getMateriaPrimeraDetectada());

            if (materiaPrimera != null) {
                linia.setMateriaPrimeraId(materiaPrimera.getId());
                linia.setMateriaPrimeraNomAssociada(materiaPrimera.getNomMateria());
                linia.setMateriaPrimeraTrobada(true);
                continue;
            }

            linia.setMateriaPrimeraTrobada(false);

            if (linia.getMateriaPrimeraDetectada() != null && !linia.getMateriaPrimeraDetectada().isBlank()) {
                linia.afegirAvis(missatge("service.albaraProveidor.ocr.materiaNoTrobada", linia.getMateriaPrimeraDetectada()));
            }
        }
    }


    /**
     * CERCA DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param cifDetectat valor de cifDetectat utilitzat pel mètode
     * @param nomDetectat valor de nomDetectat utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private Proveidor buscarProveidor(String cifDetectat, String nomDetectat) {
        for (Proveidor proveidor : proveidorService.getAllProveidors()) {
            if (cifDetectat != null && !cifDetectat.isBlank()
                    && proveidor.getCif() != null
                    && proveidor.getCif().replace(" ", "").equalsIgnoreCase(cifDetectat.replace(" ", ""))) {
                return proveidor;
            }
        }

        if (nomDetectat == null || nomDetectat.isBlank()) {
            return null;
        }

        String nomOcr = OcrUtils.normalitzarPerComparar(nomDetectat);

        for (Proveidor proveidor : proveidorService.getAllProveidors()) {
            String nomBd = OcrUtils.normalitzarPerComparar(proveidor.getNomProveidor());

            if (nomBd.equals(nomOcr)
                    || nomBd.contains(nomOcr)
                    || nomOcr.contains(nomBd)
                    || OcrUtils.coincideixNomFlexible(nomDetectat, proveidor.getNomProveidor())) {
                return proveidor;
            }
        }

        return null;
    }


    /**
     * CERCA DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param materiaDetectada valor de materiaDetectada utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private MateriaPrimera buscarMateriaPrimera(String materiaDetectada) {
        if (materiaDetectada == null || materiaDetectada.isBlank()) {
            return null;
        }

        String materiaOcr = OcrUtils.normalitzarPerComparar(materiaDetectada);

        for (MateriaPrimera materiaPrimera : materiaPrimeraService.getAllMateriesPrimeres()) {
            String materiaBd = OcrUtils.normalitzarPerComparar(materiaPrimera.getNomMateria());

            if (materiaBd.equals(materiaOcr)
                    || materiaBd.contains(materiaOcr)
                    || materiaOcr.contains(materiaBd)
                    || OcrUtils.coincideixNomFlexible(materiaDetectada, materiaPrimera.getNomMateria())) {
                return materiaPrimera;
            }
        }

        return null;
    }


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param dataOcr valor de dataOcr utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private LocalDate convertirDataOcr(String dataOcr) {
        if (dataOcr == null || dataOcr.isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(dataOcr);
        }
        catch (DateTimeParseException ignored) {
            // Continua amb altres formats habituals.
        }

        for (String patro : List.of("dd/MM/yyyy", "dd-MM-yyyy", "d/M/yyyy", "d-M-yyyy")) {
            try {
                return LocalDate.parse(dataOcr, DateTimeFormatter.ofPattern(patro));
            }
            catch (DateTimeParseException ignored) {
                // Es prova el següent format.
            }
        }

        return LocalDate.now();
    }


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param quantitat valor de quantitat utilitzat pel mètode
     * @return valor numèric obtingut
     */
    private Integer convertirQuantitatOcr(Double quantitat) {
        if (quantitat == null) {
            return null;
        }

        return (int) Math.round(quantitat);
    }


    /**
     * COMPROVACIÓ DE COINCIDÈNCIA.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param proveidor valor de proveidor utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean coincideixProveidor(AlbaraProveidor albaraProveidor, String proveidor) {

        if (filtreBuit(proveidor)) {
            return true;
        }

        String nomProveidor = albaraProveidor.getProveidor() != null
                ? albaraProveidor.getProveidor().getNomProveidor()
                : null;

        return conteText(nomProveidor, proveidor);
    }


    /**
     * COMPROVACIÓ DE COINCIDÈNCIA.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param numeroAlbara valor de numeroAlbara utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean coincideixNumeroAlbara(AlbaraProveidor albaraProveidor, String numeroAlbara) {

        if (filtreBuit(numeroAlbara)) {
            return true;
        }

        return conteText(albaraProveidor.getNumeroAlbara(), numeroAlbara)
                || conteText(obtenirNumeroAlbaraVisible(albaraProveidor), numeroAlbara);
    }


    /**
     * COMPROVACIÓ DE COINCIDÈNCIA.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param identificadorLot valor de identificadorLot utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean coincideixIdentificadorLot(AlbaraProveidor albaraProveidor, String identificadorLot) {

        if (filtreBuit(identificadorLot)) {
            return true;
        }

        if (albaraProveidor.getLots() == null) {
            return false;
        }

        for (LotProveidor lot : albaraProveidor.getLots()) {
            if (conteText(lot.getIdentificadorLot(), identificadorLot)) {
                return true;
            }
        }

        return false;
    }


    /**
     * COMPROVACIÓ DE COINCIDÈNCIA.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param dataRecepcio valor de dataRecepcio utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean coincideixDataRecepcio(AlbaraProveidor albaraProveidor, String dataRecepcio) {

        if (filtreBuit(dataRecepcio)) {
            return true;
        }

        if (albaraProveidor.getDataRecepcio() == null) {
            return false;
        }

        String dataIso = albaraProveidor.getDataRecepcio().toString();
        String dataFormatada = albaraProveidor.getDataRecepcio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        return conteText(dataIso, dataRecepcio) || conteText(dataFormatada, dataRecepcio);
    }


    /**
     * COMPROVACIÓ DE COINCIDÈNCIA.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param receptor valor de receptor utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean coincideixReceptor(AlbaraProveidor albaraProveidor, String receptor) {

        if (filtreBuit(receptor)) {
            return true;
        }

        if (albaraProveidor.getUsuariReceptor() == null) {
            return false;
        }

        Usuari usuari = albaraProveidor.getUsuariReceptor();

        return conteText(usuari.getNomComplet(), receptor)
                || conteText(usuari.getEmail(), receptor)
                || conteText(usuari.getDni(), receptor);
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param valor valor que s'ha de processar
     * @return cert si es compleix la condició indicada
     */
    private boolean filtreBuit(String valor) {
        return valor == null || valor.isBlank();
    }


    /**
     * COMPROVACIÓ DE CONTINGUT.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param text text utilitzat en el procés
     * @param filtre valor utilitzat per filtrar les dades
     * @return cert si es compleix la condició indicada
     */
    private boolean conteText(String text, String filtre) {

        if (text == null || filtreBuit(filtre)) {
            return false;
        }

        return text.toLowerCase().contains(filtre.trim().toLowerCase());
    }


    /**
     * PREPARACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     */
    private void prepararNumeroAlbara(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor.getNumeroAlbara() != null && !albaraProveidor.getNumeroAlbara().isBlank()) {
            albaraProveidor.setNumeroAlbara(albaraProveidor.getNumeroAlbara().trim());
            return;
        }

        String data = albaraProveidor.getDataRecepcio() != null
                ? albaraProveidor.getDataRecepcio().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : "SENSE-DATA";

        albaraProveidor.setNumeroAlbara("MANUAL-" + data + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }


    /**
     * PREPARACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     */
    private void prepararAlbaraPerGuardar(AlbaraProveidor albaraProveidor) {
        validarDadesAlbaraProveidor(albaraProveidor);

        List<LotProveidor> lotsPreparats = new ArrayList<>();

        for (LotProveidor lotFormulari : albaraProveidor.getLots()) {
            LotProveidor lotNou = crearLotNouDesDeFormulari(lotFormulari);
            prepararLotPerGuardar(albaraProveidor, lotNou);
            lotsPreparats.add(lotNou);
        }

        albaraProveidor.setLots(lotsPreparats);
    }


    /**
     * SUBSTITUCIÓ DE DADES.
     *
     * Gestionat el fitxer o la dada associada al registre actual
     * segons l'operació requerida.
     *
     * @param albaraActual valor de albaraActual utilitzat pel mètode
     * @param lotsFormulari valor de lotsFormulari utilitzat pel mètode
     */
    private void substituirLots(AlbaraProveidor albaraActual, List<LotProveidor> lotsFormulari) {

        if (albaraActual.getLots() == null) {
            albaraActual.setLots(new ArrayList<>());
        }

        albaraActual.getLots().clear();

        if (lotsFormulari != null) {
            for (LotProveidor lotFormulari : lotsFormulari) {
                LotProveidor lotNou = crearLotNouDesDeFormulari(lotFormulari);
                prepararLotPerGuardar(albaraActual, lotNou);
                albaraActual.getLots().add(lotNou);
            }
        }
    }


    /**
     * CREACIÓ DE DADES.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param lotFormulari valor de lotFormulari utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private LotProveidor crearLotNouDesDeFormulari(LotProveidor lotFormulari) {

        LotProveidor lotNou = new LotProveidor();

        lotNou.setIdentificadorLot(lotFormulari.getIdentificadorLot());
        lotNou.setQuantitat(lotFormulari.getQuantitat());
        lotNou.setUnitats(lotFormulari.getUnitats());
        lotNou.setMateriaPrimera(lotFormulari.getMateriaPrimera());
        lotNou.setDataCaducitat(lotFormulari.getDataCaducitat());

        return lotNou;
    }


    /**
     * PREPARACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param lot valor de lot utilitzat pel mètode
     */
    private void prepararLotPerGuardar(AlbaraProveidor albaraProveidor, LotProveidor lot) {
        validarDadesLotProveidor(lot);

        lot.setIdentificadorLot(lot.getIdentificadorLot().trim());
        lot.setUnitats(unitatMesuraService.normalitzarNom(lot.getUnitats()));
        lot.setEstat(EstatLot.EN_ESTOC);
        lot.setDataObertura(null);
        lot.setDataAcabament(null);
        lot.setAlbaraProveidor(albaraProveidor);
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param session sessió HTTP de l'usuari actual
     * @return resultat obtingut pel mètode
     */
    private Usuari obtenirUsuariReceptorSessio(HttpSession session) {

        Long usuariId = session == null ? null : (Long) session.getAttribute("usuariId");

        if (usuariId == null) {
            throw new RuntimeException(missatge("service.albaraProveidor.usuariSessioNoTrobat"));
        }

        return usuariRepository.findById(usuariId)
                .orElseThrow(() -> new RuntimeException("L'usuari receptor de la sessió no existeix."));
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     */
    private void validarDadesAlbaraProveidor(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor.getDataRecepcio() == null) {
            throw new RuntimeException(missatge("service.albaraProveidor.dataRecepcioObligatoria"));
        }

        if (albaraProveidor.getNumeroAlbara() == null || albaraProveidor.getNumeroAlbara().isBlank()) {
            throw new RuntimeException(missatge("service.albaraProveidor.numeroObligatori"));
        }

        if (albaraProveidor.getProveidor() == null || albaraProveidor.getProveidor().getId() == null) {
            throw new RuntimeException(missatge("service.albaraProveidor.proveidorObligatori"));
        }

        if (albaraProveidor.getUsuariReceptor() == null || albaraProveidor.getUsuariReceptor().getId() == null) {
            throw new RuntimeException(missatge("service.albaraProveidor.receptorNoAssignat"));
        }

        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            throw new RuntimeException(missatge("service.albaraProveidor.lotMinim"));
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
    private void validarDadesLotProveidor(LotProveidor lotProveidor) {

        if (lotProveidor.getIdentificadorLot() == null || lotProveidor.getIdentificadorLot().isBlank()) {
            throw new RuntimeException(missatge("service.albaraProveidor.identificadorLotObligatori"));
        }

        if (lotProveidor.getMateriaPrimera() == null || lotProveidor.getMateriaPrimera().getId() == null) {
            throw new RuntimeException(missatge("service.albaraProveidor.materiaObligatoria"));
        }

        if (lotProveidor.getQuantitat() == null) {
            throw new RuntimeException(missatge("service.albaraProveidor.quantitatObligatoria"));
        }

        if (lotProveidor.getQuantitat() <= 0) {
            throw new RuntimeException(missatge("service.albaraProveidor.quantitatPositiva"));
        }

        lotProveidor.setUnitats(unitatMesuraService.normalitzarNom(lotProveidor.getUnitats()));

        if (lotProveidor.getUnitats() == null || lotProveidor.getUnitats().isBlank()) {
            throw new RuntimeException(missatge("service.albaraProveidor.unitatsObligatories"));
        }

        if (!unitatMesuraService.existsByNom(lotProveidor.getUnitats())) {
            throw new RuntimeException(missatge("service.albaraProveidor.unitatNoExisteix"));
        }
    }


    // GUARDAR FITXER PUJAT O DOCUMENT OCR TEMPORAL
    private void guardarFitxerAlbara(AlbaraProveidor albaraProveidor,
                                     MultipartFile imatgeAlbara,
                                     String ocrDocumentTemporalId) {

        if (imatgeAlbara != null && !imatgeAlbara.isEmpty()) {
            guardarFitxerPujatAlbara(albaraProveidor, imatgeAlbara);
            return;
        }

        if (ocrDocumentTemporalId != null && !ocrDocumentTemporalId.isBlank()) {
            moureDocumentOcrTemporalAlbara(albaraProveidor, ocrDocumentTemporalId);
        }
    }


    /**
     * GUARDAT DE DADES.
     *
     * Gestionat el fitxer o la dada associada al registre actual
     * segons l'operació requerida.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param imatgeAlbara valor de imatgeAlbara utilitzat pel mètode
     */
    private void guardarFitxerPujatAlbara(AlbaraProveidor albaraProveidor, MultipartFile imatgeAlbara) {

        try {
            Path directori = Paths.get(directoriFitxers).toAbsolutePath().normalize();
            Files.createDirectories(directori);

            String extensio = obtenirExtensioFitxer(imatgeAlbara.getOriginalFilename());
            String nomFitxer = generarNomFitxerAlbara(albaraProveidor, extensio);
            Path rutaFitxer = directori.resolve(nomFitxer).normalize();

            if (!rutaFitxer.startsWith(directori)) {
                throw new IOException("Ruta de fitxer no vàlida.");
            }

            Files.write(rutaFitxer, imatgeAlbara.getBytes());
            substituirFitxerAssociat(albaraProveidor, nomFitxer);
        }
        catch (IOException e) {
            LOGGER.error("No s'ha pogut guardar el fitxer de l'albarà.", e);
            throw new RuntimeException(missatge("service.albaraProveidor.guardarFitxer"));
        }
    }


    /**
     * MOVIMENT DE FITXER.
     *
     * Gestionat el fitxer o la dada associada al registre actual
     * segons l'operació requerida.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param ocrDocumentTemporalId identificador utilitzat en l'operació
     */
    private void moureDocumentOcrTemporalAlbara(AlbaraProveidor albaraProveidor, String ocrDocumentTemporalId) {

        try {
            Path origen = ocrAlbaraProveidorService.obtenirRutaDocumentTemporal(ocrDocumentTemporalId);

            if (!Files.exists(origen)) {
                throw new IOException("El document OCR temporal no existeix.");
            }

            Path directori = Paths.get(directoriFitxers).toAbsolutePath().normalize();
            Files.createDirectories(directori);

            String extensio = obtenirExtensioFitxer(ocrDocumentTemporalId);
            String nomFitxer = generarNomFitxerAlbara(albaraProveidor, extensio);
            Path desti = directori.resolve(nomFitxer).normalize();

            if (!desti.startsWith(directori)) {
                throw new IOException("Ruta de document OCR definitiva no vàlida.");
            }

            Files.move(origen, desti, StandardCopyOption.REPLACE_EXISTING);
            substituirFitxerAssociat(albaraProveidor, nomFitxer);
        }
        catch (IOException e) {
            LOGGER.error("No s'ha pogut guardar el document OCR de l'albarà.", e);
            throw new RuntimeException(missatge("service.albaraProveidor.guardarDocumentOcr"));
        }
    }


    /**
     * GENERACIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param extensio valor de extensio utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String generarNomFitxerAlbara(AlbaraProveidor albaraProveidor, String extensio) {
        String data = albaraProveidor.getDataRecepcio().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "albara_" + albaraProveidor.getId() + "_" + data + "_" + UUID.randomUUID() + extensio;
    }


    /**
     * SUBSTITUCIÓ DE DADES.
     *
     * Gestionat el fitxer o la dada associada al registre actual
     * segons l'operació requerida.
     *
     * @param albaraProveidor valor de albaraProveidor utilitzat pel mètode
     * @param nomFitxer fitxer rebut per al procés
     */
    private void substituirFitxerAssociat(AlbaraProveidor albaraProveidor, String nomFitxer) {
        if (albaraProveidor.getFitxers() == null) {
            albaraProveidor.setFitxers(new ArrayList<>());
        }

        albaraProveidor.getFitxers().clear();
        albaraProveidor.getFitxers().add(URL_FITXERS + nomFitxer);
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param nomOriginal valor de nomOriginal utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String obtenirExtensioFitxer(String nomOriginal) {

        if (nomOriginal == null || !nomOriginal.contains(".")) {
            return ".bin";
        }

        String extensio = nomOriginal.substring(nomOriginal.lastIndexOf(".")).toLowerCase();

        if (!extensio.matches("\\.[a-z0-9]{1,8}")) {
            return ".bin";
        }

        return extensio;
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
