const searchInput = document.getElementById('searchInput');
const rows = document.querySelectorAll('.lot-row');

const modal = document.getElementById('confirmModal');
const modalForm = document.getElementById('modalForm');
const modalTitle = document.getElementById('modalTitle');
const modalMessage = document.getElementById('modalMessage');
const modalIcon = document.getElementById('modalIcon');
const modalSubmitIcon = document.getElementById('modalSubmitIcon');
const modalSubmitText = document.getElementById('modalSubmitText');
const modalCancel = document.getElementById('modalCancel');

const conflictAction = document.getElementById('conflictAction');
const conflictLot = document.getElementById('conflictLot');


/*----------------------- CERCA -----------------------*/
if (searchInput) {
    searchInput.addEventListener('keyup', function () {
        const filter = this.value.toLowerCase();

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(filter) ? '' : 'none';
        });
    });
}


/*----------------------- MODAL -----------------------*/
function obrirModal(action, title, message, button, icon) {
    if (!modal || !modalForm) return;

    modalForm.action = action;
    modalTitle.textContent = title;
    modalMessage.textContent = message;
    modalSubmitText.textContent = button;

    modalIcon.className = 'bi ' + icon;
    modalSubmitIcon.className = 'bi ' + icon;

    modal.style.display = 'flex';
}

function tancarModal() {
    if (!modal || !modalForm) return;

    modal.style.display = 'none';
    modalForm.action = '';
}


/*----------------------- BOTONS MODAL -----------------------*/
document.querySelectorAll('.js-open-modal').forEach(button => {
    button.addEventListener('click', function () {
        obrirModal(
            this.dataset.action,
            this.dataset.title,
            this.dataset.message,
            this.dataset.button,
            this.dataset.icon
        );
    });
});


/*----------------------- TANCAR MODAL -----------------------*/
if (modalCancel) {
    modalCancel.addEventListener('click', tancarModal);
}

if (modal) {
    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            tancarModal();
        }
    });
}


/*----------------------- MODAL LOT OBERT -----------------------*/
if (conflictAction && conflictLot) {
    obrirModal(
        conflictAction.value,
        'Finalitzar lot obert',
        'Ja hi ha un lot obert per aquesta matèria primera. Si continues, es finalitzarà el lot anterior i s’iniciarà el lot ' + conflictLot.value + '.',
        'Confirmar inici',
        'bi-exclamation-triangle'
    );
}