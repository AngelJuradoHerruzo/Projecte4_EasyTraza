package cat.copernic.easytraza.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.AlbaraProveidorRepository;
import cat.copernic.easytraza.repository.UsuariRepository;
import jakarta.servlet.http.HttpSession;

@Service
@Transactional
public class AlbaraProveidorService {

    // ---------------------------- REPOSITORIS, SERVICES I CONFIGURACIÓ ----------------------------
    private final AlbaraProveidorRepository albaraProveidorRepository;
    private final UsuariRepository usuariRepository;
    private final UnitatMesuraService unitatMesuraService;

    private static final String DIRECTORI_FITXERS = "backend/uploads/albarans-proveidor";
    private static final String URL_FITXERS = "/uploads/albarans-proveidor/";

    public AlbaraProveidorService(AlbaraProveidorRepository albaraProveidorRepository,
                                  UsuariRepository usuariRepository,
                                  UnitatMesuraService unitatMesuraService) {
        this.albaraProveidorRepository = albaraProveidorRepository;
        this.usuariRepository = usuariRepository;
        this.unitatMesuraService = unitatMesuraService;
    }


    // OBTENIR TOTS ELS ALBARANS DE PROVEÏDOR ORDENATS PER PROVEÏDOR I ID
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


    // OBTENIR ALBARANS AGRUPATS PER PROVEÏDOR
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


    // AGRUPAR ALBARANS PER NOM DE PROVEÏDOR
    private Map<String, List<AlbaraProveidor>> agruparAlbaransPerProveidor(List<AlbaraProveidor> albarans) {

        Map<String, List<AlbaraProveidor>> albaransPerProveidor = new LinkedHashMap<>();

        for (AlbaraProveidor albara : albarans) {

            String nomProveidor = "Sense proveïdor";

            if (albara.getProveidor() != null && albara.getProveidor().getNomProveidor() != null) {
                nomProveidor = albara.getProveidor().getNomProveidor();
            }

            albaransPerProveidor
                    .computeIfAbsent(nomProveidor, key -> new ArrayList<>())
                    .add(albara);
        }

        return albaransPerProveidor;
    }


    // OBTENIR ALBARÀ DE PROVEÏDOR PER ID
    public AlbaraProveidor getAlbaraProveidorById(Long id) {
        Optional<AlbaraProveidor> albaraProveidor = albaraProveidorRepository.findById(id);
        return albaraProveidor.orElse(null);
    }


    // OBTENIR ALBARÀ DE PROVEÏDOR AMB DETALL
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
                                                 HttpSession session) {

        albaraProveidor.setUsuariReceptor(obtenirUsuariReceptorSessio(session));
        prepararNumeroAlbara(albaraProveidor);
        prepararAlbaraPerGuardar(albaraProveidor);

        AlbaraProveidor albaraGuardat = albaraProveidorRepository.save(albaraProveidor);
        guardarFitxerAlbara(albaraGuardat, imatgeAlbara);

        return albaraProveidorRepository.save(albaraGuardat);
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor updateAlbaraProveidor(Long id,
                                                 AlbaraProveidor albaraProveidor,
                                                 MultipartFile imatgeAlbara,
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

        guardarFitxerAlbara(albaraActual, imatgeAlbara);

        return albaraProveidorRepository.save(albaraActual);
    }


    // ELIMINAR ALBARÀ DE PROVEÏDOR
    public void deleteAlbaraProveidor(Long id) {

        Optional<AlbaraProveidor> albaraProveidorOpt = albaraProveidorRepository.findById(id);

        if (albaraProveidorOpt.isPresent()) {
            validarAlbaraModificable(albaraProveidorOpt.get());
            albaraProveidorRepository.deleteById(id);
        }
    }


    // COMPROVAR SI L'ALBARÀ ES POT MODIFICAR O ELIMINAR
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


    // OBTENIR NÚMERO D'ALBARÀ PER MOSTRAR A LA WEB
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


    // VALIDAR SI L'ALBARÀ ES POT MODIFICAR O ELIMINAR
    public void validarAlbaraModificable(AlbaraProveidor albaraProveidor) {

        if (!esModificable(albaraProveidor)) {
            throw new RuntimeException("No es pot modificar o eliminar aquest albarà perquè té algun lot que no està en estoc.");
        }
    }


    // COMPROVAR FILTRE PER PROVEÏDOR
    private boolean coincideixProveidor(AlbaraProveidor albaraProveidor, String proveidor) {

        if (filtreBuit(proveidor)) {
            return true;
        }

        String nomProveidor = albaraProveidor.getProveidor() != null
                ? albaraProveidor.getProveidor().getNomProveidor()
                : null;

        return conteText(nomProveidor, proveidor);
    }


    // COMPROVAR FILTRE PER NÚMERO D'ALBARÀ
    private boolean coincideixNumeroAlbara(AlbaraProveidor albaraProveidor, String numeroAlbara) {

        if (filtreBuit(numeroAlbara)) {
            return true;
        }

        return conteText(albaraProveidor.getNumeroAlbara(), numeroAlbara)
                || conteText(obtenirNumeroAlbaraVisible(albaraProveidor), numeroAlbara);
    }


    // COMPROVAR FILTRE PER IDENTIFICADOR DE LOT
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


    // COMPROVAR FILTRE PER DATA DE RECEPCIÓ
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


    // COMPROVAR FILTRE PER USUARI RECEPTOR
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


    // COMPROVAR SI UN FILTRE ESTÀ BUIT
    private boolean filtreBuit(String valor) {
        return valor == null || valor.isBlank();
    }


    // COMPROVAR SI UN TEXT CONTÉ UN FILTRE
    private boolean conteText(String text, String filtre) {

        if (text == null || filtreBuit(filtre)) {
            return false;
        }

        return text.toLowerCase().contains(filtre.trim().toLowerCase());
    }


    // PREPARAR NÚMERO D'ALBARÀ ABANS DE VALIDAR I GUARDAR
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


    // PREPARAR ALBARÀ I LOTS ABANS DE GUARDAR
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


    // SUBSTITUIR LOTS D'UN ALBARÀ MODIFICABLE
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


    // CREAR UN LOT NOU A PARTIR DE LES DADES DEL FORMULARI
    private LotProveidor crearLotNouDesDeFormulari(LotProveidor lotFormulari) {

        LotProveidor lotNou = new LotProveidor();

        lotNou.setIdentificadorLot(lotFormulari.getIdentificadorLot());
        lotNou.setQuantitat(lotFormulari.getQuantitat());
        lotNou.setUnitats(lotFormulari.getUnitats());
        lotNou.setMateriaPrimera(lotFormulari.getMateriaPrimera());
        lotNou.setDataCaducitat(lotFormulari.getDataCaducitat());

        return lotNou;
    }


    // PREPARAR LOT ABANS DE GUARDAR
    private void prepararLotPerGuardar(AlbaraProveidor albaraProveidor, LotProveidor lot) {
        validarDadesLotProveidor(lot);

        lot.setIdentificadorLot(lot.getIdentificadorLot().trim());
        lot.setUnitats(unitatMesuraService.normalitzarNom(lot.getUnitats()));
        lot.setEstat(EstatLot.EN_ESTOC);
        lot.setDataObertura(null);
        lot.setDataAcabament(null);
        lot.setAlbaraProveidor(albaraProveidor);
    }


    // OBTENIR USUARI RECEPTOR DES DE LA SESSIÓ
    private Usuari obtenirUsuariReceptorSessio(HttpSession session) {

        Long usuariId = session == null ? null : (Long) session.getAttribute("usuariId");

        if (usuariId == null) {
            throw new RuntimeException("No s'ha trobat cap usuari autenticat a la sessió.");
        }

        return usuariRepository.findById(usuariId)
                .orElseThrow(() -> new RuntimeException("L'usuari receptor de la sessió no existeix."));
    }


    // VALIDAR DADES DE L'ALBARÀ DE PROVEÏDOR
    private void validarDadesAlbaraProveidor(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor.getDataRecepcio() == null) {
            throw new RuntimeException("La data de recepció és obligatòria.");
        }

        if (albaraProveidor.getNumeroAlbara() == null || albaraProveidor.getNumeroAlbara().isBlank()) {
            throw new RuntimeException("El número d'albarà és obligatori.");
        }

        if (albaraProveidor.getProveidor() == null || albaraProveidor.getProveidor().getId() == null) {
            throw new RuntimeException("El proveïdor és obligatori.");
        }

        if (albaraProveidor.getUsuariReceptor() == null || albaraProveidor.getUsuariReceptor().getId() == null) {
            throw new RuntimeException("L'usuari receptor no s'ha pogut assignar des de la sessió.");
        }

        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            throw new RuntimeException("L'albarà ha de tenir com a mínim un lot.");
        }
    }


    // VALIDAR DADES DEL LOT DE PROVEÏDOR
    private void validarDadesLotProveidor(LotProveidor lotProveidor) {

        if (lotProveidor.getIdentificadorLot() == null || lotProveidor.getIdentificadorLot().isBlank()) {
            throw new RuntimeException("L'identificador del lot és obligatori.");
        }

        if (lotProveidor.getMateriaPrimera() == null || lotProveidor.getMateriaPrimera().getId() == null) {
            throw new RuntimeException("La matèria primera és obligatòria.");
        }

        if (lotProveidor.getQuantitat() == null) {
            throw new RuntimeException("La quantitat és obligatòria.");
        }

        if (lotProveidor.getQuantitat() <= 0) {
            throw new RuntimeException("La quantitat ha de ser superior a zero.");
        }

        lotProveidor.setUnitats(unitatMesuraService.normalitzarNom(lotProveidor.getUnitats()));

        if (lotProveidor.getUnitats() == null || lotProveidor.getUnitats().isBlank()) {
            throw new RuntimeException("Les unitats són obligatòries.");
        }

        if (!unitatMesuraService.existsByNom(lotProveidor.getUnitats())) {
            throw new RuntimeException("La unitat de mesura seleccionada no existeix.");
        }
    }


    // GUARDAR FITXER PUJAT DES DEL FORMULARI
    private void guardarFitxerAlbara(AlbaraProveidor albaraProveidor, MultipartFile imatgeAlbara) {

        if (imatgeAlbara == null || imatgeAlbara.isEmpty()) {
            return;
        }

        try {
            Files.createDirectories(Paths.get(DIRECTORI_FITXERS));

            String extensio = obtenirExtensioFitxer(imatgeAlbara.getOriginalFilename());
            String data = albaraProveidor.getDataRecepcio().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nomFitxer = "albara_" + albaraProveidor.getId() + "_" + data + "_" + UUID.randomUUID() + extensio;

            Path rutaFitxer = Paths.get(DIRECTORI_FITXERS, nomFitxer);
            Files.write(rutaFitxer, imatgeAlbara.getBytes());

            if (albaraProveidor.getFitxers() == null) {
                albaraProveidor.setFitxers(new ArrayList<>());
            }

            albaraProveidor.getFitxers().clear();
            albaraProveidor.getFitxers().add(URL_FITXERS + nomFitxer);
        }
        catch (IOException e) {
            throw new RuntimeException("No s'ha pogut guardar el fitxer de l'albarà.");
        }
    }


    // OBTENIR L'EXTENSIÓ DEL FITXER ORIGINAL
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
}
