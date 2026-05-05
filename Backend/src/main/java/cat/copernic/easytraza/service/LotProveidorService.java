package cat.copernic.easytraza.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.LotProveidorRepository;

@Service
@Transactional
public class LotProveidorService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final LotProveidorRepository lotProveidorRepository;

    public LotProveidorService(LotProveidorRepository lotProveidorRepository) {
        this.lotProveidorRepository = lotProveidorRepository;
    }


    // OBTENIR TOTS ELS LOTS DE PROVEÏDOR
    public List<LotProveidor> getAllLotsProveidor() {
        return lotProveidorRepository.findAll();
    }


    // OBTENIR LOT DE PROVEÏDOR PER ID
    public LotProveidor getLotProveidorById(Long id) {
        Optional<LotProveidor> lotProveidor = lotProveidorRepository.findById(id);
        return lotProveidor.orElse(null);
    }


    // CANVIAR ESTAT DEL LOT
    public LotProveidor canviarEstatLot(Long id, EstatLot estat) {

        Optional<LotProveidor> lotProveidorOpt = lotProveidorRepository.findById(id);

        if (lotProveidorOpt.isEmpty()) {
            throw new RuntimeException("Lot no trobat.");
        }

        LotProveidor lotProveidorActual = lotProveidorOpt.get();

        validarCanviEstatLot(lotProveidorActual, estat);

        lotProveidorActual.setEstat(estat);

        if (estat == EstatLot.OBERT) {
            lotProveidorActual.setDataObertura(LocalDate.now());
        }

        if (estat == EstatLot.ACABAT) {
            lotProveidorActual.setDataAcabament(LocalDate.now());
        }

        return lotProveidorRepository.save(lotProveidorActual);
    }


    // VALIDAR CANVI D'ESTAT DEL LOT
    private void validarCanviEstatLot(LotProveidor lotProveidor, EstatLot estat) {

        if (estat == null) {
            throw new RuntimeException("L'estat és obligatori.");
        }

        if (lotProveidor.getEstat() == EstatLot.ACABAT) {
            throw new RuntimeException("No es pot modificar un lot acabat.");
        }
    }
}
