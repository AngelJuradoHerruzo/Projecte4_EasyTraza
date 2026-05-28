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

/**
 * SERVEI D'ALBARANS DE CLIENT.
 *
 * Gestionades les operacions de consulta, creació, modificació, lliurament i eliminació dels albarans de client.
 * També aplicades les validacions necessàries sobre les línies de producció associades.
 *
 * @author Ángel Jurado Herruz
 */
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


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param id identificador utilitzat en l'operació
     * @return resultat obtingut pel mètode
     */
    public AlbaraClient getAlbaraClientById(Long id) {
        Optional<AlbaraClient> albaraClient = albaraClientRepository.findById(id);
        return albaraClient.orElse(null);
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


    /**
     * CREACIÓ DEL REGISTRE.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param albaraClient valor de albaraClient utilitzat pel mètode
     * @return registre resultant de l'operació
     */
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


    /**
     * ACTUALITZACIÓ DEL REGISTRE.
     *
     * Actualitzat el registre indicat amb les dades rebudes, mantenint
     * les validacions pròpies del servei.
     *
     * @param id identificador utilitzat en l'operació
     * @param albaraClient valor de albaraClient utilitzat pel mètode
     * @return registre resultant de l'operació
     */
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


    /**
     * ELIMINACIÓ DEL REGISTRE.
     *
     * Eliminat el registre identificat quan el servei permet completar
     * l'operació sol·licitada.
     *
     * @param id identificador utilitzat en l'operació
     */
    public void deleteAlbaraClient(Long id) {

        Optional<AlbaraClient> albaraClientOpt = albaraClientRepository.findById(id);

        if (albaraClientOpt.isEmpty()) {
            throw new RuntimeException(missatge("service.albaraClient.noTrobat"));
        }

        validarAlbaraClientModificable(albaraClientOpt.get());

        albaraClientRepository.deleteById(id);
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param id identificador utilitzat en l'operació
     * @return resultat obtingut pel mètode
     */
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


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param albaraClient valor de albaraClient utilitzat pel mètode
     */
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


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param liniaProduccio valor de liniaProduccio utilitzat pel mètode
     */
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


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param albaraClient valor de albaraClient utilitzat pel mètode
     */
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


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param albaraClient valor de albaraClient utilitzat pel mètode
     */
    private void validarAlbaraClientModificable(AlbaraClient albaraClient) {

        if (albaraClient.getEstat() == EstatAlbaraClient.LLIURAT) {
            throw new RuntimeException(missatge("service.albaraClient.lliuratNoModificable"));
        }
    }


    /**
     * ORDENACIÓ DE DADES.
     *
     * Ordenada la llista rebuda segons el criteri indicat o segons
     * l'ordre propi del servei.
     *
     * @param albarans valor de albarans utilitzat pel mètode
     * @param ordre camp utilitzat per ordenar les dades
     * @param direccio direcció de l'ordenació
     */
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


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @return resultat obtingut pel mètode
     */
    private LocalDateTime dataHoraActual() {
        return LocalDateTime.now().withSecond(0).withNano(0);
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
