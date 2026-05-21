package cat.copernic.easytraza.service.ocr;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitats comunes per al processament OCR d'albarans de proveïdor.
 */
public final class OcrUtils {

    private static final Pattern PATRON_DATA = Pattern.compile(
            "\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})\\b"
    );

    private OcrUtils() { }


    /*********************       .TEXT.       *********************/
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

    public static String netejarValor(String valor) {
        return valor == null ? null : valor
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .trim()
                .replaceAll("\\s{2,}", " ");
    }

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


    /*********************       .DATES.       *********************/
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


    /*********************       .NÚMEROS I QUANTITATS.       *********************/
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


    /*********************       .LOTS I CODIS.       *********************/
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

    public static boolean esLotValid(String lot) {
        String valor = normalitzarLot(lot);

        return valor != null
                && valor.length() >= 4
                && valor.matches(".*\\d.*")
                && !valor.matches("\\d{1,4}");
    }

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


    /*********************       .COMPARACIÓ FLEXIBLE.       *********************/
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
