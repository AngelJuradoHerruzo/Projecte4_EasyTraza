package cat.copernic.easytraza.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
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

/**
 * CONTROLADOR WEB DEL PERFIL D'USUARI
 *
 * Gestiona la visualització i modificació del perfil de l'usuari autenticat.
 *
 * @author Ángel Jurado
 */
@Controller
@RequestMapping("/perfil")
public class PerfilWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfilWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;

    public PerfilWebController(UsuariService usuariService) {
        this.usuariService = usuariService;
    }


    // VISUALITZAR PERFIL
    @GetMapping
    public String veurePerfil(Model model, HttpSession session) {

        Usuari usuari = obtenirUsuariSessio(session);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);

        return "perfil/veurePerfil";
    }


    // MOSTRAR AVATAR DE L'USUARI AUTENTICAT
    @GetMapping("/avatar")
    @ResponseBody
    public ResponseEntity<byte[]> mostrarAvatarPerfil(HttpSession session) {

        Usuari usuari = obtenirUsuariSessio(session);

        if (usuari == null || usuari.getAvatar() == null || usuari.getAvatar().length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType tipusContingut;

        try {
            tipusContingut = MediaType.parseMediaType(usuari.getAvatarTipusContingut());
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut interpretar el tipus de contingut de l'avatar del perfil: {}", e.getMessage());
            tipusContingut = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(tipusContingut)
                .body(usuari.getAvatar());
    }


    // FORMULARI D'EDICIÓ DEL PERFIL
    @GetMapping("/edit")
    public String editarPerfil(Model model, HttpSession session) {

        Usuari usuari = obtenirUsuariSessio(session);

        if (usuari == null) {
            return "redirect:/login";
        }

        usuari.setPassword(null);

        model.addAttribute("usuari", usuari);

        return "perfil/formPerfil";
    }


    // ACTUALITZAR PERFIL
    @PostMapping("/update")
    public String updatePerfil(@ModelAttribute("usuari") Usuari usuari,
                               @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        Long usuariId = (Long) session.getAttribute("usuariId");

        if (usuariId == null) {
            return "redirect:/login";
        }

        try {
            Usuari usuariActualitzat = usuariService.updatePerfilUsuari(usuariId, usuari, avatarFile);
            LOGGER.info("Perfil de l'usuari amb identificador {} actualitzat correctament.", usuariId);

            session.setAttribute("usuariNom", usuariActualitzat.getNomComplet());
            session.setAttribute("usuariEmail", usuariActualitzat.getEmail());
            session.setAttribute("usuariRol", usuariActualitzat.getRolUsuari());

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El perfil s'ha actualitzat correctament."
            );

            return "redirect:/perfil";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar el perfil de l'usuari amb identificador {}: {}", usuariId, e.getMessage());
            Usuari usuariActual = usuariService.getUsuariById(usuariId);

            usuari.setId(usuariId);

            if (usuariActual != null) {
                usuari.setDni(usuariActual.getDni());
                usuari.setRolUsuari(usuariActual.getRolUsuari());
                usuari.setAvatar(usuariActual.getAvatar());
                usuari.setAvatarTipusContingut(usuariActual.getAvatarTipusContingut());
                usuari.setAvatarNomFitxer(usuariActual.getAvatarNomFitxer());
            }

            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuari", usuari);

            return "perfil/formPerfil";
        }
    }


    // OBTENIR USUARI AUTENTICAT DE LA SESSIÓ
    private Usuari obtenirUsuariSessio(HttpSession session) {

        Long usuariId = (Long) session.getAttribute("usuariId");

        if (usuariId == null) {
            return null;
        }

        Usuari usuari = usuariService.getUsuariById(usuariId);

        if (usuari == null) {
            session.invalidate();
        }

        return usuari;
    }
}
