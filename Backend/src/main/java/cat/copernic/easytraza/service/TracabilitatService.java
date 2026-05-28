package cat.copernic.easytraza.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.AlbaraClient;
import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.entities.Producte;
import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.enums.EstatAlbaraClient;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.AlbaraClientRepository;
import cat.copernic.easytraza.repository.LotProveidorRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;
import cat.copernic.easytraza.repository.ProducteRepository;
import cat.copernic.easytraza.repository.ProveidorRepository;

/**
 * SERVEI DE TRAÇABILITAT.
 *
 * Gestionades les consultes necessàries per obtenir informació de producció i seguiment associada als lots.
 * També preparades les dades utilitzades en l'explotació i consulta de la traçabilitat.
 *
 * @author Ángel Jurado Herruzo
 */
@Service
@Transactional
public class TracabilitatService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final LotProveidorRepository lotProveidorRepository;
    private final AlbaraClientRepository albaraClientRepository;
    private final MateriaPrimeraRepository materiaPrimeraRepository;
    private final ProveidorRepository proveidorRepository;
    private final ProducteRepository producteRepository;
    private final MessageSource messageSource;

    public TracabilitatService(LotProveidorRepository lotProveidorRepository,
                               AlbaraClientRepository albaraClientRepository,
                               MateriaPrimeraRepository materiaPrimeraRepository,
                               ProveidorRepository proveidorRepository,
                               ProducteRepository producteRepository,
                               MessageSource messageSource) {
        this.lotProveidorRepository = lotProveidorRepository;
        this.albaraClientRepository = albaraClientRepository;
        this.materiaPrimeraRepository = materiaPrimeraRepository;
        this.proveidorRepository = proveidorRepository;
        this.producteRepository = producteRepository;
        this.messageSource = messageSource;
    }


    // CARREGAR MODEL COMPLET DE TRAÇABILITAT
    public Map<String, Object> getModelTracabilitat(Long materiaId,
                                                    Long proveidorId,
                                                    EstatLot estat,
                                                    String identificadorLot,
                                                    LocalDate dataRecepcio,
                                                    Long lotId,
                                                    Boolean buscar,
                                                    String sortField,
                                                    String sortDir,
                                                    Long producteGraficId,
                                                    String mesGrafic) {

        Map<String, Object> model = new LinkedHashMap<>();

        boolean cercaRealitzada = Boolean.TRUE.equals(buscar);

        sortField = normalitzarSortField(sortField);
        sortDir = normalitzarSortDir(sortDir);

        model.put("materiesPrimeres", getAllMateriesPrimeresOrdenades());
        model.put("proveidors", getAllProveidorsOrdenats());
        model.put("estats", getEstatsLot());
        model.put("productesGrafic", getAllProductesOrdenats());

        if (cercaRealitzada) {
            model.put("lots", getLotsFiltrats(
                    materiaId,
                    proveidorId,
                    estat,
                    identificadorLot,
                    dataRecepcio,
                    sortField,
                    sortDir
            ));
        } else {
            model.put("lots", new ArrayList<LotProveidor>());
        }

        model.put("buscar", cercaRealitzada);

        model.put("materiaId", materiaId);
        model.put("proveidorId", proveidorId);
        model.put("estat", estat);
        model.put("identificadorLot", identificadorLot);
        model.put("dataRecepcio", dataRecepcio);
        model.put("lotId", lotId);

        model.put("sortField", sortField);
        model.put("sortDir", sortDir);

        model.put("producteGraficId", producteGraficId);

        model.put("lotSeleccionat", getLotById(lotId));
        model.put("liniesProduccio", getProduccioPerLot(lotId));

        carregarModelGraficMensual(model, producteGraficId, mesGrafic);

        return model;
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


    // OBTENIR PROVEÏDORS ORDENATS
    public List<Proveidor> getAllProveidorsOrdenats() {
        return proveidorRepository.findAll(Sort.by("nomProveidor").ascending());
    }


    // OBTENIR PRODUCTES ORDENATS
    public List<Producte> getAllProductesOrdenats() {
        return producteRepository.findAll(Sort.by("nomProducte").ascending());
    }


    // OBTENIR ESTATS DE LOT
    public EstatLot[] getEstatsLot() {
        return EstatLot.values();
    }


    // LLISTAT DE LOTS AMB FILTRES I ORDENACIÓ
    public List<LotProveidor> getLotsFiltrats(Long materiaId,
                                               Long proveidorId,
                                               EstatLot estat,
                                               String identificadorLot,
                                               LocalDate dataRecepcio,
                                               String sortField,
                                               String sortDir) {

        List<LotProveidor> lots = new ArrayList<>(lotProveidorRepository.findAll());

        if (materiaId != null) {
            lots.removeIf(lot -> lot.getMateriaPrimera() == null
                    || !materiaId.equals(lot.getMateriaPrimera().getId()));
        }

        if (proveidorId != null) {
            lots.removeIf(lot -> lot.getAlbaraProveidor() == null
                    || lot.getAlbaraProveidor().getProveidor() == null
                    || !proveidorId.equals(lot.getAlbaraProveidor().getProveidor().getId()));
        }

        if (estat != null) {
            lots.removeIf(lot -> lot.getEstat() == null || lot.getEstat() != estat);
        }

        if (identificadorLot != null && !identificadorLot.isBlank()) {
            lots.removeIf(lot -> !conteText(lot.getIdentificadorLot(), identificadorLot));
        }

        if (dataRecepcio != null) {
            lots.removeIf(lot -> lot.getAlbaraProveidor() == null
                    || lot.getAlbaraProveidor().getDataRecepcio() == null
                    || !dataRecepcio.equals(lot.getAlbaraProveidor().getDataRecepcio()));
        }

        ordenarLots(lots, sortField, sortDir);

        return lots;
    }


    // OBTENIR LOT PER ID
    public LotProveidor getLotById(Long lotId) {

        if (lotId == null) {
            return null;
        }

        Optional<LotProveidor> lotOpt = lotProveidorRepository.findById(lotId);

        if (lotOpt.isEmpty()) {
            throw new RuntimeException(missatge("service.tracabilitat.lotNoTrobat"));
        }

        return lotOpt.get();
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param lotId identificador utilitzat en l'operació
     * @return llista de resultats obtinguda
     */
    public List<LiniaProduccio> getProduccioPerLot(Long lotId) {

        if (lotId == null) {
            return new ArrayList<>();
        }

        LotProveidor lotSeleccionat = getLotById(lotId);
        List<AlbaraClient> albaransClient = albaraClientRepository.findAllByOrderByDataAlbaraDescIdDesc();
        List<LiniaProduccio> liniesProduccio = new ArrayList<>();

        for (AlbaraClient albaraClient : albaransClient) {

            if (!albaraConteLot(albaraClient, lotSeleccionat.getId())) {
                continue;
            }

            if (albaraClient.getLiniesProduccio() != null) {
                liniesProduccio.addAll(albaraClient.getLiniesProduccio());
            }
        }

        return liniesProduccio;
    }


    // CARREGAR DADES DEL GRÀFIC DIARI DEL MES SELECCIONAT
    private void carregarModelGraficMensual(Map<String, Object> model,
                                             Long producteGraficId,
                                             String mesGrafic) {

        List<YearMonth> mesosDisponibles = getMesosDisponiblesGrafic();
        YearMonth mesSeleccionat = obtenirMesSeleccionat(mesGrafic, mesosDisponibles);

        List<String> graficLabels = obtenirLabelsDiesMes(mesSeleccionat);
        List<Integer> graficDades = getVendesDiariesMes(mesSeleccionat, producteGraficId);
        Integer totalMensual = graficDades.stream().mapToInt(Integer::intValue).sum();

        model.put("mesosGrafic", mesosDisponibles);
        model.put("mesGrafic", mesSeleccionat.toString());
        model.put("mesGraficText", mesSeleccionat.format(DateTimeFormatter.ofPattern("MM/yyyy")));
        model.put("producteGraficId", producteGraficId);
        model.put("graficLabelSerie", obtenirLabelSerie(producteGraficId));
        model.put("graficLabelsJson", convertirLabelsAJson(graficLabels));
        model.put("graficDadesJson", convertirNumerosAJson(graficDades));
        model.put("totalMensual", totalMensual);
        model.put("graficTeDades", totalMensual > 0);
    }


    // OBTENIR ELS ÚLTIMS DOTZE MESOS AMB VENDES LLIURADES
    private List<YearMonth> getMesosDisponiblesGrafic() {

        List<YearMonth> mesosDisponibles = new ArrayList<>();
        List<AlbaraClient> albaransClient = albaraClientRepository.findAllByOrderByDataAlbaraDescIdDesc();

        for (AlbaraClient albaraClient : albaransClient) {

            if (albaraClient.getEstat() != EstatAlbaraClient.LLIURAT
                    || albaraClient.getDataAlbara() == null) {
                continue;
            }

            YearMonth mes = YearMonth.from(albaraClient.getDataAlbara());

            if (!mesosDisponibles.contains(mes)) {
                mesosDisponibles.add(mes);
            }

            if (mesosDisponibles.size() == 12) {
                break;
            }
        }

        if (mesosDisponibles.isEmpty()) {
            mesosDisponibles.add(YearMonth.now());
        }

        return mesosDisponibles;
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param mesGrafic valor de mesGrafic utilitzat pel mètode
     * @param mesosDisponibles valor de mesosDisponibles utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private YearMonth obtenirMesSeleccionat(String mesGrafic, List<YearMonth> mesosDisponibles) {

        if (mesGrafic != null && !mesGrafic.isBlank()) {
            try {
                YearMonth mesSollicitat = YearMonth.parse(mesGrafic);

                if (mesosDisponibles.contains(mesSollicitat)) {
                    return mesSollicitat;
                }
            }
            catch (RuntimeException e) {
                // Es manté el mes per defecte si el filtre rebut no és vàlid.
            }
        }

        return mesosDisponibles.get(0);
    }


    // OBTENIR VENDES DIÀRIES DEL MES SELECCIONAT
    private List<Integer> getVendesDiariesMes(YearMonth mesSeleccionat, Long producteGraficId) {

        List<Integer> vendesDiaries = new ArrayList<>();

        for (int dia = 0; dia < mesSeleccionat.lengthOfMonth(); dia++) {
            vendesDiaries.add(0);
        }

        List<AlbaraClient> albaransClient = albaraClientRepository.findAllByOrderByDataAlbaraDescIdDesc();

        for (AlbaraClient albaraClient : albaransClient) {

            if (albaraClient.getEstat() != EstatAlbaraClient.LLIURAT
                    || albaraClient.getDataAlbara() == null
                    || albaraClient.getLiniesProduccio() == null
                    || !mesSeleccionat.equals(YearMonth.from(albaraClient.getDataAlbara()))) {
                continue;
            }

            int posicioDia = albaraClient.getDataAlbara().getDayOfMonth() - 1;

            for (LiniaProduccio liniaProduccio : albaraClient.getLiniesProduccio()) {

                if (liniaProduccio.getProducte() == null || liniaProduccio.getQuantitat() == null) {
                    continue;
                }

                if (producteGraficId != null
                        && !producteGraficId.equals(liniaProduccio.getProducte().getId())) {
                    continue;
                }

                Integer totalDia = vendesDiaries.get(posicioDia) + liniaProduccio.getQuantitat();
                vendesDiaries.set(posicioDia, totalDia);
            }
        }

        return vendesDiaries;
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param mesSeleccionat valor de mesSeleccionat utilitzat pel mètode
     * @return llista de resultats obtinguda
     */
    private List<String> obtenirLabelsDiesMes(YearMonth mesSeleccionat) {

        List<String> labels = new ArrayList<>();

        for (int dia = 1; dia <= mesSeleccionat.lengthOfMonth(); dia++) {
            labels.add(String.valueOf(dia));
        }

        return labels;
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param producteGraficId identificador utilitzat en l'operació
     * @return text obtingut pel mètode
     */
    private String obtenirLabelSerie(Long producteGraficId) {

        if (producteGraficId == null) {
            return missatge("tracabilitat.grafic.totsProductes");
        }

        Optional<Producte> producteOpt = producteRepository.findById(producteGraficId);

        if (producteOpt.isPresent() && producteOpt.get().getNomProducte() != null) {
            return producteOpt.get().getNomProducte();
        }

        return missatge("tracabilitat.grafic.producteSeleccionat");
    }


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param labels valor de labels utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String convertirLabelsAJson(List<String> labels) {

        StringBuilder json = new StringBuilder("[");
        
        for (int i = 0; i < labels.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append("\"").append(escaparJson(labels.get(i))).append("\"");
        }

        json.append("]");

        return json.toString();
    }


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param numeros valor de numeros utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String convertirNumerosAJson(List<Integer> numeros) {

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < numeros.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(numeros.get(i));
        }

        json.append("]");

        return json.toString();
    }


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
     */
    private String escaparJson(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    /**
     * ORDENACIÓ DE DADES.
     *
     * Ordenada la llista rebuda segons el criteri indicat o segons
     * l'ordre propi del servei.
     *
     * @param lots valor de lots utilitzat pel mètode
     * @param sortField valor de sortField utilitzat pel mètode
     * @param sortDir valor de sortDir utilitzat pel mètode
     */
    private void ordenarLots(List<LotProveidor> lots, String sortField, String sortDir) {

        Comparator<LotProveidor> comparator = obtenirComparadorLots(sortField);

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        lots.sort(comparator);
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param sortField valor de sortField utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private Comparator<LotProveidor> obtenirComparadorLots(String sortField) {

        if ("identificadorLot".equals(sortField)) {
            return Comparator.comparing(lot -> textSegur(lot.getIdentificadorLot()));
        }

        if ("materia".equals(sortField)) {
            return Comparator.comparing(lot -> {
                if (lot.getMateriaPrimera() == null) {
                    return "";
                }
                return textSegur(lot.getMateriaPrimera().getNomMateria());
            });
        }

        if ("quantitat".equals(sortField)) {
            return Comparator.comparing(lot -> numeroSegur(lot.getQuantitat()));
        }

        if ("estat".equals(sortField)) {
            return Comparator.comparing(lot -> lot.getEstat() != null ? lot.getEstat().name() : "");
        }

        if ("proveidor".equals(sortField)) {
            return Comparator.comparing(lot -> {
                if (lot.getAlbaraProveidor() == null || lot.getAlbaraProveidor().getProveidor() == null) {
                    return "";
                }
                return textSegur(lot.getAlbaraProveidor().getProveidor().getNomProveidor());
            });
        }

        if ("dataRecepcio".equals(sortField)) {
            return Comparator.comparing(lot -> {
                if (lot.getAlbaraProveidor() == null || lot.getAlbaraProveidor().getDataRecepcio() == null) {
                    return LocalDate.MIN;
                }
                return lot.getAlbaraProveidor().getDataRecepcio();
            });
        }

        return Comparator.comparing(lot -> lot.getId() != null ? lot.getId() : 0L);
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param albaraClient valor de albaraClient utilitzat pel mètode
     * @param lotId identificador utilitzat en l'operació
     * @return cert si es compleix la condició indicada
     */
    private boolean albaraConteLot(AlbaraClient albaraClient, Long lotId) {

        if (albaraClient.getLotsAssociats() == null || albaraClient.getLotsAssociats().isEmpty()) {
            return false;
        }

        for (LotProveidor lotAssociat : albaraClient.getLotsAssociats()) {
            if (lotAssociat.getId() != null && lotAssociat.getId().equals(lotId)) {
                return true;
            }
        }

        return false;
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
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param sortField valor de sortField utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String normalitzarSortField(String sortField) {

        if (sortField == null || sortField.isBlank()) {
            return "id";
        }

        return sortField;
    }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param sortDir valor de sortDir utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String normalitzarSortDir(String sortDir) {

        if (sortDir == null || sortDir.isBlank()) {
            return "asc";
        }

        return sortDir;
    }


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String textSegur(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.trim().toLowerCase();
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param valor valor que s'ha de processar
     * @return valor numèric obtingut
     */
    private Integer numeroSegur(Integer valor) {

        if (valor == null) {
            return 0;
        }

        return valor;
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