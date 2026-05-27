package cat.copernic.easytraza.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * LÍNIA TEMPORAL DETECTADA PER OCR.
 *
 * Representades les dades d'una línia detectada en un albarà de proveïdor
 * perquè puguin ser mostrades i revisades abans del guardat definitiu.
 * Aquest objecte no es desa a la base de dades.
 *
 * @author Ángel Jurado Herruz
 */
public class OcrLiniaDto {

    private String identificadorLot;
    private Double quantitat;
    private String unitat;

    private String materiaPrimeraDetectada;
    private Long materiaPrimeraId;
    private String materiaPrimeraNomAssociada;
    private boolean materiaPrimeraTrobada;

    private List<String> avisos = new ArrayList<>();


    /*********************       .CONSTRUCTORS.       *********************/
    public OcrLiniaDto() { }


    /*********************       .MÈTODES DE SUPORT.       *********************/
    /**
     * INCORPORACIÓ D'UN AVÍS.
     *
     * Afegit un avís de validació a la línia temporal quan conté informació
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
     * Comprovat si la línia temporal conté avisos pendents de revisió.
     *
     * @return cert si existeix algun avís a la línia temporal
     */
    public boolean teAvisos() {
        return avisos != null && !avisos.isEmpty();
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    public String getIdentificadorLot() { return identificadorLot; }
    public void setIdentificadorLot(String identificadorLot) { this.identificadorLot = identificadorLot; }

    public Double getQuantitat() { return quantitat; }
    public void setQuantitat(Double quantitat) { this.quantitat = quantitat; }

    public String getUnitat() { return unitat; }
    public void setUnitat(String unitat) { this.unitat = unitat; }

    public String getMateriaPrimeraDetectada() { return materiaPrimeraDetectada; }
    public void setMateriaPrimeraDetectada(String materiaPrimeraDetectada) { this.materiaPrimeraDetectada = materiaPrimeraDetectada; }

    public Long getMateriaPrimeraId() { return materiaPrimeraId; }
    public void setMateriaPrimeraId(Long materiaPrimeraId) { this.materiaPrimeraId = materiaPrimeraId; }

    public String getMateriaPrimeraNomAssociada() { return materiaPrimeraNomAssociada; }
    public void setMateriaPrimeraNomAssociada(String materiaPrimeraNomAssociada) { this.materiaPrimeraNomAssociada = materiaPrimeraNomAssociada; }

    public boolean isMateriaPrimeraTrobada() { return materiaPrimeraTrobada; }
    public void setMateriaPrimeraTrobada(boolean materiaPrimeraTrobada) { this.materiaPrimeraTrobada = materiaPrimeraTrobada; }

    public List<String> getAvisos() { return avisos; }
    public void setAvisos(List<String> avisos) { this.avisos = avisos != null ? avisos : new ArrayList<>(); }
}
