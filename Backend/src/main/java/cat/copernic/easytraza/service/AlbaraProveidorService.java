package cat.copernic.easytraza.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.AlbaraProveidorRepository;

@Service
@Transactional
public class AlbaraProveidorService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final AlbaraProveidorRepository albaraProveidorRepository;

    public AlbaraProveidorService(AlbaraProveidorRepository albaraProveidorRepository) {
        this.albaraProveidorRepository = albaraProveidorRepository;
    }


    // OBTENIR TOTS ELS ALBARANS DE PROVEÏDOR
    public List<AlbaraProveidor> getAllAlbaransProveidor() {
        return albaraProveidorRepository.findAll();
    }


    // OBTENIR ALBARÀ DE PROVEÏDOR PER ID
    public AlbaraProveidor getAlbaraProveidorById(Long id) {
        Optional<AlbaraProveidor> albaraProveidor = albaraProveidorRepository.findById(id);
        return albaraProveidor.orElse(null);
    }


    // CREAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor createAlbaraProveidor(AlbaraProveidor albaraProveidor) {

        prepararIValidarAlbaraProveidor(albaraProveidor);

        int index = 1;

        for (LotProveidor lot : albaraProveidor.getLots()) {

            prepararIValidarLotProveidor(lot);

            String identificador = generarIdentificadorLot(albaraProveidor, index);
            lot.setIdentificadorLot(identificador);

            lot.setEstat(EstatLot.EN_ESTOC);
            lot.setAlbaraProveidor(albaraProveidor);

            index++;
        }

        return albaraProveidorRepository.save(albaraProveidor);
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor updateAlbaraProveidor(Long id, AlbaraProveidor albaraProveidor) {

        Optional<AlbaraProveidor> albaraProveidorOpt = albaraProveidorRepository.findById(id);

        if (albaraProveidorOpt.isPresent()) {
            AlbaraProveidor albaraProveidorActual = albaraProveidorOpt.get();

            validarAlbaraModificable(albaraProveidorActual);
            prepararIValidarAlbaraProveidor(albaraProveidor);

            albaraProveidorActual.setDataRecepcio(albaraProveidor.getDataRecepcio());
            albaraProveidorActual.setProveidor(albaraProveidor.getProveidor());
            albaraProveidorActual.setUsuariReceptor(albaraProveidor.getUsuariReceptor());

            albaraProveidorActual.getLots().clear();

            int index = 1;

            for (LotProveidor lot : albaraProveidor.getLots()) {
                prepararIValidarLotProveidor(lot);

                lot.setIdentificadorLot(generarIdentificadorLot(albaraProveidorActual, index));
                lot.setEstat(EstatLot.EN_ESTOC);
                lot.setAlbaraProveidor(albaraProveidorActual);

                albaraProveidorActual.getLots().add(lot);

                index++;
            }

            return albaraProveidorRepository.save(albaraProveidorActual);
        }

        return null;
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


    // PREPARAR I VALIDAR DADES DE L'ALBARÀ DE PROVEÏDOR
    private void prepararIValidarAlbaraProveidor(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor.getDataRecepcio() == null) {
            throw new RuntimeException("La data de recepció és obligatòria.");
        }

        if (albaraProveidor.getProveidor() == null) {
            throw new RuntimeException("El proveïdor és obligatori.");
        }

        if (albaraProveidor.getUsuariReceptor() == null) {
            throw new RuntimeException("L'usuari receptor és obligatori.");
        }

        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            throw new RuntimeException("L'albarà ha de tenir com a mínim un lot.");
        }
    }


    // PREPARAR I VALIDAR DADES DEL LOT DE PROVEÏDOR
    private void prepararIValidarLotProveidor(LotProveidor lotProveidor) {

        if (lotProveidor.getIdentificadorLot() != null) {
            lotProveidor.setIdentificadorLot(lotProveidor.getIdentificadorLot().trim().toUpperCase());
        }

        if (lotProveidor.getMateriaPrimera() == null) {
            throw new RuntimeException("La matèria primera és obligatòria.");
        }

        if (lotProveidor.getQuantitat() == null) {
            throw new RuntimeException("La quantitat és obligatòria.");
        }

        if (lotProveidor.getQuantitat() <= 0) {
            throw new RuntimeException("La quantitat ha de ser superior a zero.");
        }

        if (lotProveidor.getUnitats() != null) {
            lotProveidor.setUnitats(lotProveidor.getUnitats().trim());
        }

        if (lotProveidor.getUnitats() == null || lotProveidor.getUnitats().isBlank()) {
            throw new RuntimeException("Les unitats són obligatòries.");
        }
    }


    // VALIDAR SI L'ALBARÀ ES POT MODIFICAR O ELIMINAR
    private void validarAlbaraModificable(AlbaraProveidor albaraProveidor) {

        if (!esModificable(albaraProveidor)) {
            throw new RuntimeException("No es pot modificar o eliminar un albarà amb lots iniciats o finalitzats.");
        }
    }


    // FORMAT AUTOMÀTIC DE L'IDENTIFICADOR DE LOT
    private String generarIdentificadorLot(AlbaraProveidor albaraProveidor, int index) {

        LocalDateTime data = albaraProveidor.getDataRecepcio();

        String dia = String.format("%02d", data.getDayOfMonth());
        String mes = String.format("%02d", data.getMonthValue());
        String any = String.valueOf(data.getYear());

        return dia + "_" + mes + "_" + any + "_lote" + index;
    }
}