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
 * PARSER OCR D'ARTIPAS.
 *
 * Interpretades les dades OCR dels albarans del proveïdor Artipas.
 * També extretes les línies de lots, quantitats i matèries primeres detectades.
 *
 * @author Ángel Jurado Herruz
 */
@Service
public class OcrArtipasService implements OcrParserProveidor {

    private final MessageSource messageSource;

    public OcrArtipasService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private static final Pattern PATRON_NUMERO_ALBARA = Pattern.compile(
            "\\bWH\\s*[/\\-]?\\s*[O0]UT\\s*[/\\-]?\\s*(\\d{3,12})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_LINIA_AMB_CLAUDATORS = Pattern.compile(
        "\\[([A-Z0-9]{2,14})\\]\\s+(.+?)\\s+"
        + "(\\d+[\\.,]\\d{2})\\s+"
        + "(\\d+[\\.,]\\d{2})"
        + "(?:\\s+(\\d{6,10}))?",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATRON_LINIA_SENSE_CLAUDATORS = Pattern.compile(
        "(?:^|\\s)([A-Z0-9]{2,14})\\s+(.+?)\\s+"
        + "(\\d+[\\.,]\\d{2})\\s+"
        + "(\\d+[\\.,]\\d{2})"
        + "(?:\\s+(\\d{6,10}))?",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.ARTIPAS;
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
                "[[ARTIPAS_INFO]]",
                "[[ARTIPAS_TAULA]]"
        );

        String origen = blocInfo == null || blocInfo.isBlank() ? text : blocInfo;
        String normalitzat = OcrUtils.normalitzarPerComparar(origen);
        Matcher matcher = PATRON_NUMERO_ALBARA.matcher(normalitzat);

        if (matcher.find()) {
            return "WH/OUT/" + matcher.group(1);
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
                "[[ARTIPAS_INFO]]",
                "[[ARTIPAS_TAULA]]"
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
        String blocPrimeraLinia = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[ARTIPAS_PRIMERA_LINIA]]",
                "[[ARTIPAS_TAULA]]"
        );

        String blocTaula = OcrUtils.extreureBlocEntreMarcadors(
                text,
                "[[ARTIPAS_TAULA]]",
                "[[FI_ARTIPAS]]"
        );

        List<OcrLiniaDto> linies = new ArrayList<>();

        afegirLiniesDelBloc(blocPrimeraLinia, linies);
        afegirLiniesDelBloc(blocTaula, linies);

        return linies;
    }


    /**
     * INCORPORACIÓ DE DADES.
     *
     * Incorporada o completada la informació necessària dins de l'objecte
     * que s'està preparant.
     *
     * @param bloc valor de bloc utilitzat pel mètode
     * @param linies valor de linies utilitzat pel mètode
     */
    private void afegirLiniesDelBloc(String bloc, List<OcrLiniaDto> linies) {
        for (String liniaOriginal : OcrUtils.obtenirLiniesNoBuides(bloc)) {
            String linia = prepararLinia(liniaOriginal);

            /*
            * La primera fila pot arribar unida parcialment a la capçalera.
            * Si encara conté un codi d'article entre claudàtors, s'intenta
            * interpretar igualment.
            */
            if (esCapcaleraOSoroll(linia) && !conteCodiArticle(linia)) {
                continue;
            }

            OcrLiniaDto liniaDto = parsejarLinia(linia);

            if (liniaDto != null && !jaExisteixLinia(linies, liniaDto)) {
                linies.add(liniaDto);
            }
        }
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param linies valor de linies utilitzat pel mètode
     * @param candidata valor de candidata utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean jaExisteixLinia(List<OcrLiniaDto> linies, OcrLiniaDto candidata) {
        return linies.stream().anyMatch(linia ->
                igual(linia.getIdentificadorLot(), candidata.getIdentificadorLot())
                && igual(linia.getMateriaPrimeraDetectada(), candidata.getMateriaPrimeraDetectada())
                && igual(linia.getQuantitat(), candidata.getQuantitat())
        );
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param primer valor de primer utilitzat pel mètode
     * @param segon valor de segon utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean igual(Object primer, Object segon) {
        return primer == null ? segon == null : primer.equals(segon);
    }


    /**
     * INTERPRETACIÓ DEL TEXT OCR.
     *
     * Interpretat el text OCR rebut per construir les dades temporals
     * de l'albarà detectat.
     *
     * @param linia valor de linia utilitzat pel mètode
     * @return resultat obtingut pel mètode
     */
    private OcrLiniaDto parsejarLinia(String linia) {
        Matcher matcher = PATRON_LINIA_AMB_CLAUDATORS.matcher(linia);

        /*
        * Es prioritza el codi entre claudàtors perquè evita interpretar
        * paraules de la capçalera com si fossin el codi del producte.
        */
        if (!matcher.find()) {
            matcher = PATRON_LINIA_SENSE_CLAUDATORS.matcher(linia);

            if (!matcher.find()) {
                return null;
            }
        }

        String materia = netejarMateria(matcher.group(2));
        Double quantitatEntregada = convertirDecimal(matcher.group(4));
        String codiHs = matcher.group(5);

        OcrLiniaDto resultat = new OcrLiniaDto();
        resultat.setMateriaPrimeraDetectada(materia);
        resultat.setQuantitat(quantitatEntregada);
        resultat.setUnitat(detectarUnitat(materia));

        if (codiHs == null || codiHs.isBlank()) {
            resultat.setIdentificadorLot("-");
            resultat.afegirAvis(missatge("ocr.avis.codiHsNoDetectat"));
        } else {
            resultat.setIdentificadorLot(OcrUtils.normalitzarLot(codiHs));
        }

        if (materia == null || materia.isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.materiaNoDetectada"));
        }

        if (quantitatEntregada == null || quantitatEntregada <= 0) {
            resultat.afegirAvis(missatge("ocr.avis.quantitatEntregadaNoDetectada"));
        }

        return resultat;
    }


    /**
     * COMPROVACIÓ DE CONTINGUT.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param linia valor de linia utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean conteCodiArticle(String linia) {
        if (linia == null || linia.isBlank()) {
            return false;
        }

        return Pattern.compile("\\[[A-Z0-9]{2,14}\\]", Pattern.CASE_INSENSITIVE)
                .matcher(linia)
                .find();
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
    private String prepararLinia(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace('’', '\'')
                .replaceAll("\\s+", " ")
                .trim();
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
    private boolean esCapcaleraOSoroll(String linia) {
        String normalitzada = OcrUtils.normalitzarPerComparar(linia);

        return normalitzada.isBlank()
                || normalitzada.contains("PRODUCTO")
                || normalitzada.contains("PEDIDO")
                || normalitzada.contains("ENTREGADO")
                || normalitzada.contains("CODIGO HS")
                || normalitzada.contains("CANTIDADES RESTANTES")
                || normalitzada.contains("NO ENTREGADAS");
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

        String materia = valor
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*[|I]$", "")
                .trim();

        return materia.length() < 3 ? null : materia;
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
    private Double convertirDecimal(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return Double.valueOf(valor.trim().replace(',', '.'));
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

        if (normalitzada.contains("MILLAR")) {
            return "MILLAR";
        }

        if (normalitzada.contains("KG")) {
            return "KG";
        }

        return "UNITATS";
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
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "ARTIPAS"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "ARTIPAS"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "ARTIPAS"));
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
