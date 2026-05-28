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
 * PARSER OCR DE PASTISSA.
 *
 * Interpretades les dades OCR dels albarans del proveïdor Pastissa.
 * També aplicades lectures alternatives quan el format del document dificulta la detecció principal.
 *
 * @author Ángel Jurado Herruzo
 */
@Service
public class OcrPastissaService implements OcrParserProveidor {

    private final MessageSource messageSource;

    public OcrPastissaService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private static final Pattern PATRON_NUMERO_ALBARA = Pattern.compile(
            "\\bF\\s*[-:]?\\s*(\\d{4,12})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_CODI_DESCRIPCIO = Pattern.compile(
            "^[^A-Z0-9]*(?:[A-Z]{0,5}\\d[A-Z0-9\\-]{2,16}|CTO)\\s*[:.\\-]?\\s+(.{3,})$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_PRIMER_TOKEN = Pattern.compile("^\\s*([A-Z0-9]{5,18})\\b");
    private static final Pattern PATRON_NUMERO = Pattern.compile("\\b(\\d{1,4}(?:[.,/]\\d{2})?)\\b");

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.PASTISSA;
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
                "[[PASTISSA_INFO]]",
                "[[PASTISSA_MATERIES]]"
        );

        String origen = blocInfo == null || blocInfo.isBlank() ? text : blocInfo;
        Matcher matcher = PATRON_NUMERO_ALBARA.matcher(OcrUtils.normalitzarPerComparar(origen));

        return matcher.find() ? "F-" + matcher.group(1) : null;
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
                "[[PASTISSA_INFO]]",
                "[[PASTISSA_MATERIES]]"
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
        List<String> materies = extreureMateries(text);
        List<String> lots = extreureLots(text);
        List<Double> quantitats = extreureQuantitats(text);

        if (materies.isEmpty()) {
            return extreureLiniesFallback(text);
        }

        List<OcrLiniaDto> linies = new ArrayList<>();

        for (int index = 0; index < materies.size(); index++) {
            String materia = materies.get(index);
            String lot = obtenirValor(lots, index);
            Double quantitat = obtenirValor(quantitats, index);

            OcrLiniaDto linia = new OcrLiniaDto();
            linia.setMateriaPrimeraDetectada(materia);
            linia.setIdentificadorLot(
                    lot != null && OcrUtils.esLotValid(lot)
                            ? OcrUtils.normalitzarLot(lot)
                            : "-"
            );
            linia.setQuantitat(quantitat);
            linia.setUnitat(detectarUnitat(materia));

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
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[PASTISSA_MATERIES]]",
                "[[PASTISSA_LOTS]]"
        );

        List<String> materies = new ArrayList<>();

        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(bloc)) {
            String linia = normalitzarLinia(liniaOriginal);
            String comparar = OcrUtils.normalitzarPerComparar(linia);

            if (comparar.isBlank()
                    || comparar.contains("CODI")
                    || comparar.contains("DESCRIPCIO")
                    || comparar.contains("PREU LITRE")) {
                continue;
            }

            Matcher matcher = PATRON_CODI_DESCRIPCIO.matcher(linia);

            if (!matcher.find()) {
                continue;
            }

            String materia = netejarMateria(matcher.group(1));

            if (materia != null) {
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
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[PASTISSA_LOTS]]",
                "[[PASTISSA_QUANTITATS]]"
        );

        List<String> lots = new ArrayList<>();

        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(bloc)) {
            String linia = OcrUtils.normalitzarPerComparar(liniaOriginal);

            if (linia.isBlank() || linia.contains("LOT")) {
                continue;
            }

            Matcher matcher = PATRON_PRIMER_TOKEN.matcher(linia);

            if (matcher.find()) {
                String lot = normalitzarLotNumeric(matcher.group(1));

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
                "[[PASTISSA_QUANTITATS]]",
                "[[FI_PASTISSA]]"
        );

        List<Double> quantitats = new ArrayList<>();

        Pattern patroQuantitat = Pattern.compile("\\b(\\d{1,4}[\\.,]\\d{1,2})\\b");
        Matcher matcher = patroQuantitat.matcher(blocQuantitats == null ? "" : blocQuantitats);

        while (matcher.find()) {
            String valor = matcher.group(1).replace(".", ",");

            /*
            * Corregeix lectures sense el zero inicial, per exemple:
            * ",25" no entra pel patró; "025" s'hauria de revisar manualment.
            */
            Double quantitat = OcrUtils.convertirNumero(valor);

            if (quantitat != null && quantitat >= 0 && quantitat < 10000) {
                quantitats.add(quantitat);
            }
        }

        return quantitats;
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
    private List<OcrLiniaDto> extreureLiniesFallback(String text) {
        List<OcrLiniaDto> linies = new ArrayList<>();
        String normalitzat = OcrUtils.normalitzarPerComparar(text);
        Pattern patro = Pattern.compile(
                "(?:[A-Z]{0,5}\\d[A-Z0-9\\-]{2,16}|CTO)\\s+(.+?)\\s+"
                + "([A-Z0-9]{5,18})(?:\\s+\\d{1,2}/\\d{1,2}/\\d{2,4})?\\s+"
                + "(\\d{1,4}[.,/]\\d{2})\\b"
        );
        Matcher matcher = patro.matcher(normalitzat);

        while (matcher.find()) {
            String materia = netejarMateria(matcher.group(1));
            String lot = normalitzarLotNumeric(matcher.group(2));
            Double quantitat = convertirDecimalOcr(matcher.group(3));

            OcrLiniaDto linia = new OcrLiniaDto();
            linia.setMateriaPrimeraDetectada(materia);
            linia.setIdentificadorLot(OcrUtils.esLotValid(lot) ? lot : "-");
            linia.setQuantitat(quantitat);
            linia.setUnitat(detectarUnitat(materia));
            linies.add(linia);
        }

        return linies;
    }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String normalitzarLotNumeric(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return OcrUtils.normalitzarLot(
                valor.toUpperCase()
                        .replace('O', '0')
                        .replace('I', '1')
                        .replace('L', '1')
        );
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

        String materia = normalitzarLinia(valor)
                .replaceAll("(?<=\\p{L})!(?=\\p{L})", "I")
                .replaceAll("(?i)\\bREX2\\s*LT\\s*\\(\\s*LITRE\\s*\\)", "REX2 LT. (LITRE)")
                .replaceAll("\\s*[,;:]+$", "")
                .trim();

        return materia.length() < 3 ? null : materia;
    }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String normalitzarLinia(String valor) {
        return valor == null ? "" : valor
                .replace('’', '\'')
                .replaceAll("\\s+", " ")
                .trim();
    }


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param valor valor que s'ha de processar
     * @return valor numèric obtingut
     */
    private Double convertirDecimalOcr(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return Double.valueOf(valor.trim().replace('/', '.').replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }


    /**
     * DETECCIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param materia valor de materia utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String detectarUnitat(String materia) {
        String normalitzada = OcrUtils.normalitzarPerComparar(materia);

        if (normalitzada.contains("KG")) {
            return "KG";
        }

        if (normalitzada.contains("LITRE") || normalitzada.contains("1LT")) {
            return "L";
        }

        return "UT";
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
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "PASTISSA"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "PASTISSA"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "PASTISSA"));
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
        return valors != null && index >= 0 && index < valors.size() ? valors.get(index) : null;
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
