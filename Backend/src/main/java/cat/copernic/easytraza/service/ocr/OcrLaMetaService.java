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
 * PARSER OCR DE LA META.
 *
 * Interpretades les dades OCR dels albarans del proveïdor La Meta.
 * També separades les matèries primeres, lots i quantitats detectades al document.
 *
 * @author Ángel Jurado Herruzo
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
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
     * NETEJA DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String netejarMateria(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String materia = valor.trim()
                // Dígits de la columna de data capturats accidentalment: "HARINA PANIF.1956 07" -> "HARINA PANIF.1956".
                .replaceAll("\\s+\\d{1,2}\\s*$", "")
                
                // Lectura OCR del producte T80: "T80O" -> "T80".
                .replaceAll("(?i)\\bT80[O0]\\b", "T80")
                
                // Espais i puntuació sobrants.
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*[,;]+\\s*$", "")
                .trim();

        return materia.length() < 3 ? null : materia;
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
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "LA META"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "LA META"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "LA META"));
        }
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param valors valor que s'ha de processar
     * @param index valor de index utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private <T> T obtenirValor(List<T> valors, int index) {
        return index >= 0 && index < valors.size() ? valors.get(index) : null;
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