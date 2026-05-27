// ---------------------------- ELEMENTS DEL FORMULARI ----------------------------
const form = document.getElementById('clientForm');

const cifInput = document.getElementById('cif');
const nomInput = document.getElementById('nomComplet');
const telefonInput = document.getElementById('telefon');
const emailInput = document.getElementById('email');
const adrecaInput = document.getElementById('adreca');
const observacionsInput = document.getElementById('observacions');

const observacionsCounter = document.getElementById('observacionsCounter');


// ---------------------------- AJUDES DEL FORMULARI ----------------------------
if (form) {

    // Nom complet: només permet lletres i espais, evita espais duplicats i capitalitza cada paraula
    if (nomInput) {
        nomInput.addEventListener('input', function () {
            let value = this.value;

            value = value.replace(/[^A-Za-zÀ-ÿ\s]/g, '');
            value = value.replace(/\s+/g, ' ').trimStart();

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


    // CIF / DNI: passa a majúscules i elimina espais o caràcters no vàlids
    if (cifInput && !cifInput.hasAttribute('readonly')) {
        cifInput.addEventListener('input', function () {
            let value = this.value.toUpperCase();

            value = value.replace(/\s+/g, '');
            value = value.replace(/[^A-Z0-9]/g, '');

            if (value.length > 9) {
                value = value.substring(0, 9);
            }

            this.value = value;
        });
    }


    // Telèfon: aplica automàticament el format XXX XX XX XX
    if (telefonInput) {
        telefonInput.addEventListener('input', function () {
            let value = this.value;

            value = value.replace(/\D/g, '');

            if (value.length > 9) {
                value = value.substring(0, 9);
            }

            if (value.length > 3 && value.length <= 5) {
                value = value.substring(0, 3) + ' ' + value.substring(3);
            }
            else if (value.length > 5 && value.length <= 7) {
                value = value.substring(0, 3) + ' ' + value.substring(3, 5) + ' ' + value.substring(5);
            }
            else if (value.length > 7) {
                value = value.substring(0, 3) + ' ' + value.substring(3, 5) + ' ' + value.substring(5, 7) + ' ' + value.substring(7);
            }

            this.value = value;
        });
    }


    // Email: elimina espais i passa el text a minúscules
    if (emailInput) {
        emailInput.addEventListener('input', function () {
            this.value = this.value.replace(/\s/g, '').toLowerCase();
        });

        emailInput.addEventListener('blur', function () {
            this.value = this.value.trim().toLowerCase();
            validateField(this);
        });
    }


    // Observacions: limita l'entrada i actualitza el comptador
    if (observacionsInput) {
        observacionsInput.addEventListener('input', function () {

            if (this.value.length > 100) {
                this.value = this.value.substring(0, 100);
            }

            updateObservacionsCounter();
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


    // Actualitza el comptador de caràcters del camp observacions
    function updateObservacionsCounter() {

        if (!observacionsInput || !observacionsCounter) {
            return;
        }

        const length = observacionsInput.value.length;

        observacionsCounter.textContent = `${length}/50`;

        // 0–50 → correcte
        if (length <= 50) {
            observacionsCounter.style.color = '#198754';
        }
        // 51–100 → supera el límit acceptat pel backend
        else {
            observacionsCounter.style.color = '#dc3545';
        }
    }


    // Valida visualment un camp utilitzant les regles definides a l'HTML
    function validateField(field) {
        const value = field.value.trim();

        // Si està buit, no es pinta el camp
        if (value === '') {
            clearState(field);
            field.setCustomValidity('');
            return !field.hasAttribute('required');
        }

        // Observacions: només es valida que no superi els 50 caràcters acceptats
        if (field.id === 'observacions') {
            field.setCustomValidity('');

            if (field.value.length <= 50) {
                markValid(field);
                return true;
            }

            markInvalid(field);
            return false;
        }

        // Email: si està informat, ha de tenir format correcte
        if (field.id === 'email') {
            const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

            if (emailPattern.test(value)) {
                markValid(field);
                field.setCustomValidity('');
                return true;
            }

            markInvalid(field);
            field.setCustomValidity(form.dataset.emailInvalid);
            return false;
        }

        // Validació estàndard definida amb required, pattern, maxlength...
        field.setCustomValidity('');

        if (field.checkValidity()) {
            markValid(field);
            return true;
        }

        markInvalid(field);
        return false;
    }


    // Camps que participen en la validació visual del formulari
    const fields = [cifInput, nomInput, telefonInput, emailInput, adrecaInput, observacionsInput].filter(Boolean);


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


    // Inicialitza el comptador d'observacions
    updateObservacionsCounter();
}
