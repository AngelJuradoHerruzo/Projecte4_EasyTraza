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

    @Value("${ocr.documents.temp-path:backend/uploads/ocr-temp}")
    private String documentsTempPath;

    public OcrAlbaraProveidorService(List<OcrParserProveidor> parsers) {
        this.parsers = parsers;
    }


    /*********************       .API PÚBLICA.       *********************/
    public OcrResultatAlbaraProveidorDto processarDocument(MultipartFile fitxer) {
        validarFitxer(fitxer);

        DocumentTemporalOcr documentTemporal = guardarDocumentTemporal(fitxer);

        String textOcrOriginal = extreureTextOcr(documentTemporal);
        String textComparacioInicial = OcrUtils.normalitzarPerComparar(textOcrOriginal);
        OcrProveidorDetectat proveidorDetectat = OcrProveidorDetectat.detectar(textComparacioInicial);

        if (proveidorDetectat == OcrProveidorDetectat.LA_META) {
            String textZonesLaMeta = extreureTextZonesLaMeta(documentTemporal);

            if (textZonesLaMeta != null && !textZonesLaMeta.isBlank()) {
                textOcrOriginal = textOcrOriginal + "\n\n" + textZonesLaMeta;
            }
        }

        if (proveidorDetectat == OcrProveidorDetectat.ARTIPAS) {
            String textZonesArtipas = extreureTextZonesArtipas(documentTemporal);

            if (textZonesArtipas != null && !textZonesArtipas.isBlank()) {
                textOcrOriginal = textOcrOriginal + "\n\n" + textZonesArtipas;
            }
        }

        if (proveidorDetectat == OcrProveidorDetectat.AVICOLA_LLEONART) {
            String textZonesAvicola = extreureTextZonesAvicolaLleonart(documentTemporal);

            if (textZonesAvicola != null && !textZonesAvicola.isBlank()) {
                textOcrOriginal = textOcrOriginal + "\n\n" + textZonesAvicola;
            }
        }

        if (proveidorDetectat == OcrProveidorDetectat.PASTISSA) {
            String textZonesPastissa = extreureTextZonesPastissa(documentTemporal);

            if (textZonesPastissa != null && !textZonesPastissa.isBlank()) {
                textOcrOriginal = textOcrOriginal + "\n\n" + textZonesPastissa;
            }
        }

        if (proveidorDetectat == OcrProveidorDetectat.TAL_COM_PINTA) {
            String textZonesTalComPinta = extreureTextZonesTalComPinta(documentTemporal);

            if (textZonesTalComPinta != null && !textZonesTalComPinta.isBlank()) {
                textOcrOriginal = textOcrOriginal + "\n\n" + textZonesTalComPinta;
            }
        }

        /*
         * JOSE NOVAU es llegeix millor amb la pàgina completa i PSM 6:
         * Tesseract conserva les quatre files com a línies completes.
         * No es força una separació per columnes, que en aquest model és
         * menys estable per l'amplada estreta d'ARTICLE i QUANTITAT.
         */
        if (proveidorDetectat == OcrProveidorDetectat.JOSE_NOVAU) {
            String textJoseNovau = extreureTextJoseNovauPaginaCompleta(documentTemporal);

            if (textJoseNovau != null && !textJoseNovau.isBlank()) {
                textOcrOriginal = textOcrOriginal + "\n\n" + textJoseNovau;
            }
        }

        String textOcrNormalitzat = OcrUtils.normalitzarText(textOcrOriginal);
        String textComparacio = OcrUtils.normalitzarPerComparar(textOcrNormalitzat);

        if (proveidorDetectat == null) {
            proveidorDetectat = OcrProveidorDetectat.detectar(textComparacio);
        }

        OcrAlbaraPendent albaraPendent = parsejarAlbara(
                proveidorDetectat,
                textOcrOriginal,
                textOcrNormalitzat
        );

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

    public String obtenirUrlDocumentTemporal(String ocrDocumentTemporalId) {
        obtenirRutaDocumentTemporal(ocrDocumentTemporalId);
        return "/uploads/ocr-temp/" + ocrDocumentTemporalId;
    }

    public String obtenirContentTypeDocumentTemporal(String ocrDocumentTemporalId) {
        Path ruta = obtenirRutaDocumentTemporal(ocrDocumentTemporalId);

        try {
            String contentType = Files.probeContentType(ruta);

            if (contentType != null && !contentType.isBlank()) {
                return contentType;
            }
        } catch (IOException ignored) {
            // Es fa servir la inferència per extensió.
        }

        return inferirContentType(ocrDocumentTemporalId);
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

            return new DocumentTemporalOcr(
                    nomOriginal,
                    nomGuardat,
                    contentType,
                    desti
            );

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

    /**
     * Executa OCR per zones del model LA META, mantenint separades les columnes
     * que el text complet tendeix a barrejar.
     */
    private String extreureTextZonesLaMeta(DocumentTemporalOcr documentTemporal) {
        try {
            BufferedImage imatge = llegirPrimeraImatgeDocument(documentTemporal);

            if (imatge == null) {
                return "";
            }

            String info = executarOcrZona(imatge, 0.035, 0.315, 0.425, 0.075);
            String materies = executarOcrZona(imatge, 0.145, 0.415, 0.345, 0.115);
            String lots = executarOcrZona(imatge, 0.598, 0.395, 0.130, 0.145);
            String sacos = executarOcrZona(imatge, 0.800, 0.395, 0.080, 0.145);

            return """
                    [[LA_META_INFO]]
                    %s
                    [[LA_META_MATERIES]]
                    %s
                    [[LA_META_LOTS]]
                    %s
                    [[LA_META_SACOS]]
                    %s
                    [[FI_LA_META]]
                    """.formatted(info, materies, lots, sacos);

        } catch (Exception ex) {
            LOGGER.warn("No s'ha pogut executar l'OCR per zones de LA META. Es farà servir el text OCR general.", ex);
            return "";
        }
    }

    /**
     * Executa OCR sobre les zones pròpies d'ARTIPAS.
     */
    private String extreureTextZonesArtipas(DocumentTemporalOcr documentTemporal) {
        try {
            BufferedImage imatge = llegirPrimeraImatgeDocument(documentTemporal);

            if (imatge == null) {
                return "";
            }

            String info = executarOcrZona(imatge, 0.040, 0.285, 0.480, 0.135, 6);

            /*
             * Primera fila real de producte.
             *
             * La fila [N7K] queda just sota la capçalera de la taula. La zona
             * anterior començava massa avall i capturava la fila següent.
             */
            String primeraLinia = executarOcrZona(imatge, 0.030, 0.438, 0.850, 0.035, 7);

            /*
             * Taula principal completa.
             *
             * Es puja l'inici de la zona perquè inclogui la capçalera i la
             * primera fila [N7K].
             */
            String taula = executarOcrZona(imatge, 0.030, 0.415, 0.850, 0.225, 6);

            return """
                    [[ARTIPAS_INFO]]
                    %s
                    [[ARTIPAS_PRIMERA_LINIA]]
                    %s
                    [[ARTIPAS_TAULA]]
                    %s
                    [[FI_ARTIPAS]]
                    """.formatted(info, primeraLinia, taula);

        } catch (Exception ex) {
            LOGGER.warn("No s'ha pogut executar l'OCR per zones d'ARTIPAS. Es farà servir el text OCR general.", ex);
            return "";
        }
    }

    /**
     * Executa OCR sobre les zones pròpies d'AVÍCOLA LLEONART. Aquest model
     * conté una única línia de recepció, amb el lot situat a la línia
     * immediatament inferior a la descripció.
     */
    private String extreureTextZonesAvicolaLleonart(DocumentTemporalOcr documentTemporal) {
        try {
            BufferedImage imatge = llegirPrimeraImatgeDocument(documentTemporal);

            if (imatge == null) {
                return "";
            }

            /*
             * Informació d'albarà: NUM. ALBARAN i FECHA ALBARAN.
             */
            String info = executarOcrZona(imatge, 0.100, 0.320, 0.425, 0.095, 6);

            /*
             * Línia de producte, quantitat i lot. Es manté en un sol retall
             * perquè la quantitat 60 està alineada amb la descripció i el lot
             * apareix just sota la mateixa línia.
             */
            String taula = executarOcrZona(imatge, 0.080, 0.402, 0.760, 0.115, 6);

            return """
                    [[AVICOLA_INFO]]
                    %s
                    [[AVICOLA_TAULA]]
                    %s
                    [[FI_AVICOLA]]
                    """.formatted(info, taula);

        } catch (Exception ex) {
            LOGGER.warn("No s'ha pogut executar l'OCR per zones d'AVÍCOLA LLEONART. Es farà servir el text OCR general.", ex);
            return "";
        }
    }

    /**
     * Executa OCR per zones del model PASTISSA sense ampliar la imatge.
     *
     * Aquest document té tipografia petita i regular; en ampliar el retall
     * Tesseract pot deformar especialment el número d'albarà F - 813964.
     * La resta de proveïdors continuen utilitzant l'escalat habitual.
     */
    private String extreureTextZonesPastissa(DocumentTemporalOcr documentTemporal) {
        try {
            BufferedImage imatge = llegirPrimeraImatgeDocument(documentTemporal);

            if (imatge == null) {
                return "";
            }

            /*
            * Capçalera superior: número d'albarà i data.
            * Es puja respecte de l'anterior perquè no capturi les primeres files
            * de productes.
            */
            String numeroAlbara = executarOcrZona(imatge, 0.120, 0.185, 0.175, 0.035, 7);
            String info = executarOcrZona(imatge, 0.040, 0.165, 0.390, 0.090, 6);

            /*
            * Taula completa de productes.
            * Les zones anteriors començaven massa avall i per això només es detectaven
            * les files a partir del quart producte.
            */
            String materies = executarOcrZona(imatge, 0.050, 0.245, 0.470, 0.420, 6);
            String lots = executarOcrZona(imatge, 0.500, 0.245, 0.165, 0.420, 6);
            String quantitats = executarOcrZona(imatge, 0.635, 0.350, 0.075, 0.360, 6);

            LOGGER.info("=== OCR PASTISSA - NUMERO ALBARA ===\n{}", numeroAlbara);
            LOGGER.info("=== OCR PASTISSA - INFO ===\n{}", info);
            LOGGER.info("=== OCR PASTISSA - MATERIES ===\n{}", materies);
            LOGGER.info("=== OCR PASTISSA - LOTS ===\n{}", lots);
            LOGGER.info("=== OCR PASTISSA - QUANTITATS ===\n{}", quantitats);

            return """
                    [[PASTISSA_NUMERO_ALBARA]]
                    %s
                    [[PASTISSA_INFO]]
                    %s
                    [[PASTISSA_MATERIES]]
                    %s
                    [[PASTISSA_LOTS]]
                    %s
                    [[PASTISSA_QUANTITATS]]
                    %s
                    [[FI_PASTISSA]]
                    """.formatted(numeroAlbara, info, materies, lots, quantitats);

        } catch (Exception ex) {
            LOGGER.warn("No s'ha pogut executar l'OCR per zones de PASTISSA. Es farà servir el text OCR general.", ex);
            return "";
        }
    }

    /**
     * Executa OCR per zones del model TAL COM PINTA.
     *
     * El model pot tenir més línies que l'exemple actual. Per això la zona de
     * taula s'estén fins abans del bloc de totals i el parser filtra la part
     * realment útil.
     */
    private String extreureTextZonesTalComPinta(DocumentTemporalOcr documentTemporal) {
        try {
            BufferedImage imatge = llegirPrimeraImatgeDocument(documentTemporal);

            if (imatge == null) {
                return "";
            }

            /*
             * Capçalera amb número d'entrega i dates.
             */
            String info = executarOcrZona(imatge, 0.045, 0.155, 0.470, 0.120, 6);

            /*
             * Taula de línies. Es deixa prou alta per suportar albarans amb
             * més línies, però sense arribar al bloc inferior de totals.
             */
            String taula = executarOcrZona(imatge, 0.045, 0.245, 0.830, 0.345, 6);

            LOGGER.info("=== OCR TAL COM PINTA - INFO ===\n{}", info);
            LOGGER.info("=== OCR TAL COM PINTA - TAULA ===\n{}", taula);

            return """
                    [[TAL_COM_PINTA_INFO]]
                    %s
                    [[TAL_COM_PINTA_TAULA]]
                    %s
                    [[FI_TAL_COM_PINTA]]
                    """.formatted(info, taula);

        } catch (Exception ex) {
            LOGGER.warn("No s'ha pogut executar l'OCR per zones de TAL COM PINTA. Es farà servir el text OCR general.", ex);
            return "";
        }
    }


    /**
     * Executa OCR de JOSE NOVAU sobre la pàgina completa amb PSM 6.
     *
     * En aquest model, la lectura global conserva millor cada fila completa:
     * ARTICLE + DESCRIPCIÓ + QUANTITAT. Això és més fiable que separar
     * columnes petites per zones.
     */
    private String extreureTextJoseNovauPaginaCompleta(DocumentTemporalOcr documentTemporal) {
        try {
            BufferedImage imatge = llegirPrimeraImatgeDocument(documentTemporal);

            if (imatge == null) {
                return "";
            }

            Tesseract tesseract = crearTesseract();
            tesseract.setPageSegMode(6);

            String text = tesseract.doOCR(prepararImatgePerOcr(imatge));

            LOGGER.info("=== OCR JOSE NOVAU - PAGINA COMPLETA PSM6 ===\n{}", text);

            return """
                    [[JOSE_NOVAU_TEXT]]
                    %s
                    [[FI_JOSE_NOVAU]]
                    """.formatted(text);

        } catch (Exception ex) {
            LOGGER.warn("No s'ha pogut executar l'OCR PSM6 de JOSE NOVAU. Es farà servir el text OCR general.", ex);
            return "";
        }
    }

    /**
     * Executa OCR sobre una zona que només ha de contenir valors numèrics.
     *
     * S'utilitza per a columnes com QUANT de PASTISSA, evitant que Tesseract
     * intenti interpretar text de columnes pròximes.
     */
    private String executarOcrZonaNumerica(BufferedImage imatge,
                                        double x,
                                        double y,
                                        double amplada,
                                        double alcada,
                                        int pageSegMode) throws TesseractException {

        BufferedImage zona = retallarZona(imatge, x, y, amplada, alcada);
        Tesseract tesseract = crearTesseract();

        tesseract.setPageSegMode(pageSegMode);
        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789,.");
        tesseract.setTessVariable("preserve_interword_spaces", "1");

        return tesseract.doOCR(prepararImatgePerOcr(zona));
    }

    private BufferedImage llegirPrimeraImatgeDocument(DocumentTemporalOcr documentTemporal) throws IOException {
        String nom = documentTemporal.nomGuardat().toLowerCase(Locale.ROOT);

        if (nom.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(documentTemporal.ruta().toFile())) {
                if (document.getNumberOfPages() == 0) {
                    return null;
                }

                PDFRenderer renderer = new PDFRenderer(document);
                return renderer.renderImageWithDPI(0, 300, ImageType.RGB);
            }
        }

        return ImageIO.read(documentTemporal.ruta().toFile());
    }

    private String executarOcrZona(
            BufferedImage imatge,
            double x,
            double y,
            double amplada,
            double alcada
    ) throws TesseractException {
        return executarOcrZona(imatge, x, y, amplada, alcada, null);
    }

    private String executarOcrZona(
            BufferedImage imatge,
            double x,
            double y,
            double amplada,
            double alcada,
            Integer pageSegMode
    ) throws TesseractException {

        BufferedImage zona = retallarZona(imatge, x, y, amplada, alcada);
        Tesseract tesseract = crearTesseract();

        if (pageSegMode != null) {
            tesseract.setPageSegMode(pageSegMode);
        }

        return tesseract.doOCR(prepararImatgePerOcr(zona));
    }

    /**
     * Executa OCR sobre una zona sense canviar-ne la resolució.
     *
     * Només s'utilitza per PASTISSA per evitar deformar els caràcters petits
     * del número d'albarà i de la seva taula.
     */
    private String executarOcrZonaSenseEscalar(
            BufferedImage imatge,
            double x,
            double y,
            double amplada,
            double alcada,
            Integer pageSegMode
    ) throws TesseractException {

        BufferedImage zona = retallarZona(imatge, x, y, amplada, alcada);
        Tesseract tesseract = crearTesseract();

        if (pageSegMode != null) {
            tesseract.setPageSegMode(pageSegMode);
        }

        return tesseract.doOCR(convertirAGrisosSenseEscalar(zona));
    }

    private BufferedImage retallarZona(
            BufferedImage imatge,
            double x,
            double y,
            double amplada,
            double alcada
    ) {
        int imageWidth = imatge.getWidth();
        int imageHeight = imatge.getHeight();

        int cropX = limitar((int) Math.round(imageWidth * x), 0, imageWidth - 1);
        int cropY = limitar((int) Math.round(imageHeight * y), 0, imageHeight - 1);
        int cropWidth = limitar((int) Math.round(imageWidth * amplada), 1, imageWidth - cropX);
        int cropHeight = limitar((int) Math.round(imageHeight * alcada), 1, imageHeight - cropY);

        return imatge.getSubimage(cropX, cropY, cropWidth, cropHeight);
    }

    private int limitar(int valor, int minim, int maxim) {
        return Math.max(minim, Math.min(valor, maxim));
    }

    private BufferedImage prepararImatgePerOcr(BufferedImage original) {
        int amplada = original.getWidth() * 2;
        int alcada = original.getHeight() * 2;

        BufferedImage escalada = new BufferedImage(
                amplada,
                alcada,
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D graphics = escalada.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        graphics.drawImage(original, 0, 0, amplada, alcada, null);
        graphics.dispose();

        return escalada;
    }

    /**
     * Converteix una zona a escala de grisos mantenint la mida original.
     *
     * No amplia la imatge. Actualment només s'utilitza a PASTISSA.
     */
    private BufferedImage convertirAGrisosSenseEscalar(BufferedImage original) {
        BufferedImage grisos = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D graphics = grisos.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        graphics.drawImage(
                original,
                0,
                0,
                original.getWidth(),
                original.getHeight(),
                null
        );
        graphics.dispose();

        return grisos;
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

        String idioma = tesseractLanguage == null || tesseractLanguage.isBlank()
                ? "spa"
                : tesseractLanguage;

        Path trainedData = Paths.get(tessdataPath)
                .resolve(idioma + ".traineddata")
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(trainedData)) {
            throw new IllegalStateException("No s'ha trobat el fitxer d'idioma OCR: " + trainedData);
        }
    }


    /*********************       .DELEGACIÓ PARSER.       *********************/
    private OcrAlbaraPendent parsejarAlbara(
            OcrProveidorDetectat proveidorDetectat,
            String textOcrOriginal,
            String textOcrNormalitzat
    ) {

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

    private OcrAlbaraPendent crearResultatSenseParser(
            OcrProveidorDetectat proveidorDetectat,
            String textOcrNormalitzat
    ) {
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
    ) {
    }
}