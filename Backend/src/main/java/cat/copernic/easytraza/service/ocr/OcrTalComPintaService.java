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
 * PARSER OCR DE TAL COM PINTA.
 *
 * Interpretades les dades OCR dels albarans del proveïdor Tal Com Pinta.
 * També normalitzats els lots i les línies detectades abans de presentar-les a revisió.
 *
 * @author Ángel Jurado Herruzo
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


    /**
     * CERCA DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
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
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param bloc valor de bloc utilitzat pel mètode
     * @return llista de resultats obtinguda
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
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
     * EXTRACCIÓ DE DADES.
     *
     * Extreta la dada necessària del text o del document analitzat
     * per continuar amb el procés OCR.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
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

        return valor
                .replace('|', ' ')
                .replaceAll("^\\s*[A-Z]?\\d{4,10}\\s+", "")
                .replaceAll("\\s+\\d+[\\.,]\\d{1,2}.*$", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*[,;:]+$", "")
                .trim();
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
        if (materia == null || materia.isBlank()) {
            return false;
        }

        String comparar = OcrUtils.normalitzarPerComparar(materia);

        return comparar.matches(".*[A-Z]{3,}.*")
                && !esSoroll(comparar)
                && !comparar.contains("DATA ENTREGA")
                && !comparar.contains("ADRECA");
    }


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param valor valor que s'ha de processar
     * @return cert si es compleix la condició indicada
     */
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
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String normalitzarLot(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String lot = valor.toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        // OCR confon habitualment el prefix L6 amb 16.
        if (lot.matches("1\\d{6}")) {
            lot = "L" + lot.substring(1);
        }

        //Dins d'un lot alfanumèric amb prefix L, la lletra O correspon a zero.
        if (lot.startsWith("L")) {
            lot = "L" + lot.substring(1).replace('O', '0');
        }

        return lot;
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param lot valor de lot utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    private boolean lotValido(String lot) {
        return lot != null && lot.length() >= 4 && lot.matches(".*\\d.*");
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

        if (normalitzada.contains("LT") || normalitzada.contains("LITRE")) {
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
     * @param linia valor de linia utilitzat pel mètode
     */
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
            resultat.afegirAvis(missatge("ocr.avis.numeroAlbaraNoDetectat", "TAL COM PINTA"));
        }

        if (resultat.getDataAlbara() == null || resultat.getDataAlbara().isBlank()) {
            resultat.afegirAvis(missatge("ocr.avis.dataAlbaraNoDetectada", "TAL COM PINTA"));
        }

        if (resultat.getLinies() == null || resultat.getLinies().isEmpty()) {
            resultat.afegirAvis(missatge("ocr.avis.capLiniaProducte", "TAL COM PINTA"));
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
