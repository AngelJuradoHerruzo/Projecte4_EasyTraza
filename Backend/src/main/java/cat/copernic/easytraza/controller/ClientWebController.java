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

/**
 * CONTROLADOR WEB DE CLIENTS.
 *
 * Gestionades les pantalles de consulta, creació, edició i eliminació
 * dels clients de l'aplicació web.
 *
 * @author Ángel Jurado Herruzo
 */
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


    /**
     * LLISTAT DE CLIENTS.
     *
     * Preparada la vista amb els clients filtrats i ordenats segons
     * els criteris indicats.
     *
     * @param nomComplet nom del client utilitzat com a filtre
     * @param cif CIF del client utilitzat com a filtre
     * @param email correu electrònic utilitzat com a filtre
     * @param telefon telèfon utilitzat com a filtre
     * @param ordre camp utilitzat per ordenar el llistat
     * @param direccio sentit de l'ordenació aplicada
     * @param model model de dades de la vista
     * @return vista del llistat de clients
     */
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


    /**
     * FORMULARI DE CREACIÓ DE CLIENT.
     *
     * Preparat el formulari necessari per donar d'alta un client nou.
     *
     * @param model model de dades de la vista
     * @return vista del formulari de clients
     */
    @GetMapping("/new")
    public String formCrearClient(Model model) {
        model.addAttribute("client", new Client());
        return "clients/formClients";
    }


    /**
     * GUARDAT D'UN CLIENT.
     *
     * Processat el formulari de creació d'un client i mostrats
     * els missatges corresponents segons el resultat de l'operació.
     *
     * @param client dades del client introduïdes al formulari
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * FORMULARI D'EDICIÓ DE CLIENT.
     *
     * Carregat el client seleccionat per mostrar-ne les dades al formulari
     * d'edició.
     *
     * @param id identificador del client
     * @param model model de dades de la vista
     * @return vista del formulari o redirecció al llistat
     */
    @GetMapping("/edit/{id}")
    public String formEditarClient(@PathVariable Long id, Model model) {
        Client client = clientService.getClientById(id);

        if (client == null) {
            return "redirect:/clients/list";
        }

        model.addAttribute("client", client);
        return "clients/formClients";
    }


    /**
     * ACTUALITZACIÓ D'UN CLIENT.
     *
     * Processats els canvis del client seleccionat i gestionats
     * els missatges derivats del resultat de l'operació.
     *
     * @param id identificador del client
     * @param client dades actualitzades del client
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * ELIMINACIÓ D'UN CLIENT.
     *
     * Sol·licitada l'eliminació del client seleccionat i informat l'usuari
     * del resultat de l'operació.
     *
     * @param id identificador del client
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat de clients
     */
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


    /**
     * OBTENCIÓ D'UN MISSATGE TRADUÏT.
     *
     * Recuperat el text corresponent al codi indicat segons l'idioma
     * actiu de la interfície web.
     *
     * @param codi codi del missatge que s'ha de recuperar
     * @param arguments valors incorporats al missatge
     * @return missatge traduït a l'idioma actiu
     */
    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
