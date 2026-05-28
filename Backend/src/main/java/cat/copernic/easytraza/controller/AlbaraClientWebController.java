package cat.copernic.easytraza.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

/**
 * CONTROLADOR WEB D'ALBARANS DE CLIENT.
 *
 * Gestionades les pantalles de consulta, creació, edició, eliminació i lliurament
 * dels albarans de client de la interfície web.
 *
 * @author Ángel Jurado Herruzo
 */
@Controller
@RequestMapping("/albarans-client")
public class AlbaraClientWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbaraClientWebController.class);

    // ---------------------------- SERVICES I CONSTRUCTOR ----------------------------
    private final AlbaraClientService albaraClientService;
    private final ClientService clientService;
    private final ProducteService producteService;
    private final UsuariService usuariService;
    private final MessageSource messageSource;

    public AlbaraClientWebController(AlbaraClientService albaraClientService,
                                     ClientService clientService,
                                     ProducteService producteService,
                                     UsuariService usuariService,
                                     MessageSource messageSource) {
        this.albaraClientService = albaraClientService;
        this.clientService = clientService;
        this.producteService = producteService;
        this.usuariService = usuariService;
        this.messageSource = messageSource;
    }


    /**
     * LLISTAT D'ALBARANS DE CLIENT.
     *
     * Preparada la vista amb els albarans de client filtrats i ordenats
     * segons els criteris indicats.
     *
     * @param clientId identificador del client utilitzat com a filtre
     * @param numeroAlbara número d'albarà utilitzat com a filtre
     * @param ordre camp utilitzat per ordenar el llistat
     * @param direccio sentit de l'ordenació aplicada
     * @param model model de dades de la vista
     * @return vista del llistat d'albarans de client
     */
    @GetMapping("/list")
    public String llistarAlbaransClient(@RequestParam(required = false) Long clientId,
                                        @RequestParam(required = false) String numeroAlbara,
                                        @RequestParam(required = false, defaultValue = "dataAlbara") String ordre,
                                        @RequestParam(required = false, defaultValue = "desc") String direccio,
                                        Model model) {

        prepararModelLlistat(model, clientId, numeroAlbara, ordre, direccio);

        return "albaransClient/llistarAlbaransClient";
    }


    /**
     * DETALL D'UN ALBARÀ DE CLIENT.
     *
     * Carregades les dades d'un albarà de client per mostrar-ne el detall
     * o redirigida la petició quan no existeix.
     *
     * @param id identificador de l'albarà de client
     * @param model model de dades de la vista
     * @return vista de detall o redirecció al llistat
     */
    @GetMapping("/detail/{id}")
    public String detallAlbaraClient(@PathVariable Long id, Model model) {
        AlbaraClient albaraClient = albaraClientService.getAlbaraClientDetallById(id);

        if (albaraClient == null) {
            return "redirect:/albarans-client/list";
        }

        model.addAttribute("albaraClient", albaraClient);

        return "albaransClient/detallAlbaraClient";
    }


    /**
     * FORMULARI DE CREACIÓ D'ALBARÀ DE CLIENT.
     *
     * Preparat el formulari d'un nou albarà de client amb la data actual
     * i una línia de producció inicial.
     *
     * @param model model de dades de la vista
     * @return vista del formulari d'albarans de client
     */
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


    /**
     * GUARDAT D'UN ALBARÀ DE CLIENT.
     *
     * Processat el formulari de creació d'un albarà de client i mostrats
     * els missatges corresponents segons el resultat de l'operació.
     *
     * @param albaraClient dades de l'albarà introduïdes al formulari
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
    @PostMapping("/save")
    public String guardarAlbaraClient(@ModelAttribute("albaraClient") AlbaraClient albaraClient,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            albaraClientService.createAlbaraClient(albaraClient);
            LOGGER.info("Albarà de client creat correctament.");

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("albaraClient.missatge.creat")
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


    /**
     * FORMULARI D'EDICIÓ D'ALBARÀ DE CLIENT.
     *
     * Carregat l'albarà seleccionat per editar-lo sempre que existeixi
     * i encara es pugui modificar.
     *
     * @param id identificador de l'albarà de client
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return vista del formulari o redirecció al llistat
     */
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
                missatge("albaraClient.error.lliuratNoModificable")
            );

            return "redirect:/albarans-client/list";
        }

        model.addAttribute("albaraClient", albaraClient);
        prepararModelFormulari(model);

        return "albaransClient/formAlbaraClient";
    }


    /**
     * ACTUALITZACIÓ D'UN ALBARÀ DE CLIENT.
     *
     * Processats els canvis introduïts en un albarà de client i gestionats
     * els missatges derivats del resultat de l'operació.
     *
     * @param id identificador de l'albarà de client
     * @param albaraClient dades actualitzades de l'albarà
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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
                missatge("albaraClient.missatge.actualitzat")
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


    /**
     * ELIMINACIÓ D'UN ALBARÀ DE CLIENT.
     *
     * Sol·licitada l'eliminació de l'albarà seleccionat i informat l'usuari
     * del resultat de l'operació.
     *
     * @param id identificador de l'albarà de client
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat d'albarans de client
     */
    @PostMapping("/delete/{id}")
    public String deleteAlbaraClient(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            albaraClientService.deleteAlbaraClient(id);
            LOGGER.info("Albarà de client amb identificador {} eliminat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("albaraClient.missatge.eliminat")
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar l'albarà de client amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/albarans-client/list";
    }


    /**
     * LLIURAMENT D'UN ALBARÀ DE CLIENT.
     *
     * Marcat com a lliurat l'albarà seleccionat i informat l'usuari
     * del resultat de l'operació.
     *
     * @param id identificador de l'albarà de client
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat d'albarans de client
     */
    @PostMapping("/lliurar/{id}")
    public String lliurarAlbaraClient(@PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        try {
            albaraClientService.lliurarAlbaraClient(id);
            LOGGER.info("Albarà de client amb identificador {} marcat com a lliurat.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("albaraClient.missatge.lliurat")
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut marcar com a lliurat l'albarà de client amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/albarans-client/list";
    }


    /**
     * PREPARACIÓ DEL LLISTAT.
     *
     * Afegides al model les dades i filtres necessaris per mostrar
     * el llistat d'albarans de client.
     *
     * @param model model de dades de la vista
     * @param clientId identificador del client utilitzat com a filtre
     * @param numeroAlbara número d'albarà utilitzat com a filtre
     * @param ordre camp utilitzat per ordenar el llistat
     * @param direccio sentit de l'ordenació aplicada
     */
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


    /**
     * PREPARACIÓ DEL FORMULARI.
     *
     * Afegides al model les dades necessàries per crear o editar
     * un albarà de client.
     *
     * @param model model de dades de la vista
     */
    private void prepararModelFormulari(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("productes", producteService.getAllProductes());
        model.addAttribute("operaris", getOperaris());
    }


    /**
     * OBTENCIÓ D'OPERARIS.
     *
     * Recuperats els usuaris amb rol d'operari disponibles per assignar-los
     * a les línies de producció.
     *
     * @return llista d'usuaris operaris disponibles
     */
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
