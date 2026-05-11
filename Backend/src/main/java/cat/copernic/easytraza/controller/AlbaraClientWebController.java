package cat.copernic.easytraza.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.AlbaraClient;
import cat.copernic.easytraza.entities.LiniaProduccio;
import cat.copernic.easytraza.enums.EstatAlbaraClient;
import cat.copernic.easytraza.service.AlbaraClientService;
import cat.copernic.easytraza.service.ClientService;
import cat.copernic.easytraza.service.ProducteService;

@Controller
@RequestMapping("/albarans-client")
public class AlbaraClientWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final AlbaraClientService albaraClientService;
    private final ClientService clientService;
    private final ProducteService producteService;

    public AlbaraClientWebController(AlbaraClientService albaraClientService,
                                     ClientService clientService,
                                     ProducteService producteService) {
        this.albaraClientService = albaraClientService;
        this.clientService = clientService;
        this.producteService = producteService;
    }


    // LLISTAR ALBARANS DE CLIENT
    @GetMapping("/list")
    public String llistarAlbaransClient(Model model) {
        model.addAttribute("albarans", albaraClientService.getAllAlbaransClient());

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

        List<LiniaProduccio> liniesProduccio = new ArrayList<>();
        liniesProduccio.add(new LiniaProduccio());

        albaraClient.setLiniesProduccio(liniesProduccio);

        model.addAttribute("albaraClient", albaraClient);
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("productes", producteService.getAllProductes());

        return "albaransClient/formAlbaraClient";
    }


    // GUARDAR ALBARÀ DE CLIENT
    @PostMapping("/save")
    public String guardarAlbaraClient(@ModelAttribute("albaraClient") AlbaraClient albaraClient,
                                      Model model) {
        try {
            albaraClientService.createAlbaraClient(albaraClient);
            return "redirect:/albarans-client/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraClient", albaraClient);
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("productes", producteService.getAllProductes());

            return "albaransClient/formAlbaraClient";
        }
    }


    // FORMULARI EDITAR ALBARÀ DE CLIENT
    @GetMapping("/edit/{id}")
    public String formEditarAlbaraClient(@PathVariable Long id, Model model) {
        AlbaraClient albaraClient = albaraClientService.getAlbaraClientById(id);

        if (albaraClient == null) {
            return "redirect:/albarans-client/list";
        }

        try {
            if (albaraClient.getEstat() == EstatAlbaraClient.LLIURAT) {
                throw new RuntimeException("No es pot modificar un albarà de client lliurat");
            }

            model.addAttribute("albaraClient", albaraClient);
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("productes", producteService.getAllProductes());

            return "albaransClient/formAlbaraClient";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albarans", albaraClientService.getAllAlbaransClient());

            return "albaransClient/llistarAlbaransClient";
        }
    }


    // ACTUALITZAR ALBARÀ DE CLIENT
    @PostMapping("/update/{id}")
    public String updateAlbaraClient(@PathVariable Long id,
                                     @ModelAttribute("albaraClient") AlbaraClient albaraClient,
                                     Model model) {
        try {
            albaraClientService.updateAlbaraClient(id, albaraClient);
            return "redirect:/albarans-client/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraClient", albaraClient);
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("productes", producteService.getAllProductes());

            return "albaransClient/formAlbaraClient";
        }
    }


    // ELIMINAR ALBARÀ DE CLIENT
    @GetMapping("/delete/{id}")
    public String deleteAlbaraClient(@PathVariable Long id, Model model) {
        try {
            albaraClientService.deleteAlbaraClient(id);
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albarans", albaraClientService.getAllAlbaransClient());

            return "albaransClient/llistarAlbaransClient";
        }

        return "redirect:/albarans-client/list";
    }


    // LLIURAR ALBARÀ DE CLIENT
    @GetMapping("/lliurar/{id}")
    public String lliurarAlbaraClient(@PathVariable Long id, Model model) {
        try {
            albaraClientService.lliurarAlbaraClient(id);
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albarans", albaraClientService.getAllAlbaransClient());

            return "albaransClient/llistarAlbaransClient";
        }

        return "redirect:/albarans-client/list";
    }
}