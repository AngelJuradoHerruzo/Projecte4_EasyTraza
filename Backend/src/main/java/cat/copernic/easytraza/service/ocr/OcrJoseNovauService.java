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
 * PARSER OCR DE JOSÉ NOVAU.
 *
 * Interpretades les dades OCR dels albarans del proveïdor José Novau.
 * També normalitzades les línies de producte per obtenir lots, quantitats i matèries primeres.
 *
 * @author Ángel Jurado Herruz
 */
@Service
public class OcrJoseNovauService implements OcrParserProveidor {

    private final MessageSource messageSource;

    public OcrJoseNovauService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

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
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return text obtingut pel mètode
     */
    private String extreureDataAlbara(String text) {
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
     * INTERPRETACIÓ DEL TEXT OCR.
     *
     * Interpretat el text OCR rebut per construir les dades temporals
     * de l'albarà detectat.
     *
     * @param liniaOriginal valor de liniaOriginal utilitzat pel mètode
     * @return resultat obtingut pel mètode
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
     * CREACIÓ DE DADES.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param partEsquerra valor de partEsquerra utilitzat pel mètode
     * @param quantitat valor de quantitat utilitzat pel mètode
     * @return resultat obtingut pel mètode
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
            linia.afegirAvis(missatge("ocr.avis.articleNoDetectat"));
        }

        if (quantitat == null || quantitat <= 0) {
            linia.afegirAvis(missatge("ocr.avis.quantitatNoDetectada"));
        }

        return linia;
    }


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param article valor de article utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean esArticleValid(String article) {
        String valor = article == null ? "" : article.toUpperCase();

        return valor.matches("\\d{4,}")
                || valor.matches("M\\d{4,}")
                || valor.matches("[A-Z]+-[A-Z0-9\\-]{3,}");
    }


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param article valor de article utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean esSorollArticleInicial(String article) {
        String valor = article == null ? "" : article.toUpperCase();

        return valor.length() <= 3 || valor.matches("[A-Z]\\d{1,3}");
    }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param article valor de article utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private String normalitzarArticle(String article) {
        return article == null ? "-" : article
                .toUpperCase()
                .replaceAll("[^A-Z0-9\\-]", "")
                .trim();
    }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param linia valor de linia utilitzat pel mètode
     * @return text obtingut pel mètode
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


    /**
     * NETEJA DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param materia valor de materia utilitzat pel mètode
     * @return text obtingut pel mètode
     */
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


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param materia valor de materia utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
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


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param linia valor de linia utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
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


    /**
     * CONVERSIÓ DE DADES.
     *
     * Convertit el valor rebut al format necessari per poder-lo utilitzar
     * dins del procés del servei.
     *
     * @param valor valor que s'ha de processar
     * @return valor numèric obtingut
     */
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
        String valor = OcrUtils.normalitzarPerComparar(materia);

        if (valor.contains("KG")) {
            return "KG";
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
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "JOSE NOVAU"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "JOSE NOVAU"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "JOSE NOVAU"));
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