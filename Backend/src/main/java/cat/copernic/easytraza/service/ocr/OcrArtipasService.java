package cat.copernic.easytraza.service.ocr;

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import org.springframework.stereotype.Service;

/**
 * Parser OCR específic per al proveïdor ARTIPAS.
 */
@Service
public class OcrArtipasService implements OcrParserProveidor {

    @Override
    public OcrProveidorDetectat getProveidor() {
        return OcrProveidorDetectat.ARTIPAS;
    }

    @Override
    public OcrAlbaraPendent parsejar(String textOcrOriginal, String textOcrNormalitzat) {
        OcrAlbaraPendent resultat = new OcrAlbaraPendent();
        resultat.setProveidorDetectat(getProveidor().getNomVisible());
        resultat.setProveidorCifDetectat(getProveidor().getCifHabitual());
        resultat.setDataAlbara(OcrUtils.extreurePrimeraDataNormalitzada(textOcrNormalitzat));

        if (resultat.getDataAlbara() == null) {
            resultat.afegirAvis("No s'ha pogut detectar la data de l'albarà.");
        }

        return resultat;
    }
}
