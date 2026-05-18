package cat.copernic.easytraza.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.AlbaraClient;
import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.AlbaraClientRepository;
import cat.copernic.easytraza.repository.LotProveidorRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;
import cat.copernic.easytraza.repository.ProveidorRepository;

@Service
@Transactional
public class TracabilitatService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final LotProveidorRepository lotProveidorRepository;
    private final AlbaraClientRepository albaraClientRepository;
    private final MateriaPrimeraRepository materiaPrimeraRepository;
    private final ProveidorRepository proveidorRepository;

    public TracabilitatService(LotProveidorRepository lotProveidorRepository,
                               AlbaraClientRepository albaraClientRepository,
                               MateriaPrimeraRepository materiaPrimeraRepository,
                               ProveidorRepository proveidorRepository) {
        this.lotProveidorRepository = lotProveidorRepository;
        this.albaraClientRepository = albaraClientRepository;
        this.materiaPrimeraRepository = materiaPrimeraRepository;
        this.proveidorRepository = proveidorRepository;
    }


    // OBTENIR MATÈRIES PRIMERES ORDENADES
    public List<MateriaPrimera> getAllMateriesPrimeresOrdenades() {
        return materiaPrimeraRepository.findAll(Sort.by("nomMateria").ascending());
    }


    // OBTENIR PROVEÏDORS ORDENATS
    public List<Proveidor> getAllProveidorsOrdenats() {
        return proveidorRepository.findAll(Sort.by("nomProveidor").ascending());
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