package cat.copernic.easytraza.service.ocr;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import cat.copernic.easytraza.dto.OcrLiniaDto;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Parser OCR específic per al proveïdor JOSE NOVAU DIL.
 *
 * Aquest model s'interpreta a partir del text complet de la pàgina, ja que
 * Tesseract conserva prou bé les files de la taula quan es processen juntes.
 */
@Service
public class OcrJoseNovauService implements OcrParserProveidor {

    private static final Pattern PATRON_NUMERO_ALBARA = Pattern.compile(
            "\\b(\\d{6})\\b"
    );

    private static final Pattern PATRON_QUANTITAT_DECIMAL = Pattern.compile(
            "(?<!\\d)(\\d{1,4}[\\.,]\\d{2})(?!\\d)"
    );

    private static final Pattern PATRON_QUANTITAT_SENSE_SEPARADOR = Pattern.compile(
            "\\b(\\d{3})\\s*$"
    );

    private static final Pattern PATRON_INICI_AMB_TOKEN = Pattern.compile(
            "^([A-Z0-9\\-]{1,20})\\s+(.+)$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.JOSE_NOVAU;
    }

    @Override
    public OcrAlbaraPendent parsejar(String textOcrOriginal, String textOcrNormalitzat) {
        String textJoseNovau = obtenirTextJoseNovau(textOcrOriginal);

        OcrAlbaraPendent resultat = new OcrAlbaraPendent();
        resultat.setProveidorDetectat(getProveidor().getNomVisible());
        resultat.setProveidorCifDetectat(getProveidor().getCifHabitual());
        resultat.setNumeroAlbara(extreureNumeroAlbara(textJoseNovau));
        resultat.setDataAlbara(extreureDataAlbara(textJoseNovau));
        resultat.setLinies(extreureLinies(textJoseNovau));

        afegirAvisosGenerals(resultat);

        return resultat;
    }

    /**
     * Recupera la lectura OCR específica de JOSE NOVAU si existeixen marcadors.
     */
    private String obtenirTextJoseNovau(String text) {
        String bloc = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[JOSE_NOVAU_TEXT]]",
                "[[FI_JOSE_NOVAU]]"
        );

        return bloc == null || bloc.isBlank() ? text : bloc;
    }

    /**
     * Extreu el número de la línia DOCUMENT / NÚMERO / DATA.
     *
     * Tesseract ha llegit "Albarà" com "Ambara", per això no es valida
     * literalment el nom del camp.
     */
    private String extreureNumeroAlbara(String text) {
        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(text)) {
            String linia = OcrUtils.normalitzarPerComparar(liniaOriginal);

            if (!linia.contains("AMBARA") && !linia.contains("ALBARA") && !linia.contains("ALBARAN")) {
                continue;
            }

            Matcher matcher = PATRON_NUMERO_ALBARA.matcher(linia);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    /**
     * Extreu la primera data vàlida del document.
     */
    private String extreureDataAlbara(String text) {
        return OcrUtils.extreurePrimeraDataNormalitzada(text);
    }

    /**
     * Interpreta només les files que hi ha entre la capçalera ARTICLE /
     * DESCRIPCIÓ i el final de la taula.
     */
    private List<OcrLiniaDto> extreureLinies(String text) {
        List<OcrLiniaDto> linies = new ArrayList<>();
        boolean dinsTaula = false;

        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(text)) {
            String comparar = OcrUtils.normalitzarPerComparar(liniaOriginal);

            if (comparar.contains("ARTICLE") && comparar.contains("DESCRIPCIO")) {
                dinsTaula = true;
                continue;
            }

            if (!dinsTaula) {
                continue;
            }

            if (esFinalTaula(comparar)) {
                break;
            }

            OcrLiniaDto linia = parsejarLiniaProducte(liniaOriginal);

            if (linia != null) {
                linies.add(linia);
            }
        }

        return linies;
    }

    /**
     * Extreu article, matèria i primera quantitat de cadascuna de les files.
     */
    private OcrLiniaDto parsejarLiniaProducte(String liniaOriginal) {
        String linia = normalitzarLinia(liniaOriginal);
        Matcher quantitatDecimal = PATRON_QUANTITAT_DECIMAL.matcher(linia);

        if (quantitatDecimal.find()) {
            String partEsquerra = linia.substring(0, quantitatDecimal.start()).trim();
            Double quantitat = OcrUtils.convertirNumero(quantitatDecimal.group(1));

            return crearLiniaDesDePartEsquerra(partEsquerra, quantitat);
        }

        /*
         * La quarta línia es llegeix com "ENSAIMADA ... 100".
         * En aquest document correspon a 1,00 unitats.
         */
        Matcher quantitatSenseSeparador = PATRON_QUANTITAT_SENSE_SEPARADOR.matcher(linia);

        if (quantitatSenseSeparador.find()) {
            String partEsquerra = linia.substring(0, quantitatSenseSeparador.start()).trim();
            Double quantitat = convertirQuantitatSenseSeparador(quantitatSenseSeparador.group(1));

            return crearLiniaDesDePartEsquerra(partEsquerra, quantitat);
        }

        return null;
    }

    /**
     * Separa el codi ARTICLE de la descripció.
     *
     * Quan Tesseract retorna un codi curt clarament erroni, com "a2", no es
     * desa com a lot i es mostra "-" perquè l'usuari el corregeixi.
     */
    private OcrLiniaDto crearLiniaDesDePartEsquerra(String partEsquerra, Double quantitat) {
        String valor = partEsquerra
                .replaceAll("^[|_>\\s]+", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (valor.isBlank()) {
            return null;
        }

        String identificador = "-";
        String materia = valor;

        Matcher matcher = PATRON_INICI_AMB_TOKEN.matcher(valor);

        if (matcher.find()) {
            String candidatArticle = matcher.group(1);
            String possibleMateria = matcher.group(2);

            if (esArticleValid(candidatArticle)) {
                identificador = normalitzarArticle(candidatArticle);
                materia = possibleMateria;
            } else if (esSorollArticleInicial(candidatArticle)) {
                materia = possibleMateria;
            }
        }

        materia = netejarMateria(materia);

        if (materia == null || materia.isBlank() || !semblaMateria(materia)) {
            return null;
        }

        OcrLiniaDto linia = new OcrLiniaDto();
        linia.setIdentificadorLot(identificador);
        linia.setMateriaPrimeraDetectada(materia);
        linia.setQuantitat(quantitat);
        linia.setUnitat(detectarUnitat(materia));

        if ("-".equals(identificador)) {
            linia.afegirAvis("No s'ha pogut detectar correctament el valor de la columna ARTICLE.");
        }

        if (quantitat == null || quantitat <= 0) {
            linia.afegirAvis("No s'ha pogut detectar correctament la quantitat.");
        }

        return linia;
    }

    private boolean esArticleValid(String article) {
        String valor = article == null ? "" : article.toUpperCase();

        return valor.matches("\\d{4,}")
                || valor.matches("M\\d{4,}")
                || valor.matches("[A-Z]+-[A-Z0-9\\-]{3,}");
    }

    private boolean esSorollArticleInicial(String article) {
        String valor = article == null ? "" : article.toUpperCase();

        return valor.length() <= 3 || valor.matches("[A-Z]\\d{1,3}");
    }

    private String normalitzarArticle(String article) {
        return article == null ? "-" : article
                .toUpperCase()
                .replaceAll("[^A-Z0-9\\-]", "")
                .trim();
    }

    /**
     * Neteja una línia OCR abans d'interpretar article, descripció i quantitat.
     *
     * @param linia text original detectat per Tesseract.
     * @return línia normalitzada.
     */
    private String normalitzarLinia(String linia) {
        if (linia == null) {
            return "";
        }

        return linia
                .replace('|', ' ')
                .replace('’', '\'')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String netejarMateria(String materia) {
        if (materia == null || materia.isBlank()) {
            return null;
        }

        String neta = materia
                .replace('|', ' ')
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*[,;:]+$", "")
                .trim();

        return neta.length() < 3 ? null : neta;
    }

    private boolean semblaMateria(String materia) {
        String valor = OcrUtils.normalitzarPerComparar(materia);

        return valor.matches(".*[A-Z]{3,}.*")
                && !OcrUtils.conteAlguna(
                        valor,
                        "FINANCAMENT",
                        "PAGAMENT",
                        "TOTAL",
                        "SUBTOTAL",
                        "IMPORT",
                        "DESCOMPTE",
                        "OBSERVACIONES",
                        "BASE",
                        "IVA"
                );
    }

    private boolean esFinalTaula(String linia) {
        return OcrUtils.conteAlguna(
                linia,
                "FINANCAMENT",
                "PAGAMENT INMEDIAT",
                "OBSERVACIONES",
                "BASE IMPOSABLE",
                "TOTAL EUR"
        );
    }

    private Double convertirQuantitatSenseSeparador(String valor) {
        if (valor == null || !valor.matches("\\d{3}")) {
            return null;
        }

        String decimal = valor.substring(0, valor.length() - 2)
                + "."
                + valor.substring(valor.length() - 2);

        try {
            return Double.valueOf(decimal);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String detectarUnitat(String materia) {
        String valor = OcrUtils.normalitzarPerComparar(materia);

        if (valor.contains("KG")) {
            return "KG";
        }

        return "UT";
    }

    private void afegirAvisosGenerals(OcrAlbaraPendent resultat) {
        if (resultat.getNumeroAlbara() == null || resultat.getNumeroAlbara().isBlank()) {
            resultat.afegirAvis("No s'ha pogut detectar el número d'albarà de JOSE NOVAU.");
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis("No s'ha pogut detectar la data de l'albarà de JOSE NOVAU.");
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis("No s'ha pogut detectar cap línia de producte de JOSE NOVAU.");
        }
    }
}