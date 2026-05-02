const form = document.getElementById('materiaPrimeraForm');
const nomInput = document.getElementById('nomMateria');
const descripcioInput = document.getElementById('descripcio');
const descripcioCounter = document.getElementById('descripcioCounter');


// NOM: FORMAT CORRECTE (MAJÚSCULA INICIAL) I ESPAIS CONTROLATS
nomInput.addEventListener('input', function () {
    let value = this.value;

    value = value.replace(/\s+/g, ' ').trimStart();

    value = value
        .split(' ')
        .map(word => word ? word.charAt(0).toUpperCase() + word.slice(1).toLowerCase() : '')
        .join(' ');

    this.value = value;
});


// DESCRIPCIÓ: LIMITA L'ENTRADA A 100 CARÀCTERS
descripcioInput.addEventListener('input', function () {
    if (this.value.length > 100) {
        this.value = this.value.substring(0, 100);
    }

    updateCounter();
});


// ELIMINA QUALSEVOL ESTAT DE VALIDACIÓ
function clearState(field) {
    field.classList.remove('field-valid', 'field-invalid');
}


// MARCA EL CAMP COM A VÀLID
function markValid(field) {
    field.classList.remove('field-invalid');
    field.classList.add('field-valid');
}


// MARCA EL CAMP COM A INVÀLID
function markInvalid(field) {
    field.classList.remove('field-valid');
    field.classList.add('field-invalid');
}


// ACTUALITZA EL COMPTADOR DE LA DESCRIPCIÓ
function updateCounter() {
    const length = descripcioInput.value.length;

    descripcioCounter.textContent = `${length}/50`;

    if (length <= 50) {
        descripcioCounter.style.color = '#198754'; // verd
    } else {
        descripcioCounter.style.color = '#dc3545'; // vermell
    }
}


// VALIDACIÓ ESTIL USUARIS
function validateField(field) {
    const value = field.value.trim();

    // Si està buit → no aplicar cap color
    if (value === '') {
        clearState(field);
        return !field.hasAttribute('required');
    }

    // CAS ESPECIAL DESCRIPCIÓ (VISUAL NOMÉS)
    if (field.id === 'descripcio') {
        if (field.value.length <= 50) {
            markValid(field);
        } else {
            markInvalid(field); // es veu vermell
        }

        return true; // 🔥 IMPORTANT → sempre deixa guardar
    }

    // VALIDACIÓ NORMAL (com Usuaris)
    if (field.checkValidity()) {
        markValid(field);
        return true;
    } 
    else {
        markInvalid(field);
        return false;
    }
}


// VALIDACIÓ EN TEMPS REAL
[nomInput, descripcioInput].forEach(field => {
    field.addEventListener('input', () => validateField(field));
    field.addEventListener('change', () => validateField(field));
});


// VALIDACIÓ FINAL ABANS D'ENVIAR
form.addEventListener('submit', function (event) {
    let valid = true;

    const fields = [nomInput, descripcioInput];

    fields.forEach(field => {
        const ok = validateField(field);
        if (!ok) valid = false;
    });

    if (!valid) {
        event.preventDefault();
    }
});


// INIT
updateCounter();