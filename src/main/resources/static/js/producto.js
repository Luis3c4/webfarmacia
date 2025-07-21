// Variables globales
let currentPage = 1;
const pageSize = 10;
let fetchAndRenderFunction = null;
let isFiltered = false;

// Funciones globales para manejo de imágenes
function handleImageError(img) {
  console.log('Error cargando imagen:', img.src);
  
  // Si ya intentamos con la imagen por defecto, mostrar el ícono
  if (img.src.includes('default-medicine.jpg')) {
    img.style.display = 'none';
    const noImageDiv = img.nextElementSibling;
    if (noImageDiv && noImageDiv.classList.contains('no-image')) {
      noImageDiv.innerHTML = '<i class="fas fa-image"></i>';
      noImageDiv.style.display = 'flex';
    }
  } else {
    // Intentar con la imagen por defecto
    img.src = '/imagenes/productos/default-medicine.jpg';
  }
}

function handleImageLoad(img) {
  console.log('Imagen cargada exitosamente:', img.src);
  img.style.display = 'block';
  const noImageDiv = img.nextElementSibling;
  if (noImageDiv && noImageDiv.classList.contains('no-image')) {
    noImageDiv.style.display = 'none';
  }
}

// Función global para cerrar modal de editar
function closeModalEditar() {
  const modalEditar = document.getElementById("modalEditarProducto");
  const formEditar = document.getElementById("formEditarProducto");
  if (modalEditar) {
    modalEditar.style.display = "none";
  }
  if (formEditar) {
    formEditar.reset();
  }
}

// Función para procesar URL de imagen
function processImageUrl(url) {
  if (!url || url.trim() === '') return '/imagenes/productos/default-medicine.jpg';
  
  // Limpiar la URL
  url = url.trim();
  
  // Si es una URL local, asegurar que tenga el protocolo correcto
  if (url.startsWith('/')) {
    return url;
  }
  
  // Si es una URL externa (Supabase), mantener como está
  if (url.startsWith('http')) {
    return url;
  }
  
  // Si no tiene protocolo, asumir que es local y agregar la ruta base
  if (!url.startsWith('/')) {
    url = '/' + url;
  }
  
  // Si la URL no incluye la ruta completa de imágenes, agregarla
  if (!url.includes('/imagenes/')) {
    url = '/imagenes/productos/' + url.replace(/^\/+/, '');
  }
  
  return url;
}

// Función para crear elemento de imagen optimizado
function createOptimizedImage(producto) {
  let imageUrl = producto.imgUrl && producto.imgUrl.trim() !== ''
    ? processImageUrl(producto.imgUrl)
    : '/imagenes/productos/default-medicine.jpg';

  console.log(`Creando imagen para producto ${producto.nombre}:`, {
    originalUrl: producto.imgUrl,
    processedUrl: imageUrl
  });

  return `
    <img src="${imageUrl}"
         alt="${producto.nombre}"
         class="product-image"
         onload="handleImageLoad(this)"
         onerror="handleImageError(this)"
         loading="lazy">
    <div class="no-image" style="display: flex;">
      <i class="fas fa-spinner fa-spin"></i>
    </div>
  `;
}

// Función para renderizar la tabla de productos optimizada
function renderProductosTabla(productos) {
  const tbody = document.querySelector('.productos-tabla tbody');
  const emptyState = document.getElementById('emptyState');
  
  if (!tbody) {
    console.error('No se encontró el tbody de la tabla');
    return;
  }
  
  // Limpiar tabla
  tbody.innerHTML = '';
  
  if (!productos || productos.length === 0) {
    const tableContainer = document.querySelector('.table-container');
    if (tableContainer) tableContainer.style.display = 'none';
    if (emptyState) emptyState.style.display = 'flex';
    return;
  }
  
  const tableContainer = document.querySelector('.table-container');
  if (tableContainer) tableContainer.style.display = 'block';
  if (emptyState) emptyState.style.display = 'none';
  
  // Crear fragmento para mejor rendimiento
  const fragment = document.createDocumentFragment();
  
  productos.forEach(producto => {
    if (!producto.productoId) {
      console.warn('[RENDER] Producto sin productoId:', producto);
      return; // No renderizar productos sin ID
    }
    if (producto.activo === false) {
      // No mostrar productos inactivos (eliminados)
      return;
    }
    console.log('[RENDER] Producto:', producto);
    const tr = document.createElement('tr');
    tr.className = 'producto-row';
    
    // Determinar la clase de estado del stock
    let stockStatusClass = '';
    let stockStatusText = '';
    
    if (producto.stock === 0) {
      stockStatusClass = 'out-stock';
      stockStatusText = 'Sin stock';
    } else if (producto.stock <= 10) {
      stockStatusClass = 'low-stock';
      stockStatusText = 'Stock bajo';
    } else {
      stockStatusClass = 'in-stock';
      stockStatusText = 'En stock';
    }
    
    // Usar solo producto.productoId como id
    const id = producto.productoId;
    tr.innerHTML = `
      <td class="td-id">${id}</td>
      <td class="td-image">
        <div class="product-image-container">
          ${createOptimizedImage(producto)}
        </div>
      </td>
      <td class="td-name">
        <div class="product-info">
          <div class="product-name">${producto.nombre}</div>
          <div class="product-description">${producto.descripcion || 'Sin descripción'}</div>
        </div>
      </td>
      <td class="td-categoria">
        <div class="categoria-producto">
          <span class="categoria-badge">${producto.categoria || 'Sin categoría'}</span>
        </div>
      </td>
      <td class="td-price">
        <div class="price-container">
          <span class="currency">S/</span>
          <span class="price-value">${parseFloat(producto.precio_unitario).toFixed(2)}</span>
        </div>
      </td>
      <td class="td-stock">
        <div class="stock-badge">
          <i class="fas fa-boxes"></i>
          <span>${producto.stock}</span>
        </div>
      </td>
      <td class="td-status">
        <div class="status-badge ${stockStatusClass}">
          <div class="status-indicator"></div>
          <span>${stockStatusText}</span>
        </div>
      </td>
      <td class="td-actions">
        <div class="action-buttons">
          <button class="btn-editar" data-id="${id}" title="Editar producto">
            <i class="fas fa-edit"></i>
          </button>
          <button class="btn-eliminar-nuevo" data-id="${id}" title="Eliminar producto">
            <i class="fas fa-trash"></i> Eliminar
          </button>
        </div>
      </td>
    `;
    
    fragment.appendChild(tr);
  });
  
  // Agregar todo el fragmento de una vez para mejor rendimiento
  tbody.appendChild(fragment);
}

// Función para inicializar el formulario de productos optimizada
function initProductosForm() {
  console.log("Inicializando formulario de productos...");
  
  currentPage = 0;
  const tbody = document.querySelector(".productos-tabla tbody");
  const pageInfo = document.querySelector(".page-info");
  const btnPrev = document.querySelector(".btn-prev");
  const btnNext = document.querySelector(".btn-next");
  const btnFiltrar = document.getElementById("btnFiltrar");
  const filterInput = document.getElementById("filterById");
  
  if (!tbody) {
    console.error("❌ No se encontró el tbody de la tabla de productos");
    return;
  }

  // Función optimizada para cargar y renderizar productos
  async function fetchAndRender(page) {
    console.log("Iniciando fetchAndRender para página:", page);
    
    try {
      // Mostrar estado de carga
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; padding: 40px;">
            <div style="display: flex; align-items: center; justify-content: center; gap: 1rem;">
              <div style="width: 20px; height: 20px; border: 2px solid #e2e8f0; border-top: 2px solid #3b82f6; border-radius: 50%; animation: spin 1s linear infinite;"></div>
              <span>Cargando productos...</span>
            </div>
          </td>
        </tr>
      `;
      
      const response = await fetch(`/api/productos?page=${page}&size=${pageSize}`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (!data) {
        throw new Error("No se recibieron datos del servidor");
      }
      
      console.log("Datos recibidos:", data);
      
      // Renderizar productos
      renderProductosTabla(data.content || []);
      
      // Actualizar paginación
      const currentPageSpan = document.querySelector('.current-page');
      const totalPagesSpan = document.querySelector('.total-pages');
      
      if (currentPageSpan) currentPageSpan.textContent = (data.number || 0) + 1;
      if (totalPagesSpan) totalPagesSpan.textContent = data.totalPages || 1;
      
      if (pageInfo) pageInfo.textContent = `Página ${(data.number || 0) + 1} de ${data.totalPages || 1}`;
      if (btnPrev) btnPrev.disabled = (data.number || 0) === 0;
      if (btnNext) btnNext.disabled = (data.number || 0) + 1 >= (data.totalPages || 1);
      
      currentPage = data.number || 0;
      isFiltered = false;
      
    } catch (error) {
      console.error("Error en fetchAndRender:", error);
      
      // Mostrar mensaje de error
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; padding: 20px; color: #dc3545;">
            <i class="fas fa-exclamation-triangle"></i>
            Error al cargar los productos: ${error.message}
            <br><small>Intenta recargar la página o contacta al administrador.</small>
          </td>
        </tr>
      `;
      
      // Ocultar paginación en caso de error
      const pagination = document.querySelector('.pagination');
      if (pagination) {
        pagination.style.display = 'none';
      }
    }
  }

  // Configurar eventos de paginación
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

  // Configurar evento de filtrado
  if (btnFiltrar && filterInput) {
    btnFiltrar.addEventListener('click', () => {
      const searchTerm = filterInput.value.trim();
      if (searchTerm) {
        filtrarPorNombre();
      } else {
        fetchAndRender(0);
      }
    });

    // Permitir búsqueda con Enter
    filterInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        const searchTerm = filterInput.value.trim();
        if (searchTerm) {
          filtrarPorNombre();
        } else {
          fetchAndRender(0);
        }
      }
    });
  }

  // Cargar productos iniciales
  fetchAndRender(0);
  
  // Configurar modales
  setupModals();
}

// Función para configurar modales
function setupModals() {
  // Modal de agregar producto
  const modalAgregar = document.getElementById("modalAgregarProducto");
  const btnAgregar = document.querySelector(".btn-agregar");
  const closeBtn = modalAgregar?.querySelector(".close");
  
  if (btnAgregar && modalAgregar) {
    btnAgregar.addEventListener('click', () => {
      modalAgregar.style.display = "block";
      cargarCategoriasEnSelectAgregar();
    });
  }
  
  if (closeBtn && modalAgregar) {
    closeBtn.addEventListener('click', () => {
      modalAgregar.style.display = "none";
    });
  }
  
  // Cerrar modal al hacer clic fuera
  if (modalAgregar) {
    window.addEventListener('click', (event) => {
      if (event.target === modalAgregar) {
        modalAgregar.style.display = "none";
      }
    });
  }

  // Modal de editar producto
  const modalEditar = document.getElementById("modalEditarProducto");
  const closeBtnEditar = modalEditar?.querySelector(".close");
  
  if (closeBtnEditar && modalEditar) {
    closeBtnEditar.addEventListener('click', () => {
      modalEditar.style.display = "none";
    });
  }
  
  // Cerrar modal de editar al hacer clic fuera
  if (modalEditar) {
    window.addEventListener('click', (event) => {
      if (event.target === modalEditar) {
        modalEditar.style.display = "none";
      }
    });
  }

  // Configurar formularios
  setupFormularios();
}

// Función para configurar los formularios
function setupFormularios() {
  // Formulario de agregar producto
  const formAgregar = document.getElementById('formAgregarProducto');
  if (formAgregar) {
    formAgregar.addEventListener('submit', handleSubmitAgregarProducto);
  }

  // Formulario de editar producto
  const formEditar = document.getElementById('formEditarProducto');
  if (formEditar) {
    formEditar.addEventListener('submit', handleSubmitEditarProducto);
  }

  // Eliminar referencias y lógica de cantidadIngresada y stock en formularios y procesamiento
  // (Eliminar líneas que usan cantidadIngresada y stock en formularios de agregar/editar producto)
  // (Eliminar funciones y event listeners relacionados con agregar stock y campos de stock/cantidadIngresada)
  // (Eliminar asignaciones y obtención de valores de cantidadIngresada y stock en edición y creación)
}

// Función para configurar botones de cancelar
function setupBotonesCancelar() {
  // Botón cancelar del modal de agregar producto
  const btnCancelarAgregar = document.querySelector('#modalAgregarProducto .btn-cancelar');
  if (btnCancelarAgregar) {
    btnCancelarAgregar.addEventListener('click', () => {
      const modal = document.getElementById('modalAgregarProducto');
      const form = document.getElementById('formAgregarProducto');
      if (modal) modal.style.display = 'none';
      if (form) form.reset();
      removeImage(); // Limpiar imagen seleccionada
    });
  }

  // Botón cancelar del modal de editar producto
  const btnCancelarEditar = document.querySelector('#modalEditarProducto .btn-cancelar');
  if (btnCancelarEditar) {
    btnCancelarEditar.addEventListener('click', () => {
      const modal = document.getElementById('modalEditarProducto');
      const form = document.getElementById('formEditarProducto');
      if (modal) modal.style.display = 'none';
      if (form) form.reset();
      removeEditImage(); // Limpiar imagen seleccionada
    });
  }

  // Botón cancelar del modal de agregar stock
  const btnCancelarStock = document.querySelector('#modalAgregarStock .btn-cancelar');
  if (btnCancelarStock) {
    btnCancelarStock.addEventListener('click', cerrarModalAgregarStock);
  }
}

// Función para manejar el envío del formulario de agregar producto
async function handleSubmitAgregarProducto(e) {
  e.preventDefault();
  
  try {
    const formData = new FormData(e.target);
    const productoData = {
      nombre: formData.get('nombre'),
      precio_unitario: parseFloat(formData.get('precio')),
      categoria: formData.get('categoria') === '__nueva__' ? formData.get('nuevaCategoria') : formData.get('categoria'),
      descripcion: formData.get('descripcion'),
      fechaProducto: formData.get('fechaProducto') || null,
      descuento: parseFloat(formData.get('descuento')) || 0,
      metodoEntrega: formData.get('metodoEntrega'),
      imgUrl: formData.get('img_url')
    };

    const response = await fetch('/api/productos', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productoData)
    });

    if (response.ok) {
      alert('Producto agregado exitosamente');
      
      // Cerrar modal
      const modal = document.getElementById('modalAgregarProducto');
      if (modal) modal.style.display = 'none';
      
      // Limpiar formulario
      e.target.reset();
      
      // Recargar la tabla
      if (typeof fetchAndRender === 'function') {
        fetchAndRender(currentPage);
      } else {
        location.reload();
      }
    } else {
      const error = await response.text();
      alert('Error al agregar producto: ' + error);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Error al agregar producto');
  }
}

// Función para manejar el envío del formulario de editar producto
async function handleSubmitEditarProducto(e) {
  e.preventDefault();
  
  try {
    const formData = new FormData(e.target);
    const productoId = formData.get('productoId');
    
    const productoData = {
      nombre: formData.get('nombre'),
      precio_unitario: parseFloat(formData.get('precio')),
      categoria: formData.get('categoria') === '__nueva__' ? formData.get('nuevaCategoria') : formData.get('categoria'),
      descripcion: formData.get('descripcion'),
      fechaProducto: formData.get('fechaProducto') || null,
      descuento: parseFloat(formData.get('descuento')) || 0,
      metodoEntrega: formData.get('metodoEntrega'),
      imgUrl: formData.get('img_url')
    };

    const response = await fetch(`/api/productos/${productoId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productoData)
    });

    if (response.ok) {
      alert('Producto actualizado exitosamente');
      
      // Cerrar modal
      const modal = document.getElementById('modalEditarProducto');
      if (modal) modal.style.display = 'none';
      
      // Limpiar formulario
      e.target.reset();
      
      // Recargar la tabla
      if (typeof fetchAndRender === 'function') {
        fetchAndRender(currentPage);
      } else {
        location.reload();
      }
    } else {
      const error = await response.text();
      alert('Error al actualizar producto: ' + error);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Error al actualizar producto');
  }
}

// Función para filtrar por nombre optimizada
async function filtrarPorNombre() {
  const filterInput = document.getElementById("filterById");
  const nombre = filterInput?.value.trim();
  const tbody = document.querySelector('.productos-tabla tbody');
  const pageInfo = document.querySelector(".page-info");
  const btnPrev = document.querySelector(".btn-prev");
  const btnNext = document.querySelector(".btn-next");
  
  if (!nombre) {
    fetchAndRender(0);
    return;
  }

  try {
    // Mostrar estado de carga
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 40px;">
          <div style="display: flex; align-items: center; justify-content: center; gap: 1rem;">
            <div style="width: 20px; height: 20px; border: 2px solid #e2e8f0; border-top: 2px solid #3b82f6; border-radius: 50%; animation: spin 1s linear infinite;"></div>
            <span>Buscando productos...</span>
          </div>
        </td>
      </tr>
    `;

    const response = await fetch(`/api/productos/buscar?nombre=${encodeURIComponent(nombre)}&page=0&size=${pageSize}`);
    
    if (!response.ok) {
      throw new Error(`Error en la búsqueda: ${response.status}`);
    }
    
    const data = await response.json();
    
    if (data && data.content && data.content.length > 0) {
      renderProductosTabla(data.content);
      
      if (pageInfo) pageInfo.textContent = `Encontrados ${data.totalElements || 0} productos con "${nombre}"`;
      if (btnPrev) btnPrev.disabled = true;
      if (btnNext) btnNext.disabled = (data.totalPages || 1) <= 1;
      isFiltered = true;
      
    } else {
      renderProductosTabla([]);
      if (pageInfo) pageInfo.textContent = `No se encontraron productos con "${nombre}"`;
      if (btnPrev) btnPrev.disabled = true;
      if (btnNext) btnNext.disabled = true;
      isFiltered = true;
    }
    
  } catch (error) {
    console.error('Error al filtrar:', error);
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 20px; color: #dc3545;">
          <i class="fas fa-exclamation-triangle"></i>
          Error en la búsqueda: ${error.message}
        </td>
      </tr>
    `;
    if (pageInfo) pageInfo.textContent = `Error en la búsqueda`;
    if (btnPrev) btnPrev.disabled = true;
    if (btnNext) btnNext.disabled = true;
    isFiltered = true;
  }
}

// Función para inicializar el sistema de subida de imágenes
function initImageUpload() {
  const fileInput = document.getElementById('imageFile');
  const uploadArea = document.getElementById('uploadArea');
  const imagePreview = document.getElementById('imagePreview');
  const previewImg = document.getElementById('previewImg');
  const imgUrlInput = document.getElementById('imgUrl');
  const uploadStatus = document.getElementById('uploadStatus');

  if (!fileInput || !uploadArea) return;

  // Configurar drag and drop
  uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('dragover');
  });

  uploadArea.addEventListener('dragleave', () => {
    uploadArea.classList.remove('dragover');
  });

  uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
    const files = e.dataTransfer.files;
    if (files.length > 0) {
      handleImageSelection(files[0], uploadArea, imagePreview, previewImg, imgUrlInput);
    }
  });

  // Configurar click para seleccionar archivo
  uploadArea.addEventListener('click', () => {
    fileInput.click();
  });

  // Configurar cambio de archivo
  fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
      handleImageSelection(e.target.files[0], uploadArea, imagePreview, previewImg, imgUrlInput);
    }
  });
}

// Función para inicializar el sistema de subida de imágenes para el modal de editar
function initImageUploadEdit() {
  const fileInput = document.getElementById('editImageFile');
  const uploadArea = document.getElementById('editUploadArea');
  const imagePreview = document.getElementById('editImagePreview');
  const previewImg = document.getElementById('editPreviewImg');
  const imgUrlInput = document.getElementById('editImgUrl');
  const uploadStatus = document.getElementById('editUploadStatus');

  if (!fileInput || !uploadArea) return;

  // Configurar drag and drop
  uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('dragover');
  });

  uploadArea.addEventListener('dragleave', () => {
    uploadArea.classList.remove('dragover');
  });

  uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
    const files = e.dataTransfer.files;
    if (files.length > 0) {
      handleImageSelection(files[0], uploadArea, imagePreview, previewImg, imgUrlInput);
    }
  });

  // Configurar click para seleccionar archivo
  uploadArea.addEventListener('click', () => {
    fileInput.click();
  });

  // Configurar cambio de archivo
  fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
      handleImageSelection(e.target.files[0], uploadArea, imagePreview, previewImg, imgUrlInput);
    }
  });
}

// Función para manejar la selección de imagen
function handleImageSelection(file, uploadArea, imagePreview, previewImg, imgUrlInput) {
  // Validar tipo de archivo
  if (!file.type.startsWith('image/')) {
    alert('Por favor selecciona un archivo de imagen válido.');
    return;
  }

  // Validar tamaño (5MB)
  if (file.size > 5 * 1024 * 1024) {
    alert('El archivo es demasiado grande. Máximo 5MB.');
    return;
  }

  // Mostrar vista previa
  const reader = new FileReader();
  reader.onload = (e) => {
    previewImg.src = e.target.result;
    uploadArea.style.display = 'none';
    imagePreview.style.display = 'block';
  };
  reader.readAsDataURL(file);

  // Subir imagen
  uploadImage(file, imgUrlInput);
}

// Función para subir imagen optimizada
async function uploadImage(file, imgUrlInput) {
  // Determinar qué uploadStatus usar basado en el imgUrlInput
  let uploadStatus;
  if (imgUrlInput.id === 'editImgUrl') {
    uploadStatus = document.getElementById('editUploadStatus');
  } else {
    uploadStatus = document.getElementById('uploadStatus');
  }
  
  try {
    if (uploadStatus) {
      uploadStatus.style.display = 'flex';
    }
    
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/supabase/upload', {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Error al subir imagen: ${response.status}`);
    }

    // El controlador devuelve directamente la URL como string
    const imageUrl = await response.text();
    
    if (imageUrl && imageUrl.trim() !== '') {
      imgUrlInput.value = imageUrl;
      if (uploadStatus) {
        uploadStatus.innerHTML = '<i class="fas fa-check-circle"></i><span>Imagen subida exitosamente</span>';
        uploadStatus.style.color = '#10b981';
        
        // Ocultar mensaje después de 2 segundos
        setTimeout(() => {
          uploadStatus.style.display = 'none';
        }, 2000);
      }
    } else {
      throw new Error('No se recibió URL de la imagen');
    }
    
  } catch (error) {
    console.error('Error al subir imagen:', error);
    if (uploadStatus) {
      uploadStatus.innerHTML = '<i class="fas fa-exclamation-circle"></i><span>Error al subir imagen</span>';
      uploadStatus.style.color = '#ef4444';
      
      // Ocultar mensaje después de 3 segundos
      setTimeout(() => {
        uploadStatus.style.display = 'none';
      }, 3000);
    }
  }
}

// Funciones para remover imágenes
function removeImage() {
  const uploadArea = document.getElementById('uploadArea');
  const imagePreview = document.getElementById('imagePreview');
  const fileInput = document.getElementById('imageFile');
  const imgUrlInput = document.getElementById('imgUrl');
  
  if (uploadArea) uploadArea.style.display = 'flex';
  if (imagePreview) imagePreview.style.display = 'none';
  if (fileInput) fileInput.value = '';
  if (imgUrlInput) imgUrlInput.value = '';
}

function removeEditImage() {
  const editUploadArea = document.getElementById('editUploadArea');
  const editImagePreview = document.getElementById('editImagePreview');
  const editFileInput = document.getElementById('editImageFile');
  const editImgUrlInput = document.getElementById('editImgUrl');
  
  if (editUploadArea) editUploadArea.style.display = 'flex';
  if (editImagePreview) editImagePreview.style.display = 'none';
  if (editFileInput) editFileInput.value = '';
  if (editImgUrlInput) editImgUrlInput.value = '';
}


// Función para cerrar modal de agregar stock
function cerrarModalAgregarStock() {
  const modal = document.getElementById('modalAgregarStock');
  const form = document.getElementById('formAgregarStock');
  
  if (modal) modal.style.display = 'none';
  if (form) form.reset();
}

// Función para manejar el envío del formulario de agregar stock
async function handleSubmitAgregarStock(e) {
  e.preventDefault();
  
  const productoId = document.getElementById('productoIdStock').value;
  const cantidadAgregar = parseInt(document.getElementById('cantidadAgregar').value);
  
  if (!cantidadAgregar || cantidadAgregar <= 0) {
    alert('Por favor ingresa una cantidad válida.');
    return;
  }
  
  try {
    const response = await fetch(`/api/productos/${productoId}/stock`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ cantidad: cantidadAgregar })
    });
    
    if (response.ok) {
      alert('Stock agregado exitosamente');
      cerrarModalAgregarStock();
      
      // Recargar la tabla
      if (typeof fetchAndRender === 'function') {
        fetchAndRender(currentPage);
      } else {
        location.reload();
      }
    } else {
      const error = await response.text();
      alert('Error al agregar stock: ' + error);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Error al agregar stock');
  }
}

// Inicializar event listeners para el modal de agregar stock
document.addEventListener("DOMContentLoaded", function() {
  const modalAgregarStock = document.getElementById("modalAgregarStock");
  const formAgregarStock = document.getElementById("formAgregarStock");
  
  if (modalAgregarStock) {
    // Cerrar modal con el botón X
    const closeBtn = modalAgregarStock.querySelector(".close");
    if (closeBtn) {
      closeBtn.addEventListener("click", cerrarModalAgregarStock);
    }
    
    // Cerrar modal con el botón Cancelar
    const cancelBtn = modalAgregarStock.querySelector(".btn-cancelar");
    if (cancelBtn) {
      cancelBtn.addEventListener("click", cerrarModalAgregarStock);
    }
    
    // Cerrar modal haciendo clic fuera del contenido
    modalAgregarStock.addEventListener("click", function(e) {
      if (e.target === modalAgregarStock) {
        cerrarModalAgregarStock();
      }
    });
  }
  
  if (formAgregarStock) {
    formAgregarStock.addEventListener("submit", handleSubmitAgregarStock);
  }
});

// Inicializar cuando se carga la vista de productos
document.addEventListener("DOMContentLoaded", function () {
  // Verificar si estamos en la vista de productos
  if (
    window.location.hash === "#/productos" ||
    document.querySelector(".productos-tabla")
  ) {
    console.log("Cargando productos...");
    initProductosForm();
  }
});

// Reinicializar cuando cambia la vista (para el router)
window.addEventListener("hashchange", function () {
  if (window.location.hash === "#/productos") {
    setTimeout(initProductosForm, 100); // Pequeño delay para asegurar que el DOM está listo
  }
});

// Función para cargar categorías en el select de editar
async function cargarCategoriasEnSelectEdit() {
  const select = document.getElementById('editCategoriaSelect');
  if (!select) return;
  
  try {
    const response = await fetch('/api/productos/categorias');
    if (response.ok) {
      const categorias = await response.json();
      
      select.innerHTML = '<option value="">Seleccionar categoría</option>';
      categorias.forEach(categoria => {
        const option = document.createElement('option');
        option.value = categoria;
        option.textContent = categoria;
        select.appendChild(option);
      });
      select.innerHTML += '<option value="__nueva__">Agregar nueva categoría...</option>';
      
      setupNuevaCategoriaInputEdit();
    }
  } catch (error) {
    console.error('Error al cargar categorías:', error);
  }
}

// Función para configurar input de nueva categoría en editar
function setupNuevaCategoriaInputEdit() {
  const select = document.getElementById('editCategoriaSelect');
  const input = document.getElementById('editCategoriaInput');
  
  if (!select || !input) return;
  
  function handleCategoriaChangeEdit() {
    if (select.value === '__nueva__') {
      input.style.display = 'block';
      input.focus();
    } else {
      input.style.display = 'none';
    }
  }
  
  select.addEventListener('change', handleCategoriaChangeEdit);
}

// Función para editar producto optimizada
async function editarProducto(productoId) {
  try {
    const response = await fetch(`/api/productos/${productoId}`);
    
    if (!response.ok) {
      throw new Error(`Error al cargar producto: ${response.status}`);
    }
    
    const producto = await response.json();
    const modal = document.getElementById('modalEditarProducto');
    
    if (!modal) {
      throw new Error('Modal de editar no encontrado');
    }
    
    // Llenar formulario
    document.getElementById('editProductoId').value = producto.productoId;
    document.getElementById('editNombre').value = producto.nombre;
    document.getElementById('editPrecio').value = producto.precio_unitario;
    document.getElementById('editDescripcion').value = producto.descripcion || '';
    document.getElementById('editFechaProducto').value = producto.fechaProducto || '';
    document.getElementById('editDescuento').value = producto.descuento || 0;
    document.getElementById('editMetodoEntrega').value = producto.metodoEntrega || '';
    document.getElementById('editImgUrl').value = producto.imgUrl || '';
    
    // Configurar imagen actual si existe
    if (producto.imgUrl && producto.imgUrl.trim() !== '') {
      const editUploadArea = document.getElementById('editUploadArea');
      const editImagePreview = document.getElementById('editImagePreview');
      const editPreviewImg = document.getElementById('editPreviewImg');
      
      if (editUploadArea && editImagePreview && editPreviewImg) {
        editPreviewImg.src = processImageUrl(producto.imgUrl);
        editUploadArea.style.display = 'none';
        editImagePreview.style.display = 'block';
      }
    } else {
      // Si no hay imagen, mostrar el área de upload
      const editUploadArea = document.getElementById('editUploadArea');
      const editImagePreview = document.getElementById('editImagePreview');
      
      if (editUploadArea && editImagePreview) {
        editUploadArea.style.display = 'flex';
        editImagePreview.style.display = 'none';
      }
    }
    
    // Cargar categorías y seleccionar la correcta
    await cargarCategoriasEnSelectEdit();
    const categoriaSelect = document.getElementById('editCategoriaSelect');
    if (categoriaSelect) {
      categoriaSelect.value = producto.categoria || '';
    }
    
    // Inicializar subida de imágenes para el modal de editar
    initImageUploadEdit();
    
    // Mostrar modal
    modal.style.display = 'block';
    
  } catch (error) {
    console.error('Error al editar producto:', error);
    alert('Error al cargar los datos del producto: ' + error.message);
  }
}

// Función para cargar categorías en el select de agregar
async function cargarCategoriasEnSelectAgregar() {
  const select = document.getElementById('categoriaSelect');
  if (!select) return;
  
  try {
    const response = await fetch('/api/productos/categorias');
    if (response.ok) {
      const categorias = await response.json();
      
      select.innerHTML = '<option value="">Seleccionar categoría</option>';
      categorias.forEach(categoria => {
        const option = document.createElement('option');
        option.value = categoria;
        option.textContent = categoria;
        select.appendChild(option);
      });
      select.innerHTML += '<option value="__nueva__">Agregar nueva categoría...</option>';
      
      setupNuevaCategoriaInputAgregar();
    }
  } catch (error) {
    console.error('Error al cargar categorías:', error);
  }
}

// Función para configurar input de nueva categoría en agregar
function setupNuevaCategoriaInputAgregar() {
  const select = document.getElementById('categoriaSelect');
  const input = document.getElementById('categoriaInput');
  
  if (!select || !input) return;
  
  function handleCategoriaChangeAgregar() {
    if (select.value === '__nueva__') {
      input.style.display = 'block';
      input.focus();
    } else {
      input.style.display = 'none';
    }
  }
  
  select.addEventListener('change', handleCategoriaChangeAgregar);
}

// Función para eliminar producto optimizada
async function eliminarProducto(productoId, skipConfirm = false) {
  console.log('[JS] Intentando eliminar producto', productoId);
  if (!skipConfirm) {
    if (!confirm('¿Estás seguro de que quieres eliminar este producto?')) {
      return;
    }
  }
  try {
    const response = await fetch(`/api/productos/${productoId}`, {
      method: 'DELETE'
    });
    if (response.ok) {
      alert('Producto eliminado exitosamente');
      // Recargar la tabla
      if (typeof fetchAndRender === 'function') {
        fetchAndRender(currentPage);
      } else {
        location.reload();
      }
    } else {
      const error = await response.text();
      alert('Error al eliminar producto: ' + error);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Error al eliminar producto');
  }
}

// Función para mostrar el modal de confirmación de eliminación
function mostrarModalEliminarProducto(productoId) {
  console.log('[MODAL] mostrarModalEliminarProducto llamado con productoId:', productoId);
  const modal = document.getElementById('modalEliminarProducto');
  if (!modal) {
    alert('No se encontró el modal de confirmación de eliminación.');
    return;
  }
  document.getElementById('eliminarProductoId').value = productoId;
  modal.style.display = 'block';
}

// Función para cerrar el modal de eliminación
function cerrarModalEliminarProducto() {
  const modal = document.getElementById('modalEliminarProducto');
  if (modal) modal.style.display = 'none';
  document.getElementById('eliminarProductoId').value = '';
}

// Función para confirmar la eliminación
async function confirmarEliminarProducto() {
  const productoId = document.getElementById('eliminarProductoId').value;
  console.log('[MODAL] confirmarEliminarProducto llamado. Producto a eliminar:', productoId);
  await eliminarProducto(productoId, true);
  cerrarModalEliminarProducto();
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
  // Verificar si estamos en la vista de productos
  if (window.location.hash === '#/productos' || document.querySelector('.productos-tabla')) {
    console.log('Inicializando vista de productos...');
    setTimeout(() => {
      initProductosForm();
      initImageUpload();
    }, 100);
  }
});

// Exportar funciones para uso global
window.ProductosModule = {
  initProductosForm,
  renderProductosTabla,
  filtrarPorNombre,
  editarProducto,
  eliminarProducto,
  cerrarModalAgregarStock,
  handleImageLoad,
  handleImageError,
  closeModalEditar
};
window.cerrarModalEliminarProducto = cerrarModalEliminarProducto;
window.confirmarEliminarProducto = confirmarEliminarProducto;
window.mostrarModalEliminarProducto = mostrarModalEliminarProducto;

// Delegación de eventos para el nuevo botón eliminar
// Elimina producto directamente y actualiza la tabla
// (No usa modal para máxima claridad de prueba)
document.addEventListener('click', async function(e) {
  // Botón eliminar nuevo
  if (e.target.closest('.btn-eliminar-nuevo')) {
    const btn = e.target.closest('.btn-eliminar-nuevo');
    const id = btn.getAttribute('data-id');
    if (!id) return;
    if (!confirm('¿Estás seguro de que quieres eliminar este producto?')) return;
    try {
      const response = await fetch(`/api/productos/${id}`, { method: 'DELETE' });
      if (response.ok) {
        alert('Producto eliminado exitosamente');
        // Recargar la tabla sin el producto eliminado
        if (typeof fetchAndRender === 'function') {
          fetchAndRender(currentPage);
        } else {
          location.reload();
        }
      } else {
        const error = await response.text();
        alert('Error al eliminar producto: ' + error);
      }
    } catch (error) {
      alert('Error al eliminar producto');
      console.error(error);
    }
  }
});




