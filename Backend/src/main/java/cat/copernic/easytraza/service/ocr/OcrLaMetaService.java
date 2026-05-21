package cat.copernic.easytraza.service.ocr;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import cat.copernic.easytraza.dto.OcrLiniaDto;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Parser OCR específic per al proveïdor LA META.
 */
@Service
public class OcrLaMetaService implements OcrParserProveidor {

    private static final Pattern PATRON_NUMERO_ALBARA = Pattern.compile(
            "\\b(AV[A-Z0-9]{5,12})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_LOT_LA_META = Pattern.compile(
            "\\b([A-Z]{0,3}\\d[A-Z0-9]{4,12})\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.LA_META;
    }

    @Override
    public OcrAlbaraPendent parsejar(String textOcrOriginal, String textOcrNormalitzat) {
        String textOriginal = OcrUtils.normalitzarText(textOcrOriginal);
        String textComparar = OcrUtils.normalitzarPerComparar(textOcrOriginal);

        OcrAlbaraPendent resultat = new OcrAlbaraPendent();
        resultat.setProveidorDetectat(getProveidor().getNomVisible());
        resultat.setProveidorCifDetectat(getProveidor().getCifHabitual());
        resultat.setNumeroAlbara(extreureNumeroAlbara(textComparar));
        resultat.setDataAlbara(extreureDataAlbara(textComparar));
        resultat.setLinies(extreureLinies(textOriginal));

        afegirAvisos(resultat);
        return resultat;
    }

    private String extreureNumeroAlbara(String text) {
        String zonaInfo = OcrUtils.extreureBlocEntreMarcadors(text, "[[LA_META_INFO]]", "[[LA_META_MATERIES]]");
        String origen = zonaInfo == null || zonaInfo.isBlank() ? text : zonaInfo;
        Matcher matcher = PATRON_NUMERO_ALBARA.matcher(OcrUtils.normalitzarPerComparar(origen));

        while (matcher.find()) {
            String numero = OcrUtils.normalitzarCodi(matcher.group(1));

            if (numero != null && numero.length() >= 6 && !numero.equals("AVM")) {
                return numero;
            }
        }

        return null;
    }

    private String extreureDataAlbara(String text) {
        String zonaInfo = OcrUtils.extreureBlocEntreMarcadors(text, "[[LA_META_INFO]]", "[[LA_META_MATERIES]]");

        if (zonaInfo != null && !zonaInfo.isBlank()) {
            String data = OcrUtils.extreurePrimeraDataNormalitzada(zonaInfo);

            if (data != null) {
                return data;
            }
        }

        return OcrUtils.extreurePrimeraDataNormalitzada(text);
    }

    private List<OcrLiniaDto> extreureLinies(String text) {
        List<String> materies = extreureMateries(text);
        List<String> lots = extreureLots(text);
        List<Double> quantitats = extreureQuantitats(text);

        int total = Math.max(materies.size(), Math.max(lots.size(), quantitats.size()));
        List<OcrLiniaDto> linies = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            OcrLiniaDto linia = new OcrLiniaDto();

            String materia = i < materies.size() ? materies.get(i) : null;
            String lot = i < lots.size() ? lots.get(i) : "-";
            Double quantitat = i < quantitats.size() ? quantitats.get(i) : null;

            linia.setMateriaPrimeraDetectada(materia);
            linia.setIdentificadorLot(OcrUtils.esLotValid(lot) ? OcrUtils.normalitzarLot(lot) : "-");
            linia.setQuantitat(quantitat);
            linia.setUnitat("SACOS");

            if (materia == null || materia.isBlank()) {
                linia.afegirAvis("No s'ha pogut detectar la matèria primera.");
            }

            if (lot == null || lot.isBlank() || "-".equals(linia.getIdentificadorLot())) {
                linia.afegirAvis("No s'ha pogut detectar correctament el lot.");
            }

            if (quantitat == null || quantitat <= 0) {
                linia.afegirAvis("No s'ha pogut detectar correctament la quantitat.");
            }

            linies.add(linia);
        }

        if (linies.isEmpty()) {
            linies.addAll(extreureLiniesFallbackTextComplet(text));
        }

        return linies;
    }

    private List<String> extreureMateries(String text) {
        String blocMateries = OcrUtils.extreureBlocEntreMarcadors(text, "[[LA_META_MATERIES]]", "[[LA_META_LOTS]]");
        List<String> materies = new ArrayList<>();

        for (String linia : OcrUtils.obtenirLiniesNoBuides(blocMateries)) {
            String normalitzada = OcrUtils.normalitzarPerComparar(linia);

            if (normalitzada.contains("CONCEPTO")
                    || normalitzada.contains("ARTICLE")
                    || normalitzada.contains("ARTICULO")
                    || normalitzada.contains("ENVASES")
                    || normalitzada.contains("PALET")) {
                continue;
            }

            String materia = netejarMateriaLaMeta(linia);

            if (materia != null && !materia.isBlank()) {
                materies.add(materia);
            }
        }

        if (materies.size() < 4) {
            materies = completarMateriesConocidasLaMeta(blocMateries, materies);
        }

        return materies;
    }

    private List<String> completarMateriesConocidasLaMeta(String blocMateries, List<String> materiesActuales) {
        List<String> materies = new ArrayList<>(materiesActuales);
        String text = OcrUtils.normalitzarPerComparar(blocMateries);

        afegirMateriaSiFalta(materies, text, "HARINA PANIF. ILERDA Esp.", "HARINA PANIF", "ILERDA");
        afegirMateriaSiFalta(materies, text, "HARINA PANIF.1956", "HARINA PANIF", "1956");
        afegirMateriaSiFalta(materies, text, "SEMOLINA TRIGO DURO", "SEMOLINA", "TRIGO DURO");
        afegirMateriaSiFalta(materies, text, "HARINA MOLIDA A LA PIEDRA BISE T80", "MOLIDA", "PIEDRA");

        return materies;
    }

    private void afegirMateriaSiFalta(List<String> materies, String text, String materia, String primeraClau, String segonaClau) {
        boolean existeix = materies.stream()
                .map(OcrUtils::normalitzarPerComparar)
                .anyMatch(valor -> valor.contains(OcrUtils.normalitzarPerComparar(primeraClau))
                && valor.contains(OcrUtils.normalitzarPerComparar(segonaClau)));

        if (!existeix && text != null && text.contains(OcrUtils.normalitzarPerComparar(primeraClau))
                && text.contains(OcrUtils.normalitzarPerComparar(segonaClau))) {
            materies.add(materia);
        }
    }

    private List<String> extreureLots(String text) {
        String blocLots = OcrUtils.extreureBlocEntreMarcadors(text, "[[LA_META_LOTS]]", "[[LA_META_SACOS]]");
        String textLots = OcrUtils.normalitzarPerComparar(blocLots)
                .replace("LOTE", " ")
                .replace("L0TE", " ")
                .replace("IOTE", " ");

        List<String> lots = new ArrayList<>();
        Matcher matcher = PATRON_LOT_LA_META.matcher(textLots);

        while (matcher.find()) {
            String lot = OcrUtils.normalitzarLot(matcher.group(1));

            if (OcrUtils.esLotValid(lot) && !lots.contains(lot)) {
                lots.add(lot);
            }
        }

        return lots;
    }

    private List<Double> extreureQuantitats(String text) {
        String blocSacos = OcrUtils.extreureBlocEntreMarcadors(text, "[[LA_META_SACOS]]", "[[FI_LA_META]]");
        String normalitzat = OcrUtils.normalitzarPerComparar(blocSacos)
                .replace("SACOS", " ")
                .replace("SACO", " ");

        List<Double> quantitats = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\b\\d{1,4}\\b").matcher(normalitzat);

        while (matcher.find()) {
            Double quantitat = OcrUtils.convertirNumero(matcher.group());

            if (quantitat != null && quantitat > 0 && quantitat < 10000) {
                quantitats.add(quantitat);
            }
        }

        return quantitats;
    }

    private List<OcrLiniaDto> extreureLiniesFallbackTextComplet(String text) {
        List<OcrLiniaDto> linies = new ArrayList<>();
        String normalitzat = OcrUtils.normalitzarPerComparar(text);

        Pattern patroFila = Pattern.compile(
                "\\b(\\d{4,5})\\s+(.+?)\\s+"
                + "(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\s+"
                + "([A-Z0-9]{5,14})\\s+SACO\\s*25\\s+(\\d{1,4})\\b",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = patroFila.matcher(normalitzat);

        while (matcher.find()) {
            OcrLiniaDto linia = new OcrLiniaDto();
            linia.setMateriaPrimeraDetectada(netejarMateriaLaMeta(matcher.group(2)));
            linia.setIdentificadorLot(OcrUtils.normalitzarLot(matcher.group(4)));
            linia.setQuantitat(OcrUtils.convertirNumero(matcher.group(5)));
            linia.setUnitat("SACOS");
            linies.add(linia);
        }

        return linies;
    }

    private void afegirAvisos(OcrAlbaraPendent resultat) {
        if (resultat.getNumeroAlbara() == null || resultat.getNumeroAlbara().isBlank()) {
            resultat.afegirAvis("No s'ha pogut detectar el número d'albarà de LA META.");
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis("No s'ha pogut detectar la data de l'albarà de LA META.");
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis("No s'ha pogut detectar cap línia de producte de LA META.");
        }
    }

    private String netejarMateriaLaMeta(String materia) {
        if (materia == null) {
            return null;
        }

        String neta = materia
                .replace("¡", "H")
                .replace("|", "I")
                .replaceAll("[?¡]ARINA", "HARINA")
                .replaceAll("\\bARINA\\b", "HARINA")
                .replaceAll("\\bPANIF\\s*\\.?", "PANIF.")
                .replaceAll("\\bESP\\.?\\b", "Esp.")
                .replaceAll("\\bCONCEPTO\\b", " ")
                .replaceAll("\\bARTICULO\\b", " ")
                .replaceAll("\\bARTICLE\\b", " ")
                .replaceAll("\\bLOTE\\b", " ")
                .replaceAll("\\bSACOS\\b", " ")
                .replaceAll("\\bSACO\\b", " ")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*[.,;:]+$", "")
                .trim();

        return neta.length() < 3 ? null : neta;
    }
}