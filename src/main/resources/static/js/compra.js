console.log('[compra.js] cargado');
(function() {
// Configuración global optimizada para compras
const COMPRA_CONFIG = {
  refreshInterval: 30000, // 30 segundos
  currency: 'S/.',
  loadingTimeout: 10000, // 10 segundos
  cacheTimeout: 60000, // 1 minuto de cache
  maxRetries: 3,
  retryDelay: 1000,
  batchSize: 10, // Para paginación
  debounceDelay: 300
};

// Cache para datos de compras
const dataCache = {
  purchaseRevenue: { data: null, timestamp: 0 },
  purchases: { data: null, timestamp: 0 },
  avgPurchase: { data: null, timestamp: 0 },
  compras: { data: null, timestamp: 0 },
  detalles: new Map() // Cache para detalles de compras
};

// Estados de carga con debouncing
let isLoading = {
  purchaseRevenue: false,
  purchases: false,
  avgPurchase: false,
  compras: false
};

// Debounce function para evitar múltiples requests
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Función para verificar si el cache es válido
function isCacheValid(cacheKey) {
  const cache = dataCache[cacheKey];
  return cache && (Date.now() - cache.timestamp) < COMPRA_CONFIG.cacheTimeout;
}

// Función para limpiar cache expirado
function cleanExpiredCache() {
  const now = Date.now();
  Object.keys(dataCache).forEach(key => {
    if (key !== 'detalles') {
      const cache = dataCache[key];
      if (cache && (now - cache.timestamp) > COMPRA_CONFIG.cacheTimeout) {
        dataCache[key] = { data: null, timestamp: 0 };
      }
    }
  });
  
  // Limpiar detalles expirados
  for (const [key, value] of dataCache.detalles.entries()) {
    if (now - value.timestamp > COMPRA_CONFIG.cacheTimeout) {
      dataCache.detalles.delete(key);
    }
  }
}

// Función para hacer requests con retry y timeout
async function fetchWithRetry(url, options = {}, retries = COMPRA_CONFIG.maxRetries) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), COMPRA_CONFIG.loadingTimeout);
  
  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal
    });
    clearTimeout(timeoutId);
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    clearTimeout(timeoutId);
    
    if (retries > 0 && !controller.signal.aborted) {
      console.warn(`Request failed, retrying... (${retries} attempts left)`);
      await new Promise(resolve => setTimeout(resolve, COMPRA_CONFIG.retryDelay));
      return fetchWithRetry(url, options, retries - 1);
    }
    
    throw error;
  }
}

// Función para mostrar estado de carga optimizada
function showLoading(elementId, message = 'Cargando...') {
  const element = document.getElementById(elementId);
  if (element && !element.querySelector('.loading-row')) {
    element.innerHTML = `
      <div class="loading-row">
        <div class="loading-spinner"></div>
        <span>${message}</span>
      </div>
    `;
  }
}

// Función para mostrar estado vacío
function showEmptyState(elementId, icon, title, message) {
  const element = document.getElementById(elementId);
  if (element) {
    element.innerHTML = `
      <div class="empty-state">
        <i class="${icon}"></i>
        <h3>${title}</h3>
        <p>${message}</p>
      </div>
    `;
  }
}

// Función para mostrar error con retry
function showError(elementId, message, retryFunction = null) {
  const element = document.getElementById(elementId);
  if (element) {
    const retryButton = retryFunction ? `
      <button class="retry-button" onclick="this.parentElement.remove(); ${retryFunction}()">
        <i class="fas fa-redo"></i>
        Reintentar
      </button>
    ` : '';
    
    element.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-exclamation-triangle"></i>
        <h3>Error</h3>
        <p>${message}</p>
        ${retryButton}
      </div>
    `;
  }
}

// Función para formatear moneda
function formatCurrency(amount) {
  return `${COMPRA_CONFIG.currency}${parseFloat(amount || 0).toFixed(2)}`;
}

// Función para formatear números
function formatNumber(num) {
  return new Intl.NumberFormat('es-PE').format(num || 0);
}

// Función para formatear porcentaje
function formatPercentage(value) {
  const num = parseFloat(value || 0);
  const sign = num >= 0 ? '+' : '';
  return `${sign}${num.toFixed(1)}%`;
}

// Función para formatear fecha
function formatDate(dateString) {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('es-PE', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// Función para cargar el total de gastos en compras
async function cargarTotalPurchaseRevenue() {
  console.log('[Egresos] Llamando a cargarTotalPurchaseRevenue...');
  if (isLoading.purchaseRevenue) return;
  
  // Verificar cache
  if (isCacheValid('purchaseRevenue')) {
    const totalRevenueElement = document.getElementById('total-purchase-revenue');
    if (totalRevenueElement) {
      totalRevenueElement.textContent = formatCurrency(dataCache.purchaseRevenue.data);
      totalRevenueElement.style.color = '#1e293b';
    }
    console.log('[Egresos] Usando cache:', dataCache.purchaseRevenue.data);
    return;
  }
  
  isLoading.purchaseRevenue = true;
  
  try {
    // Usar el endpoint específico para total revenue
    const totalRevenue = await fetchWithRetry('/api/compras/stats/total-revenue');
    console.log('[Egresos] Respuesta del backend:', totalRevenue);
    
    // Actualizar cache
    dataCache.purchaseRevenue = { data: totalRevenue, timestamp: Date.now() };
    
    const totalRevenueElement = document.getElementById('total-purchase-revenue');
    if (totalRevenueElement) {
      totalRevenueElement.textContent = formatCurrency(totalRevenue);
      totalRevenueElement.style.color = '#1e293b';
    }
  } catch (error) {
    console.error('[Egresos] Error al cargar los gastos totales:', error);
    const totalRevenueElement = document.getElementById('total-purchase-revenue');
    if (totalRevenueElement) {
      totalRevenueElement.textContent = 'Error';
      totalRevenueElement.style.color = '#ef4444';
    }
  } finally {
    isLoading.purchaseRevenue = false;
  }
}

// Función para cargar el total de compras
async function cargarTotalPurchases() {
  if (isLoading.purchases) return;
  
  // Verificar cache
  if (isCacheValid('purchases')) {
    const totalPurchasesElement = document.getElementById('total-purchases');
    if (totalPurchasesElement) {
      totalPurchasesElement.textContent = formatNumber(dataCache.purchases.data);
      totalPurchasesElement.style.color = '#1e293b';
    }
    return;
  }
  
  isLoading.purchases = true;
  
  try {
    const totalCount = await fetchWithRetry('/api/compras/stats/total-count');
    
    // Actualizar cache
    dataCache.purchases = { data: totalCount, timestamp: Date.now() };
    
    const totalPurchasesElement = document.getElementById('total-purchases');
    if (totalPurchasesElement) {
      totalPurchasesElement.textContent = formatNumber(totalCount);
      totalPurchasesElement.style.color = '#1e293b';
    }
  } catch (error) {
    console.error('Error al cargar las compras totales:', error);
    const totalPurchasesElement = document.getElementById('total-purchases');
    if (totalPurchasesElement) {
      totalPurchasesElement.textContent = 'Error';
      totalPurchasesElement.style.color = '#ef4444';
    }
  } finally {
    isLoading.purchases = false;
  }
}

// Función para cargar el promedio por compra
async function cargarAvgPurchase() {
  if (isLoading.avgPurchase) return;
  
  // Verificar cache
  if (isCacheValid('avgPurchase')) {
    const avgPurchaseElement = document.getElementById('avg-purchase');
    if (avgPurchaseElement) {
      avgPurchaseElement.textContent = formatCurrency(dataCache.avgPurchase.data);
      avgPurchaseElement.style.color = '#1e293b';
    }
    return;
  }
  
  isLoading.avgPurchase = true;
  
  try {
    const avgPurchase = await fetchWithRetry('/api/compras/stats/average-purchase');
    
    // Actualizar cache
    dataCache.avgPurchase = { data: avgPurchase, timestamp: Date.now() };
    
    const avgPurchaseElement = document.getElementById('avg-purchase');
    if (avgPurchaseElement) {
      avgPurchaseElement.textContent = formatCurrency(avgPurchase);
      avgPurchaseElement.style.color = '#1e293b';
    }
  } catch (error) {
    console.error('Error al cargar el promedio por compra:', error);
    const avgPurchaseElement = document.getElementById('avg-purchase');
    if (avgPurchaseElement) {
      avgPurchaseElement.textContent = 'Error';
      avgPurchaseElement.style.color = '#ef4444';
    }
  } finally {
    isLoading.avgPurchase = false;
  }
}



// Función para cargar compras recientes
async function cargarComprasRecientes() {
  if (isLoading.compras) return;
  
  // Verificar cache
  if (isCacheValid('compras')) {
    renderComprasRecientes(dataCache.compras.data);
    return;
  }
  
  isLoading.compras = true;
  showLoading('compras-container', 'Cargando compras recientes...');
  
  try {
    // Cambiar endpoint a detalle-compra/recientes
    const detalles = await fetchWithRetry('/api/detalle-compra/recientes?limit=20');
    
    // Actualizar cache
    dataCache.compras = { data: detalles, timestamp: Date.now() };
    
    renderComprasRecientes(detalles);
    
  } catch (error) {
    console.error('Error al cargar compras recientes:', error);
    showError('compras-container', 'Error al cargar compras recientes', 'cargarComprasRecientes');
  } finally {
    isLoading.compras = false;
  }
}

// Función para renderizar compras recientes (ahora por detalle)
function renderComprasRecientes(detalles) {
  const container = document.getElementById('compras-container');
  if (!container) return;
  
  if (!detalles || detalles.length === 0) {
    showEmptyState('compras-container', 'fas fa-shopping-basket', 'No hay compras registradas', 'Aún no se han registrado compras en el sistema.');
    return;
  }
  
  container.innerHTML = detalles.map(detalle => {
    const producto = detalle.producto || {};
    const subtotal = (parseFloat(detalle.costeUnitario || 0) * parseInt(detalle.cantidad || 0)) - parseFloat(detalle.descuentos || 0);
    return `
      <div class="table-row">
        <div class="cell">
          <span style="font-family: monospace; background: #f3f4f6; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600;">
            #${detalle.detalleId || ''}
          </span>
        </div>
        <div class="cell">
          <span style="background: rgba(239, 68, 68, 0.1); color: #ef4444; padding: 0.25rem 0.75rem; border-radius: 12px; font-size: 0.75rem; font-weight: 500;">
            ${producto.nombre || 'Producto N/A'}
          </span>
        </div>
        <div class="cell">
          <span>${detalle.cantidad}</span>
        </div>
        <div class="cell">
          <span>${formatCurrency(detalle.costeUnitario)}</span>
        </div>
        <div class="cell">
          <span>${formatCurrency(detalle.descuentos || 0)}</span>
        </div>
        <div class="cell">
          <span style="font-weight: 600; color: #ef4444;">${formatCurrency(subtotal)}</span>
        </div>
      </div>
    `;
  }).join('');
}

// Función para mostrar detalles de compra
async function mostrarDetallesCompra(compraId) {
  try {
    // Verificar cache
    if (dataCache.detalles.has(compraId)) {
      const cachedData = dataCache.detalles.get(compraId);
      if (Date.now() - cachedData.timestamp < COMPRA_CONFIG.cacheTimeout) {
        renderDetallesCompra(cachedData.data);
        return;
      }
    }
    
    // Obtener compra específica
    const compras = await fetchWithRetry('/api/compras');
    const compra = compras.find(c => c.compraId === compraId);
    
    if (!compra) {
      throw new Error('Compra no encontrada');
    }
    
    // Actualizar cache
    dataCache.detalles.set(compraId, { data: compra, timestamp: Date.now() });
    
    renderDetallesCompra(compra);
    
  } catch (error) {
    console.error('Error al cargar detalles de compra:', error);
    showNotification('Error al cargar detalles de compra', 'error');
  }
}

// Función para renderizar detalles de compra
function renderDetallesCompra(compra) {
  // Actualizar información del modal
  document.getElementById('modal-compra-id').textContent = compra.compraId;
  document.getElementById('modal-proveedor').textContent = `Proveedor ${compra.proveedorId || 'N/A'}`;
  document.getElementById('modal-fecha').textContent = formatDate(compra.fecha);
  document.getElementById('modal-total-productos').textContent = compra.detalles ? compra.detalles.length : 0;
  document.getElementById('modal-estado').textContent = 'Completada';
  document.getElementById('modal-total').textContent = formatCurrency(compra.total);
  
  // Renderizar detalles de productos
  const detallesContainer = document.getElementById('modal-detalles-container');
  if (detallesContainer) {
    if (compra.detalles && compra.detalles.length > 0) {
      detallesContainer.innerHTML = compra.detalles.map(detalle => `
        <div class="table-row">
          <div class="cell">
            <div style="display: flex; align-items: center; gap: 0.75rem;">
              <div style="width: 24px; height: 24px; background: #ef4444; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: white; font-size: 0.625rem;">
                <i class="fas fa-pills"></i>
              </div>
              <span style="font-weight: 500;">${detalle.producto ? detalle.producto.nombre : 'Producto N/A'}</span>
            </div>
          </div>
          <div class="cell">
            <span style="font-weight: 600;">${detalle.cantidad}</span>
          </div>
          <div class="cell">
            <span>${formatCurrency(detalle.costeUnitario)}</span>
          </div>
          <div class="cell">
            <span>${formatCurrency(detalle.descuentos || 0)}</span>
          </div>
          <div class="cell">
            <span style="font-weight: 600; color: #ef4444;">
              ${formatCurrency((parseFloat(detalle.costeUnitario) * detalle.cantidad) - parseFloat(detalle.descuentos || 0))}
            </span>
          </div>
        </div>
      `).join('');
    } else {
      detallesContainer.innerHTML = `
        <div class="table-row">
          <div class="cell" style="grid-column: 1 / -1; text-align: center; color: #6b7280; padding: 2rem;">
            No hay detalles disponibles para esta compra
          </div>
        </div>
      `;
    }
  }
  
  // Mostrar modal
  const modal = document.getElementById('compra-details-modal');
  if (modal) {
    modal.classList.add('show');
  }
}

// Función para cerrar modal
function cerrarModal() {
  const modal = document.getElementById('compra-details-modal');
  if (modal) {
    modal.classList.remove('show');
  }
}

// Función para configurar eventos del modal
function setupModalEvents() {
  const modal = document.getElementById('compra-details-modal');
  const closeBtn = document.getElementById('close-modal');
  
  if (closeBtn) {
    closeBtn.addEventListener('click', cerrarModal);
  }
  
  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        cerrarModal();
      }
    });
  }
}

// Función para cargar todos los datos del dashboard
async function cargarDashboardData() {
  try {
    // Cargar datos en paralelo
    await Promise.all([
      cargarTotalPurchaseRevenue(),
      cargarTotalPurchases(),
      cargarAvgPurchase(),
      cargarComprasRecientes()
    ]);
    
    showNotification('Datos actualizados correctamente', 'success');
    
  } catch (error) {
    console.error('Error al cargar datos del dashboard:', error);
    showNotification('Error al cargar algunos datos', 'error');
  }
}

// Función para actualizar todos los datos
async function actualizarTodosLosDatos() {
  // Limpiar cache
  Object.keys(dataCache).forEach(key => {
    if (key !== 'detalles') {
      dataCache[key] = { data: null, timestamp: 0 };
    }
  });
  dataCache.detalles.clear();
  
  // Limpiar cache expirado
  cleanExpiredCache();
  
  // Recargar datos
  await cargarDashboardData();
}

// Función para configurar botón de refresh
function setupRefreshButton() {
  const refreshBtn = document.getElementById('refresh-data');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', async () => {
      refreshBtn.classList.add('loading');
      try {
        await actualizarTodosLosDatos();
      } finally {
        refreshBtn.classList.remove('loading');
      }
    });
  }
}

// Función para mostrar notificaciones
function showNotification(message, type = 'info') {
  const notification = document.createElement('div');
  notification.className = `notification notification-${type}`;
  notification.innerHTML = `
    <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
    <span>${message}</span>
  `;
  
  document.body.appendChild(notification);
  
  // Mostrar notificación
  setTimeout(() => notification.classList.add('show'), 100);
  
  // Ocultar después de 3 segundos
  setTimeout(() => {
    notification.classList.remove('show');
    setTimeout(() => notification.remove(), 300);
  }, 3000);
}

// Función para inicializar la vista de compras
function initComprasView() {
  console.log('Inicializando vista de compras...');
  
  // Configurar eventos
  setupModalEvents();
  setupRefreshButton();
  setupFormularioAgregarCompra();
  
  // Cargar datos iniciales
  cargarTotalPurchaseRevenue();
  cargarTotalPurchases();
  cargarAvgPurchase();
  cargarComprasRecientes();
  
  // Eliminar actualización automática (setInterval)
  // Eliminar actualización al volver a la vista (document.visibilitychange)
}

// Función para configurar el formulario de agregar compra
function setupFormularioAgregarCompra() {
  const form = document.getElementById('form-agregar-compra');
  if (form) {
    form.addEventListener('submit', handleSubmitAgregarCompra);
  }
  
  // Configurar eventos para cerrar modal al hacer clic fuera
  const modal = document.getElementById('modal-agregar-compra');
  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        cerrarModalAgregarCompra();
      }
    });
  }
}

// Función para verificar si estamos en la vista de compras
function isComprasView() {
  return window.location.hash === '#/compras' || 
         document.querySelector('#compras-container') !== null;
}

// Variables para el modal de agregar compra
let productoIndex = 1;

// Función para abrir modal de agregar compra
function abrirModalAgregarCompra() {
  const modal = document.getElementById('modal-agregar-compra');
  if (modal) {
    // Establecer fecha actual por defecto
    const fechaInput = document.getElementById('fecha-compra');
    if (fechaInput) {
      const now = new Date();
      const localDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
      fechaInput.value = localDateTime;
    }
    
    modal.classList.add('show');
    console.log('Modal de agregar compra abierto');
  }
}

// Función para cerrar modal de agregar compra
function cerrarModalAgregarCompra() {
  const modal = document.getElementById('modal-agregar-compra');
  if (modal) {
    modal.classList.remove('show');
    // Limpiar formulario
    const form = document.getElementById('form-agregar-compra');
    if (form) {
      form.reset();
    }
    // Resetear productos
    resetearProductos();
  }
}

// Función para agregar producto al formulario
function agregarProducto() {
  const container = document.getElementById('productos-container');
  if (!container) return;
  
  const productoItem = document.createElement('div');
  productoItem.className = 'producto-item';
  productoItem.setAttribute('data-index', productoIndex);
  
  productoItem.innerHTML = `
    <div class="producto-header">
      <h4>Producto #${productoIndex + 1}</h4>
      <button type="button" class="btn-eliminar-producto" onclick="eliminarProducto(${productoIndex})" title="Eliminar producto">
        <i class="fas fa-trash"></i>
      </button>
    </div>
    <div class="form-grid">
      <div class="form-group">
        <label for="producto-id-${productoIndex}">Producto ID</label>
        <input type="number" id="producto-id-${productoIndex}" name="productos[${productoIndex}].productoId" required min="1">
      </div>
      <div class="form-group">
        <label for="cantidad-${productoIndex}">Cantidad</label>
        <input type="number" id="cantidad-${productoIndex}" name="productos[${productoIndex}].cantidad" required min="1" onchange="calcularSubtotal(${productoIndex})">
      </div>
      <div class="form-group">
        <label for="costo-unitario-${productoIndex}">Costo Unitario (S/.)</label>
        <input type="number" id="costo-unitario-${productoIndex}" name="productos[${productoIndex}].costeUnitario" required min="0" step="0.01" onchange="calcularSubtotal(${productoIndex})">
      </div>
      <div class="form-group">
        <label for="descuento-${productoIndex}">Descuento (S/.)</label>
        <input type="number" id="descuento-${productoIndex}" name="productos[${productoIndex}].descuentos" min="0" step="0.01" value="0" onchange="calcularSubtotal(${productoIndex})">
      </div>
      <div class="form-group">
        <label>Subtotal</label>
        <div class="subtotal-display" id="subtotal-${productoIndex}">S/.0.00</div>
      </div>
    </div>
  `;
  
  container.appendChild(productoItem);
  productoIndex++;
}

// Función para eliminar producto del formulario
function eliminarProducto(index) {
  const productoItem = document.querySelector(`[data-index="${index}"]`);
  if (productoItem) {
    productoItem.remove();
    calcularTotalCompra();
  }
}

// Función para calcular subtotal de un producto
function calcularSubtotal(index) {
  const cantidad = parseFloat(document.getElementById(`cantidad-${index}`)?.value || 0);
  const costoUnitario = parseFloat(document.getElementById(`costo-unitario-${index}`)?.value || 0);
  const descuento = parseFloat(document.getElementById(`descuento-${index}`)?.value || 0);
  
  const subtotal = (cantidad * costoUnitario) - descuento;
  const subtotalDisplay = document.getElementById(`subtotal-${index}`);
  if (subtotalDisplay) {
    subtotalDisplay.textContent = formatCurrency(subtotal);
  }
  
  calcularTotalCompra();
}

// Función para calcular total de la compra
function calcularTotalCompra() {
  let total = 0;
  const productos = document.querySelectorAll('.producto-item');
  
  productos.forEach(producto => {
    const index = producto.getAttribute('data-index');
    const cantidad = parseFloat(document.getElementById(`cantidad-${index}`)?.value || 0);
    const costoUnitario = parseFloat(document.getElementById(`costo-unitario-${index}`)?.value || 0);
    const descuento = parseFloat(document.getElementById(`descuento-${index}`)?.value || 0);
    
    total += (cantidad * costoUnitario) - descuento;
  });
  
  const totalDisplay = document.getElementById('total-compra');
  if (totalDisplay) {
    totalDisplay.textContent = formatCurrency(total);
  }
}

// Función para resetear productos
function resetearProductos() {
  const container = document.getElementById('productos-container');
  if (container) {
    // Mantener solo el primer producto
    const productos = container.querySelectorAll('.producto-item');
    productos.forEach((producto, index) => {
      if (index > 0) {
        producto.remove();
      }
    });
    
    // Limpiar el primer producto
    const primerProducto = container.querySelector('.producto-item');
    if (primerProducto) {
      const inputs = primerProducto.querySelectorAll('input');
      inputs.forEach(input => {
        if (input.name.includes('descuentos')) {
          input.value = '0';
        } else {
          input.value = '';
        }
      });
      document.getElementById('subtotal-0').textContent = 'S/.0.00';
    }
    
    productoIndex = 1;
    calcularTotalCompra();
  }
}

// Función para manejar el envío del formulario de agregar compra
async function handleSubmitAgregarCompra(e) {
  e.preventDefault();
  
  try {
    const formData = new FormData(e.target);
    const proveedorId = parseInt(formData.get('proveedorId'));
    const fecha = formData.get('fecha');
    
    // Recopilar productos
    const productos = [];
    const productoItems = document.querySelectorAll('.producto-item');
    
    productoItems.forEach((item, index) => {
      const productoId = parseInt(document.getElementById(`producto-id-${index}`)?.value);
      const cantidad = parseInt(document.getElementById(`cantidad-${index}`)?.value);
      const costeUnitario = parseFloat(document.getElementById(`costo-unitario-${index}`)?.value);
      const descuentos = parseFloat(document.getElementById(`descuento-${index}`)?.value || 0);
      
      if (productoId && cantidad && costeUnitario) {
        productos.push({
          productoId,
          cantidad,
          costeUnitario,
          descuentos
        });
      }
    });
    
    if (productos.length === 0) {
      showNotification('Debe agregar al menos un producto', 'error');
      return;
    }
    
    // Calcular total
    const total = productos.reduce((sum, producto) => {
      return sum + (producto.cantidad * producto.costeUnitario) - producto.descuentos;
    }, 0);
    
    const compraData = {
      proveedorId,
      fecha,
      total,
      productos
    };
    
    // Enviar al backend
    const response = await fetch('/api/compras', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(compraData)
    });

    if (response.ok) {
      cerrarModalAgregarCompra();
      showNotification('Compra agregada exitosamente', 'success');
      actualizarTodosLosDatos();
    } else {
      showNotification('Error al guardar la compra', 'error');
    }
  } catch (error) {
    console.error('Error al procesar la compra:', error);
    showNotification('Error al procesar la compra', 'error');
  }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
  if (isComprasView()) {
    console.log('Vista de compras detectada, inicializando...');
    setTimeout(initComprasView, 100);
  }
});

// Reinicializar cuando cambia la vista (para el router)
window.addEventListener('hashchange', function() {
  if (window.location.hash === '#/compras') {
    console.log('Cambio a vista de compras detectado');
    setTimeout(initComprasView, 100);
  }
});

// Exportar funciones para uso global
window.ComprasModule = {
  initComprasView,
  cargarDashboardData,
  actualizarTodosLosDatos,
  mostrarDetallesCompra,
  cerrarModal,
  showNotification,
  abrirModalAgregarCompra,
  cerrarModalAgregarCompra,
  agregarProducto,
  eliminarProducto,
  calcularSubtotal,
  calcularTotalCompra
};
window.initComprasView = initComprasView;
window.abrirModalAgregarCompra = abrirModalAgregarCompra;
window.cerrarModalAgregarCompra = cerrarModalAgregarCompra;
window.agregarProducto = agregarProducto;
window.eliminarProducto = eliminarProducto;
window.calcularSubtotal = calcularSubtotal;
window.calcularTotalCompra = calcularTotalCompra;
console.log('[compra.js] exponiendo cargarTotalPurchaseRevenue:', typeof cargarTotalPurchaseRevenue);
window.cargarTotalPurchaseRevenue = cargarTotalPurchaseRevenue;
window.cargarTotalPurchases = cargarTotalPurchases;
})(); 