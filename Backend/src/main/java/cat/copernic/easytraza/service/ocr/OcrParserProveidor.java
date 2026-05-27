package cat.copernic.easytraza.service.ocr;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;

/**
 * CONTRACTE DELS PARSERS OCR.
 *
 * Definides les operacions comunes que han d'implementar els parsers OCR específics de cada proveïdor.
 * Així el procés general pot obtenir el proveïdor suportat i interpretar el text reconegut.
 *
 * @author Ángel Jurado Herruz
 */
public interface OcrParserProveidor {

    OcrProveidorDetectat getProveidor();

    OcrAlbaraPendent parsejar(String textOcrOriginal, String textOcrNormalitzat);


    /**
     * COMPROVACIÓ DEL PROVEÏDOR.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param proveidorDetectat valor de proveidorDetectat utilitzat pel mètode
     * @return cert si es compleix la condició indicada
     */
    default boolean suporta(OcrProveidorDetectat proveidorDetectat) {
        return getProveidor() == proveidorDetectat;
    }
}
