package cat.copernic.easytraza.service.ocr;

import java.util.List;

/**
 * Proveïdors suportats pel flux OCR d'albarans de proveïdor.
 */
public enum OcrProveidorDetectat {

    LA_META(
            "LA META",
            "A25004573",
            List.of("LA META", "META II", "HARINA PANIF")
    ),

    ARTIPAS(
            "ARTIPAS",
            "B61551172",
            List.of("ARTIPAS", "CAKEDECOR", "WH/OUT", "WH-OUT", "BOLSAS DE PAN")
    ),

    PASTISSA(
            "PASTISSA",
            "A08854847",
            List.of(
                    "PASTISSA",
                    "PASTISA",
                    "PASTISART",
                    "PRODUCTES AUXILIARS"
            )
    ),

    TAL_COM_PINTA(
            "TAL COM PINTA",
            "B60859311",
            List.of("TAL COM PINTA", "ALBARA D ENTREGA", "DILLUNS TANCAT")
    ),

    AVICOLA_LLEONART(
            "AVÍCOLA LLEONART",
            "A08560021",
            List.of("AVICOLA LLEONART", "GRANJAS LLEONART", "LLEONART")
    ),

    JOSE_NOVAU(
            "JOSE NOVAU DIL",
            "47183180Z",
            List.of(
                    "JOSE NOVAU",
                    "NOVAU DIL",
                    "NOVAU DIT",
                    "JOSENOVAUDIL",
                    "JOSENOVAUDIT",
                    "WWW JOSENOVAUDIL",
                    "WWW JOSENOVAUDIT",
                    "47183180"
            )
    );

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

    /**
     * Detecta el proveïdor a partir del text OCR normalitzat.
     *
     * JOSE NOVAU es comprova abans que PASTISSA perquè comparteixen camps
     * genèrics d'albarà i pagament, però JOSE NOVAU disposa de marques
     * pròpies prou identificatives.
     *
     * @param textNormalitzat text OCR normalitzat per comparar.
     * @return proveïdor detectat o null si no s'ha pogut identificar.
     */
    public static OcrProveidorDetectat detectar(String textNormalitzat) {
        String text = textNormalitzat == null ? "" : textNormalitzat;

        if (conteAlgunaClau(JOSE_NOVAU, text)) {
            return JOSE_NOVAU;
        }

        if (conteAlgunaClau(LA_META, text)) {
            return LA_META;
        }

        if (conteAlgunaClau(ARTIPAS, text)) {
            return ARTIPAS;
        }

        if (conteAlgunaClau(AVICOLA_LLEONART, text)) {
            return AVICOLA_LLEONART;
        }

        if (conteAlgunaClau(TAL_COM_PINTA, text)) {
            return TAL_COM_PINTA;
        }

        if (conteAlgunaClau(PASTISSA, text)) {
            return PASTISSA;
        }

        return null;
    }

    /**
     * Comprova si el text OCR conté alguna de les claus del proveïdor.
     *
     * @param proveidor proveïdor a comprovar.
     * @param text text OCR normalitzat.
     * @return true si existeix alguna coincidència.
     */
    private static boolean conteAlgunaClau(OcrProveidorDetectat proveidor, String text) {
        return proveidor.clausDeteccio.stream().anyMatch(text::contains);
    }
}