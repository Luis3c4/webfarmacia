// Función para inicializar la tabla de clientes
function initClientesTable() {
    console.log('Inicializando tabla de clientes...');
    let currentPage = 0;
    const pageSize = 10;
    const tbody = document.querySelector('.clients-table tbody');
    const pageInfo = document.querySelector('.page-info');
    const btnPrev = document.querySelector('.btn-pagination:first-child');
    const btnNext = document.querySelector('.btn-pagination:last-child');

    function fetchAndRender(page) {
        fetch(`/api/clientes?page=${page}&size=${pageSize}`)
            .then(response => response.json())
            .then(data => {
                console.log('Datos de clientes recibidos:', data);
                tbody.innerHTML = '';
                
                if (data.content && data.content.length > 0) {
                    data.content.forEach(cliente => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${cliente.nombre || ''}</td>
                            <td>${cliente.producto || ''}</td>
                            <td>${cliente.numeroContacto || ''}</td>
                            <td>${cliente.correo || ''}</td>
                            <td><span class="status ${cliente.estado === 'TAKING_RETURN' ? 'taking-return' : 'not-taking-return'}">${cliente.estado === 'TAKING_RETURN' ? 'Taking Return' : 'Not Taking Return'}</span></td>
                        `;
                        tbody.appendChild(row);
                    });
                } else {
                    // Mostrar mensaje cuando no hay clientes
                    const row = document.createElement('tr');
                    row.innerHTML = '<td colspan="5" style="text-align: center; padding: 20px;">No hay clientes registrados</td>';
                    tbody.appendChild(row);
                }
                
                // Actualizar paginación
                pageInfo.textContent = `Page ${data.number + 1} of ${data.totalPages || 1}`;
                btnPrev.disabled = data.number === 0;
                btnNext.disabled = data.number + 1 >= data.totalPages;
                currentPage = data.number;
            })
            .catch(error => {
                console.error('Error al cargar clientes:', error);
                tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: red; padding: 20px;">Error al cargar los clientes</td></tr>';
            });
    }

    // Listeners de paginación
    if (btnPrev) {
        btnPrev.addEventListener('click', () => {
            if (currentPage > 0) {
                fetchAndRender(currentPage - 1);
            }
        });
    }

    if (btnNext) {
        btnNext.addEventListener('click', () => {
            fetchAndRender(currentPage + 1);
        });
    }

    // Cargar la primera página
    fetchAndRender(currentPage);
}

// Inicializar cuando se carga la vista de clientes
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si estamos en la vista de clientes
    if (window.location.hash === '#/clientes' || document.querySelector('.clients-table')) {
        console.log('Cargando clientes...');
        initClientesTable();
    }
});

// Reinicializar cuando cambia la vista (para el router)
window.addEventListener('hashchange', function() {
    if (window.location.hash === '#/clientes') {
        setTimeout(initClientesTable, 100); // Pequeño delay para asegurar que el DOM está listo
    }
}); 