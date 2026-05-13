package cat.copernic.easytraza.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.entities.Proveidor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

/**
 * SERVEI OCR ALBARÀ DE PROVEÏDOR
 *
 * Processa una imatge temporalment per extreure text amb OCR.
 * No guarda cap fitxer; el fitxer només es guardarà quan es confirmi l'albarà.
 *
 * @author Ángel Jurado
 */
@Service
public class OcrAlbaraProveidorService {

    // ---------------------------- SERVICES I CONFIGURACIÓ ----------------------------
    private final ProveidorService proveidorService;
    private final MateriaPrimeraService materiaPrimeraService;

    private static final String TESSDATA_PATH = "C:/Program Files/Tesseract-OCR/tessdata";
    private static final String OCR_LANGUAGE = "eng";

    public OcrAlbaraProveidorService(ProveidorService proveidorService,
                                     MateriaPrimeraService materiaPrimeraService) {
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
    }


    // PROCESSAR IMATGE OCR I RETORNAR UN ALBARÀ PREOMPLERT
    public AlbaraProveidor processarImatge(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Has de seleccionar una imatge.");
        }

        String textOcr = extreureText(file);

        AlbaraProveidor albaraProveidor = new AlbaraProveidor();

        albaraProveidor.setDataRecepcio(detectarData(textOcr));
        albaraProveidor.setProveidor(detectarProveidor(textOcr));
        albaraProveidor.setLots(detectarLots(textOcr));

        return albaraProveidor;
    }


    // CONVERTIR LA IMATGE A BASE64 PER MANTENIR-LA AL FORMULARI SENSE GUARDAR-LA
    public String convertirImatgeBase64(MultipartFile file) {

        try {
            String contentType = file.getContentType();

            if (contentType == null || contentType.isBlank()) {
                contentType = "image/png";
            }

            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            return "data:" + contentType + ";base64," + base64;
        }
        catch (Exception e) {
            throw new RuntimeException("No s'ha pogut preparar la imatge de l'albarà.");
        }
    }


    // EXTREURE TEXT DE LA IMATGE SENSE GUARDAR-LA DEFINITIVAMENT
    private String extreureText(MultipartFile file) {

        File fitxerTemporal = null;

        try {
            BufferedImage imatgeOriginal = ImageIO.read(file.getInputStream());

            if (imatgeOriginal == null) {
                throw new RuntimeException("El fitxer no és una imatge vàlida.");
            }

            BufferedImage imatgeRgb = new BufferedImage(
                    imatgeOriginal.getWidth(),
                    imatgeOriginal.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = imatgeRgb.createGraphics();
            graphics.drawImage(imatgeOriginal, 0, 0, null);
            graphics.dispose();

            fitxerTemporal = File.createTempFile("ocr_convertit_", ".png");
            ImageIO.write(imatgeRgb, "png", fitxerTemporal);

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSDATA_PATH);
            tesseract.setLanguage(OCR_LANGUAGE);

            return tesseract.doOCR(fitxerTemporal);
        }
        catch (Throwable e) {
            throw new RuntimeException("No s'ha pogut llegir el text de la imatge amb OCR.");
        }
        finally {
            if (fitxerTemporal != null && fitxerTemporal.exists()) {
                fitxerTemporal.delete();
            }
        }
    }


    // DETECTAR DATA EN FORMAT DD/MM/AAAA, DD-MM-AAAA O DD.MM.AAAA
    private LocalDate detectarData(String textOcr) {

        Pattern pattern = Pattern.compile("(\\d{2})[\\-/\\.](\\d{2})[\\-/\\.](\\d{4})");
        Matcher matcher = pattern.matcher(textOcr);

        if (matcher.find()) {
            int dia = Integer.parseInt(matcher.group(1));
            int mes = Integer.parseInt(matcher.group(2));
            int any = Integer.parseInt(matcher.group(3));

            return LocalDate.of(any, mes, dia);
        }

        return null;
    }


    // DETECTAR PROVEÏDOR COMPARANT EL TEXT AMB ELS PROVEÏDORS EXISTENTS
    private Proveidor detectarProveidor(String textOcr) {

        String textNormalitzat = textOcr.toLowerCase();

        for (Proveidor proveidor : proveidorService.getAllProveidors()) {

            if (proveidor.getNomProveidor() != null &&
                textNormalitzat.contains(proveidor.getNomProveidor().toLowerCase())) {
                return proveidor;
            }

            if (proveidor.getCif() != null &&
                textNormalitzat.contains(proveidor.getCif().toLowerCase())) {
                return proveidor;
            }
        }

        return null;
    }


    // DETECTAR LOTS BUSCANT MATÈRIES PRIMERES EXISTENTS DINS DEL TEXT
    private List<LotProveidor> detectarLots(String textOcr) {

        List<LotProveidor> lots = new ArrayList<>();

        String[] linies = textOcr.split("\\R");

        for (String linia : linies) {

            MateriaPrimera materiaPrimera = detectarMateriaPrimera(linia);

            if (materiaPrimera != null) {
                LotProveidor lotProveidor = new LotProveidor();

                lotProveidor.setMateriaPrimera(materiaPrimera);
                lotProveidor.setQuantitat(detectarQuantitat(linia));
                lotProveidor.setUnitats(detectarUnitats(linia));

                lots.add(lotProveidor);
            }
        }

        if (lots.isEmpty()) {
            lots.add(new LotProveidor());
        }

        return lots;
    }


    // DETECTAR MATÈRIA PRIMERA PER NOM
    private MateriaPrimera detectarMateriaPrimera(String linia) {

        String liniaNormalitzada = linia.toLowerCase();

        for (MateriaPrimera materiaPrimera : materiaPrimeraService.getAllMateriesPrimeres()) {
            if (materiaPrimera.getNomMateria() != null &&
                liniaNormalitzada.contains(materiaPrimera.getNomMateria().toLowerCase())) {
                return materiaPrimera;
            }
        }

        return null;
    }


    // DETECTAR QUANTITAT NUMÈRICA DINS D'UNA LÍNIA
    private Integer detectarQuantitat(String linia) {

        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(linia);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }


    // DETECTAR UNITATS HABITUALS
    private String detectarUnitats(String linia) {

        String liniaNormalitzada = linia.toLowerCase();

        if (liniaNormalitzada.contains("kg")) {
            return "kg";
        }

        if (liniaNormalitzada.contains("ml")) {
            return "ml";
        }

        if (liniaNormalitzada.contains(" g") || liniaNormalitzada.endsWith("g")) {
            return "g";
        }

        if (liniaNormalitzada.contains(" l") || liniaNormalitzada.endsWith("l")) {
            return "l";
        }

        if (liniaNormalitzada.contains("unitats")) {
            return "unitats";
        }

        return "";
    }
}