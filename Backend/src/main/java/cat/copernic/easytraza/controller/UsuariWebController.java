package cat.copernic.easytraza.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;

    public UsuariWebController(UsuariService usuariService) {
        this.usuariService = usuariService;
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

            redirectAttributes.addFlashAttribute(
                "missatge",
                "L'usuari s'ha creat correctament."
            );

            return "redirect:/usuaris/list";
        }
        catch (RuntimeException e) {
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

            redirectAttributes.addFlashAttribute(
                "missatge",
                "L'usuari s'ha actualitzat correctament."
            );

            return "redirect:/usuaris/list";
        }
        catch (RuntimeException e) {
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

            redirectAttributes.addFlashAttribute(
                "missatge",
                "L'usuari s'ha eliminat correctament."
            );
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                "error",
                "No es pot eliminar l'usuari perquè està relacionat amb altres dades."
            );
        }

        return "redirect:/usuaris/list";
    }
}
