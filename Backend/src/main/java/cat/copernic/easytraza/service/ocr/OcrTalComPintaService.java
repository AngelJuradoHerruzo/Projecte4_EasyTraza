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
 * Parser OCR específic per al proveïdor TAL COM PINTA.
 *
 * La matèria primera es llegeix des de la columna Descripció. El número
 * d'albarà correspon al camp N. Entrega. Les línies es construeixen per ordre
 * vertical i no depenen del codi d'article.
 */
@Service
public class OcrTalComPintaService implements OcrParserProveidor {

    private final MessageSource messageSource;

    public OcrTalComPintaService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private static final Pattern PATRON_NUMERO_ENTREGA = Pattern.compile(
            "\\b([0-9OIL]{6,10})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_DATA = Pattern.compile(
            "\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b"
    );

    private static final Pattern PATRON_SUBLINIA_LOT = Pattern.compile(
            "(?<!\\d)\\d{1,3}(?:[\\.,]\\d{1,2})?\\s+"
            + "([A-Z0-9]{4,14})\\s+"
            + ".*?\\d{1,3}[/-]\\d{1,2}[/-]\\d{2,5}",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_QUANTITAT = Pattern.compile(
            "(?<!\\d)(\\d{1,4}[\\.,]\\d{1,2}|[\\.,]\\d{2})(?!\\d)"
    );

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.TAL_COM_PINTA;
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
     * N. Entrega és el número d'albarà del model TAL COM PINTA.
     */
    private String extreureNumeroAlbara(String text) {
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[TAL_COM_PINTA_NUMERO_ENTREGA]]",
                "[[TAL_COM_PINTA_DATA_ENTREGA]]"
        );

        String numero = buscarNumeroEntrega(bloc);

        if (numero != null) {
            return numero;
        }

        String taulaCompleta = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[TAL_COM_PINTA_TAULA]]",
                "[[FI_TAL_COM_PINTA]]"
        );

        return buscarNumeroEntrega(taulaCompleta);
    }

    private String buscarNumeroEntrega(String valor) {
        String normalitzat = OcrUtils.normalitzarPerComparar(valor);
        Matcher matcher = PATRON_NUMERO_ENTREGA.matcher(normalitzat);

        while (matcher.find()) {
            String candidat = matcher.group(1)
                    .replace('O', '0')
                    .replace('I', '1')
                    .replace('L', '1');

            if (candidat.matches("\\d{6,10}")) {
                return candidat;
            }
        }

        return null;
    }

    private String extreureDataAlbara(String text) {
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[TAL_COM_PINTA_DATA_ENTREGA]]",
                "[[TAL_COM_PINTA_DESCRIPCIONS]]"
        );

        String data = OcrUtils.extreurePrimeraDataNormalitzada(bloc);

        if (data != null) {
            return data;
        }

        return OcrUtils.extreurePrimeraDataNormalitzada(text);
    }

    /**
     * Les descripcions defineixen el nombre de línies reals. Això evita que
     * els imports o altres números creïn files fictícies.
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
            OcrLiniaDto linia = new OcrLiniaDto();
            String materia = materies.get(index);
            String lot = obtenirValor(lots, index);
            Double quantitat = obtenirValor(quantitats, index);

            linia.setMateriaPrimeraDetectada(materia);
            linia.setIdentificadorLot(lotValido(lot) ? lot : "-");
            linia.setQuantitat(quantitat);
            linia.setUnitat(detectarUnitat(materia));

            afegirAvisosLinia(linia);
            linies.add(linia);
        }

        return linies;
    }

    /**
     * Llegeix les files de producte sense dependre del codi d'article.
     * Les files de lot tenen data i es descarten de la llista de matèries.
     */
    private List<String> extreureMateries(String text) {
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[TAL_COM_PINTA_DESCRIPCIONS]]",
                "[[TAL_COM_PINTA_QUANTITATS]]"
        );

        List<String> materies = new ArrayList<>();

        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(bloc)) {
            String comparar = OcrUtils.normalitzarPerComparar(liniaOriginal);

            if (esSoroll(comparar) || PATRON_DATA.matcher(comparar).find()) {
                continue;
            }

            String materia = netejarMateria(liniaOriginal);

            if (semblaMateria(materia)) {
                materies.add(materia);
            }
        }

        return materies;
    }

    /**
     * Extreu els lots des de la zona específica de sublínies.
     *
     * No valida estrictament la data perquè el lot pot ser correcte encara que
     * Tesseract deformi la data de caducitat.
     */
    private List<String> extreureLots(String text) {
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[TAL_COM_PINTA_LOTS]]",
                "[[TAL_COM_PINTA_TAULA]]"
        );

        List<String> lots = obtenirLotsDelBloc(bloc);

        if (lots.isEmpty()) {
            String blocTaula = OcrUtils.extreureBlocEntreMarcadors(
                    text,
                    "[[TAL_COM_PINTA_TAULA]]",
                    "[[FI_TAL_COM_PINTA]]"
            );

            lots = obtenirLotsDelBloc(blocTaula);
        }

        return lots;
    }

    /**
     * Localitza les sublínies quantitat + lot + data i retorna el lot normalitzat.
     */
    private List<String> obtenirLotsDelBloc(String bloc) {
        List<String> lots = new ArrayList<>();

        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(bloc)) {
            String linia = OcrUtils.normalitzarPerComparar(liniaOriginal);
            Matcher matcher = PATRON_SUBLINIA_LOT.matcher(linia);

            if (!matcher.find()) {
                continue;
            }

            String lot = normalitzarLot(matcher.group(1));

            if (lotValido(lot)) {
                lots.add(lot);
            }
        }

        return lots;
    }

    /**
     * Quantitats de la columna Quantitat. La zona és prou alta per admetre
     * més files que les tres de la mostra.
     */
    private List<Double> extreureQuantitats(String text) {
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[TAL_COM_PINTA_QUANTITATS]]",
                "[[TAL_COM_PINTA_TAULA]]"
        );

        List<Double> quantitats = new ArrayList<>();

        for (String linia : OcrUtils.obtenirLiniesNoBuides(bloc)) {
            Matcher matcher = PATRON_QUANTITAT.matcher(linia);

            if (!matcher.find()) {
                continue;
            }

            String valor = matcher.group(1);

            /*
             * En una columna aïllada, una lectura ",00" correspon a la pèrdua
             * del dígit inicial d'una quantitat unitària.
             */
            if (valor.startsWith(",") || valor.startsWith(".")) {
                valor = "1" + valor;
            }

            Double quantitat = OcrUtils.convertirNumero(valor);

            if (quantitat != null && quantitat > 0 && quantitat < 10000) {
                quantitats.add(quantitat);
            }
        }

        return quantitats;
    }

    /**
     * Retorn de seguretat sobre la lectura completa de la taula.
     */
    private List<OcrLiniaDto> extreureLiniesFallback(String text) {
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[TAL_COM_PINTA_TAULA]]",
                "[[FI_TAL_COM_PINTA]]"
        );

        List<OcrLiniaDto> linies = new ArrayList<>();
        OcrLiniaDto pendent = null;

        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(bloc)) {
            String comparar = OcrUtils.normalitzarPerComparar(liniaOriginal);

            if (esSoroll(comparar)) {
                continue;
            }

            if (PATRON_DATA.matcher(comparar).find() && pendent != null) {
                Matcher lotMatcher = PATRON_SUBLINIA_LOT.matcher(comparar);

                if (lotMatcher.find()) {
                    pendent.setIdentificadorLot(normalitzarLot(lotMatcher.group(1)));
                }

                continue;
            }

            String materia = netejarMateria(liniaOriginal);

            if (!semblaMateria(materia)) {
                continue;
            }

            pendent = new OcrLiniaDto();
            pendent.setMateriaPrimeraDetectada(materia);
            pendent.setIdentificadorLot("-");
            pendent.setUnitat(detectarUnitat(materia));

            Matcher quantitatMatcher = PATRON_QUANTITAT.matcher(liniaOriginal);

            if (quantitatMatcher.find()) {
                String valor = quantitatMatcher.group(1);

                if (valor.startsWith(",") || valor.startsWith(".")) {
                    valor = "1" + valor;
                }

                pendent.setQuantitat(OcrUtils.convertirNumero(valor));
            }

            linies.add(pendent);
        }

        linies.forEach(this::afegirAvisosLinia);
        return linies;
    }

    private String netejarMateria(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor
                .replace('|', ' ')
                .replaceAll("^\\s*[A-Z]?\\d{4,10}\\s+", "")
                .replaceAll("\\s+\\d+[\\.,]\\d{1,2}.*$", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*[,;:]+$", "")
                .trim();
    }

    private boolean semblaMateria(String materia) {
        if (materia == null || materia.isBlank()) {
            return false;
        }

        String comparar = OcrUtils.normalitzarPerComparar(materia);

        return comparar.matches(".*[A-Z]{3,}.*")
                && !esSoroll(comparar)
                && !comparar.contains("DATA ENTREGA")
                && !comparar.contains("ADRECA");
    }

    private boolean esSoroll(String valor) {
        return valor == null
                || valor.isBlank()
                || OcrUtils.conteAlguna(
                        valor,
                        "ARTICLE", "DESCRIPCIO", "QUANTITAT", "PVP", "PREU NET",
                        "IMPORT", "IVA", "SUBTOTAL", "BASE IMPOSABLE", "TOTAL",
                        "OPERADOR", "ADRECA", "CONFORME", "NO S ACCEPTEN",
                        "PAGINA", "TEL", "RUTA", "ENTREGA"
                );
    }

    /**
     * Normalitza errors OCR habituals en els lots de TAL COM PINTA.
     *
     * Exemples:
     * - 1602248  -> L602248
     * - 1602265  -> L602265
     * - L6O2248  -> L602248
     * - 0926     -> 0926
     */
    private String normalitzarLot(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String lot = valor.toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        /*
        * OCR confon habitualment el prefix L6 amb 16.
        */
        if (lot.matches("1\\d{6}")) {
            lot = "L" + lot.substring(1);
        }

        /*
        * Dins d'un lot alfanumèric amb prefix L, la lletra O correspon a zero.
        */
        if (lot.startsWith("L")) {
            lot = "L" + lot.substring(1).replace('O', '0');
        }

        return lot;
    }

    private boolean lotValido(String lot) {
        return lot != null && lot.length() >= 4 && lot.matches(".*\\d.*");
    }

    private String detectarUnitat(String materia) {
        String normalitzada = OcrUtils.normalitzarPerComparar(materia);

        if (normalitzada.contains("KG")) {
            return "KG";
        }

        if (normalitzada.contains("LT") || normalitzada.contains("LITRE")) {
            return "L";
        }

        return "UT";
    }

    private void afegirAvisosLinia(OcrLiniaDto linia) {
        if (linia.getMateriaPrimeraDetectada() == null || linia.getMateriaPrimeraDetectada().isBlank()) {
            linia.afegirAvis(missatge("ocr.avis.materiaNoDetectada"));
        }

        if (!lotValido(linia.getIdentificadorLot())) {
            linia.setIdentificadorLot("-");
            linia.afegirAvis(missatge("ocr.avis.lotNoDetectat"));
        }

        if (linia.getQuantitat() == null || linia.getQuantitat() <= 0) {
            linia.afegirAvis(missatge("ocr.avis.quantitatNoDetectada"));
        }
    }

    private void afegirAvisosGenerals(OcrAlbaraPendent resultat) {
        if (resultat.getNumeroAlbara() == null || resultat.getNumeroAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "TAL COM PINTA"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "TAL COM PINTA"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "TAL COM PINTA"));
        }
    }

    private <T> T obtenirValor(List<T> valors, int index) {
        return valors != null && index >= 0 && index < valors.size() ? valors.get(index) : null;
    }

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
