/*********************       .ELEMENTS DEL FORMULARI.       *********************/
const form = document.getElementById('materiaPrimeraForm');
const nomInput = document.getElementById('nomMateria');
const descripcioInput = document.getElementById('descripcio');
const descripcioCounter = document.getElementById('descripcioCounter');


/*********************       .SUPORT VISUAL DEL FORMULARI.       *********************/
if (form) {

    // ACTUALITZAR EL COMPTADOR VISUAL DE LA DESCRIPCIÓ
    function actualitzarComptadorDescripcio() {

        if (!descripcioInput || !descripcioCounter) {
            return;
        }

        const longitud = descripcioInput.value.length;

        descripcioCounter.textContent = `${longitud}/50`;
        descripcioCounter.classList.remove('counter-valid', 'counter-limit');

        if (longitud >= 45) {
            descripcioCounter.classList.add('counter-limit');
        }
        else {
            descripcioCounter.classList.add('counter-valid');
        }
    }


    // ELIMINAR L'ESTAT VISUAL D'UN CAMP
    function netejarEstatCamp(field) {
        field.classList.remove('field-valid', 'field-invalid');
    }


    // ACTUALITZAR L'ESTAT VISUAL SEGONS LA VALIDACIÓ HTML DEL CAMP
    function actualitzarEstatCamp(field) {

        if (!field || field.readOnly || field.disabled) {
            return;
        }

        if (field.value.trim() === '') {
            netejarEstatCamp(field);
            return;
        }

        if (field.checkValidity()) {
            field.classList.remove('field-invalid');
            field.classList.add('field-valid');
        }
        else {
            field.classList.remove('field-valid');
            field.classList.add('field-invalid');
        }
    }


    // APLICAR RETORN VISUAL ALS CAMPS DEL FORMULARI
    [nomInput, descripcioInput].filter(Boolean).forEach(field => {
        field.addEventListener('input', function () {
            actualitzarEstatCamp(this);
        });

        field.addEventListener('change', function () {
            actualitzarEstatCamp(this);
        });
    });


    // ACTUALITZAR COMPTADOR EN ESCRIURE LA DESCRIPCIÓ
    if (descripcioInput) {
        descripcioInput.addEventListener('input', actualitzarComptadorDescripcio);
    }


    // INICIALITZAR EL COMPTADOR EN CARREGAR EL FORMULARI
    actualitzarComptadorDescripcio();
}
