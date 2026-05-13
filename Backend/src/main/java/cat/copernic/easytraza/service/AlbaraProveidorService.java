package cat.copernic.easytraza.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.repository.AlbaraProveidorRepository;

@Service
@Transactional
public class AlbaraProveidorService {

    // ---------------------------- REPOSITORIS, SERVICES I CONFIGURACIÓ ----------------------------
    private final AlbaraProveidorRepository albaraProveidorRepository;
    private final UnitatMesuraService unitatMesuraService;

    private static final String DIRECTORI_FITXERS = "Backend/uploads/albarans-proveidor";

    public AlbaraProveidorService(AlbaraProveidorRepository albaraProveidorRepository,
                                  UnitatMesuraService unitatMesuraService) {
        this.albaraProveidorRepository = albaraProveidorRepository;
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

        Map<String, List<AlbaraProveidor>> albaransPerProveidor = new LinkedHashMap<>();

        for (AlbaraProveidor albara : getAllAlbaransProveidor()) {

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

        // Força la càrrega dels lots per poder mostrar-los al formulari i al detall
        if (albaraProveidor.getLots() != null) {
            albaraProveidor.getLots().size();

            for (LotProveidor lot : albaraProveidor.getLots()) {
                if (lot.getMateriaPrimera() != null) {
                    lot.getMateriaPrimera().getId();
                }
            }
        }

        // Força la càrrega de la imatge guardada
        if (albaraProveidor.getFitxers() != null) {
            albaraProveidor.getFitxers().size();
        }

        return albaraProveidor;
    }


    // CREAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor createAlbaraProveidor(AlbaraProveidor albaraProveidor,
                                                 String ocrImageBase64,
                                                 String ocrImageOriginalName,
                                                 MultipartFile imatgeAlbara) {

        validarDadesAlbaraProveidor(albaraProveidor);

        int index = 1;

        for (LotProveidor lot : albaraProveidor.getLots()) {
            validarDadesLotProveidor(lot);

            lot.setIdentificadorLot(generarIdentificadorLot(albaraProveidor, index));
            lot.setUnitats(unitatMesuraService.normalitzarNom(lot.getUnitats()));
            lot.setEstat(EstatLot.EN_ESTOC);
            lot.setDataObertura(null);
            lot.setAlbaraProveidor(albaraProveidor);

            index++;
        }

        AlbaraProveidor albaraGuardat = albaraProveidorRepository.save(albaraProveidor);

        guardarImatgeFitxer(albaraGuardat, imatgeAlbara);
        guardarImatgeOcrDefinitiva(albaraGuardat, ocrImageBase64, ocrImageOriginalName);

        return albaraProveidorRepository.save(albaraGuardat);
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor updateAlbaraProveidor(Long id,
                                                AlbaraProveidor albaraProveidor,
                                                String ocrImageBase64,
                                                String ocrImageOriginalName,
                                                MultipartFile imatgeAlbara) {

        Optional<AlbaraProveidor> albaraProveidorOpt = albaraProveidorRepository.findById(id);

        if (albaraProveidorOpt.isEmpty()) {
            throw new RuntimeException("Albarà de proveïdor no trobat.");
        }

        AlbaraProveidor albaraProveidorActual = albaraProveidorOpt.get();

        validarAlbaraModificable(albaraProveidorActual);
        validarDadesAlbaraProveidor(albaraProveidor);

        albaraProveidorActual.setDataRecepcio(albaraProveidor.getDataRecepcio());
        albaraProveidorActual.setProveidor(albaraProveidor.getProveidor());
        albaraProveidorActual.setUsuariReceptor(albaraProveidor.getUsuariReceptor());

        guardarImatgeFitxer(albaraProveidorActual, imatgeAlbara);
        guardarImatgeOcrDefinitiva(albaraProveidorActual, ocrImageBase64, ocrImageOriginalName);

        return albaraProveidorRepository.save(albaraProveidorActual);
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


    // VALIDAR DADES DE L'ALBARÀ DE PROVEÏDOR
    private void validarDadesAlbaraProveidor(AlbaraProveidor albaraProveidor) {

        if (albaraProveidor.getDataRecepcio() == null) {
            throw new RuntimeException("La data de recepció és obligatòria.");
        }

        if (albaraProveidor.getProveidor() == null || albaraProveidor.getProveidor().getId() == null) {
            throw new RuntimeException("El proveïdor és obligatori.");
        }

        if (albaraProveidor.getUsuariReceptor() == null || albaraProveidor.getUsuariReceptor().getId() == null) {
            throw new RuntimeException("L'usuari receptor és obligatori.");
        }

        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            throw new RuntimeException("L'albarà ha de tenir com a mínim un lot.");
        }
    }


    // VALIDAR DADES DEL LOT DE PROVEÏDOR
    private void validarDadesLotProveidor(LotProveidor lotProveidor) {

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


    // GENERAR IDENTIFICADOR DEL LOT SEGONS LA DATA DE RECEPCIÓ I LA POSICIÓ DEL LOT
    private String generarIdentificadorLot(AlbaraProveidor albaraProveidor, int index) {

        String dia = String.format("%02d", albaraProveidor.getDataRecepcio().getDayOfMonth());
        String mes = String.format("%02d", albaraProveidor.getDataRecepcio().getMonthValue());
        String any = String.valueOf(albaraProveidor.getDataRecepcio().getYear());

        return dia + "_" + mes + "_" + any + "_lot" + index;
    }


    // GUARDAR IMATGE DES D'UN FITXER PUJAT AL FORMULARI
    private void guardarImatgeFitxer(AlbaraProveidor albaraProveidor, MultipartFile imatgeAlbara) {

        if (imatgeAlbara == null || imatgeAlbara.isEmpty()) {
            return;
        }

        try {
            Files.createDirectories(Paths.get(DIRECTORI_FITXERS));

            String extensio = obtenirExtensioFitxer(imatgeAlbara.getOriginalFilename());
            String data = albaraProveidor.getDataRecepcio().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nomFitxer = "albara_" + albaraProveidor.getId() + "_" + data + extensio;

            Path rutaFitxer = Paths.get(DIRECTORI_FITXERS, nomFitxer);
            Files.write(rutaFitxer, imatgeAlbara.getBytes());

            albaraProveidor.getFitxers().clear();
            albaraProveidor.getFitxers().add("/uploads/albarans-proveidor/" + nomFitxer);
        }
        catch (IOException e) {
            throw new RuntimeException("No s'ha pogut guardar la imatge de l'albarà.");
        }
    }


    // GUARDAR DEFINITIVAMENT LA IMATGE OCR NOMÉS QUAN ES GUARDA L'ALBARÀ
    private void guardarImatgeOcrDefinitiva(AlbaraProveidor albaraProveidor,
                                            String ocrImageBase64,
                                            String ocrImageOriginalName) {

        if (ocrImageBase64 == null || ocrImageBase64.isBlank()) {
            return;
        }

        try {
            Files.createDirectories(Paths.get(DIRECTORI_FITXERS));

            String extensio = obtenirExtensioFitxer(ocrImageOriginalName);
            String data = albaraProveidor.getDataRecepcio().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nomFitxer = "albara_" + albaraProveidor.getId() + "_" + data + extensio;

            String base64Net = ocrImageBase64;

            if (base64Net.contains(",")) {
                base64Net = base64Net.substring(base64Net.indexOf(",") + 1);
            }

            byte[] bytes = Base64.getDecoder().decode(base64Net);

            Path rutaFitxer = Paths.get(DIRECTORI_FITXERS, nomFitxer);
            Files.write(rutaFitxer, bytes);

            albaraProveidor.getFitxers().clear();
            albaraProveidor.getFitxers().add("/uploads/albarans-proveidor/" + nomFitxer);
        }
        catch (IOException e) {
            throw new RuntimeException("No s'ha pogut guardar la imatge de l'albarà.");
        }
    }


    // OBTENIR L'EXTENSIÓ DEL FITXER ORIGINAL
    private String obtenirExtensioFitxer(String nomOriginal) {

        if (nomOriginal == null || !nomOriginal.contains(".")) {
            return ".png";
        }

        return nomOriginal.substring(nomOriginal.lastIndexOf("."));
    }


    // VALIDAR SI L'ALBARÀ ES POT MODIFICAR O ELIMINAR
    private void validarAlbaraModificable(AlbaraProveidor albaraProveidor) {

        if (!esModificable(albaraProveidor)) {
            throw new RuntimeException("No es pot modificar o eliminar aquest albarà perquè té algun lot que no està en estoc.");
        }
    }
}
