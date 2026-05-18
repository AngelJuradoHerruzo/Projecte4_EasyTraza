const form = document.getElementById('proveidorForm');

const cifInput = document.getElementById('cif');
const nomInput = document.getElementById('nomProveidor');
const adrecaInput = document.getElementById('adreca');
const descripcioInput = document.getElementById('descripcio');

const descripcioCounter = document.getElementById('descripcioCounter');
const tipusDocument = document.getElementById('tipusDocument');


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
cifInput.addEventListener('input', function () {
    let value = this.value.toUpperCase();

    value = value.replace(/\s+/g, '');
    value = value.replace(/[^A-Z0-9]/g, '');

    this.value = value;

    updateTipusDocument();
});


// LIMIT D'ENTRADA: 100 CARÀCTERS
descripcioInput.addEventListener('input', function () {

    if (this.value.length > 100) {
        this.value = this.value.substring(0, 100);
    }

    updateDescripcioCounter();
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


// DETECTA DNI O CIF SEGONS EL PRIMER CARÀCTER
function updateTipusDocument() {
    const value = cifInput.value.trim();

    if (value === '') {
        tipusDocument.textContent = '';
        return;
    }

    if (/^\d/.test(value)) {
        tipusDocument.textContent = 'DNI';
    } 
    else if (/^[A-Z]/.test(value)) {
        tipusDocument.textContent = 'CIF';
    } 
    else {
        tipusDocument.textContent = '';
    }
}


// COMPTADOR DESCRIPCIÓ
function updateDescripcioCounter() {
    const length = descripcioInput.value.length;

    descripcioCounter.textContent = `${length}/50`;

    // 0–50 → verd
    if (length <= 50) {
        descripcioCounter.style.color = '#198754';
    }
    // 51–100 → vermell
    else {
        descripcioCounter.style.color = '#dc3545';
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

    // DESCRIPCIÓ → només validem longitud
    if (field.id === 'descripcio') {
        if (field.value.length <= 50) {
            markValid(field);
            return true;
        } 
        else {
            markInvalid(field);
            return false;
        }
    }

    // VALIDACIÓ ESTÀNDARD (pattern, required…)
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
[cifInput, nomInput, adrecaInput, descripcioInput].forEach(field => {
    field.addEventListener('input', () => validateField(field));
    field.addEventListener('change', () => validateField(field));
});


// SUBMIT
form.addEventListener('submit', function (event) {

    let valid = true;
    const fields = [cifInput, nomInput, adrecaInput, descripcioInput];

    fields.forEach(field => {
        const ok = validateField(field);
        if (!ok) valid = false;
    });

    if (!valid) {
        event.preventDefault();
    }
});


// INIT
updateTipusDocument();
updateDescripcioCounter();