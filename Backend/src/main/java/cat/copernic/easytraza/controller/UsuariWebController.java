package cat.copernic.easytraza.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import jakarta.servlet.http.HttpSession;

/**
 * CONTROLADOR WEB D'USUARIS.
 *
 * Gestionades les pantalles de consulta, creació, edició i eliminació
 * dels usuaris de l'aplicació web, incloent-hi els seus avatars.
 *
 * @author Ángel Jurado Herruzo
 */
@Controller
@RequestMapping("/usuaris")
public class UsuariWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsuariWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;
    private final MessageSource messageSource;

    public UsuariWebController(UsuariService usuariService, MessageSource messageSource) {
        this.usuariService = usuariService;
        this.messageSource = messageSource;
    }


    /**
     * LLISTAT D'USUARIS.
     *
     * Preparada la vista amb els usuaris filtrats i ordenats segons
     * els criteris indicats, incorporant l'usuari autenticat a la sessió.
     *
     * @param dni DNI de l'usuari utilitzat com a filtre
     * @param nomComplet nom de l'usuari utilitzat com a filtre
     * @param email correu electrònic utilitzat com a filtre
     * @param ordre camp utilitzat per ordenar el llistat
     * @param direccio sentit de l'ordenació aplicada
     * @param model model de dades de la vista
     * @param session sessió web de l'usuari
     * @return vista del llistat d'usuaris
     */
    @GetMapping("/list")
    public String llistarUsuaris(@RequestParam(required = false) String dni,
                                 @RequestParam(required = false) String nomComplet,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false, defaultValue = "nomComplet") String ordre,
                                 @RequestParam(required = false, defaultValue = "asc") String direccio,
                                 Model model,
                                 HttpSession session) {

        Long usuariId = (Long) session.getAttribute("usuariId");

        model.addAttribute(
            "usuaris",
            usuariService.getUsuarisLlistat(dni, nomComplet, email, ordre, direccio, usuariId)
        );

        model.addAttribute("dni", dni);
        model.addAttribute("nomComplet", nomComplet);
        model.addAttribute("email", email);
        model.addAttribute("ordre", ordre);
        model.addAttribute("direccio", direccio);

        return "usuaris/llistarUsuaris";
    }


    /**
     * OBTENCIÓ DE L'AVATAR D'UN USUARI.
     *
     * Retornada la imatge de l'avatar de l'usuari seleccionat quan existeix
     * i està disponible.
     *
     * @param id identificador de l'usuari
     * @return resposta amb la imatge de l'avatar o resposta de recurs no trobat
     */
    @GetMapping("/avatar/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> mostrarAvatar(@PathVariable Long id) {

        Usuari usuari = usuariService.getUsuariById(id);

        if (usuari == null || usuari.getAvatar() == null || usuari.getAvatar().length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType tipusContingut;

        try {
            tipusContingut = MediaType.parseMediaType(usuari.getAvatarTipusContingut());
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut interpretar el tipus de contingut de l'avatar de l'usuari amb identificador {}: {}", id, e.getMessage());
            tipusContingut = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(tipusContingut)
                .body(usuari.getAvatar());
    }


    /**
     * FORMULARI DE CREACIÓ D'USUARI.
     *
     * Preparat el formulari necessari per donar d'alta un usuari nou.
     *
     * @param model model de dades de la vista
     * @return vista del formulari d'usuaris
     */
    @GetMapping("/new")
    public String formCrearUsuari(Model model) {
        model.addAttribute("usuari", new Usuari());
        return "usuaris/formUsuaris";
    }


    /**
     * GUARDAT D'UN USUARI.
     *
     * Processat el formulari de creació d'un usuari juntament amb el seu
     * avatar i mostrats els missatges corresponents al resultat de l'operació.
     *
     * @param usuari dades de l'usuari introduïdes al formulari
     * @param avatarFile imatge d'avatar adjuntada al formulari
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
    @PostMapping("/save")
    public String guardarUsuari(@ModelAttribute("usuari") Usuari usuari,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            usuariService.createUsuari(usuari, avatarFile);
            LOGGER.info("Usuari creat correctament.");

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("usuaris.missatge.creat")
            );

            return "redirect:/usuaris/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear l'usuari: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuari", usuari);
            return "usuaris/formUsuaris";
        }
    }


    /**
     * FORMULARI D'EDICIÓ D'USUARI.
     *
     * Carregat l'usuari seleccionat per mostrar-ne les dades
     * al formulari d'edició.
     *
     * @param id identificador de l'usuari
     * @param model model de dades de la vista
     * @return vista del formulari o redirecció al llistat
     */
    @GetMapping("/edit/{id}")
    public String formEditarUsuari(@PathVariable Long id, Model model) {

        Usuari usuari = usuariService.getUsuariById(id);

        if (usuari == null) {
            return "redirect:/usuaris/list";
        }

        model.addAttribute("usuari", usuari);
        return "usuaris/formUsuaris";
    }


    /**
     * ACTUALITZACIÓ D'UN USUARI.
     *
     * Processats els canvis de l'usuari seleccionat i del seu avatar,
     * gestionant els missatges derivats del resultat de l'operació.
     *
     * @param id identificador de l'usuari
     * @param usuari dades actualitzades de l'usuari
     * @param avatarFile imatge d'avatar adjuntada al formulari
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
    @PostMapping("/update/{id}")
    public String updateUsuari(@PathVariable Long id,
                               @ModelAttribute("usuari") Usuari usuari,
                               @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            usuariService.updateUsuari(id, usuari, avatarFile);
            LOGGER.info("Usuari amb identificador {} actualitzat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("usuaris.missatge.actualitzat")
            );

            return "redirect:/usuaris/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar l'usuari amb identificador {}: {}", id, e.getMessage());
            Usuari usuariActual = usuariService.getUsuariById(id);

            usuari.setId(id);

            if (usuariActual != null) {
                usuari.setDni(usuariActual.getDni());
                usuari.setAvatar(usuariActual.getAvatar());
                usuari.setAvatarTipusContingut(usuariActual.getAvatarTipusContingut());
                usuari.setAvatarNomFitxer(usuariActual.getAvatarNomFitxer());
            }

            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuari", usuari);

            return "usuaris/formUsuaris";
        }
    }


    /**
     * ELIMINACIÓ D'UN USUARI.
     *
     * Sol·licitada l'eliminació de l'usuari seleccionat i informat l'usuari
     * del resultat de l'operació.
     *
     * @param id identificador de l'usuari
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat d'usuaris
     */
    @GetMapping("/delete/{id}")
    public String deleteUsuari(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            usuariService.deleteUsuari(id);
            LOGGER.info("Usuari amb identificador {} eliminat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("usuaris.missatge.eliminat")
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar l'usuari amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(
                "error",
                missatge("usuaris.error.eliminarRelacionat")
            );
        }

        return "redirect:/usuaris/list";
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
