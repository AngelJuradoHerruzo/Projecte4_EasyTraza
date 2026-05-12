package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String llistarClients(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
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
    public String guardarClient(@ModelAttribute("client") Client client, Model model) {
        try {
            clientService.createClient(client);
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
                               Model model) {
        try {
            clientService.updateClient(id, client);
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
    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id, Model model) {
        try {
            clientService.deleteClient(id);
            return "redirect:/clients/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clients", clientService.getAllClients());
            return "clients/llistarClients";
        }
    }
}