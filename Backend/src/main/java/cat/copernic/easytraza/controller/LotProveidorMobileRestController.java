package cat.copernic.easytraza.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.service.LotProveidorService;

/**
 * CONTROLADOR REST DE LOTS DE PROVEÏDOR MÒBIL
 *
 * Gestiona el llistat, detall, inici i finalització de lots des de l'app mòbil.
 *
 * @author Ángel Jurado
 */
@RestController
@RequestMapping("/api/mobile/lots")
public class LotProveidorMobileRestController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final LotProveidorService lotProveidorService;

    public LotProveidorMobileRestController(LotProveidorService lotProveidorService) {
        this.lotProveidorService = lotProveidorService;
    }


    // LLISTAR LOTS DE PROVEÏDOR
    @GetMapping
    public ResponseEntity<List<LotMobileResponse>> llistarLots() {

        List<LotMobileResponse> lots = lotProveidorService.getAllLotsProveidor()
                .stream()
                .map(this::crearLotMobileResponse)
                .toList();

        return ResponseEntity.ok(lots);
    }


    // CONSULTAR DETALL DEL LOT
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarLot(@PathVariable Long id) {

        try {
            LotProveidor lotProveidor = lotProveidorService.getLotProveidorById(id);

            return ResponseEntity.ok(crearLotMobileResponse(lotProveidor));
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // INICIAR LOT
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<?> iniciarLot(@PathVariable Long id) {

        try {
            if (lotProveidorService.existeixLotObertMateixaMateria(id)) {
                LotProveidor lotProveidor = lotProveidorService.getLotProveidorById(id);

                ConfirmarIniciLotResponse response = new ConfirmarIniciLotResponse(
                        true,
                        "Ja hi ha un lot obert per aquesta matèria primera. Vols finalitzar-lo i iniciar aquest lot?",
                        crearLotMobileResponse(lotProveidor)
                );

                return ResponseEntity.ok(response);
            }

            LotProveidor lotProveidor = lotProveidorService.iniciarLot(id, false);

            ConfirmarIniciLotResponse response = new ConfirmarIniciLotResponse(
                    false,
                    "Lot iniciat correctament.",
                    crearLotMobileResponse(lotProveidor)
            );

            return ResponseEntity.ok(response);
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // CONFIRMAR INICI DE LOT
    @PostMapping("/{id}/iniciar-confirmat")
    public ResponseEntity<?> confirmarIniciLot(@PathVariable Long id) {

        try {
            LotProveidor lotProveidor = lotProveidorService.iniciarLot(id, true);

            ConfirmarIniciLotResponse response = new ConfirmarIniciLotResponse(
                    false,
                    "Lot iniciat correctament.",
                    crearLotMobileResponse(lotProveidor)
            );

            return ResponseEntity.ok(response);
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // FINALITZAR LOT
    @PostMapping("/{id}/finalitzar")
    public ResponseEntity<?> finalitzarLot(@PathVariable Long id) {

        try {
            LotProveidor lotProveidor = lotProveidorService.finalitzarLot(id);

            return ResponseEntity.ok(crearLotMobileResponse(lotProveidor));
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // CREAR DTO DE RESPOSTA DEL LOT
    private LotMobileResponse crearLotMobileResponse(LotProveidor lotProveidor) {

        return new LotMobileResponse(
                lotProveidor.getId(),
                lotProveidor.getIdentificadorLot(),
                lotProveidor.getQuantitat(),
                lotProveidor.getUnitats(),
                lotProveidor.getEstat() != null ? lotProveidor.getEstat().name() : "",
                lotProveidor.getDataCaducitat() != null ? lotProveidor.getDataCaducitat().toString() : "",
                lotProveidor.getDataObertura() != null ? lotProveidor.getDataObertura().toString() : "",
                lotProveidor.getDataAcabament() != null ? lotProveidor.getDataAcabament().toString() : "",
                lotProveidor.getMateriaPrimera() != null ? lotProveidor.getMateriaPrimera().getId() : null,
                lotProveidor.getMateriaPrimera() != null ? lotProveidor.getMateriaPrimera().getNomMateria() : "",
                lotProveidor.getAlbaraProveidor() != null ? lotProveidor.getAlbaraProveidor().getId() : null
        );
    }


    /**
     * DTO intern per retornar les dades d'un lot a l'app mòbil.
     */
    public static class LotMobileResponse {

        private Long id;
        private String identificadorLot;
        private Integer quantitat;
        private String unitats;
        private String estat;
        private String dataCaducitat;
        private String dataObertura;
        private String dataAcabament;
        private Long materiaPrimeraId;
        private String materiaPrimeraNom;
        private Long albaraProveidorId;

        public LotMobileResponse() {
        }

        public LotMobileResponse(Long id, String identificadorLot, Integer quantitat, String unitats,
                                 String estat, String dataCaducitat, String dataObertura,
                                 String dataAcabament, Long materiaPrimeraId, String materiaPrimeraNom,
                                 Long albaraProveidorId) {
            this.id = id;
            this.identificadorLot = identificadorLot;
            this.quantitat = quantitat;
            this.unitats = unitats;
            this.estat = estat;
            this.dataCaducitat = dataCaducitat;
            this.dataObertura = dataObertura;
            this.dataAcabament = dataAcabament;
            this.materiaPrimeraId = materiaPrimeraId;
            this.materiaPrimeraNom = materiaPrimeraNom;
            this.albaraProveidorId = albaraProveidorId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getIdentificadorLot() {
            return identificadorLot;
        }

        public void setIdentificadorLot(String identificadorLot) {
            this.identificadorLot = identificadorLot;
        }

        public Integer getQuantitat() {
            return quantitat;
        }

        public void setQuantitat(Integer quantitat) {
            this.quantitat = quantitat;
        }

        public String getUnitats() {
            return unitats;
        }

        public void setUnitats(String unitats) {
            this.unitats = unitats;
        }

        public String getEstat() {
            return estat;
        }

        public void setEstat(String estat) {
            this.estat = estat;
        }

        public String getDataCaducitat() {
            return dataCaducitat;
        }

        public void setDataCaducitat(String dataCaducitat) {
            this.dataCaducitat = dataCaducitat;
        }

        public String getDataObertura() {
            return dataObertura;
        }

        public void setDataObertura(String dataObertura) {
            this.dataObertura = dataObertura;
        }

        public String getDataAcabament() {
            return dataAcabament;
        }

        public void setDataAcabament(String dataAcabament) {
            this.dataAcabament = dataAcabament;
        }

        public Long getMateriaPrimeraId() {
            return materiaPrimeraId;
        }

        public void setMateriaPrimeraId(Long materiaPrimeraId) {
            this.materiaPrimeraId = materiaPrimeraId;
        }

        public String getMateriaPrimeraNom() {
            return materiaPrimeraNom;
        }

        public void setMateriaPrimeraNom(String materiaPrimeraNom) {
            this.materiaPrimeraNom = materiaPrimeraNom;
        }

        public Long getAlbaraProveidorId() {
            return albaraProveidorId;
        }

        public void setAlbaraProveidorId(Long albaraProveidorId) {
            this.albaraProveidorId = albaraProveidorId;
        }
    }


    /**
     * DTO intern per retornar si cal confirmar l'inici d'un lot.
     */
    public static class ConfirmarIniciLotResponse {

        private boolean requereixConfirmacio;
        private String missatge;
        private LotMobileResponse lot;

        public ConfirmarIniciLotResponse() {
        }

        public ConfirmarIniciLotResponse(boolean requereixConfirmacio, String missatge, LotMobileResponse lot) {
            this.requereixConfirmacio = requereixConfirmacio;
            this.missatge = missatge;
            this.lot = lot;
        }

        public boolean isRequereixConfirmacio() {
            return requereixConfirmacio;
        }

        public void setRequereixConfirmacio(boolean requereixConfirmacio) {
            this.requereixConfirmacio = requereixConfirmacio;
        }

        public String getMissatge() {
            return missatge;
        }

        public void setMissatge(String missatge) {
            this.missatge = missatge;
        }

        public LotMobileResponse getLot() {
            return lot;
        }

        public void setLot(LotMobileResponse lot) {
            this.lot = lot;
        }
    }
}