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


    // LLISTAR USUARIS
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


    // MOSTRAR AVATAR D'UN USUARI
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


    // FORMULARI CREAR USUARI
    @GetMapping("/new")
    public String formCrearUsuari(Model model) {
        model.addAttribute("usuari", new Usuari());
        return "usuaris/formUsuaris";
    }


    // GUARDAR USUARI
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


    // FORMULARI EDITAR USUARI
    @GetMapping("/edit/{id}")
    public String formEditarUsuari(@PathVariable Long id, Model model) {

        Usuari usuari = usuariService.getUsuariById(id);

        if (usuari == null) {
            return "redirect:/usuaris/list";
        }

        model.addAttribute("usuari", usuari);
        return "usuaris/formUsuaris";
    }


    // ACTUALITZAR USUARI
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


    // ELIMINAR USUARI
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

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
