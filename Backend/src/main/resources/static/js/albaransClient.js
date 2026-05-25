/*********************       .ELEMENTS DEL FORMULARI.       *********************/
const form = document.getElementById('albaraClientForm');
const liniesContainer = document.getElementById('liniesContainer');
const addLiniaBtn = document.getElementById('addLiniaBtn');


/*********************       .SUPORT VISUAL DE LES LÍNIES.       *********************/
if (form && liniesContainer && addLiniaBtn) {

    // ACTUALITZAR ELS ÍNDEXS DELS CAMPS DINÀMICS I LA NUMERACIÓ VISUAL
    function reindexarLinies() {
        const linies = liniesContainer.querySelectorAll('.linia-row');

        linies.forEach((linia, index) => {
            const numero = linia.querySelector('.linia-number-value');

            if (numero) {
                numero.textContent = index + 1;
            }

            linia.querySelectorAll('input, select').forEach(camp => {
                const name = camp.getAttribute('name');
                const id = camp.getAttribute('id');

                if (name) {
                    camp.setAttribute(
                        'name',
                        name.replace(/liniesProduccio\[\d+\]/, `liniesProduccio[${index}]`)
                    );
                }

                if (id) {
                    camp.setAttribute(
                        'id',
                        id.replace(/liniesProduccio\d+/, `liniesProduccio${index}`)
                    );
                }
            });
        });

        actualitzarBotonsEliminar();
    }


    // ACTIVAR O DESACTIVAR L'ELIMINACIÓ SEGONS EL NOMBRE DE LÍNIES VISIBLES
    function actualitzarBotonsEliminar() {
        const linies = liniesContainer.querySelectorAll('.linia-row');
        const unaSolaLinia = linies.length === 1;

        linies.forEach(linia => {
            const boto = linia.querySelector('.remove-linia-btn');

            if (boto) {
                boto.disabled = unaSolaLinia;
            }
        });
    }


    // MOSTRAR L'ESTAT VISUAL DEL CAMP A PARTIR DE LES VALIDACIONS HTML
    function actualitzarEstatVisual(camp) {
        if (!camp || camp.value.trim() === '') {
            camp.classList.remove('field-valid', 'field-invalid');
            return;
        }

        if (camp.checkValidity()) {
            camp.classList.remove('field-invalid');
            camp.classList.add('field-valid');
        }
        else {
            camp.classList.remove('field-valid');
            camp.classList.add('field-invalid');
        }
    }


    // CONNECTAR EL RETORN VISUAL DELS CAMPS DE CADA LÍNIA
    function vincularCampsVisuals() {
        liniesContainer.querySelectorAll('select, input[type="number"]').forEach(camp => {
            camp.onchange = function () {
                actualitzarEstatVisual(this);
            };

            camp.oninput = function () {
                actualitzarEstatVisual(this);
            };
        });
    }


    // CONNECTAR ELS BOTONS D'ELIMINACIÓ DE CADA LÍNIA
    function vincularBotonsEliminar() {
        liniesContainer.querySelectorAll('.remove-linia-btn').forEach(boto => {
            boto.onclick = function () {
                const linies = liniesContainer.querySelectorAll('.linia-row');

                if (linies.length > 1) {
                    this.closest('.linia-row').remove();
                    reindexarLinies();
                    vincularBotonsEliminar();
                    vincularCampsVisuals();
                }
            };
        });
    }


    // AFEGIR UNA NOVA LÍNIA VISUAL AL FORMULARI
    addLiniaBtn.addEventListener('click', function () {
        const linies = liniesContainer.querySelectorAll('.linia-row');
        const novaLinia = linies[linies.length - 1].cloneNode(true);

        novaLinia.querySelectorAll('input, select').forEach(camp => {
            camp.value = '';
            camp.classList.remove('field-valid', 'field-invalid');
        });

        liniesContainer.appendChild(novaLinia);

        reindexarLinies();
        vincularBotonsEliminar();
        vincularCampsVisuals();
    });


    // INICIALITZAR COMPORTAMENTS VISUALS DEL FORMULARI
    reindexarLinies();
    vincularBotonsEliminar();
    vincularCampsVisuals();
}
