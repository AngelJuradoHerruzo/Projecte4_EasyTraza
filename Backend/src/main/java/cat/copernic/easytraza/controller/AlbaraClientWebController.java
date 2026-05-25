package cat.copernic.easytraza.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.AlbaraClient;
import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.enums.EstatAlbaraClient;
import cat.copernic.easytraza.enums.RolUsuari;
import cat.copernic.easytraza.service.AlbaraClientService;
import cat.copernic.easytraza.service.ClientService;
import cat.copernic.easytraza.service.ProducteService;
import cat.copernic.easytraza.service.UsuariService;

@Controller
@RequestMapping("/albarans-client")
public class AlbaraClientWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbaraClientWebController.class);

    // ---------------------------- SERVICES I CONSTRUCTOR ----------------------------
    private final AlbaraClientService albaraClientService;
    private final ClientService clientService;
    private final ProducteService producteService;
    private final UsuariService usuariService;

    public AlbaraClientWebController(AlbaraClientService albaraClientService,
                                     ClientService clientService,
                                     ProducteService producteService,
                                     UsuariService usuariService) {
        this.albaraClientService = albaraClientService;
        this.clientService = clientService;
        this.producteService = producteService;
        this.usuariService = usuariService;
    }


    // LLISTAR ALBARANS DE CLIENT
    @GetMapping("/list")
    public String llistarAlbaransClient(@RequestParam(required = false) Long clientId,
                                        @RequestParam(required = false) String numeroAlbara,
                                        @RequestParam(required = false, defaultValue = "dataAlbara") String ordre,
                                        @RequestParam(required = false, defaultValue = "desc") String direccio,
                                        Model model) {

        prepararModelLlistat(model, clientId, numeroAlbara, ordre, direccio);

        return "albaransClient/llistarAlbaransClient";
    }


    // DETALL ALBARÀ DE CLIENT
    @GetMapping("/detail/{id}")
    public String detallAlbaraClient(@PathVariable Long id, Model model) {
        AlbaraClient albaraClient = albaraClientService.getAlbaraClientDetallById(id);

        if (albaraClient == null) {
            return "redirect:/albarans-client/list";
        }

        model.addAttribute("albaraClient", albaraClient);

        return "albaransClient/detallAlbaraClient";
    }


    // FORMULARI CREAR ALBARÀ DE CLIENT
    @GetMapping("/new")
    public String formCrearAlbaraClient(Model model) {
        AlbaraClient albaraClient = new AlbaraClient();

        albaraClient.setDataAlbara(LocalDateTime.now().withSecond(0).withNano(0));

        List<LiniaProduccio> liniesProduccio = new ArrayList<>();
        liniesProduccio.add(new LiniaProduccio());

        albaraClient.setLiniesProduccio(liniesProduccio);

        model.addAttribute("albaraClient", albaraClient);
        prepararModelFormulari(model);

        return "albaransClient/formAlbaraClient";
    }


    // GUARDAR ALBARÀ DE CLIENT
    @PostMapping("/save")
    public String guardarAlbaraClient(@ModelAttribute("albaraClient") AlbaraClient albaraClient,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            albaraClientService.createAlbaraClient(albaraClient);
            LOGGER.info("Albarà de client creat correctament.");

            redirectAttributes.addFlashAttribute(
                "missatge",
                "L'albarà de client s'ha creat correctament."
            );

            return "redirect:/albarans-client/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear l'albarà de client: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraClient", albaraClient);
            prepararModelFormulari(model);

            return "albaransClient/formAlbaraClient";
        }
    }


    // FORMULARI EDITAR ALBARÀ DE CLIENT
    @GetMapping("/edit/{id}")
    public String formEditarAlbaraClient(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        AlbaraClient albaraClient = albaraClientService.getAlbaraClientById(id);

        if (albaraClient == null) {
            return "redirect:/albarans-client/list";
        }

        if (albaraClient.getEstat() == EstatAlbaraClient.LLIURAT) {
            redirectAttributes.addFlashAttribute(
                "error",
                "No es pot modificar un albarà de client lliurat."
            );

            return "redirect:/albarans-client/list";
        }

        model.addAttribute("albaraClient", albaraClient);
        prepararModelFormulari(model);

        return "albaransClient/formAlbaraClient";
    }


    // ACTUALITZAR ALBARÀ DE CLIENT
    @PostMapping("/update/{id}")
    public String updateAlbaraClient(@PathVariable Long id,
                                     @ModelAttribute("albaraClient") AlbaraClient albaraClient,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            albaraClientService.updateAlbaraClient(id, albaraClient);
            LOGGER.info("Albarà de client amb identificador {} actualitzat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                "L'albarà de client s'ha actualitzat correctament."
            );

            return "redirect:/albarans-client/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar l'albarà de client amb identificador {}: {}", id, e.getMessage());
            albaraClient.setId(id);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraClient", albaraClient);
            prepararModelFormulari(model);

            return "albaransClient/formAlbaraClient";
        }
    }


    // ELIMINAR ALBARÀ DE CLIENT
    @PostMapping("/delete/{id}")
    public String deleteAlbaraClient(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            albaraClientService.deleteAlbaraClient(id);
            LOGGER.info("Albarà de client amb identificador {} eliminat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                "L'albarà de client s'ha eliminat correctament."
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar l'albarà de client amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/albarans-client/list";
    }


    // LLIURAR ALBARÀ DE CLIENT
    @PostMapping("/lliurar/{id}")
    public String lliurarAlbaraClient(@PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        try {
            albaraClientService.lliurarAlbaraClient(id);
            LOGGER.info("Albarà de client amb identificador {} marcat com a lliurat.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                "L'albarà de client s'ha marcat com a lliurat."
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut marcar com a lliurat l'albarà de client amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/albarans-client/list";
    }


    // PREPARAR DADES DEL LLISTAT
    private void prepararModelLlistat(Model model,
                                       Long clientId,
                                       String numeroAlbara,
                                       String ordre,
                                       String direccio) {

        model.addAttribute(
            "albarans",
            albaraClientService.getAlbaransClientLlistat(clientId, numeroAlbara, ordre, direccio)
        );

        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("clientId", clientId);
        model.addAttribute("numeroAlbara", numeroAlbara);
        model.addAttribute("ordre", ordre);
        model.addAttribute("direccio", direccio);
    }


    // PREPARAR DADES DEL FORMULARI
    private void prepararModelFormulari(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("productes", producteService.getAllProductes());
        model.addAttribute("operaris", getOperaris());
    }


    // OBTENIR OPERARIS DISPONIBLES PER A LES LÍNIES DE PRODUCCIÓ
    private List<Usuari> getOperaris() {
        return usuariService.getAllUsuaris()
                .stream()
                .filter(usuari -> usuari.getRolUsuari() == RolUsuari.OPERARI)
                .sorted(Comparator.comparing(
                    usuari -> usuari.getNomComplet() != null ? usuari.getNomComplet() : "",
                    String.CASE_INSENSITIVE_ORDER
                ))
                .collect(Collectors.toList());
    }
}
