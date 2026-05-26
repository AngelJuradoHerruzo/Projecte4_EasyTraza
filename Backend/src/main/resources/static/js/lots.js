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
        modal.dataset.conflictTitle,
        modal.dataset.conflictBefore + conflictLot.value + modal.dataset.conflictAfter,
        modal.dataset.conflictButton,
        'bi-exclamation-triangle'
    );
}