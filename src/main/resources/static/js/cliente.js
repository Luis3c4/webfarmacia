// Función para inicializar la tabla de clientes
function initClientesTable() {
    console.log('Inicializando tabla de clientes...');
    let currentPage = 0;
    const pageSize = 10;
    let isFiltered = false; // Variable para controlar si estamos en modo filtrado
    const tbody = document.querySelector('.clients-table tbody');
    const pageInfo = document.querySelector('.page-info');
    const btnPrev = document.querySelector('.btn-pagination:first-child');
    const btnNext = document.querySelector('.btn-pagination:last-child');
    const btnFiltrar = document.getElementById('btnFiltrarCliente');
    const filterInput = document.getElementById('filterClienteNombre');

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
                isFiltered = false;
            })
            .catch(error => {
                console.error('Error al cargar clientes:', error);
                tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: red; padding: 20px;">Error al cargar los clientes</td></tr>';
            });
    }

    // Función para filtrar por nombre
    async function filtrarPorNombre() {
        const nombre = filterInput.value.trim();
        
        if (!nombre) {
            // Si el input está vacío, recargar la paginación normal
            fetchAndRender(currentPage);
            return;
        }

        try {
            const response = await fetch(`/api/clientes/buscar?nombre=${encodeURIComponent(nombre)}&page=0&size=${pageSize}`);
            
            if (response.ok) {
                const data = await response.json();
                
                // Limpiar tabla y mostrar los clientes encontrados
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
                    
                    // Actualizar paginación para mostrar resultados filtrados
                    pageInfo.textContent = `Encontrados ${data.totalElements} clientes con "${nombre}"`;
                    btnPrev.disabled = data.number === 0;
                    btnNext.disabled = data.number + 1 >= data.totalPages;
                    isFiltered = true;
                    
                } else {
                    // No se encontraron clientes
                    tbody.innerHTML = `
                        <tr>
                            <td colspan="5" style="text-align: center; padding: 20px; color: #6c757d; font-style: italic;">
                                No se encontraron clientes con el nombre: "${nombre}"
                            </td>
                        </tr>
                    `;
                    pageInfo.textContent = `No se encontraron clientes`;
                    btnPrev.disabled = true;
                    btnNext.disabled = true;
                    isFiltered = true;
                }
                
            } else {
                // Error en la respuesta
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" style="text-align: center; padding: 20px; color: #dc3545; font-style: italic;">
                            Error al buscar clientes. Inténtalo de nuevo.
                        </td>
                    </tr>
                `;
                pageInfo.textContent = `Error en la búsqueda`;
                btnPrev.disabled = true;
                btnNext.disabled = true;
                isFiltered = true;
            }
        } catch (error) {
            console.error('Error al filtrar:', error);
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 20px; color: #dc3545; font-style: italic;">
                        Error al buscar clientes. Inténtalo de nuevo.
                    </td>
                </tr>
            `;
            pageInfo.textContent = `Error en la búsqueda`;
            btnPrev.disabled = true;
            btnNext.disabled = true;
            isFiltered = true;
        }
    }

    // Event listener para el botón de filtrar
    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', filtrarPorNombre);
    }

    // Event listener para filtrar al presionar Enter en el input
    if (filterInput) {
        filterInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                filtrarPorNombre();
            }
        });
    }

    // Listeners de paginación
    if (btnPrev) {
        btnPrev.addEventListener('click', () => {
            if (currentPage > 0 && !isFiltered) {
                fetchAndRender(currentPage - 1);
            }
        });
    }

    if (btnNext) {
        btnNext.addEventListener('click', () => {
            if (!isFiltered) {
                fetchAndRender(currentPage + 1);
            }
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