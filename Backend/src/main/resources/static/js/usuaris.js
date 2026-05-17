const form = document.getElementById('usuariForm');

const dniInput = document.getElementById('dni');
const nomInput = document.getElementById('nomComplet');
const emailInput = document.getElementById('email');
const rolSelect = document.getElementById('rolUsuari');
const passwordInput = document.getElementById('password');
const togglePassword = document.getElementById('togglePassword');



// ---------------------------- AJUDES DEL FORMULARI ----------------------------
if (form) {

    // DNI: elimina caràcters no vàlids, passa la lletra a majúscula i limita a 9 caràcters
    if (dniInput) {
        dniInput.addEventListener('input', function () {
            let value = this.value;

            value = value.replace(/[^0-9A-Za-z]/g, '');
            value = value.toUpperCase();

            if (value.length > 9) {
                value = value.substring(0, 9);
            }

            this.value = value;
        });
    }


    // Nom complet: només permet lletres i espais, evita espais duplicats i capitalitza cada paraula
    if (nomInput) {
        nomInput.addEventListener('input', function () {
            let value = this.value;

            value = value.replace(/[^A-Za-zÀ-ÿ\s]/g, '');
            value = value.replace(/\s+/g, ' ');
            value = value.trimStart();

            value = value
                .split(' ')
                .map(word => word ? word.charAt(0).toUpperCase() + word.slice(1).toLowerCase() : '')
                .join(' ');

            this.value = value;
        });

        // En sortir del camp, elimina espais sobrants al principi i al final
        nomInput.addEventListener('blur', function () {
            this.value = this.value.trim();
        });
    }


    // Correu electrònic: elimina espais i passa tot el text a minúscules
    if (emailInput) {
        emailInput.addEventListener('input', function () {
            this.value = this.value.replace(/\s/g, '').toLowerCase();
        });
    }


    // Contrasenya: permet mostrar o ocultar el contingut del camp
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function () {
            const icon = this.querySelector('i');

            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                icon.className = 'bi bi-eye-slash';
            }
            else {
                passwordInput.type = 'password';
                icon.className = 'bi bi-eye';
            }
        });
    }


    // Elimina els estats visuals de validació del camp
    function clearState(field) {
        field.classList.remove('field-valid', 'field-invalid');
    }


    // Marca visualment el camp com a vàlid
    function markValid(field) {
        field.classList.remove('field-invalid');
        field.classList.add('field-valid');
    }


    // Marca visualment el camp com a invàlid
    function markInvalid(field) {
        field.classList.remove('field-valid');
        field.classList.add('field-invalid');
    }


    // Valida visualment un camp utilitzant les regles definides a l'HTML
    function validateField(field) {
        const value = field.value.trim();

        const passwordOptional = field.id === 'password' && !field.hasAttribute('required');

        if (value === '') {
            clearState(field);
            return passwordOptional;
        }

        if (field.checkValidity()) {
            markValid(field);
            return true;
        }

        markInvalid(field);
        return false;
    }


    // Camps que participen en la validació visual del formulari
    const fields = [dniInput, nomInput, emailInput, rolSelect, passwordInput].filter(Boolean);


    // Valida visualment els camps mentre l'usuari escriu o canvia valors
    fields.forEach(field => {
        field.addEventListener('input', () => validateField(field));
        field.addEventListener('change', () => validateField(field));
    });


    // Abans d'enviar, comprova que tots els camps compleixin les validacions HTML
    form.addEventListener('submit', function (event) {
        let valid = true;

        fields.forEach(field => {
            const ok = validateField(field);

            if (!ok) {
                valid = false;
            }
        });

        if (!valid) {
            event.preventDefault();
        }
    });
}
