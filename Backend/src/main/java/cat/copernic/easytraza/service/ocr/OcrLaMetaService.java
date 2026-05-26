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
 * Parser OCR específic per al proveïdor LA META.
 *
 * Aquest parser treballa amb les zones OCR generades per
 * OcrAlbaraProveidorService:
 * - informació general de l'albarà;
 * - matèries primeres;
 * - lots;
 * - quantitats en sacs.
 *
 * Les línies es relacionen per la seva posició vertical dins del document.
 */
@Service
public class OcrLaMetaService implements OcrParserProveidor {

    private final MessageSource messageSource;

    public OcrLaMetaService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private static final Pattern PATRON_NUMERO_ALBARA = Pattern.compile(
            "\\b(AV[A-Z0-9]{5,12})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_LOT = Pattern.compile(
            "\\b([A-Z]{0,3}\\d[A-Z0-9]{4,14})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_ENTER = Pattern.compile("\\b\\d{1,4}\\b");

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.LA_META;
    }

    @Override
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
     * Extreu el número d'albarà des de la zona taronja.
     */
    private String extreureNumeroAlbara(String text) {
        String blocInfo = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[LA_META_INFO]]",
                "[[LA_META_MATERIES]]"
        );

        String origen = blocInfo == null || blocInfo.isBlank() ? text : blocInfo;
        String normalitzat = OcrUtils.normalitzarPerComparar(origen);

        Matcher matcher = PATRON_NUMERO_ALBARA.matcher(normalitzat);

        if (matcher.find()) {
            return OcrUtils.normalitzarCodi(matcher.group(1));
        }

        return null;
    }

    /**
     * Extreu la data de l'albarà des de la zona taronja.
     */
    private String extreureDataAlbara(String text) {
        String blocInfo = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[LA_META_INFO]]",
                "[[LA_META_MATERIES]]"
        );

        String data = OcrUtils.extreurePrimeraDataNormalitzada(blocInfo);

        if (data != null) {
            return data;
        }

        return OcrUtils.extreurePrimeraDataNormalitzada(text);
    }

    /**
     * Construeix les línies unint matèria, lot i quantitat per índex.
     */
    private List<OcrLiniaDto> extreureLinies(String text) {
        List<String> materies = extreureMateries(text);
        List<String> lots = extreureLots(text);
        List<Double> quantitats = extreureQuantitats(text);

        int nombreLinies = Math.max(
                materies.size(),
                Math.max(lots.size(), quantitats.size())
        );

        List<OcrLiniaDto> linies = new ArrayList<>();

        for (int index = 0; index < nombreLinies; index++) {
            OcrLiniaDto linia = new OcrLiniaDto();

            String materia = obtenirValor(materies, index);
            String lot = obtenirValor(lots, index);
            Double quantitat = obtenirValor(quantitats, index);

            linia.setMateriaPrimeraDetectada(materia);
            linia.setIdentificadorLot(
                    lot != null && OcrUtils.esLotValid(lot)
                            ? OcrUtils.normalitzarLot(lot)
                            : "-"
            );
            linia.setQuantitat(quantitat);
            linia.setUnitat("SACOS");

            if (materia == null || materia.isBlank()) {
                linia.afegirAvis(missatge("ocr.avis.materiaNoDetectada"));
            }

            if (lot == null || !OcrUtils.esLotValid(lot)) {
                linia.afegirAvis(missatge("ocr.avis.lotNoDetectat"));
            }

            if (quantitat == null || quantitat <= 0) {
                linia.afegirAvis(missatge("ocr.avis.quantitatNoDetectada"));
            }

            linies.add(linia);
        }

        return linies;
    }

    /**
     * Extreu les matèries de la columna CONCEPTO.
     *
     * Elimina els dos dígits de la data de consum que el retall anterior podia
     * capturar al final de la línia.
     */
    private List<String> extreureMateries(String text) {
        String blocMateries = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[LA_META_MATERIES]]",
                "[[LA_META_LOTS]]"
        );

        List<String> materies = new ArrayList<>();

        for (String linia : OcrUtils.obtenirLiniesNoBuides(blocMateries)) {
            String normalitzada = OcrUtils.normalitzarPerComparar(linia);

            if (normalitzada.isBlank()
                    || normalitzada.contains("CONCEPTO")
                    || normalitzada.contains("ARTICULO")
                    || normalitzada.contains("ENVASES")
                    || normalitzada.contains("PALET")) {
                continue;
            }

            String materia = netejarMateria(linia);

            if (materia != null && !materia.isBlank()) {
                materies.add(materia);
            }
        }

        return materies;
    }

    /**
     * Extreu un lot per línia de la columna LOTE.
     *
     * Corregeix lectures separades com "M1 983086" convertint-les a
     * "M1983086" abans de validar-les.
     */
    private List<String> extreureLots(String text) {
        String blocLots = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[LA_META_LOTS]]",
                "[[LA_META_SACOS]]"
        );

        List<String> lots = new ArrayList<>();

        for (String linia : OcrUtils.obtenirLiniesNoBuides(blocLots)) {
            String normalitzada = OcrUtils.normalitzarPerComparar(linia);

            if (normalitzada.isBlank() || normalitzada.contains("LOTE")) {
                continue;
            }

            /*
             * Si el retall encara captura una part de Saco25, s'elimina abans
             * de reconstruir el lot.
             */
            String candidatura = normalitzada
                    .replaceAll("\\s+(?:SA|SACO|SAC0|SAC025|SACO25).*$", "")
                    .replaceAll("\\s+", "")
                    .replaceAll("[^A-Z0-9]", "");

            Matcher matcher = PATRON_LOT.matcher(candidatura);

            if (matcher.find()) {
                String lot = OcrUtils.normalitzarLot(matcher.group(1));

                if (OcrUtils.esLotValid(lot)) {
                    lots.add(lot);
                }
            }
        }

        return lots;
    }

    /**
     * Extreu la quantitat de sacs.
     *
     * Si Tesseract retorna una línia com "5 41", es pren sempre l'últim número,
     * perquè el primer correspon al residu OCR de "Saco25".
     */
    private List<Double> extreureQuantitats(String text) {
        String blocQuantitats = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[LA_META_SACOS]]",
                "[[FI_LA_META]]"
        );

        List<Double> quantitats = new ArrayList<>();

        for (String linia : OcrUtils.obtenirLiniesNoBuides(blocQuantitats)) {
            String normalitzada = OcrUtils.normalitzarPerComparar(linia);

            if (normalitzada.isBlank()
                    || normalitzada.contains("SACOS")
                    || normalitzada.contains("TONELADAS")) {
                continue;
            }

            Matcher matcher = PATRON_ENTER.matcher(normalitzada);
            Double ultimaQuantitat = null;

            while (matcher.find()) {
                Double candidata = OcrUtils.convertirNumero(matcher.group());

                if (candidata != null && candidata > 0 && candidata < 10000) {
                    ultimaQuantitat = candidata;
                }
            }

            if (ultimaQuantitat != null) {
                quantitats.add(ultimaQuantitat);
            }
        }

        return quantitats;
    }

    /**
     * Neteja només errors reals observats a la zona de matèries.
     *
     * No introdueix matèries fixes ni assumeix el contingut de futurs albarans.
     */
    private String netejarMateria(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String materia = valor.trim()
                /*
                 * Dígits de la columna de data capturats accidentalment:
                 * "HARINA PANIF.1956 07" -> "HARINA PANIF.1956".
                 */
                .replaceAll("\\s+\\d{1,2}\\s*$", "")
                /*
                 * Lectura OCR del producte T80:
                 * "T80O" -> "T80".
                 */
                .replaceAll("(?i)\\bT80[O0]\\b", "T80")
                /*
                 * Espais i puntuació sobrants.
                 */
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*[,;]+\\s*$", "")
                .trim();

        return materia.length() < 3 ? null : materia;
    }

    private void afegirAvisosGenerals(OcrAlbaraPendent resultat) {
        if (resultat.getNumeroAlbara() == null || resultat.getNumeroAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "LA META"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "LA META"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "LA META"));
        }
    }

    private <T> T obtenirValor(List<T> valors, int index) {
        return index >= 0 && index < valors.size() ? valors.get(index) : null;
    }

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}