package cat.copernic.easytraza.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.service.RecuperacioContrasenyaService;

/**
 * CONTROLADOR DE RECUPERACIÓ DE CONTRASENYA.
 *
 * Gestionades les pantalles públiques que permeten sol·licitar
 * un enllaç temporal i establir una nova contrasenya.
 *
 * @author Ángel Jurado Herruz
 */
@Controller
public class RecuperacioContrasenyaWebController {

    private final RecuperacioContrasenyaService recuperacioContrasenyaService;
    private final MessageSource messageSource;

    public RecuperacioContrasenyaWebController(RecuperacioContrasenyaService recuperacioContrasenyaService,
                                               MessageSource messageSource) {
        this.recuperacioContrasenyaService = recuperacioContrasenyaService;
        this.messageSource = messageSource;
    }


    /**
     * FORMULARI DE SOL·LICITUD.
     *
     * Mostrada la pantalla on l'usuari pot indicar el correu
     * associat al seu compte per iniciar la recuperació.
     *
     * @return vista del formulari de recuperació
     */
    @GetMapping("/recuperar-contrasenya")
    public String mostrarSollicitud() {
        return "auth/recuperarContrasenya";
    }


    /**
     * ENVIAMENT DE LA SOL·LICITUD.
     *
     * Processat el correu introduït i mostrat sempre un missatge neutre
     * per evitar revelar l'existència d'un compte concret.
     *
     * @param email correu electrònic indicat per l'usuari
     * @param redirectAttributes atributs temporals per mostrar el resultat
     * @return redirecció al formulari de recuperació
     */
    @PostMapping("/recuperar-contrasenya")
    public String solLicitarRecuperacio(@RequestParam String email, RedirectAttributes redirectAttributes) {

        recuperacioContrasenyaService.solLicitarRecuperacio(email);

        redirectAttributes.addFlashAttribute(
                "success",
                missatge("recuperacio.solicitud.correcta")
        );

        return "redirect:/recuperar-contrasenya";
    }


    /**
     * FORMULARI DE NOVA CONTRASENYA.
     *
     * Validat l'enllaç rebut i mostrada la pantalla per establir
     * la nova contrasenya quan el token encara és vàlid.
     *
     * @param token token rebut des del correu de recuperació
     * @param model dades que s'han de mostrar a la vista
     * @return vista del formulari de nova contrasenya
     */
    @GetMapping("/restablir-contrasenya")
    public String mostrarRestabliment(@RequestParam(required = false) String token, Model model) {

        boolean tokenValid = recuperacioContrasenyaService.tokenValid(token);

        model.addAttribute("token", token);
        model.addAttribute("tokenValid", tokenValid);

        if (!tokenValid) {
            model.addAttribute("error", missatge("recuperacio.enllacNoValid"));
        }

        return "auth/restablirContrasenya";
    }


    /**
     * GUARDAT DE LA NOVA CONTRASENYA.
     *
     * Actualitzada la contrasenya quan el token és correcte
     * o retornada la vista amb l'error corresponent.
     *
     * @param token token rebut des del correu de recuperació
     * @param password nova contrasenya indicada per l'usuari
     * @param confirmPassword repetició de la nova contrasenya
     * @param model dades que s'han de mostrar a la vista
     * @return redirecció al login o vista del formulari amb error
     */
    @PostMapping("/restablir-contrasenya")
    public String restablirContrasenya(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model) {
        try {
            recuperacioContrasenyaService.restablirContrasenya(token, password, confirmPassword);
            return "redirect:/login?passwordChanged=true";
        }
        catch (IllegalArgumentException ex) {
            model.addAttribute("token", token);
            model.addAttribute("tokenValid", recuperacioContrasenyaService.tokenValid(token));
            model.addAttribute("error", ex.getMessage());
            return "auth/restablirContrasenya";
        }
    }


    /**
     * OBTENCIÓ DEL MISSATGE.
     *
     * Obtingut el text internacionalitzat corresponent al codi rebut
     * i als arguments indicats.
     *
     * @param codi codi del missatge que s'ha d'obtenir
     * @param arguments arguments aplicats al missatge
     * @return text obtingut pel mètode
     */
    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }
}
