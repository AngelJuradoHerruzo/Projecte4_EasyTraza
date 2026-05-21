package cat.copernic.easytraza.service.ocr;

import java.util.Arrays;
import java.util.List;

/**
 * Proveïdors suportats pel flux OCR d'albarans de proveïdor.
 */
public enum OcrProveidorDetectat {

    LA_META("LA META", "A25004573", List.of("LA META", "META II", "HARINA PANIF")),
    ARTIPAS("ARTIPAS", "B61551172", List.of("ARTIPAS", "CAKEDECOR", "WH-OUT", "BOLSAS DE PAN")),
    PASTISSA("PASTISSA", "A08854847", List.of("PASTISSA", "N ALBARA", "CODI DESCRIPCIO LOT")),
    TAL_COM_PINTA("TAL COM PINTA", "B60859311", List.of("TAL COM PINTA", "ALBARA D'ENTREGA", "DILLUNS TANCAT")),
    AVICOLA_LLEONART("AVÍCOLA LLEONART", "A08560021", List.of("AVICOLA LLEONART", "AVÍCOLA LLEONART", "GRANJAS LLEONART", "LLEONART")),
    JOSE_NOVAU("JOSE NOVAU", "47183180Z", List.of("JOSE NOVAU", "NOVAU DIL", "NOVAU DIT", "47183180"));

    private final String nomVisible;
    private final String cifHabitual;
    private final List<String> clausDeteccio;

    OcrProveidorDetectat(String nomVisible, String cifHabitual, List<String> clausDeteccio) {
        this.nomVisible = nomVisible;
        this.cifHabitual = cifHabitual;
        this.clausDeteccio = clausDeteccio;
    }

    public String getNomVisible() {
        return nomVisible;
    }

    public String getCifHabitual() {
        return cifHabitual;
    }

    public List<String> getClausDeteccio() {
        return clausDeteccio;
    }

    public static OcrProveidorDetectat detectar(String textNormalitzat) {
        String text = textNormalitzat == null ? "" : textNormalitzat;

        return Arrays.stream(values())
                .filter(proveidor -> proveidor.clausDeteccio.stream().anyMatch(text::contains))
                .findFirst()
                .orElse(null);
    }
}
