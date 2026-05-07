const form = document.getElementById('clientForm');

const nifInput = document.getElementById('nif');
const nomInput = document.getElementById('nomComplet');
const telefonInput = document.getElementById('telefon');
const emailInput = document.getElementById('email');
const adrecaInput = document.getElementById('adreca');
const observacionsInput = document.getElementById('observacions');

const observacionsCounter = document.getElementById('observacionsCounter');


// FORMAT CORRECTE (MAJÚSCULA INICIAL I ESPAIS CONTROLATS)
nomInput.addEventListener('input', function () {
    let value = this.value;

    value = value.replace(/\s+/g, ' ').trimStart();

    value = value
        .split(' ')
        .map(word => word ? word.charAt(0).toUpperCase() + word.slice(1).toLowerCase() : '')
        .join(' ');

    this.value = value;
});


// MAJÚSCULES I SENSE CARÀCTERS INVÀLIDS
nifInput.addEventListener('input', function () {
    let value = this.value.toUpperCase();

    value = value.replace(/\s+/g, '');
    value = value.replace(/[^A-Z0-9]/g, '');

    this.value = value;
});


// FORMAT AUTOMÀTIC XXX XX XX XX
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


// MINÚSCULES I SENSE ESPAIS ALS EXTREMS
emailInput.addEventListener('blur', function () {
    this.value = this.value.trim().toLowerCase();
    validateField(this);
});


// LIMIT D'ENTRADA: 100 CARÀCTERS
observacionsInput.addEventListener('input', function () {

    if (this.value.length > 100) {
        this.value = this.value.substring(0, 100);
    }

    updateObservacionsCounter();
});


// FUNCIONS ESTAT
function clearState(field) {
    field.classList.remove('field-valid', 'field-invalid');
}

function markValid(field) {
    field.classList.remove('field-invalid');
    field.classList.add('field-valid');
}

function markInvalid(field) {
    field.classList.remove('field-valid');
    field.classList.add('field-invalid');
}


// COMPTADOR OBSERVACIONS
function updateObservacionsCounter() {
    const length = observacionsInput.value.length;

    observacionsCounter.textContent = `${length}/50`;

    // 0–50 → verd
    if (length <= 50) {
        observacionsCounter.style.color = '#198754';
    }
    // 51–100 → vermell
    else {
        observacionsCounter.style.color = '#dc3545';
    }
}


// VALIDA SEGONS HTML + CASOS ESPECIALS
function validateField(field) {
    const value = field.value.trim();

    // Si està buit → no color
    if (value === '') {
        clearState(field);
        return !field.hasAttribute('required');
    }

    // OBSERVACIONS → només validem longitud
    if (field.id === 'observacions') {
        if (field.value.length <= 50) {
            markValid(field);
            return true;
        }
        else {
            markInvalid(field);
            return false;
        }
    }

    // EMAIL → validem format complet
    if (field.id === 'email') {
        const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

        if (emailPattern.test(value)) {
            markValid(field);
            field.setCustomValidity('');
            return true;
        }
        else {
            markInvalid(field);
            field.setCustomValidity('Introdueix un correu electrònic vàlid, per exemple exemple@gmail.com');
            return false;
        }
    }

    // VALIDACIÓ ESTÀNDARD (pattern, required…)
    field.setCustomValidity('');

    if (field.checkValidity()) {
        markValid(field);
        return true;
    }
    else {
        markInvalid(field);
        return false;
    }
}


// EVENTS VALIDACIÓ
[nifInput, nomInput, telefonInput, emailInput, adrecaInput, observacionsInput].forEach(field => {
    field.addEventListener('input', () => validateField(field));
    field.addEventListener('change', () => validateField(field));
});


// SUBMIT
form.addEventListener('submit', function (event) {

    let valid = true;

    const fields = [nifInput, nomInput, telefonInput, emailInput, adrecaInput, observacionsInput];

    fields.forEach(field => {
        const ok = validateField(field);
        if (!ok) valid = false;
    });

    if (!valid) {
        event.preventDefault();
    }
});


// INIT
updateObservacionsCounter();