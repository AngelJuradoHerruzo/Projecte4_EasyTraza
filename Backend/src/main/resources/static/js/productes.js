const form = document.getElementById('producteForm');
const nomInput = document.getElementById('nomProducte');
const descripcioInput = document.getElementById('descripcio');
const descripcioCounter = document.getElementById('descripcioCounter');


/*----------------------- NOM -----------------------*/
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


/*----------------------- DESCRIPCIÓ -----------------------*/
// LIMIT D'ENTRADA: 100 CARÀCTERS
descripcioInput.addEventListener('input', function () {

    if (this.value.length > 100) {
        this.value = this.value.substring(0, 100);
    }

    updateDescripcioCounter();
});


/*----------------------- FUNCIONS ESTAT -----------------------*/
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


/*----------------------- COMPTADOR DESCRIPCIÓ -----------------------*/
function updateDescripcioCounter() {
    const length = descripcioInput.value.length;

    descripcioCounter.textContent = `${length}/50`;

    if (length <= 50) {
        descripcioCounter.style.color = '#198754';
    } else {
        descripcioCounter.style.color = '#dc3545';
    }
}


/*----------------------- VALIDACIÓ -----------------------*/
function validateField(field) {
    const value = field.value.trim();

    // Si està buit → no aplicar cap color
    if (value === '') {
        clearState(field);
        return !field.hasAttribute('required');
    }

    // DESCRIPCIÓ → només indicador visual, però deixa guardar
    if (field.id === 'descripcio') {
        if (field.value.length <= 50) {
            markValid(field);
        } else {
            markInvalid(field);
        }

        return true;
    }

    // Validació estàndard amb checkValidity
    if (field.checkValidity()) {
        markValid(field);
        return true;
    } else {
        markInvalid(field);
        return false;
    }
}


/*----------------------- EVENTS VALIDACIÓ -----------------------*/
[nomInput, descripcioInput].forEach(field => {
    field.addEventListener('input', () => validateField(field));
    field.addEventListener('change', () => validateField(field));
});


/*----------------------- SUBMIT -----------------------*/
form.addEventListener('submit', function (event) {

    let valid = true;

    const fields = [nomInput, descripcioInput];

    fields.forEach(field => {
        const ok = validateField(field);
        if (!ok) valid = false;
    });

    // Només bloquegem si fallen camps reals obligatoris
    if (!valid) {
        event.preventDefault();
    }
});


/*----------------------- INIT -----------------------*/
updateDescripcioCounter();