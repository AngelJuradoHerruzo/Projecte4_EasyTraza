package cat.copernic.easytraza.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.AlbaraClient;
import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.enums.EstatAlbaraClient;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.enums.RolUsuari;
import cat.copernic.easytraza.repository.AlbaraClientRepository;
import cat.copernic.easytraza.repository.LotProveidorRepository;
import cat.copernic.easytraza.repository.UsuariRepository;

@Service
@Transactional
public class AlbaraClientService {

    // ---------------------------- REPOSITORIS I CONSTRUCTOR ----------------------------
    private final AlbaraClientRepository albaraClientRepository;
    private final LotProveidorRepository lotProveidorRepository;
    private final UsuariRepository usuariRepository;
    private final MessageSource messageSource;

    public AlbaraClientService(AlbaraClientRepository albaraClientRepository,
                               LotProveidorRepository lotProveidorRepository,
                               UsuariRepository usuariRepository,
                               MessageSource messageSource) {
        this.albaraClientRepository = albaraClientRepository;
        this.lotProveidorRepository = lotProveidorRepository;
        this.usuariRepository = usuariRepository;
        this.messageSource = messageSource;
    }


    // OBTENIR TOTS ELS ALBARANS DE CLIENT ORDENATS PER DATA I HORA
    public List<AlbaraClient> getAllAlbaransClient() {
        return albaraClientRepository.findAllByOrderByDataAlbaraDescIdDesc();
    }


    // PREPARAR LLISTAT WEB D'ALBARANS DE CLIENT AMB FILTRES I ORDENACIÓ OPCIONALS
    public List<AlbaraClient> getAlbaransClientLlistat(Long clientId,
                                                        String numeroAlbara,
                                                        String ordre,
                                                        String direccio) {

        List<AlbaraClient> albarans = new ArrayList<>(albaraClientRepository.findAll());

        if (clientId != null) {
            albarans.removeIf(albara -> albara.getClient() == null
                    || !clientId.equals(albara.getClient().getId()));
        }

        if (numeroAlbara != null && !numeroAlbara.isBlank()) {
            albarans.removeIf(albara -> albara.getId() == null
                    || !String.valueOf(albara.getId()).contains(numeroAlbara.trim()));
        }

        ordenarAlbarans(albarans, ordre, direccio);

        return albarans;
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

                if (liniaProduccio.getOperari() != null) {
                    liniaProduccio.getOperari().getId();
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
            albaraClient.setDataAlbara(dataHoraActual());
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
            throw new RuntimeException(missatge("service.albaraClient.noTrobat"));
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
            throw new RuntimeException(missatge("service.albaraClient.noTrobat"));
        }

        validarAlbaraClientModificable(albaraClientOpt.get());

        albaraClientRepository.deleteById(id);
    }


    // LLIURAR ALBARÀ DE CLIENT
    public AlbaraClient lliurarAlbaraClient(Long id) {

        Optional<AlbaraClient> albaraClientOpt = albaraClientRepository.findById(id);

        if (albaraClientOpt.isEmpty()) {
            throw new RuntimeException(missatge("service.albaraClient.noTrobat"));
        }

        AlbaraClient albaraClientActual = albaraClientOpt.get();

        validarAlbaraClientModificable(albaraClientActual);

        albaraClientActual.setEstat(EstatAlbaraClient.LLIURAT);

        return albaraClientRepository.save(albaraClientActual);
    }


    // VALIDAR DADES DE L'ALBARÀ DE CLIENT
    private void validarDadesAlbaraClient(AlbaraClient albaraClient) {

        if (albaraClient.getDataAlbara() == null) {
            throw new RuntimeException(missatge("service.albaraClient.dataHoraObligatoria"));
        }

        if (albaraClient.getClient() == null || albaraClient.getClient().getId() == null) {
            throw new RuntimeException(missatge("service.albaraClient.clientObligatori"));
        }

        if (albaraClient.getLiniesProduccio() == null || albaraClient.getLiniesProduccio().isEmpty()) {
            throw new RuntimeException(missatge("service.albaraClient.liniaMinima"));
        }
    }


    // VALIDAR DADES DE LA LÍNIA DE PRODUCCIÓ
    private void validarDadesLiniaProduccio(LiniaProduccio liniaProduccio) {

        if (liniaProduccio.getProducte() == null || liniaProduccio.getProducte().getId() == null) {
            throw new RuntimeException(missatge("service.albaraClient.producteObligatori"));
        }

        if (liniaProduccio.getQuantitat() == null) {
            throw new RuntimeException(missatge("service.albaraClient.quantitatObligatoria"));
        }

        if (liniaProduccio.getQuantitat() <= 0) {
            throw new RuntimeException(missatge("service.albaraClient.quantitatPositiva"));
        }

        if (liniaProduccio.getOperari() == null || liniaProduccio.getOperari().getId() == null) {
            throw new RuntimeException(missatge("service.albaraClient.operariObligatori"));
        }

        Usuari operari = usuariRepository.findById(liniaProduccio.getOperari().getId())
                .orElseThrow(() -> new RuntimeException("L'operari seleccionat no existeix."));

        if (operari.getRolUsuari() != RolUsuari.OPERARI) {
            throw new RuntimeException(missatge("service.albaraClient.rolOperari"));
        }

        liniaProduccio.setOperari(operari);
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
                    throw new RuntimeException(missatge("service.albaraClient.producteRepetit"));
                }
            }
        }
    }


    // VALIDAR QUE L'ALBARÀ DE CLIENT ES POT MODIFICAR
    private void validarAlbaraClientModificable(AlbaraClient albaraClient) {

        if (albaraClient.getEstat() == EstatAlbaraClient.LLIURAT) {
            throw new RuntimeException(missatge("service.albaraClient.lliuratNoModificable"));
        }
    }


    // ORDENAR ALBARANS PEL CAMP SELECCIONAT
    private void ordenarAlbarans(List<AlbaraClient> albarans, String ordre, String direccio) {

        String campOrdre = ordre != null && !ordre.isBlank() ? ordre : "dataAlbara";
        Comparator<AlbaraClient> comparator;

        switch (campOrdre) {
            case "id":
                comparator = Comparator.comparing(AlbaraClient::getId, Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "client":
                comparator = Comparator.comparing(
                    albara -> albara.getClient() != null && albara.getClient().getNomComplet() != null
                        ? albara.getClient().getNomComplet() : "",
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "estat":
                comparator = Comparator.comparing(
                    albara -> albara.getEstat() != null ? albara.getEstat().name() : "",
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "linies":
                comparator = Comparator.comparingInt(
                    albara -> albara.getLiniesProduccio() != null ? albara.getLiniesProduccio().size() : 0
                );
                break;

            case "dataAlbara":
            default:
                comparator = Comparator.comparing(
                    AlbaraClient::getDataAlbara,
                    Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
        }

        if (!"asc".equalsIgnoreCase(direccio)) {
            comparator = comparator.reversed();
        }

        albarans.sort(comparator.thenComparing(
            AlbaraClient::getId,
            Comparator.nullsLast(Comparator.reverseOrder())
        ));
    }


    // OBTENIR DATA I HORA ACTUAL SENSE SEGONS
    private LocalDateTime dataHoraActual() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
