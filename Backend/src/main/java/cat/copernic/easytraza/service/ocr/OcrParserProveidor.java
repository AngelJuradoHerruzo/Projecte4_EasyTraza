package cat.copernic.easytraza.service.ocr;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;

/**
 * Contracte comú per als parsers OCR específics de cada proveïdor.
 */
public interface OcrParserProveidor {

    OcrProveidorDetectat getProveidor();

    OcrAlbaraPendent parsejar(String textOcrOriginal, String textOcrNormalitzat);

    default boolean suporta(OcrProveidorDetectat proveidorDetectat) {
        return getProveidor() == proveidorDetectat;
    }
}
