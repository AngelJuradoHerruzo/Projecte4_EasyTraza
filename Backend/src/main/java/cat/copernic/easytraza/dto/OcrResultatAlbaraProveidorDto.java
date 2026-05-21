package cat.copernic.easytraza.dto;

import cat.copernic.easytraza.service.ocr.OcrProveidorDetectat;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de resultat de l'anàlisi OCR d'un albarà de proveïdor.
 *
 * Conté el document temporal i les dades detectades. No implica cap guardat
 * d'albarans, lots, proveïdors o matèries primeres.
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
    public void afegirAvis(String avis) {
        if (avis != null && !avis.isBlank()) {
            this.avisos.add(avis);
        }
    }

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
