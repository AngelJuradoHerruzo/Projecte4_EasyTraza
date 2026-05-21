package cat.copernic.easytraza.service;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import cat.copernic.easytraza.dto.OcrResultatAlbaraProveidorDto;
import cat.copernic.easytraza.service.ocr.OcrParserProveidor;
import cat.copernic.easytraza.service.ocr.OcrProveidorDetectat;
import cat.copernic.easytraza.service.ocr.OcrUtils;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servei orquestrador de l'OCR d'albarans de proveïdor.
 *
 * Només extreu informació temporal del document i delega el parseig al servei
 * específic del proveïdor detectat. No desa albarans, no crea lots i no assigna
 * l'usuari receptor.
 */
@Service
public class OcrAlbaraProveidorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OcrAlbaraProveidorService.class);

    private final List<OcrParserProveidor> parsers;

    @Value("${ocr.tessdata.path}")
    private String tessdataPath;

    @Value("${ocr.tesseract.language:spa}")
    private String tesseractLanguage;

    @Value("${ocr.documents.temp-path:uploads/ocr-temp}")
    private String documentsTempPath;

    public OcrAlbaraProveidorService(List<OcrParserProveidor> parsers) {
        this.parsers = parsers;
    }


    /*********************       .API PÚBLICA.       *********************/
    public OcrResultatAlbaraProveidorDto processarDocument(MultipartFile fitxer) {
        validarFitxer(fitxer);

        DocumentTemporalOcr documentTemporal = guardarDocumentTemporal(fitxer);
        String textOcrOriginal = extreureTextOcr(documentTemporal);
        String textOcrNormalitzat = OcrUtils.normalitzarText(textOcrOriginal);
        String textComparacio = OcrUtils.normalitzarPerComparar(textOcrNormalitzat);
        OcrProveidorDetectat proveidorDetectat = OcrProveidorDetectat.detectar(textComparacio);

        OcrAlbaraPendent albaraPendent = parsejarAlbara(proveidorDetectat, textOcrOriginal, textOcrNormalitzat);

        OcrResultatAlbaraProveidorDto resultat = new OcrResultatAlbaraProveidorDto();
        resultat.setProveidorDetectat(proveidorDetectat);
        resultat.setAlbaraPendent(albaraPendent);
        resultat.setTextOcrOriginal(textOcrOriginal);
        resultat.setTextOcrNormalitzat(textOcrNormalitzat);
        resultat.setOcrDocumentTemporalId(documentTemporal.nomGuardat());
        resultat.setOcrDocumentNomOriginal(documentTemporal.nomOriginal());
        resultat.setOcrDocumentContentType(documentTemporal.contentType());
        resultat.setOcrDocumentUrlTemporal("/uploads/ocr-temp/" + documentTemporal.nomGuardat());

        if (proveidorDetectat == null) {
            resultat.afegirAvis("No s'ha pogut detectar cap proveïdor suportat a partir del document OCR.");
        }

        return resultat;
    }

    public Path obtenirRutaDocumentTemporal(String ocrDocumentTemporalId) {
        if (ocrDocumentTemporalId == null || ocrDocumentTemporalId.isBlank()) {
            throw new IllegalArgumentException("L'identificador del document OCR temporal és obligatori.");
        }

        Path directori = Paths.get(documentsTempPath).toAbsolutePath().normalize();
        Path document = directori.resolve(ocrDocumentTemporalId).normalize();

        if (!document.startsWith(directori)) {
            throw new IllegalArgumentException("Identificador de document OCR temporal no vàlid.");
        }

        return document;
    }


    /*********************       .VALIDACIÓ I DOCUMENT TEMPORAL.       *********************/
    private void validarFitxer(MultipartFile fitxer) {
        if (fitxer == null || fitxer.isEmpty()) {
            throw new IllegalArgumentException("El document de l'albarà és obligatori per executar l'OCR.");
        }

        String nomOriginal = Optional.ofNullable(fitxer.getOriginalFilename()).orElse("");
        String extensio = obtenirExtensio(nomOriginal);

        if (!List.of(".pdf", ".png", ".jpg", ".jpeg").contains(extensio)) {
            throw new IllegalArgumentException("El document OCR ha de ser una imatge JPG/PNG o un PDF.");
        }
    }

    private DocumentTemporalOcr guardarDocumentTemporal(MultipartFile fitxer) {
        String nomOriginal = Optional.ofNullable(fitxer.getOriginalFilename())
                .filter(nom -> !nom.isBlank())
                .orElse("document-ocr");

        String extensio = obtenirExtensio(nomOriginal);
        String nomGuardat = UUID.randomUUID() + extensio;

        try {
            Path directori = Paths.get(documentsTempPath).toAbsolutePath().normalize();
            Files.createDirectories(directori);

            Path desti = directori.resolve(nomGuardat).normalize();

            if (!desti.startsWith(directori)) {
                throw new IllegalStateException("Ruta de document OCR temporal no vàlida.");
            }

            try (InputStream input = fitxer.getInputStream()) {
                Files.copy(input, desti, StandardCopyOption.REPLACE_EXISTING);
            }

            String contentType = Optional.ofNullable(fitxer.getContentType())
                    .filter(valor -> !valor.isBlank())
                    .orElse(inferirContentType(nomOriginal));

            return new DocumentTemporalOcr(nomOriginal, nomGuardat, contentType, desti);

        } catch (IOException ex) {
            LOGGER.error("No s'ha pogut guardar el document temporal OCR.", ex);
            throw new IllegalStateException("No s'ha pogut guardar el document temporal OCR.", ex);
        }
    }


    /*********************       .EXECUCIÓ TESSERACT.       *********************/
    private String extreureTextOcr(DocumentTemporalOcr documentTemporal) {
        String nom = documentTemporal.nomGuardat().toLowerCase(Locale.ROOT);

        if (nom.endsWith(".pdf")) {
            return extreureTextPdf(documentTemporal.ruta());
        }

        return extreureTextImatge(documentTemporal.ruta());
    }

    private String extreureTextImatge(Path rutaImatge) {
        try {
            BufferedImage imatgeOriginal = ImageIO.read(rutaImatge.toFile());

            if (imatgeOriginal == null) {
                throw new IllegalStateException("No s'ha pogut llegir la imatge enviada.");
            }

            return crearTesseract().doOCR(prepararImatgePerOcr(imatgeOriginal));

        } catch (IOException ex) {
            LOGGER.error("Error en llegir una imatge d'albarà per OCR.", ex);
            throw new IllegalStateException("Error llegint la imatge per OCR.", ex);
        } catch (TesseractException ex) {
            LOGGER.error("Error en executar Tesseract sobre una imatge d'albarà.", ex);
            throw new IllegalStateException("Error executant Tesseract OCR.", ex);
        }
    }

    private String extreureTextPdf(Path rutaPdf) {
        try (PDDocument document = Loader.loadPDF(rutaPdf.toFile())) {
            StringBuilder text = new StringBuilder();
            PDFRenderer renderer = new PDFRenderer(document);
            Tesseract tesseract = crearTesseract();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage imatge = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
                text.append(tesseract.doOCR(prepararImatgePerOcr(imatge))).append("\n");
            }

            return text.toString();

        } catch (IOException ex) {
            LOGGER.error("Error en llegir un PDF d'albarà per OCR.", ex);
            throw new IllegalStateException("Error llegint el PDF per OCR.", ex);
        } catch (TesseractException ex) {
            LOGGER.error("Error en executar Tesseract sobre un PDF d'albarà.", ex);
            throw new IllegalStateException("Error executant Tesseract OCR sobre el PDF.", ex);
        }
    }

    private BufferedImage prepararImatgePerOcr(BufferedImage original) {
        int amplada = original.getWidth() * 2;
        int alcada = original.getHeight() * 2;

        BufferedImage escalada = new BufferedImage(amplada, alcada, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = escalada.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(original, 0, 0, amplada, alcada, null);
        graphics.dispose();

        return escalada;
    }

    private Tesseract crearTesseract() {
        validarConfiguracioTesseract();

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage(tesseractLanguage);
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        tesseract.setTessVariable("user_defined_dpi", "300");
        return tesseract;
    }

    private void validarConfiguracioTesseract() {
        if (tessdataPath == null || tessdataPath.isBlank()) {
            throw new IllegalStateException("No s'ha configurat la ruta de tessdata: ocr.tessdata.path");
        }

        String idioma = tesseractLanguage == null || tesseractLanguage.isBlank() ? "spa" : tesseractLanguage;
        Path trainedData = Paths.get(tessdataPath).resolve(idioma + ".traineddata").toAbsolutePath().normalize();

        if (!Files.exists(trainedData)) {
            throw new IllegalStateException("No s'ha trobat el fitxer d'idioma OCR: " + trainedData);
        }
    }


    /*********************       .DELEGACIÓ PARSER.       *********************/
    private OcrAlbaraPendent parsejarAlbara(OcrProveidorDetectat proveidorDetectat,
                                            String textOcrOriginal,
                                            String textOcrNormalitzat) {

        if (proveidorDetectat == null) {
            OcrAlbaraPendent pendent = new OcrAlbaraPendent();
            pendent.afegirAvis("Proveïdor no detectat. Revisa el document i selecciona el proveïdor manualment.");
            pendent.setDataAlbara(OcrUtils.extreurePrimeraDataNormalitzada(textOcrNormalitzat));
            return pendent;
        }

        return parsers.stream()
                .filter(parser -> parser.suporta(proveidorDetectat))
                .findFirst()
                .map(parser -> parser.parsejar(textOcrOriginal, textOcrNormalitzat))
                .orElseGet(() -> crearResultatSenseParser(proveidorDetectat, textOcrNormalitzat));
    }

    private OcrAlbaraPendent crearResultatSenseParser(OcrProveidorDetectat proveidorDetectat, String textOcrNormalitzat) {
        OcrAlbaraPendent pendent = new OcrAlbaraPendent();
        pendent.setProveidorDetectat(proveidorDetectat.getNomVisible());
        pendent.setProveidorCifDetectat(proveidorDetectat.getCifHabitual());
        pendent.setDataAlbara(OcrUtils.extreurePrimeraDataNormalitzada(textOcrNormalitzat));
        pendent.afegirAvis("No s'ha trobat cap parser OCR configurat per a " + proveidorDetectat.getNomVisible() + ".");
        return pendent;
    }


    /*********************       .SUPORT FITXERS.       *********************/
    private String obtenirExtensio(String nomFitxer) {
        String nom = nomFitxer == null ? "" : nomFitxer.trim();
        int index = nom.lastIndexOf('.');

        if (index < 0 || index == nom.length() - 1) {
            return ".bin";
        }

        String extensio = nom.substring(index).toLowerCase(Locale.ROOT);
        return extensio.matches("\\.[a-z0-9]{1,8}") ? extensio : ".bin";
    }

    private String inferirContentType(String nomFitxer) {
        String nom = nomFitxer == null ? "" : nomFitxer.toLowerCase(Locale.ROOT);

        if (nom.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (nom.endsWith(".png")) {
            return "image/png";
        }

        if (nom.endsWith(".jpg") || nom.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        return "application/octet-stream";
    }

    private record DocumentTemporalOcr(
            String nomOriginal,
            String nomGuardat,
            String contentType,
            Path ruta
    ) { }
}
