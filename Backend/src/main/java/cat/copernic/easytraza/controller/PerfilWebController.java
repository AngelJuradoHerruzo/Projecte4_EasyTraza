package cat.copernic.easytraza.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
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
 * CONTROLADOR WEB DEL PERFIL D'USUARI.
 *
 * Gestionada la visualització i modificació del perfil de l'usuari autenticat,
 * incloent-hi la consulta del seu avatar.
 *
 * @author Ángel Jurado Herruz
 */
@Controller
@RequestMapping("/perfil")
public class PerfilWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfilWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;
    private final MessageSource messageSource;

    public PerfilWebController(UsuariService usuariService, MessageSource messageSource) {
        this.usuariService = usuariService;
        this.messageSource = messageSource;
    }


    /**
     * VISUALITZACIÓ DEL PERFIL.
     *
     * Carregades les dades del perfil de l'usuari autenticat per mostrar-les
     * a la pantalla corresponent.
     *
     * @param model model de dades de la vista
     * @param session sessió web de l'usuari
     * @return vista del perfil o redirecció al login
     */
    @GetMapping
    public String veurePerfil(Model model, HttpSession session) {

        Usuari usuari = obtenirUsuariSessio(session);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);

        return "perfil/veurePerfil";
    }


    /**
     * OBTENCIÓ DE L'AVATAR DEL PERFIL.
     *
     * Retornada la imatge de l'avatar de l'usuari autenticat quan existeix
     * i està disponible a la seva sessió.
     *
     * @param session sessió web de l'usuari
     * @return resposta amb la imatge de l'avatar o resposta de recurs no trobat
     */
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


    /**
     * FORMULARI D'EDICIÓ DEL PERFIL.
     *
     * Carregades les dades de l'usuari autenticat per mostrar-les
     * al formulari d'edició del seu perfil.
     *
     * @param model model de dades de la vista
     * @param session sessió web de l'usuari
     * @return vista del formulari o redirecció al login
     */
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


    /**
     * ACTUALITZACIÓ DEL PERFIL.
     *
     * Processats els canvis de les dades personals i de l'avatar de l'usuari
     * autenticat, actualitzant la informació conservada a la sessió.
     *
     * @param usuari dades actualitzades del perfil
     * @param avatarFile imatge d'avatar adjuntada al formulari
     * @param model model de dades de la vista
     * @param session sessió web de l'usuari
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al perfil o vista del formulari amb errors
     */
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
                missatge("perfil.missatge.actualitzat")
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


    /**
     * OBTENCIÓ DE L'USUARI AUTENTICAT.
     *
     * Recuperat l'usuari associat a l'identificador mantingut a la sessió
     * web activa.
     *
     * @param session sessió web de l'usuari
     * @return usuari autenticat o valor nul si no està disponible
     */
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
