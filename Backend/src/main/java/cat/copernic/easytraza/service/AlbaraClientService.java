package cat.copernic.easytraza.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.AlbaraClient;
import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.enums.EstatAlbaraClient;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.AlbaraClientRepository;
import cat.copernic.easytraza.repository.LotProveidorRepository;

@Service
@Transactional
public class AlbaraClientService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final AlbaraClientRepository albaraClientRepository;
    private final LotProveidorRepository lotProveidorRepository;

    public AlbaraClientService(AlbaraClientRepository albaraClientRepository,
                               LotProveidorRepository lotProveidorRepository) {
        this.albaraClientRepository = albaraClientRepository;
        this.lotProveidorRepository = lotProveidorRepository;
    }


    // OBTENIR TOTS ELS ALBARANS DE CLIENT
    public List<AlbaraClient> getAllAlbaransClient() {
        return albaraClientRepository.findAll();
    }


    // OBTENIR ALBARÀ DE CLIENT PER ID
    public AlbaraClient getAlbaraClientById(Long id) {
        Optional<AlbaraClient> albaraClient = albaraClientRepository.findById(id);
        return albaraClient.orElse(null);
    }


    // OBTENIR ALBARÀ DE CLIENT AMB DETALL
    public AlbaraClient getAlbaraClientDetallById(Long id) {
        Optional<AlbaraClient> albaraClientOpt = albaraClientRepository.findById(id);

        if (albaraClientOpt.isEmpty()) {
            return null;
        }

        AlbaraClient albaraClient = albaraClientOpt.get();

        if (albaraClient.getLiniesProduccio() != null) {
            albaraClient.getLiniesProduccio().size();

            for (LiniaProduccio liniaProduccio : albaraClient.getLiniesProduccio()) {
                if (liniaProduccio.getProducte() != null) {
                    liniaProduccio.getProducte().getId();
                }
            }
        }

        if (albaraClient.getLotsAssociats() != null) {
            albaraClient.getLotsAssociats().size();

            for (LotProveidor lotProveidor : albaraClient.getLotsAssociats()) {
                lotProveidor.getId();

                if (lotProveidor.getMateriaPrimera() != null) {
                    lotProveidor.getMateriaPrimera().getId();
                }
            }
        }

        return albaraClient;
    }


    // CREAR ALBARÀ DE CLIENT
    public AlbaraClient createAlbaraClient(AlbaraClient albaraClient) {

        if (albaraClient.getDataAlbara() == null) {
            albaraClient.setDataAlbara(LocalDate.now());
        }

        albaraClient.setEstat(EstatAlbaraClient.PENDENT_LLIURAR);

        validarDadesAlbaraClient(albaraClient);
        validarProductesDuplicats(albaraClient);

        List<LotProveidor> lotsOberts = lotProveidorRepository.findByEstat(EstatLot.OBERT);
        albaraClient.setLotsAssociats(new ArrayList<>(lotsOberts));

        for (LiniaProduccio liniaProduccio : albaraClient.getLiniesProduccio()) {
            validarDadesLiniaProduccio(liniaProduccio);
            liniaProduccio.setAlbaraClient(albaraClient);
        }

        return albaraClientRepository.save(albaraClient);
    }


    // ACTUALITZAR ALBARÀ DE CLIENT
    public AlbaraClient updateAlbaraClient(Long id, AlbaraClient albaraClient) {

        Optional<AlbaraClient> albaraClientOpt = albaraClientRepository.findById(id);

        if (albaraClientOpt.isEmpty()) {
            throw new RuntimeException("Albarà de client no trobat");
        }

        AlbaraClient albaraClientActual = albaraClientOpt.get();

        validarAlbaraClientModificable(albaraClientActual);
        validarDadesAlbaraClient(albaraClient);
        validarProductesDuplicats(albaraClient);

        albaraClientActual.setDataAlbara(albaraClient.getDataAlbara());
        albaraClientActual.setClient(albaraClient.getClient());

        List<LotProveidor> lotsOberts = lotProveidorRepository.findByEstat(EstatLot.OBERT);
        albaraClientActual.setLotsAssociats(new ArrayList<>(lotsOberts));

        albaraClientActual.getLiniesProduccio().clear();

        for (LiniaProduccio liniaProduccio : albaraClient.getLiniesProduccio()) {
            validarDadesLiniaProduccio(liniaProduccio);

            liniaProduccio.setAlbaraClient(albaraClientActual);
            albaraClientActual.getLiniesProduccio().add(liniaProduccio);
        }

        return albaraClientRepository.save(albaraClientActual);
    }


    // ELIMINAR ALBARÀ DE CLIENT
    public void deleteAlbaraClient(Long id) {

        Optional<AlbaraClient> albaraClientOpt = albaraClientRepository.findById(id);

        if (albaraClientOpt.isEmpty()) {
            throw new RuntimeException("Albarà de client no trobat");
        }

        validarAlbaraClientModificable(albaraClientOpt.get());

        albaraClientRepository.deleteById(id);
    }


    // LLIURAR ALBARÀ DE CLIENT
    public AlbaraClient lliurarAlbaraClient(Long id) {

        Optional<AlbaraClient> albaraClientOpt = albaraClientRepository.findById(id);

        if (albaraClientOpt.isEmpty()) {
            throw new RuntimeException("Albarà de client no trobat");
        }

        AlbaraClient albaraClientActual = albaraClientOpt.get();

        validarAlbaraClientModificable(albaraClientActual);

        albaraClientActual.setEstat(EstatAlbaraClient.LLIURAT);

        return albaraClientRepository.save(albaraClientActual);
    }


    // VALIDAR DADES DE L'ALBARÀ DE CLIENT
    private void validarDadesAlbaraClient(AlbaraClient albaraClient) {

        if (albaraClient.getDataAlbara() == null) {
            throw new RuntimeException("La data de l'albarà és obligatòria");
        }

        if (albaraClient.getClient() == null || albaraClient.getClient().getId() == null) {
            throw new RuntimeException("El client és obligatori");
        }

        if (albaraClient.getLiniesProduccio() == null || albaraClient.getLiniesProduccio().isEmpty()) {
            throw new RuntimeException("L'albarà ha de tenir com a mínim una línia de producció");
        }
    }


    // VALIDAR DADES DE LA LÍNIA DE PRODUCCIÓ
    private void validarDadesLiniaProduccio(LiniaProduccio liniaProduccio) {

        if (liniaProduccio.getProducte() == null || liniaProduccio.getProducte().getId() == null) {
            throw new RuntimeException("El producte és obligatori");
        }

        if (liniaProduccio.getQuantitat() == null) {
            throw new RuntimeException("La quantitat és obligatòria");
        }

        if (liniaProduccio.getQuantitat() <= 0) {
            throw new RuntimeException("La quantitat ha de ser superior a zero");
        }
    }


    // VALIDAR PRODUCTES DUPLICATS
    private void validarProductesDuplicats(AlbaraClient albaraClient) {

        for (int i = 0; i < albaraClient.getLiniesProduccio().size(); i++) {
            LiniaProduccio liniaActual = albaraClient.getLiniesProduccio().get(i);

            if (liniaActual.getProducte() == null || liniaActual.getProducte().getId() == null) {
                continue;
            }

            Long producteActualId = liniaActual.getProducte().getId();

            for (int j = i + 1; j < albaraClient.getLiniesProduccio().size(); j++) {
                LiniaProduccio liniaComparada = albaraClient.getLiniesProduccio().get(j);

                if (liniaComparada.getProducte() == null || liniaComparada.getProducte().getId() == null) {
                    continue;
                }

                if (producteActualId.equals(liniaComparada.getProducte().getId())) {
                    throw new RuntimeException("No es pot repetir el mateix producte dins d'un albarà");
                }
            }
        }
    }


    // VALIDAR QUE L'ALBARÀ DE CLIENT ES POT MODIFICAR
    private void validarAlbaraClientModificable(AlbaraClient albaraClient) {

        if (albaraClient.getEstat() == EstatAlbaraClient.LLIURAT) {
            throw new RuntimeException("No es pot modificar ni eliminar un albarà de client lliurat");
        }
    }
}