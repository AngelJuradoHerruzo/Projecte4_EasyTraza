package cat.copernic.easytraza.dto;

import cat.copernic.easytraza.service.ocr.OcrProveidorDetectat;
import java.util.ArrayList;
import java.util.List;

/**
 * RESULTAT DE L'ANÀLISI OCR.
 *
 * Agrupades les dades detectades durant l'anàlisi OCR d'un albarà de proveïdor,
 * incloent-hi el document temporal, els textos reconeguts i els avisos generats.
 * Aquest objecte no implica el guardat de cap dada de negoci.
 *
 * @author Ángel Jurado Herruzo
 */
public class OcrResultatAlbaraProveidorDto {

    private OcrProveidorDetectat proveidorDetectat;
    private OcrAlbaraPendent albaraPendent = new OcrAlbaraPendent();

    private String textOcrOriginal;
    private String textOcrNormalitzat;

    private String ocrDocumentTemporalId;
    private String ocrDocumentNomOriginal;
    private String ocrDocumentContentType;
    private String ocrDocumentUrlTemporal;

    private List<String> avisos = new ArrayList<>();


    /*********************       .CONSTRUCTORS.       *********************/
    public OcrResultatAlbaraProveidorDto() { }


    /*********************       .MÈTODES DE SUPORT.       *********************/
    /**
     * INCORPORACIÓ D'UN AVÍS.
     *
     * Afegit un avís al resultat OCR quan conté informació útil per mostrar
     * durant la revisió del document.
     *
     * @param avis missatge d'avís que s'ha d'incorporar
     */
    public void afegirAvis(String avis) {
        if (avis != null && !avis.isBlank()) {
            this.avisos.add(avis);
        }
    }

    
    /**
     * COMPROVACIÓ D'AVISOS.
     *
     * Comprovat si el resultat OCR o l'albarà temporal associat conté avisos
     * pendents de revisió.
     *
     * @return cert si existeix algun avís al resultat o a l'albarà temporal
     */
    public boolean teAvisos() {
        return avisos != null && !avisos.isEmpty()
                || albaraPendent != null && albaraPendent.teAvisos(); 
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    public OcrProveidorDetectat getProveidorDetectat() { return proveidorDetectat; }
    public void setProveidorDetectat(OcrProveidorDetectat proveidorDetectat) { this.proveidorDetectat = proveidorDetectat; }

    public OcrAlbaraPendent getAlbaraPendent() { return albaraPendent; }
    public void setAlbaraPendent(OcrAlbaraPendent albaraPendent) { this.albaraPendent = albaraPendent != null ? albaraPendent : new OcrAlbaraPendent(); }

    public String getTextOcrOriginal() { return textOcrOriginal; }
    public void setTextOcrOriginal(String textOcrOriginal) { this.textOcrOriginal = textOcrOriginal; }

    public String getTextOcrNormalitzat() { return textOcrNormalitzat; }
    public void setTextOcrNormalitzat(String textOcrNormalitzat) { this.textOcrNormalitzat = textOcrNormalitzat; }

    public String getOcrDocumentTemporalId() { return ocrDocumentTemporalId; }
    public void setOcrDocumentTemporalId(String ocrDocumentTemporalId) { this.ocrDocumentTemporalId = ocrDocumentTemporalId; }

    public String getOcrDocumentNomOriginal() { return ocrDocumentNomOriginal; }
    public void setOcrDocumentNomOriginal(String ocrDocumentNomOriginal) { this.ocrDocumentNomOriginal = ocrDocumentNomOriginal; }

    public String getOcrDocumentContentType() { return ocrDocumentContentType; }
    public void setOcrDocumentContentType(String ocrDocumentContentType) { this.ocrDocumentContentType = ocrDocumentContentType; }

    public String getOcrDocumentUrlTemporal() { return ocrDocumentUrlTemporal; }
    public void setOcrDocumentUrlTemporal(String ocrDocumentUrlTemporal) { this.ocrDocumentUrlTemporal = ocrDocumentUrlTemporal; }

    public List<String> getAvisos() { return avisos; }
    public void setAvisos(List<String> avisos) { this.avisos = avisos != null ? avisos : new ArrayList<>(); }
}
