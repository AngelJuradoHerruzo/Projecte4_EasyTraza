package cat.copernic.easytraza.service.ocr;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UTILITATS DEL PROCÉS OCR.
 *
 * Agrupades funcions auxiliars utilitzades pels parsers OCR per normalitzar textos, 
 * detectar dates, convertir números i comparar valors. Aquesta classe no manté estat propi.
 *
 * @author Ángel Jurado Herruz
 */
public final class OcrUtils {

    private static final Pattern PATRON_DATA = Pattern.compile(
            "\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})\\b"
    );

    private OcrUtils() { }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    public static String normalitzarText(String valor) {
        return valor == null ? "" : valor
                .replace("\r", "\n")
                .replace("\t", " ")
                .replace("º", "o")
                .replace("ª", "a")
                .replaceAll("[ ]{2,}", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
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
    public static String normalitzarPerComparar(String valor) {
        if (valor == null) {
            return "";
        }

        String text = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);

        return text
                .replace("L0T", "LOT")
                .replace("10T", "LOT")
                .replace("IOT", "LOT")
                .replace(" K6", " KG")
                .replace(" K0", " KG")
                .replace(" KO", " KG")
                .replace(" RG", " KG")
                .replaceAll("\\s+", " ")
                .trim();
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
    public static String netejarValor(String valor) {
        return valor == null ? null : valor
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .trim()
                .replaceAll("\\s{2,}", " ");
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param text text utilitzat en el procés
     * @return llista de resultats obtinguda
     */
    public static List<String> obtenirLiniesNoBuides(String text) {
        List<String> linies = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return linies;
        }

        for (String part : text.split("\\R")) {
            String linia = netejarValor(part);

            if (linia != null && !linia.isBlank()) {
                linies.add(linia);
            }
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
     * @param marcadorInici valor de marcadorInici utilitzat pel mètode
     * @param marcadorFi valor de marcadorFi utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    public static String extreureBlocEntreMarcadors(String text, String marcadorInici, String marcadorFi) {
        if (text == null || marcadorInici == null || marcadorFi == null) {
            return "";
        }

        int inici = text.indexOf(marcadorInici);

        if (inici < 0) {
            return "";
        }

        inici += marcadorInici.length();

        int fi = text.indexOf(marcadorFi, inici);

        if (fi < 0) {
            return text.substring(inici).trim();
        }

        return text.substring(inici, fi).trim();
    }


    /**
     * COMPROVACIÓ DE CONTINGUT.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param text text utilitzat en el procés
     * @param valors valor que s'ha de processar
     * @return cert si es compleix la condició indicada
     */
    public static boolean conteAlguna(String text, String... valors) {
        if (text == null || valors == null) {
            return false;
        }

        for (String valor : valors) {
            if (valor != null && text.contains(valor)) {
                return true;
            }
        }

        return false;
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
    public static String extreurePrimeraDataNormalitzada(String text) {
        Matcher matcher = PATRON_DATA.matcher(text == null ? "" : text);

        while (matcher.find()) {
            String data = normalitzarData(matcher.group(1));

            if (data != null) {
                return data;
            }
        }

        return null;
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
    public static String normalitzarData(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String data = valor.trim().replace("-", "/");
        String[] parts = data.split("/");

        if (parts.length != 3) {
            return null;
        }

        try {
            if (parts[0].length() == 4) {
                int any = Integer.parseInt(parts[0]);
                int mes = Integer.parseInt(parts[1]);
                int dia = Integer.parseInt(parts[2]);
                return validarIFormatarData(any, mes, dia);
            }

            int dia = Integer.parseInt(parts[0]);
            int mes = Integer.parseInt(parts[1]);
            int any = Integer.parseInt(parts[2]);

            if (any < 100) {
                any += 2000;
            }

            return validarIFormatarData(any, mes, dia);

        } catch (NumberFormatException ex) {
            return null;
        }
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param any valor de any utilitzat pel mètode
     * @param mes valor de mes utilitzat pel mètode
     * @param dia valor de dia utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    private static String validarIFormatarData(int any, int mes, int dia) {
        if (any < 2020 || any > 2100) {
            return null;
        }

        try {
            return LocalDate.of(any, mes, dia).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (RuntimeException ex) {
            return null;
        }
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
    public static Double convertirNumero(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String net = valor.trim()
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".");

        try {
            return Double.parseDouble(net);
        } catch (NumberFormatException ex) {
            return null;
        }
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
    public static Double convertirQuantitatOcr(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String net = valor.trim();

        if (!net.contains(",") && !net.contains(".") && net.matches("\\d{3,4}")) {
            return convertirNumero(net.substring(0, net.length() - 2) + "." + net.substring(net.length() - 2));
        }

        return convertirNumero(net);
    }


    /**
     * NORMALITZACIÓ DE DADES.
     *
     * Preparat el valor rebut perquè pugui ser comparat, mostrat
     * o processat de manera coherent pel servei.
     *
     * @param lot valor de lot utilitzat pel mètode
     * @return text obtingut pel mètode
     */
    public static String normalitzarLot(String lot) {
        if (lot == null || lot.isBlank()) {
            return null;
        }

        return lot.toUpperCase(Locale.ROOT)
                .replace(" ", "-")
                .replace(".", "-")
                .replace("/", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^[^A-Z0-9]+", "")
                .replaceAll("[^A-Z0-9]+$", "")
                .trim();
    }


    /**
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param lot valor de lot utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    public static boolean esLotValid(String lot) {
        String valor = normalitzarLot(lot);

        return valor != null
                && valor.length() >= 4
                && valor.matches(".*\\d.*")
                && !valor.matches("\\d{1,4}");
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
    public static String normalitzarCodi(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String codi = valor.toUpperCase(Locale.ROOT)
                .replace("/", "-")
                .replace(" ", "-")
                .replaceAll("[^A-Z0-9\\-]", "")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        return codi.isBlank() ? null : codi;
    }


    /**
     * COMPROVACIÓ DE COINCIDÈNCIA.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param nomDetectat valor de nomDetectat utilitzat pel mètode
     * @param nomSistema valor de nomSistema utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    public static boolean coincideixNomFlexible(String nomDetectat, String nomSistema) {
        String detectat = normalitzarPerComparar(nomDetectat);
        String sistema = normalitzarPerComparar(nomSistema);

        if (detectat.isBlank() || sistema.isBlank()) {
            return false;
        }

        if (detectat.contains(sistema) || sistema.contains(detectat)) {
            return true;
        }

        int distancia = distanciaLevenshtein(detectat, sistema);
        int llargada = Math.max(detectat.length(), sistema.length());

        return llargada > 0 && ((double) distancia / llargada) <= 0.25;
    }


    /**
     * GESTIÓ DE DADES.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param primer valor de primer utilitzat pel mètode
     * @param segon valor de segon utilitzat pel mètode
     * @return valor numèric obtingut
     */
    public static int distanciaLevenshtein(String primer, String segon) {
        String a = primer == null ? "" : primer;
        String b = segon == null ? "" : segon;

        int[][] distancia = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            distancia[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            distancia[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                distancia[i][j] = Math.min(
                        Math.min(distancia[i - 1][j] + 1, distancia[i][j - 1] + 1),
                        distancia[i - 1][j - 1] + cost
                );
            }
        }

        return distancia[a.length()][b.length()];
    }
}
