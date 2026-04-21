const form = document.getElementById('usuariForm');
const nomInput = document.getElementById('nomComplet');
const emailInput = document.getElementById('email');
const rolSelect = document.getElementById('rolUsuari');
const passwordInput = document.getElementById('password');
const togglePassword = document.getElementById('togglePassword');


// NOM: NOMÉS LLETRES, ESPAIS I FORMAT CORRECTE (MAJÚSCULA INICIAL)
nomInput.addEventListener('input', function () {
    let value = this.value;

    value = value.replace(/[^A-Za-zÀ-ÿ\s]/g, '');       // Eliminem caràcters no vàlids
    value = value.replace(/\s+/g, ' ').trimStart();     // Evitem espais duplicats

    value = value
        .split(' ')
        .map(word => word ? word.charAt(0).toUpperCase() + word.slice(1).toLowerCase() : '')
        .join(' ');

    this.value = value;
});


// PERMET MOSTRAR O OCULTAR LA CONTRASENYA FENT CLIC A LA ICONA
togglePassword.addEventListener('click', function () {
    const icon = this.querySelector('i');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text'; // Mostra la contrasenya
        icon.className = 'bi bi-eye-slash';
    } 
    else {
        passwordInput.type = 'password'; // Amaga la contrasenya
        icon.className = 'bi bi-eye';
    }
});


// ELIMINA QUALSEVOL ESTAT DE VALIDACIÓ (VERD O VERMELL)
function clearState(field) {
    field.classList.remove('field-valid', 'field-invalid');
}


// MARCA EL CAMP COM A VÀLID (VERD)
function markValid(field) {
    field.classList.remove('field-invalid');
    field.classList.add('field-valid');
}


// MARCA EL CAMP COM A INVÀLID (VERMELL)
function markInvalid(field) {
    field.classList.remove('field-valid');
    field.classList.add('field-invalid');
}


// VALIDA UN CAMP CONCRET SEGONS LES REGLES HTML
function validateField(field) {
    const value = field.value.trim();

    // Comprovem si la contrasenya és opcional (mode edició)
    const passwordOptional = field.id === 'password' && !field.hasAttribute('required');

    // Si està buit → no aplicar cap color
    if (value === '') {
        clearState(field);
        return passwordOptional;
    }

    // Validació amb checkValidity (pattern, required, etc.)
    if (field.checkValidity()) {
        markValid(field);
        return true;
    } 
    else {
        markInvalid(field);
        return false;
    }
}


// VALIDACIÓ EN TEMPS REAL QUAN L'USUARI ESCRIU O CANVIA UN VALOR
[nomInput, emailInput, rolSelect, passwordInput].forEach(field => {
    field.addEventListener('input', () => validateField(field));
    field.addEventListener('change', () => validateField(field)); // per als selects
});


// VALIDACIÓ FINAL ABANS D'ENVIAR EL FORMULARI
form.addEventListener('submit', function (event) {
    let valid = true;

    const fields = [nomInput, emailInput, rolSelect, passwordInput];

    fields.forEach(field => {
        const ok = validateField(field); // Validem cada camp
        if (!ok) valid = false;
    });

    // Si hi ha algun error → es bloqueja l'enviament
    if (!valid) {
        event.preventDefault();
    }
});