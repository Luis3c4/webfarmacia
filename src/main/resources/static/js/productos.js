// Función para inicializar el formulario de productos
function initProductosForm() {
    console.log('Inicializando formulario de productos...');
    
    const modal = document.getElementById('modalAgregarProducto');
    const btnAgregar = document.querySelector('.btn-agregar');
    const closeBtn = document.querySelector('.close');
    const btnCancelar = document.querySelector('.btn-cancelar');
    const form = document.getElementById('formAgregarProducto');

    console.log('Elementos encontrados:', {
        modal: !!modal,
        btnAgregar: !!btnAgregar,
        closeBtn: !!closeBtn,
        btnCancelar: !!btnCancelar,
        form: !!form
    });


    console.log('Todos los elementos encontrados, configurando eventos...');

    // Abrir modal
    btnAgregar.addEventListener('click', () => {
        console.log('Botón agregar clickeado');
        modal.style.display = 'block';
    });

    // Cerrar modal
    const closeModal = () => {
        modal.style.display = 'none';
        form.reset();
    };

    closeBtn.addEventListener('click', closeModal);
    btnCancelar.addEventListener('click', closeModal);
    window.addEventListener('click', (e) => {
        console.log(e.target);
        if (e.target === modal) {
            closeModal();
        }
    });

    // Manejar envío del formulario
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        console.log('Formulario enviado');

        const stock = parseInt(document.getElementById('stock').value);
        let stockStatus;
        if (stock <= 0) {
            stockStatus = 'OUT_OF_STOCK';
        } else if (stock <= 10) {
            stockStatus = 'LOW_STOCK';
        } else {
            stockStatus = 'IN_STOCK';
        }

        const formData = {
            nombre: document.getElementById('nombre').value,
            precio: parseFloat(document.getElementById('precio').value),
            cantidadIngresada: parseInt(document.getElementById('cantidadIngresada').value),
            descripcion: document.getElementById('descripcion').value,
            stock: stock,
            stockStatus: stockStatus
        };

        console.log('Datos del formulario:', formData);

        try {
            const response = await fetch('/api/productos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                alert('Producto agregado exitosamente');
                closeModal();
                // Recargar la tabla de productos
                location.reload();
            } else {
                const error = await response.text();
                alert('Error al agregar el producto: ' + error);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Error al agregar el producto');
        }
    });

    console.log('Formulario de productos inicializado correctamente');
}

// Inicializar cuando se carga la vista de productos
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si estamos en la vista de productos
    if (window.location.hash === '#/productos' || document.querySelector('.productos-tabla')) {
        initProductosForm();
    }
});

// Reinicializar cuando cambia la vista (para el router)
window.addEventListener('hashchange', function() {
    if (window.location.hash === '#/productos') {
        setTimeout(initProductosForm, 100); // Pequeño delay para asegurar que el DOM está listo
    }
}); 