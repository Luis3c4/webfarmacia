// Variables globales para usuarios
let usuariosCurrentPage = 1;
const usuariosPageSize = 10;
let usuariosFetchAndRenderFunction = null;
let usuariosIsFiltered = false;

// Función para renderizar la tabla de usuarios
function renderUsuariosTabla(usuarios) {
  const tbody = document.querySelector('.users-table tbody');
  const emptyState = document.getElementById('emptyState');
  
  if (!tbody) {
    console.error('No se encontró el tbody de la tabla');
    return;
  }
  
  tbody.innerHTML = '';
  
  if (!usuarios || usuarios.length === 0) {
    const tableContainer = document.querySelector('.table-container');
    if (tableContainer) tableContainer.style.display = 'none';
    if (emptyState) emptyState.style.display = 'flex';
    return;
  }
  
  const tableContainer = document.querySelector('.table-container');
  if (tableContainer) tableContainer.style.display = 'block';
  if (emptyState) emptyState.style.display = 'none';
  
  usuarios.forEach(usuario => {
    const tr = document.createElement('tr');
    tr.className = 'usuario-row';
    
    // Determinar la clase de estado del usuario
    let statusClass = '';
    let statusText = '';
    
    if (usuario.activo) {
      statusClass = 'active';
      statusText = 'Activo';
    } else {
      statusClass = 'inactive';
      statusText = 'Inactivo';
    }
    
    // Determinar la clase del tipo de usuario
    let tipoUsuarioClass = 'default';
    let tipoUsuarioText = usuario.tipoUsuario || 'Sin tipo';
    
    switch(usuario.tipoUsuario?.toLowerCase()) {
      case 'admin':
        tipoUsuarioClass = 'admin';
        break;
      case 'cliente':
        tipoUsuarioClass = 'cliente';
        break;
      case 'vendedor':
        tipoUsuarioClass = 'vendedor';
        break;
      default:
        tipoUsuarioClass = 'default';
    }
    
    // Función para obtener el icono del tipo de usuario
    function getTipoUsuarioIcon(tipoClass) {
      switch(tipoClass) {
        case 'admin':
          return 'fa-user-shield';
        case 'cliente':
          return 'fa-user';
        case 'vendedor':
          return 'fa-user-tie';
        default:
          return 'fa-user-circle';
      }
    }
    
    // Función para formatear fechas
    function formatearFecha(fecha) {
      if (!fecha) return '-';
      
      try {
        // Intentar parsear la fecha
        let date;
        
        // Si es un string, intentar parsearlo
        if (typeof fecha === 'string') {
          // Remover caracteres extra si los hay
          const fechaLimpia = fecha.replace(/[^\d\-\s:]/g, '');
          date = new Date(fechaLimpia);
        } else {
          date = new Date(fecha);
        }
        
        // Verificar si la fecha es válida
        if (isNaN(date.getTime())) {
          console.warn('Fecha inválida:', fecha);
          return '-';
        }
        
        // Formatear la fecha
        const fechaFormateada = date.toLocaleDateString('es-ES', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'
        });
        
        const horaFormateada = date.toLocaleTimeString('es-ES', {
          hour: '2-digit',
          minute: '2-digit'
        });
        
        return `${fechaFormateada} ${horaFormateada}`;
      } catch (error) {
        console.error('Error formateando fecha:', fecha, error);
        return '-';
      }
    }
    
    tr.innerHTML = `
      <td class="td-id">
        <div class="user-id">#${usuario.idUsuario || '-'}</div>
      </td>
      <td class="td-name">
        <div class="usuario-info">
          <div class="usuario-nombre">${usuario.nombre || '-'}</div>
          <div class="usuario-email-mobile">${usuario.email || '-'}</div>
        </div>
      </td>
      <td class="td-email">
        <div class="email-container">
          <i class="fas fa-envelope"></i>
          <span>${usuario.email || '-'}</span>
        </div>
      </td>
      <td class="td-phone">
        <div class="phone-container">
          <i class="fas fa-phone"></i>
          <span>${usuario.telefono || '-'}</span>
        </div>
      </td>
      <td class="td-type">
        <div class="tipo-usuario-badge">
          <span class="status ${tipoUsuarioClass}">
            <i class="fas ${getTipoUsuarioIcon(tipoUsuarioClass)}"></i>
            ${tipoUsuarioText}
          </span>
        </div>
      </td>
      <td class="td-status">
        <div class="status-badge ${statusClass}">
          <div class="status-indicator"></div>
          <span>${statusText}</span>
        </div>
      </td>
      <td class="td-date">
        <div class="date-container">
          <i class="fas fa-calendar-plus"></i>
          <span>${formatearFecha(usuario.fechaCreacion)}</span>
        </div>
      </td>
      <td class="td-last-access">
        <div class="date-container">
          <i class="fas fa-clock"></i>
          <span>${formatearFecha(usuario.ultimoAcceso)}</span>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// Función para inicializar el formulario de usuarios
function initUsuariosForm() {
  console.log("Inicializando formulario de usuarios...");
  console.log("Elementos del DOM encontrados:");
  
  usuariosCurrentPage = 0;
  const tbody = document.querySelector(".users-table tbody");
  const pageInfo = document.querySelector(".page-info");
  const btnPrev = document.querySelector(".btn-prev");
  const btnNext = document.querySelector(".btn-next");
  const btnFiltrar = document.getElementById("btnFiltrarUsuario");
  const filterInput = document.getElementById("filterUsuarioNombre");
  
  console.log("- tbody:", !!tbody);
  console.log("- pageInfo:", !!pageInfo);
  console.log("- btnPrev:", !!btnPrev);
  console.log("- btnNext:", !!btnNext);
  console.log("- btnFiltrar:", !!btnFiltrar);
  console.log("- filterInput:", !!filterInput);
  
  if (!tbody) {
    console.error("❌ No se encontró el tbody de la tabla de usuarios");
    return;
  }

  function fetchAndRenderUsuarios(page) {
    console.log("Iniciando fetchAndRender para página:", page);
    
    fetch(`/api/usuarios`)
      .then((response) => {
        console.log("Respuesta del servidor:", response.status, response.statusText);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        console.log("Datos recibidos:", data);
        
        // Verificar que data no sea null
        if (!data) {
          throw new Error("No se recibieron datos del servidor");
        }
        
        // El endpoint devuelve una lista simple, no paginada
        const usuarios = Array.isArray(data) ? data : [];
        console.log("Usuarios recibidos:", usuarios);
        
        // Implementar paginación del lado del cliente
        const startIndex = page * usuariosPageSize;
        const endIndex = startIndex + usuariosPageSize;
        const usuariosPaginados = usuarios.slice(startIndex, endIndex);
        const totalPages = Math.ceil(usuarios.length / usuariosPageSize);
        
        // Usar la función renderUsuariosTabla
        if (typeof renderUsuariosTabla === 'function') {
          console.log("Llamando a renderUsuariosTabla...");
          renderUsuariosTabla(usuariosPaginados);
        } else {
          console.error("renderUsuariosTabla no está disponible");
        }
        
        // Actualizar paginación
        const currentPageSpan = document.querySelector('.current-page');
        const totalPagesSpan = document.querySelector('.total-pages');
        
        if (currentPageSpan) currentPageSpan.textContent = page + 1;
        if (totalPagesSpan) totalPagesSpan.textContent = totalPages;
        
        pageInfo.textContent = `Página ${page + 1} de ${totalPages}`;
        btnPrev.disabled = page === 0;
        btnNext.disabled = page + 1 >= totalPages;
        usuariosCurrentPage = page;
        usuariosIsFiltered = false;
      })
      .catch((error) => {
        console.error("Error en fetchAndRender:", error);
        // Mostrar mensaje de error en la tabla
        const tbody = document.querySelector('.users-table tbody');
        if (tbody) {
          tbody.innerHTML = `
            <tr>
              <td colspan="8" style="text-align: center; padding: 20px; color: #dc3545;">
                <i class="fas fa-exclamation-triangle"></i>
                Error al cargar los usuarios: ${error.message}
                <br><small>Intenta recargar la página o contacta al administrador.</small>
              </td>
            </tr>
          `;
        }
        
        // Ocultar paginación en caso de error
        const pagination = document.querySelector('.pagination');
        if (pagination) {
          pagination.style.display = 'none';
        }
      });
  }

  // Función para filtrar por nombre
  async function filtrarUsuariosPorNombre() {
    const nombre = filterInput.value.trim();
    
    if (!nombre) {
      // Si el input está vacío, recargar la paginación normal
      fetchAndRenderUsuarios(usuariosCurrentPage);
      return;
    }

    try {
      // Obtener todos los usuarios y filtrar del lado del cliente
      const response = await fetch(`/api/usuarios`);
      
      if (response.ok) {
        const usuarios = await response.json();
        
        // Filtrar usuarios del lado del cliente
        const usuariosFiltrados = usuarios.filter(usuario => 
          usuario.nombre?.toLowerCase().includes(nombre.toLowerCase()) ||
          usuario.email?.toLowerCase().includes(nombre.toLowerCase()) ||
          usuario.tipoUsuario?.toLowerCase().includes(nombre.toLowerCase())
        );
        
        // Limpiar tabla y mostrar los usuarios encontrados
        if (tbody) {
          tbody.innerHTML = '';
        }
        
        if (usuariosFiltrados.length > 0) {
          renderUsuariosTabla(usuariosFiltrados);
          
          // Actualizar paginación para mostrar resultados filtrados
          if (pageInfo) {
            pageInfo.textContent = `Encontrados ${usuariosFiltrados.length} usuarios con "${nombre}"`;
          }
          if (btnPrev) {
            btnPrev.disabled = true; // No hay paginación en filtros
          }
          if (btnNext) {
            btnNext.disabled = true; // No hay paginación en filtros
          }
          usuariosIsFiltered = true;
          
        } else {
          // No se encontraron usuarios
          if (tbody) {
            tbody.innerHTML = `
              <tr>
                <td colspan="8" style="text-align: center; padding: 20px; color: #6c757d; font-style: italic;">
                  No se encontraron usuarios con el nombre: "${nombre}"
                </td>
              </tr>
            `;
          }
          if (pageInfo) {
            pageInfo.textContent = `No se encontraron usuarios`;
          }
          if (btnPrev) {
            btnPrev.disabled = true;
          }
          if (btnNext) {
            btnNext.disabled = true;
          }
          usuariosIsFiltered = true;
        }
        
      } else {
        // Error en la respuesta
        if (tbody) {
          tbody.innerHTML = `
            <tr>
              <td colspan="8" style="text-align: center; padding: 20px; color: #dc3545; font-style: italic;">
                Error al buscar usuarios. Inténtalo de nuevo.
              </td>
            </tr>
          `;
        }
        if (pageInfo) {
          pageInfo.textContent = `Error en la búsqueda`;
        }
        if (btnPrev) {
          btnPrev.disabled = true;
        }
        if (btnNext) {
          btnNext.disabled = true;
        }
        usuariosIsFiltered = true;
      }
    } catch (error) {
      console.error('Error al filtrar:', error);
      if (tbody) {
        tbody.innerHTML = `
          <tr>
            <td colspan="8" style="text-align: center; padding: 20px; color: #dc3545; font-style: italic;">
              Error al buscar usuarios. Inténtalo de nuevo.
            </td>
          </tr>
        `;
      }
      if (pageInfo) {
        pageInfo.textContent = `Error en la búsqueda`;
      }
      if (btnPrev) {
        btnPrev.disabled = true;
      }
      if (btnNext) {
        btnNext.disabled = true;
      }
      usuariosIsFiltered = true;
    }
  }

  // Event listeners
  if (btnFiltrar) {
    btnFiltrar.addEventListener('click', filtrarUsuariosPorNombre);
  }

  if (filterInput) {
    filterInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        filtrarUsuariosPorNombre();
      }
    });
  }

  if (btnPrev) {
    btnPrev.addEventListener('click', () => {
      if (usuariosCurrentPage > 0 && !usuariosIsFiltered) {
        fetchAndRenderUsuarios(usuariosCurrentPage - 1);
      }
    });
  }

  if (btnNext) {
    btnNext.addEventListener('click', () => {
      if (!usuariosIsFiltered) {
        fetchAndRenderUsuarios(usuariosCurrentPage + 1);
      }
    });
  }

  // Cargar la primera página
  fetchAndRenderUsuarios(usuariosCurrentPage);
}

// Inicializar cuando se carga la vista de usuarios
document.addEventListener('DOMContentLoaded', function() {
  // Verificar si estamos en la vista de usuarios
  if (window.location.hash === '#/clientes' || document.querySelector('.users-table')) {
    console.log('Cargando usuarios...');
    // Pequeño delay para asegurar que el DOM esté completamente cargado
    setTimeout(initUsuariosForm, 100);
  }
});

// Reinicializar cuando cambia la vista (para el router)
window.addEventListener('hashchange', function() {
  if (window.location.hash === '#/clientes') {
    setTimeout(initUsuariosForm, 100); // Pequeño delay para asegurar que el DOM está listo
  }
}); 