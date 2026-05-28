// ---------------------------- INICIALITZACIÓ ----------------------------
document.addEventListener('DOMContentLoaded', function () {

    // Inicialitza els botons de l'apartat de nova unitat
    inicialitzarBotonsUnitatsMesura();
});


// ---------------------------- INICIALITZAR BOTONS ----------------------------
function inicialitzarBotonsUnitatsMesura() {

    // Botons + de cada lot
    document.querySelectorAll('.btn-toggle-unitat-mesura').forEach(button => {
        button.onclick = function () {
            mostrarPanelUnitatMesura(this);
        };
    });


    // Botó de cancel·lar del panel general
    const cancelarButton = document.getElementById('cancelarUnitatMesura');

    if (cancelarButton) {
        cancelarButton.onclick = function () {
            tancarPanelUnitatMesura();
        };
    }


    // Input de nova unitat: ajuda visual per escriure en minúscules
    const input = document.getElementById('novaUnitatMesura');

    if (input) {
        input.oninput = function () {
            normalitzarInputUnitatMesura(this);
        };
    }
}


// ---------------------------- MOSTRAR PANEL ----------------------------
function mostrarPanelUnitatMesura(button) {

    const panel = document.getElementById('unitatMesuraPanel');
    const input = document.getElementById('novaUnitatMesura');
    const indexInput = document.getElementById('indexUnitatMesura');

    if (!panel) {
        return;
    }

    panel.style.display = 'block';

    if (indexInput) {
        indexInput.value = button.dataset.lotIndex;
    }

    if (input) {
        input.value = '';
        input.focus();
    }
}


// ---------------------------- TANCAR PANEL ----------------------------
function tancarPanelUnitatMesura() {

    const panel = document.getElementById('unitatMesuraPanel');
    const input = document.getElementById('novaUnitatMesura');
    const indexInput = document.getElementById('indexUnitatMesura');

    if (panel) {
        panel.style.display = 'none';
    }

    if (input) {
        input.value = '';
    }

    if (indexInput) {
        indexInput.value = '';
    }
}


// ---------------------------- NORMALITZAR INPUT ----------------------------
function normalitzarInputUnitatMesura(input) {

    let value = input.value;

    // Ajuda visual: elimina espais, caràcters no vàlids i passa a minúscules
    value = value.replace(/\s+/g, '');
    value = value.replace(/[^A-Za-z0-9]/g, '');
    value = value.toLowerCase();

    if (value.length > 4) {
        value = value.substring(0, 4);
    }

    input.value = value;
}