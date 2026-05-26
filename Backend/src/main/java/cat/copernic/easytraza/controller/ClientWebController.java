package cat.copernic.easytraza.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.Client;
import cat.copernic.easytraza.service.ClientService;

@Controller
@RequestMapping("/clients")
public class ClientWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final ClientService clientService;
    private final MessageSource messageSource;

    public ClientWebController(ClientService clientService, MessageSource messageSource) {
        this.clientService = clientService;
        this.messageSource = messageSource;
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
            LOGGER.info("Client creat correctament.");

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("clients.missatge.creat")
            );

            return "redirect:/clients/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear el client: {}", e.getMessage());
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
            LOGGER.info("Client amb identificador {} actualitzat correctament.", id);

            if (clientActualitzat == null) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    missatge("clients.error.noTrobatModificar")
                );

                return "redirect:/clients/list";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("clients.missatge.actualitzat")
            );

            return "redirect:/clients/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar el client amb identificador {}: {}", id, e.getMessage());
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
            LOGGER.info("Client amb identificador {} eliminat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("clients.missatge.eliminat")
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar el client amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/clients/list";
    }

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
