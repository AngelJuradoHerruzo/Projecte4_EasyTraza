package cat.copernic.easytraza.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.LotProveidorRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;

@Service
@Transactional
public class LotProveidorService {

    // ---------------------------- REPOSITORIS I CONSTRUCTOR ----------------------------
    private final LotProveidorRepository lotProveidorRepository;
    private final MateriaPrimeraRepository materiaPrimeraRepository;

    public LotProveidorService(LotProveidorRepository lotProveidorRepository,
                               MateriaPrimeraRepository materiaPrimeraRepository) {
        this.lotProveidorRepository = lotProveidorRepository;
        this.materiaPrimeraRepository = materiaPrimeraRepository;
    }


    // OBTENIR TOTS ELS LOTS DE PROVEÏDOR
    public List<LotProveidor> getAllLotsProveidor() {
        return lotProveidorRepository.findAll();
    }


    // PREPARAR LLISTAT WEB DE LOTS AMB ORDENACIÓ OPCIONAL
    public List<LotProveidor> getLotsProveidorLlistat(String sort, String dir) {

        List<LotProveidor> lots = new java.util.ArrayList<>(lotProveidorRepository.findAll());

        if (sort == null || sort.isBlank() || dir == null || dir.isBlank()) {
            return lots;
        }

        java.util.Comparator<LotProveidor> comparador = switch (sort) {
            case "id" -> java.util.Comparator.comparing(LotProveidor::getId);

            case "identificadorLot" -> java.util.Comparator.comparing(
                    lot -> lot.getIdentificadorLot() != null ? lot.getIdentificadorLot() : "",
                    String.CASE_INSENSITIVE_ORDER
            );

            case "quantitat" -> java.util.Comparator.comparing(
                    lot -> lot.getQuantitat() != null ? lot.getQuantitat() : 0
            );

            case "estat" -> java.util.Comparator.comparing(
                    lot -> lot.getEstat() != null ? lot.getEstat().name() : "",
                    String.CASE_INSENSITIVE_ORDER
            );

            case "dataCaducitat" -> java.util.Comparator.comparing(
                    LotProveidor::getDataCaducitat,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
            );

            case "dataObertura" -> java.util.Comparator.comparing(
                    LotProveidor::getDataObertura,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
            );

            case "dataAcabament" -> java.util.Comparator.comparing(
                    LotProveidor::getDataAcabament,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
            );

            default -> null;
        };

        if (comparador == null) {
            return lots;
        }

        if ("desc".equalsIgnoreCase(dir)) {
            comparador = comparador.reversed();
        }

        lots.sort(comparador);

        return lots;
    }


    // OBTENIR TOTES LES MATÈRIES PRIMERES ORDENADES
    public List<MateriaPrimera> getAllMateriesPrimeresOrdenades() {
        return materiaPrimeraRepository.findAll(Sort.by("nomMateria").ascending());
    }


    // OBTENIR LOT DE PROVEÏDOR PER ID
    public LotProveidor getLotProveidorById(Long id) {
        return obtenirLotValidat(id);
    }


    // COMPROVAR SI EXISTEIX UN LOT OBERT DE LA MATEIXA MATÈRIA PRIMERA
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


    // INICIAR LOT
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
                throw new RuntimeException("Ja hi ha un lot obert per aquesta matèria primera.");
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


    // FINALITZAR LOT
    public LotProveidor finalitzarLot(Long id) {

        LotProveidor lotProveidor = obtenirLotValidat(id);

        validarLotPerFinalitzar(lotProveidor);

        lotProveidor.setEstat(EstatLot.ACABAT);
        lotProveidor.setDataAcabament(LocalDate.now());

        return lotProveidorRepository.save(lotProveidor);
    }


    // OBTENIR LOT VALIDAT
    private LotProveidor obtenirLotValidat(Long id) {

        Optional<LotProveidor> lotProveidorOpt = lotProveidorRepository.findById(id);

        if (lotProveidorOpt.isEmpty()) {
            throw new RuntimeException("Lot no trobat.");
        }

        return lotProveidorOpt.get();
    }


    // VALIDAR LOT PER INICIAR
    private void validarLotPerIniciar(LotProveidor lotProveidor) {

        if (lotProveidor.getMateriaPrimera() == null) {
            throw new RuntimeException("La matèria primera del lot és obligatòria.");
        }

        if (lotProveidor.getEstat() == null) {
            throw new RuntimeException("L'estat del lot és obligatori.");
        }

        if (lotProveidor.getEstat() != EstatLot.EN_ESTOC) {
            throw new RuntimeException("Només es poden iniciar lots en estoc.");
        }
    }


    // VALIDAR LOT PER FINALITZAR
    private void validarLotPerFinalitzar(LotProveidor lotProveidor) {

        if (lotProveidor.getEstat() == null) {
            throw new RuntimeException("L'estat del lot és obligatori.");
        }

        if (lotProveidor.getEstat() != EstatLot.OBERT) {
            throw new RuntimeException("Només es poden finalitzar lots oberts.");
        }
    }
}