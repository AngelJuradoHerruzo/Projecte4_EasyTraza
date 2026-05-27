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
 * Parser OCR específic per al proveïdor ARTIPAS.
 *
 * Aquest parser treballa amb una zona d'informació general i amb la taula
 * principal d'articles. En aquest format, el Codi HS s'utilitza com a
 * identificador de lot. Si la línia no incorpora Codi HS, es retorna "-" per
 * tal que l'usuari el completi abans de guardar.
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
     * Extreu el número d'albarà de la zona superior, tolerant la lectura OCR
     * WH/0UT en lloc de WH/OUT.
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
     * Extreu la data d'enviament, que és la data operativa de l'albarà.
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
     * Extreu les línies de la taula principal.
     *
     * La primera línia es tracta en una zona separada perquè al document ARTIPAS
     * queda molt pròxima a la capçalera i Tesseract pot ometre-la quan interpreta
     * tota la taula conjuntament.
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
     * Interpreta totes les línies vàlides d'un bloc OCR i evita duplicats.
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
     * Evita duplicar la primera fila si també apareix en la lectura general.
     */
    private boolean jaExisteixLinia(List<OcrLiniaDto> linies, OcrLiniaDto candidata) {
        return linies.stream().anyMatch(linia ->
                igual(linia.getIdentificadorLot(), candidata.getIdentificadorLot())
                && igual(linia.getMateriaPrimeraDetectada(), candidata.getMateriaPrimeraDetectada())
                && igual(linia.getQuantitat(), candidata.getQuantitat())
        );
    }

    private boolean igual(Object primer, Object segon) {
        return primer == null ? segon == null : primer.equals(segon);
    }

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
     * Comprova si la línia conté l'inici real d'un article ARTIPAS.
     *
     * Permet conservar la primera línia quan Tesseract la fusiona amb la
     * capçalera de la taula.
     */
    private boolean conteCodiArticle(String linia) {
        if (linia == null || linia.isBlank()) {
            return false;
        }

        return Pattern.compile("\\[[A-Z0-9]{2,14}\\]", Pattern.CASE_INSENSITIVE)
                .matcher(linia)
                .find();
    }

    private String prepararLinia(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace('’', '\'')
                .replaceAll("\\s+", " ")
                .trim();
    }

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
     * En ARTIPAS les quantitats incorporen decimals amb coma o punt. No es pot
     * utilitzar una normalització que elimini el punt decimal.
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

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
