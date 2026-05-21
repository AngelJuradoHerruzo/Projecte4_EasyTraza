package cat.copernic.easytraza.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO temporal d'un albarà de proveïdor detectat per OCR.
 *
 * Aquest objecte no es desa a base de dades. Serveix per emplenar el formulari
 * web i perquè l'usuari validi o corregeixi les dades abans del guardat final.
 */
public class OcrAlbaraPendent {

    private String numeroAlbara;
    private String dataAlbara;

    private String proveidorDetectat;
    private String proveidorCifDetectat;
    private Long proveidorId;
    private String proveidorNomAssociat;
    private boolean proveidorTrobat;

    private List<OcrLiniaDto> linies = new ArrayList<>();
    private List<String> avisos = new ArrayList<>();


    /*********************       .CONSTRUCTORS.       *********************/
    public OcrAlbaraPendent() { }


    /*********************       .MÈTODES DE SUPORT.       *********************/
    public void afegirLinia(OcrLiniaDto linia) {
        if (linia != null) {
            this.linies.add(linia);
        }
    }

    public void afegirAvis(String avis) {
        if (avis != null && !avis.isBlank()) {
            this.avisos.add(avis);
        }
    }

    public boolean teAvisos() {
        if (avisos != null && !avisos.isEmpty()) {
            return true;
        }

        return linies != null && linies.stream().anyMatch(OcrLiniaDto::teAvisos);
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    public String getNumeroAlbara() {
        return numeroAlbara;
    }

    public void setNumeroAlbara(String numeroAlbara) {
        this.numeroAlbara = numeroAlbara;
    }

    public String getDataAlbara() {
        return dataAlbara;
    }

    public void setDataAlbara(String dataAlbara) {
        this.dataAlbara = dataAlbara;
    }

    public String getProveidorDetectat() {
        return proveidorDetectat;
    }

    public void setProveidorDetectat(String proveidorDetectat) {
        this.proveidorDetectat = proveidorDetectat;
    }

    public String getProveidorCifDetectat() {
        return proveidorCifDetectat;
    }

    public void setProveidorCifDetectat(String proveidorCifDetectat) {
        this.proveidorCifDetectat = proveidorCifDetectat;
    }

    public Long getProveidorId() {
        return proveidorId;
    }

    public void setProveidorId(Long proveidorId) {
        this.proveidorId = proveidorId;
    }

    public String getProveidorNomAssociat() {
        return proveidorNomAssociat;
    }

    public void setProveidorNomAssociat(String proveidorNomAssociat) {
        this.proveidorNomAssociat = proveidorNomAssociat;
    }

    public boolean isProveidorTrobat() {
        return proveidorTrobat;
    }

    public void setProveidorTrobat(boolean proveidorTrobat) {
        this.proveidorTrobat = proveidorTrobat;
    }

    public List<OcrLiniaDto> getLinies() {
        return linies;
    }

    public void setLinies(List<OcrLiniaDto> linies) {
        this.linies = linies != null ? linies : new ArrayList<>();
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public void setAvisos(List<String> avisos) {
        this.avisos = avisos != null ? avisos : new ArrayList<>();
    }
}
