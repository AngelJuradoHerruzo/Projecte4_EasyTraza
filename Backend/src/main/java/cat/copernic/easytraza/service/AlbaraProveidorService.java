package cat.copernic.easytraza.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

    // ---------------------------- REPOSITORI I CONFIGURACIÓ ----------------------------
    private final AlbaraProveidorRepository albaraProveidorRepository;

    private static final String DIRECTORI_FITXERS = "C:/Users/crono/Desktop/DAM/Projecte4_EasyTraza/Backend/uploads/albarans-proveidor";

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
    public AlbaraProveidor createAlbaraProveidor(AlbaraProveidor albaraProveidor,
                                                 String ocrImageBase64,
                                                 String ocrImageOriginalName) {
        validarDadesAlbaraProveidor(albaraProveidor);

        int index = 1;

        for (LotProveidor lot : albaraProveidor.getLots()) {
            validarDadesLotProveidor(lot);

            lot.setIdentificadorLot(generarIdentificadorLot(albaraProveidor, index));
            lot.setEstat(EstatLot.EN_ESTOC);
            lot.setAlbaraProveidor(albaraProveidor);

            index++;
        }

        AlbaraProveidor albaraGuardat = albaraProveidorRepository.save(albaraProveidor);

        guardarImatgeOcrDefinitiva(albaraGuardat, ocrImageBase64, ocrImageOriginalName);

        return albaraProveidorRepository.save(albaraGuardat);
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    public AlbaraProveidor updateAlbaraProveidor(Long id,
                                                 AlbaraProveidor albaraProveidor,
                                                 String ocrImageBase64,
                                                 String ocrImageOriginalName) {

        Optional<AlbaraProveidor> albaraProveidorOpt = albaraProveidorRepository.findById(id);

        if (albaraProveidorOpt.isPresent()) {
            AlbaraProveidor albaraProveidorActual = albaraProveidorOpt.get();

            validarAlbaraModificable(albaraProveidorActual);
            validarDadesAlbaraProveidor(albaraProveidor);

            albaraProveidorActual.setDataRecepcio(albaraProveidor.getDataRecepcio());
            albaraProveidorActual.setProveidor(albaraProveidor.getProveidor());
            albaraProveidorActual.setUsuariReceptor(albaraProveidor.getUsuariReceptor());

            albaraProveidorActual.getLots().clear();

            int index = 1;

            for (LotProveidor lot : albaraProveidor.getLots()) {
                validarDadesLotProveidor(lot);

                lot.setIdentificadorLot(generarIdentificadorLot(albaraProveidorActual, index));
                lot.setEstat(EstatLot.EN_ESTOC);
                lot.setAlbaraProveidor(albaraProveidorActual);

                albaraProveidorActual.getLots().add(lot);

                index++;
            }

            guardarImatgeOcrDefinitiva(albaraProveidorActual, ocrImageBase64, ocrImageOriginalName);

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


    // VALIDAR DADES DE L'ALBARÀ DE PROVEÏDOR
    private void validarDadesAlbaraProveidor(AlbaraProveidor albaraProveidor) {

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


    // VALIDAR DADES DEL LOT DE PROVEÏDOR
    private void validarDadesLotProveidor(LotProveidor lotProveidor) {

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


    // GENERAR IDENTIFICADOR DEL LOT SEGONS LA DATA DE RECEPCIÓ I LA POSICIÓ DEL LOT
    private String generarIdentificadorLot(AlbaraProveidor albaraProveidor, int index) {

        LocalDateTime data = albaraProveidor.getDataRecepcio();

        String dia = String.format("%02d", data.getDayOfMonth());
        String mes = String.format("%02d", data.getMonthValue());
        String any = String.valueOf(data.getYear());

        return dia + "_" + mes + "_" + any + "_lote" + index;
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
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomFitxer = "albara_" + albaraProveidor.getId() + "_" + timestamp + extensio;

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
            throw new RuntimeException("No es pot modificar o eliminar un albarà amb lots iniciats o finalitzats.");
        }
    }
}