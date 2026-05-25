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

@Service
@Transactional
public class TracabilitatService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final LotProveidorRepository lotProveidorRepository;
    private final AlbaraClientRepository albaraClientRepository;
    private final MateriaPrimeraRepository materiaPrimeraRepository;
    private final ProveidorRepository proveidorRepository;
    private final ProducteRepository producteRepository;

    public TracabilitatService(LotProveidorRepository lotProveidorRepository,
                               AlbaraClientRepository albaraClientRepository,
                               MateriaPrimeraRepository materiaPrimeraRepository,
                               ProveidorRepository proveidorRepository,
                               ProducteRepository producteRepository) {
        this.lotProveidorRepository = lotProveidorRepository;
        this.albaraClientRepository = albaraClientRepository;
        this.materiaPrimeraRepository = materiaPrimeraRepository;
        this.proveidorRepository = proveidorRepository;
        this.producteRepository = producteRepository;
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


    // OBTENIR MATÈRIES PRIMERES ORDENADES
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
            throw new RuntimeException("Lot no trobat.");
        }

        return lotOpt.get();
    }


    // PRODUCCIÓ GENERADA AMB UN LOT
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


    // OBTENIR EL MES SELECCIONAT O EL MÉS RECENT DISPONIBLE
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


    // OBTENIR ETIQUETES DELS DIES DEL MES
    private List<String> obtenirLabelsDiesMes(YearMonth mesSeleccionat) {

        List<String> labels = new ArrayList<>();

        for (int dia = 1; dia <= mesSeleccionat.lengthOfMonth(); dia++) {
            labels.add(String.valueOf(dia));
        }

        return labels;
    }


    // OBTENIR NOM DE LA SÈRIE MOSTRADA AL GRÀFIC
    private String obtenirLabelSerie(Long producteGraficId) {

        if (producteGraficId == null) {
            return "Tots els productes";
        }

        Optional<Producte> producteOpt = producteRepository.findById(producteGraficId);

        if (producteOpt.isPresent() && producteOpt.get().getNomProducte() != null) {
            return producteOpt.get().getNomProducte();
        }

        return "Producte seleccionat";
    }


    // CONVERTIR ETIQUETES A JSON
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


    // CONVERTIR LLISTA DE NÚMEROS A JSON
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


    // ESCAPAR TEXT PER JSON
    private String escaparJson(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    // ORDENAR LOTS
    private void ordenarLots(List<LotProveidor> lots, String sortField, String sortDir) {

        Comparator<LotProveidor> comparator = obtenirComparadorLots(sortField);

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        lots.sort(comparator);
    }


    // OBTENIR COMPARADOR SEGONS LA COLUMNA SELECCIONADA
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


    // COMPROVAR SI UN ALBARÀ DE CLIENT TÉ ASSOCIAT EL LOT
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


    // COMPROVAR SI UN TEXT CONTÉ UN FILTRE
    private boolean conteText(String valor, String filtre) {

        if (filtre == null || filtre.isBlank()) {
            return true;
        }

        if (valor == null) {
            return false;
        }

        return valor.toLowerCase().contains(filtre.trim().toLowerCase());
    }


    // NORMALITZAR CAMP D'ORDENACIÓ
    private String normalitzarSortField(String sortField) {

        if (sortField == null || sortField.isBlank()) {
            return "id";
        }

        return sortField;
    }


    // NORMALITZAR DIRECCIÓ D'ORDENACIÓ
    private String normalitzarSortDir(String sortDir) {

        if (sortDir == null || sortDir.isBlank()) {
            return "asc";
        }

        return sortDir;
    }


    // RETORNAR TEXT SEGUR PER ORDENAR
    private String textSegur(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.trim().toLowerCase();
    }


    // RETORNAR NÚMERO SEGUR PER ORDENAR
    private Integer numeroSegur(Integer valor) {

        if (valor == null) {
            return 0;
        }

        return valor;
    }
}