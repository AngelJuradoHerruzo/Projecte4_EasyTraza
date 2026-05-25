package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.Client;
import cat.copernic.easytraza.service.ClientService;

@Controller
@RequestMapping("/clients")
public class ClientWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final ClientService clientService;

    public ClientWebController(ClientService clientService) {
        this.clientService = clientService;
    }


    // LLISTAR CLIENTS
    @GetMapping("/list")
    public String llistarClients(@RequestParam(required = false) String nomComplet,
                                 @RequestParam(required = false) String cif,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String telefon,
                                 @RequestParam(required = false, defaultValue = "nomComplet") String ordre,
                                 @RequestParam(required = false, defaultValue = "asc") String direccio,
                                 Model model) {

        model.addAttribute(
            "clients",
            clientService.getClientsLlistat(nomComplet, cif, email, telefon, ordre, direccio)
        );
        model.addAttribute("nomComplet", nomComplet);
        model.addAttribute("cif", cif);
        model.addAttribute("email", email);
        model.addAttribute("telefon", telefon);
        model.addAttribute("ordre", ordre);
        model.addAttribute("direccio", direccio);

        return "clients/llistarClients";
    }


    // FORMULARI CREAR CLIENT
    @GetMapping("/new")
    public String formCrearClient(Model model) {
        model.addAttribute("client", new Client());
        return "clients/formClients";
    }


    // GUARDAR CLIENT
    @PostMapping("/save")
    public String guardarClient(@ModelAttribute("client") Client client,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            clientService.createClient(client);

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El client s'ha creat correctament."
            );

            return "redirect:/clients/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("client", client);
            return "clients/formClients";
        }
    }


    // FORMULARI EDITAR CLIENT
    @GetMapping("/edit/{id}")
    public String formEditarClient(@PathVariable Long id, Model model) {
        Client client = clientService.getClientById(id);

        if (client == null) {
            return "redirect:/clients/list";
        }

        model.addAttribute("client", client);
        return "clients/formClients";
    }


    // ACTUALITZAR CLIENT
    @PostMapping("/update/{id}")
    public String updateClient(@PathVariable Long id,
                               @ModelAttribute("client") Client client,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Client clientActualitzat = clientService.updateClient(id, client);

            if (clientActualitzat == null) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client que vols modificar."
                );

                return "redirect:/clients/list";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El client s'ha actualitzat correctament."
            );

            return "redirect:/clients/list";
        }
        catch (RuntimeException e) {
            client.setId(id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("client", client);
            return "clients/formClients";
        }
    }


    // ELIMINAR CLIENT
    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClient(id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El client s'ha eliminat correctament."
            );
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/clients/list";
    }
}
