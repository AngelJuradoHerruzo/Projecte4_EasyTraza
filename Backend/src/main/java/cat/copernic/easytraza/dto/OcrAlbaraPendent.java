package cat.copernic.easytraza.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * ALBARÀ TEMPORAL DETECTAT PER OCR.
 *
 * Representades les dades d'un albarà de proveïdor detectat mitjançant OCR
 * perquè puguin ser mostrades i revisades abans del guardat definitiu.
 * Aquest objecte no es desa a la base de dades.
 *
 * @author Ángel Jurado Herruzo
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
    /**
     * INCORPORACIÓ D'UNA LÍNIA OCR.
     *
     * Afegida una línia detectada a l'albarà temporal sempre que la dada
     * rebuda no sigui nul·la.
     *
     * @param linia línia OCR que s'ha d'incorporar a l'albarà
     */
    public void afegirLinia(OcrLiniaDto linia) {
        if (linia != null) {
            this.linies.add(linia);
        }
    }


    /**
     * INCORPORACIÓ D'UN AVÍS.
     *
     * Afegit un avís de validació a l'albarà temporal quan conté informació
     * útil per mostrar a l'usuari.
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
     * Comprovat si l'albarà o alguna de les seves línies conté avisos
     * pendents de revisió.
     *
     * @return cert si existeix algun avís a l'albarà o a les seves línies
     */
    public boolean teAvisos() {
        if (avisos != null && !avisos.isEmpty()) {
            return true;
        }

        return linies != null && linies.stream().anyMatch(OcrLiniaDto::teAvisos);
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    public String getNumeroAlbara() { return numeroAlbara; }
    public void setNumeroAlbara(String numeroAlbara) { this.numeroAlbara = numeroAlbara; }

    public String getDataAlbara() { return dataAlbara; }
    public void setDataAlbara(String dataAlbara) { this.dataAlbara = dataAlbara; }

    public String getProveidorDetectat() { return proveidorDetectat; }
    public void setProveidorDetectat(String proveidorDetectat) { this.proveidorDetectat = proveidorDetectat; }

    public String getProveidorCifDetectat() { return proveidorCifDetectat; }
    public void setProveidorCifDetectat(String proveidorCifDetectat) { this.proveidorCifDetectat = proveidorCifDetectat; }

    public Long getProveidorId() { return proveidorId; }
    public void setProveidorId(Long proveidorId) { this.proveidorId = proveidorId; }

    public String getProveidorNomAssociat() { return proveidorNomAssociat; }
    public void setProveidorNomAssociat(String proveidorNomAssociat) { this.proveidorNomAssociat = proveidorNomAssociat; }

    public boolean isProveidorTrobat() { return proveidorTrobat; }
    public void setProveidorTrobat(boolean proveidorTrobat) { this.proveidorTrobat = proveidorTrobat; }

    public List<OcrLiniaDto> getLinies() { return linies; }
    public void setLinies(List<OcrLiniaDto> linies) { this.linies = linies != null ? linies : new ArrayList<>(); }

    public List<String> getAvisos() { return avisos; }
    public void setAvisos(List<String> avisos) { this.avisos = avisos != null ? avisos : new ArrayList<>(); }
}
