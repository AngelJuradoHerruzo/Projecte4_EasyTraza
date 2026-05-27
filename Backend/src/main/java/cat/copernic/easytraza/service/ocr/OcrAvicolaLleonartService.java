package cat.copernic.easytraza.service.ocr;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import cat.copernic.easytraza.dto.OcrLiniaDto;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * PARSER OCR D'AVÍCOLA LLEONART.
 *
 * Interpretades les dades OCR dels albarans del proveïdor Avícola Lleonart.
 * També extretes les dades principals i els avisos necessaris per revisar el resultat.
 *
 * @author Ángel Jurado Herruz
 */
@Service
public class OcrAvicolaLleonartService implements OcrParserProveidor {

    private final MessageSource messageSource;

    public OcrAvicolaLleonartService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private static final Pattern PATRON_NUMERO_ALBARA = Pattern.compile(
            "(?:NUM\\.?\\s*ALBAR[A-Z]*|ALBAR[A-Z]*)\\s*[:+\\-]?\\s*(\\d{3,12})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_PRODUCTE = Pattern.compile(
            "\\b(L\\s+GRANEL\\s+CAT[\\.\\- ]*A[\\.\\- ]*RUBIO)\\s+(\\d{1,4})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_LOT = Pattern.compile(
            "\\b(\\d{3}\\s*[-/]?\\s*\\d{4}\\s*[-/]?\\s*\\d{4,6})\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.AVICOLA_LLEONART;
    }

    @Override


    /**
     * INTERPRETACIÓ DEL TEXT OCR.
     *
     * Interpretat el text OCR rebut per construir les dades temporals
     * de l'albarà detectat.
     *
     * @param textOcrOriginal text utilitzat en el procés
     * @param textOcrNormalitzat text utilitzat en el procés
     * @return resultat obtingut pel mètode
     */
    public OcrAlbaraPendent parsejar(String textOcrOriginal, String textOcrNormalitzat) {
        OcrAlbaraPendent resultat = new OcrAlbaraPendent();

        resultat.setProveidorDetectat(getProveidor().getNomVisible());
        resultat.setProveidorCifDetectat(getProveidor().getCifHabitual());
        resultat.setNumeroAlbara(extreureNumeroAlbara(textOcrOriginal));
        resultat.setDataAlbara(extreureDataAlbara(textOcrOriginal));
        resultat.setLinies(extreureLinies(textOcrOriginal));

        afegirAvisosGenerals(resultat);
        return resultat;
    }


    /**
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
     */
    private String extreureNumeroAlbara(String text) {
        String blocInfo = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[AVICOLA_INFO]]",
                "[[AVICOLA_TAULA]]"
        );

        String origen = blocInfo == null || blocInfo.isBlank() ? text : blocInfo;
        String normalitzat = prepararText(origen);
        Matcher matcher = PATRON_NUMERO_ALBARA.matcher(normalitzat);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }


    /**
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
     */
    private String extreureDataAlbara(String text) {
        String blocInfo = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[AVICOLA_INFO]]",
                "[[AVICOLA_TAULA]]"
        );

        String data = OcrUtils.extreurePrimeraDataNormalitzada(blocInfo);
        return data != null ? data : OcrUtils.extreurePrimeraDataNormalitzada(text);
    }


    /**
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
     */
    private List<OcrLiniaDto> extreureLinies(String text) {
        String blocTaula = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[AVICOLA_TAULA]]",
                "[[FI_AVICOLA]]"
        );

        String origen = blocTaula == null || blocTaula.isBlank() ? text : blocTaula;
        String normalitzat = prepararText(origen);

        String materia = extreureMateria(normalitzat);
        Double quantitat = extreureQuantitat(normalitzat);
        String lot = extreureLot(normalitzat);

        List<OcrLiniaDto> linies = new ArrayList<>();

        if (materia == null && quantitat == null && lot == null) {
            return linies;
        }

        OcrLiniaDto linia = new OcrLiniaDto();
        linia.setMateriaPrimeraDetectada(materia);
        linia.setQuantitat(quantitat);
        linia.setUnitat("UNITATS");
        linia.setIdentificadorLot(lot != null && OcrUtils.esLotValid(lot) ? lot : "-");

        if (materia == null || materia.isBlank()) {
            linia.afegirAvis(missatge("ocr.avis.materiaNoDetectada"));
        }

        if (quantitat == null || quantitat <= 0) {
            linia.afegirAvis(missatge("ocr.avis.quantitatNoDetectada"));
        }

        if (lot == null || !OcrUtils.esLotValid(lot)) {
            linia.afegirAvis(missatge("ocr.avis.lotNoDetectat"));
        }

        linies.add(linia);
        return linies;
    }


    /**
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
     */
    private String extreureMateria(String text) {
        Matcher matcher = PATRON_PRODUCTE.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1)
                .replaceAll("\\s+", " ")
                .replace("CAT A", "CAT.A")
                .trim();
    }


    /**
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return valor numèric obtingut
     */
    private Double extreureQuantitat(String text) {
        Matcher matcher = PATRON_PRODUCTE.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        try {
            return Double.valueOf(matcher.group(2));
        } catch (NumberFormatException ex) {
            return null;
        }
    }


    /**
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
     */
    private String extreureLot(String text) {
        String normalitzat = text
                .replace("LOTF", "LOTE")
                .replace("LOIE", "LOTE")
                .replace("LO7E", "LOTE");

        int posicioLot = normalitzat.indexOf("LOTE");
        String origen = posicioLot >= 0 ? normalitzat.substring(posicioLot) : normalitzat;
        Matcher matcher = PATRON_LOT.matcher(origen);

        if (matcher.find()) {
            return OcrUtils.normalitzarLot(matcher.group(1).replaceAll("\\s+", ""));
        }

        return null;
    }


    /**
     * PREPARACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String prepararText(String valor) {
        return OcrUtils.normalitzarPerComparar(valor)
                .replace("RUBID", "RUBIO")
                .replace("RUB1O", "RUBIO")
                .replaceAll("CAT\\s*[\\.\\- ]\\s*A\\s*[\\.\\- ]\\s*RUBIO", "CAT.A-RUBIO")
                .replaceAll("\\s+", " ")
                .trim();
    }


    /**
     * INCORPORACIÓ DE DADES.
     *
     * Incorporada o completada la informació necessària dins de l'objecte
     * que s'està preparant.
     *
     * @param resultat valor de resultat utilitzat pel mètode
     */
    private void afegirAvisosGenerals(OcrAlbaraPendent resultat) {
        if (resultat.getNumeroAlbara() == null || resultat.getNumeroAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "AVÍCOLA LLEONART"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "AVÍCOLA LLEONART"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "AVÍCOLA LLEONART"));
        }
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
