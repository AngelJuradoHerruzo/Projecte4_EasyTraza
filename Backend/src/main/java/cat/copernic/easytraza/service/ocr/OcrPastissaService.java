package cat.copernic.easytraza.service.ocr;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import cat.copernic.easytraza.dto.OcrLiniaDto;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Parser OCR específic per al proveïdor PASTISSA.
 *
 * Treballa amb quatre zones independents: informació de l'albarà,
 * descripcions, lots i quantitats. Les línies es relacionen segons el seu
 * ordre vertical, fent que la columna de descripcions determini les files.
 */
@Service
public class OcrPastissaService implements OcrParserProveidor {

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
     * Les descripcions defineixen el nombre real de files. D'aquesta manera,
     * un número residual a la zona de quantitats no crea una línia buida nova.
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
                linia.afegirAvis("No s'ha pogut detectar correctament el lot.");
            }

            if (quantitat == null || quantitat <= 0) {
                linia.afegirAvis("No s'ha pogut detectar correctament la quantitat.");
            }

            linies.add(linia);
        }

        return linies;
    }

    /**
     * Llegeix la columna de descripció. Es descarta "preu litre" perquè no és
     * part de la matèria primera, sinó informació comercial del primer article.
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
     * Llegeix només el primer token de cada fila de la columna LOT; la data de
     * caducitat situada a la dreta queda fora del valor retornat.
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
     * Extreu les quantitats detectades a la columna QUANT.
     *
     * La zona OCR és exclusivament numèrica i pot contenir espais buits entre
     * línies. Es conserva cada valor decimal llegit en ordre vertical.
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
     * Retorn de seguretat si no es poden obtenir les descripcions del retall.
     * Interpreta únicament files completes presents al text OCR general.
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

    private String normalitzarLinia(String valor) {
        return valor == null ? "" : valor
                .replace('’', '\'')
                .replaceAll("\\s+", " ")
                .trim();
    }

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

    private void afegirAvisosGenerals(OcrAlbaraPendent resultat) {
        if (resultat.getNumeroAlbara() == null || resultat.getNumeroAlbara().isBlank()) {
            resultat.afegirAvis("No s'ha pogut detectar el número d'albarà de PASTISSA.");
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis("No s'ha pogut detectar la data de l'albarà de PASTISSA.");
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis("No s'ha pogut detectar cap línia de producte de PASTISSA.");
        }
    }

    private <T> T obtenirValor(List<T> valors, int index) {
        return valors != null && index >= 0 && index < valors.size() ? valors.get(index) : null;
    }
}
