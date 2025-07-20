// Configuración global optimizada
const CONFIG = {
  refreshInterval: 30000, // 30 segundos
  currency: 'S/.',
  loadingTimeout: 10000, // 10 segundos
  cacheTimeout: 60000, // 1 minuto de cache
  maxRetries: 3,
  retryDelay: 1000,
  batchSize: 10, // Para paginación
  debounceDelay: 300
};

// Cache para datos
const dataCache = {
  revenue: { data: null, timestamp: 0 },
  sales: { data: null, timestamp: 0 },
  products: { data: null, timestamp: 0 },
  ventas: { data: null, timestamp: 0 },
  detalles: new Map() // Cache para detalles de ventas
};

// Estados de carga con debouncing
let isLoading = {
  revenue: false,
  sales: false,
  products: false,
  ventas: false
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
  return cache && (Date.now() - cache.timestamp) < CONFIG.cacheTimeout;
}

// Función para limpiar cache expirado
function cleanExpiredCache() {
  const now = Date.now();
  Object.keys(dataCache).forEach(key => {
    if (key !== 'detalles') {
      const cache = dataCache[key];
      if (cache && (now - cache.timestamp) > CONFIG.cacheTimeout) {
        dataCache[key] = { data: null, timestamp: 0 };
      }
    }
  });
  
  // Limpiar detalles expirados
  for (const [key, value] of dataCache.detalles.entries()) {
    if (now - value.timestamp > CONFIG.cacheTimeout) {
      dataCache.detalles.delete(key);
    }
  }
}

// Función para hacer requests con retry y timeout
async function fetchWithRetry(url, options = {}, retries = CONFIG.maxRetries) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), CONFIG.loadingTimeout);
  
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
      await new Promise(resolve => setTimeout(resolve, CONFIG.retryDelay));
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
  return `${CONFIG.currency}${parseFloat(amount || 0).toFixed(2)}`;
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

// Función para cargar el total revenue con cache
async function cargarTotalRevenue() {
  if (isLoading.revenue) return;
  
  // Verificar cache
  if (isCacheValid('revenue')) {
    const totalRevenueElement = document.getElementById('total-revenue');
    if (totalRevenueElement) {
      totalRevenueElement.textContent = formatCurrency(dataCache.revenue.data);
      totalRevenueElement.style.color = '#1e293b';
    }
    return;
  }
  
  isLoading.revenue = true;
  
  try {
    const revenue = await fetchWithRetry('/api/detalle-venta/total-revenue');
    
    // Actualizar cache
    dataCache.revenue = { data: revenue, timestamp: Date.now() };
    
    const totalRevenueElement = document.getElementById('total-revenue');
    if (totalRevenueElement) {
      totalRevenueElement.textContent = formatCurrency(revenue);
      totalRevenueElement.style.color = '#1e293b';
    }
  } catch (error) {
    console.error('Error al cargar los ingresos totales:', error);
    const totalRevenueElement = document.getElementById('total-revenue');
    if (totalRevenueElement) {
      totalRevenueElement.textContent = 'Error';
      totalRevenueElement.style.color = '#ef4444';
    }
  } finally {
    isLoading.revenue = false;
  }
}

// Función para cargar el total sales con cache
async function cargarTotalSales() {
  if (isLoading.sales) return;
  
  // Verificar cache
  if (isCacheValid('sales')) {
    const totalSalesElement = document.getElementById('total-sales');
    if (totalSalesElement) {
      totalSalesElement.textContent = formatNumber(dataCache.sales.data);
      totalSalesElement.style.color = '#1e293b';
    }
    return;
  }
  
  isLoading.sales = true;
  
  try {
    const sales = await fetchWithRetry('/api/ventas/total-sales');
    
    // Actualizar cache
    dataCache.sales = { data: sales, timestamp: Date.now() };
    
    const totalSalesElement = document.getElementById('total-sales');
    if (totalSalesElement) {
      totalSalesElement.textContent = formatNumber(sales);
      totalSalesElement.style.color = '#1e293b';
    }
  } catch (error) {
    console.error('Error al cargar las ventas totales:', error);
    const totalSalesElement = document.getElementById('total-sales');
    if (totalSalesElement) {
      totalSalesElement.textContent = 'Error';
      totalSalesElement.style.color = '#ef4444';
    }
  } finally {
    isLoading.sales = false;
  }
}

// Función para cargar los productos más vendidos con cache y lazy loading
async function cargarBestSellingProducts() {
  if (isLoading.products) return;
  
  // Verificar cache
  if (isCacheValid('products')) {
    renderBestSellingProducts(dataCache.products.data);
    return;
  }
  
  isLoading.products = true;
  
  const container = document.getElementById('best-products-container');
  if (!container) return;
  
  showLoading('best-products-container', 'Cargando productos...');
  
  try {
    const productos = await fetchWithRetry('/api/detalle-venta/best-selling-products');
    
    // Actualizar cache
    dataCache.products = { data: productos, timestamp: Date.now() };
    
    renderBestSellingProducts(productos);
  } catch (error) {
    console.error('Error al cargar los productos más vendidos:', error);
    showError(
      'best-products-container',
      'No se pudieron cargar los productos. Intente nuevamente.',
      'cargarBestSellingProducts'
    );
  } finally {
    isLoading.products = false;
  }
}

// Función para renderizar productos más vendidos
function renderBestSellingProducts(productos) {
  const container = document.getElementById('best-products-container');
  if (!container) return;
  
  if (productos && productos.length > 0) {
    // Usar DocumentFragment para mejor rendimiento
    const fragment = document.createDocumentFragment();
    
    productos.forEach((producto, index) => {
      const row = document.createElement('div');
      row.className = 'table-row';
      row.style.animationDelay = `${index * 0.05}s`; // Animación más rápida
      
      const increaseClass = producto.increaseBy >= 0 ? 'increase-positive' : 'increase-negative';
      const increaseSign = producto.increaseBy >= 0 ? '+' : '';
      
      row.innerHTML = `
        <div class="cell">
          <i class="fas fa-pills" style="color: #3b82f6; margin-right: 0.5rem;"></i>
          ${producto.producto || 'Producto sin nombre'}
        </div>
        <div class="cell">
          <i class="fas fa-barcode" style="color: #8b5cf6; margin-right: 0.5rem;"></i>
          ${producto.productoId || 'N/A'}
        </div>
        <div class="cell">
          <i class="fas fa-tag" style="color: #10b981; margin-right: 0.5rem;"></i>
          ${producto.category || 'Sin categoría'}
        </div>
        <div class="cell">
          <i class="fas fa-boxes" style="color: #f59e0b; margin-right: 0.5rem;"></i>
          Stock disponible
        </div>
        <div class="cell">
          <i class="fas fa-chart-line" style="color: #ef4444; margin-right: 0.5rem;"></i>
          <span class="moneda">S/.</span>${formatCurrency(producto.turnOver).replace('S/.', '')}
        </div>
        <div class="cell ${increaseClass}">
          ${increaseSign}${formatPercentage(producto.increaseBy)}
        </div>
      `;
      fragment.appendChild(row);
    });
    
    container.innerHTML = '';
    container.appendChild(fragment);
  } else {
    showEmptyState(
      'best-products-container',
      'fas fa-star',
      'No hay productos disponibles',
      'No se encontraron productos para mostrar.'
    );
  }
}

// Función para cargar las ventas recientes optimizada con cache y paginación
async function cargarVentasRecientes() {
  if (isLoading.ventas) return;
  
  // Verificar cache
  if (isCacheValid('ventas')) {
    renderVentasRecientes(dataCache.ventas.data);
    return;
  }
  
  isLoading.ventas = true;
  
  const container = document.getElementById('ventas-container');
  if (!container) return;
  
  showLoading('ventas-container', 'Cargando ventas...');
  
  try {
    const ventas = await fetchWithRetry('/api/ventas');
    
    // Actualizar cache
    dataCache.ventas = { data: ventas, timestamp: Date.now() };
    
    renderVentasRecientes(ventas);
  } catch (error) {
    console.error('Error al cargar las ventas:', error);
    showError(
      'ventas-container',
      'No se pudieron cargar las ventas. Intente nuevamente.',
      'cargarVentasRecientes'
    );
  } finally {
    isLoading.ventas = false;
  }
}

// Función para renderizar ventas recientes
function renderVentasRecientes(ventas) {
  const container = document.getElementById('ventas-container');
  if (!container) return;
  
  if (ventas && ventas.length > 0) {
    // Mostrar solo las últimas 5 ventas para mayor velocidad
    const ventasRecientes = ventas.slice(-5).reverse();
    
    // Usar DocumentFragment para mejor rendimiento
    const fragment = document.createDocumentFragment();
    
    ventasRecientes.forEach((venta, index) => {
      const row = document.createElement('div');
      row.className = 'table-row';
      row.style.animationDelay = `${index * 0.03}s`; // Animación más rápida
      
      const fecha = new Date(venta.fecha).toLocaleDateString('es-PE', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
      
      row.innerHTML = `
        <div class="cell">
          <i class="fas fa-hashtag" style="color: #3b82f6; margin-right: 0.5rem;"></i>
          ${venta.ventaId}
        </div>
        <div class="cell">
          <i class="fas fa-user" style="color: #8b5cf6; margin-right: 0.5rem;"></i>
          ${venta.clienteNombre || 'Cliente'}
        </div>
        <div class="cell">
          <i class="fas fa-calendar" style="color: #10b981; margin-right: 0.5rem;"></i>
          ${fecha}
        </div>
        <div class="cell">
          <i class="fas fa-credit-card" style="color: #f59e0b; margin-right: 0.5rem;"></i>
          ${venta.metodoPago || 'No especificado'}
        </div>
        <div class="cell">
          <span class="moneda" style="color: #ef4444; margin-right: 0.5rem;">S/.</span>
          ${formatCurrency(venta.total).replace('S/.', '')}
        </div>
        <div class="cell">
          <button class="action-button" onclick="mostrarDetallesVenta(${venta.ventaId})">
            <i class="fas fa-eye"></i>
            Ver Detalles
          </button>
        </div>
      `;
      fragment.appendChild(row);
    });
    
    container.innerHTML = '';
    container.appendChild(fragment);
  } else {
    showEmptyState(
      'ventas-container',
      'fas fa-receipt',
      'No hay ventas disponibles',
      'No se encontraron ventas para mostrar.'
    );
  }
}

// Función para mostrar el popup de detalles de venta con cache
async function mostrarDetallesVenta(ventaId) {
  const modal = document.getElementById('venta-details-modal');
  const modalVentaId = document.getElementById('modal-venta-id');
  const modalCliente = document.getElementById('modal-cliente');
  const modalFecha = document.getElementById('modal-fecha');
  const modalMetodoPago = document.getElementById('modal-metodo-pago');
  const modalEntrega = document.getElementById('modal-entrega');
  const modalTotal = document.getElementById('modal-total');
  const modalDetallesContainer = document.getElementById('modal-detalles-container');
  
  if (!modal) return;
  
  // Mostrar modal con estado de carga
  modal.classList.add('show');
  modalDetallesContainer.innerHTML = `
    <div class="loading-row">
      <div class="loading-spinner"></div>
      <span>Cargando detalles...</span>
    </div>
  `;
  
  try {
    // Verificar cache para detalles
    const cacheKey = `detalles_${ventaId}`;
    let venta, detalles;
    
    if (dataCache.detalles.has(cacheKey)) {
      const cached = dataCache.detalles.get(cacheKey);
      if (Date.now() - cached.timestamp < CONFIG.cacheTimeout) {
        venta = cached.venta;
        detalles = cached.detalles;
      }
    }
    
    // Si no hay cache válido, cargar datos
    if (!venta || !detalles) {
      const [ventaResponse, detallesResponse] = await Promise.all([
        fetchWithRetry(`/api/ventas/${ventaId}`),
        fetchWithRetry(`/api/detalle-venta/venta/${ventaId}`)
      ]);
      
      venta = ventaResponse;
      detalles = detallesResponse;
      
      // Actualizar cache
      dataCache.detalles.set(cacheKey, {
        venta,
        detalles,
        timestamp: Date.now()
      });
    }
    
    // Llenar información de la venta
    modalVentaId.textContent = venta.ventaId;
    modalCliente.textContent = venta.clienteNombre || 'Cliente';
    modalFecha.textContent = new Date(venta.fecha).toLocaleDateString('es-PE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
    modalMetodoPago.textContent = venta.metodoPago || 'No especificado';
    modalEntrega.textContent = venta.metodoEntrega || 'Tienda';
    modalTotal.textContent = formatCurrency(venta.total);
    
    // Llenar detalles de productos
    if (detalles && detalles.length > 0) {
      const fragment = document.createDocumentFragment();
      
      detalles.forEach(detalle => {
        const row = document.createElement('div');
        row.className = 'table-row';
        
        const subtotal = parseFloat(detalle.precioUnitario) * detalle.cantidad;
        
        row.innerHTML = `
          <div class="cell">
            <i class="fas fa-pills" style="color: #3b82f6; margin-right: 0.5rem;"></i>
            ${detalle.productoNombre || 'Producto no encontrado'}
          </div>
          <div class="cell">
            <i class="fas fa-hashtag" style="color: #8b5cf6; margin-right: 0.5rem;"></i>
            ${detalle.cantidad}
          </div>
          <div class="cell">
            <span class="moneda" style="color: #10b981; margin-right: 0.5rem;">S/.</span>
            ${formatCurrency(detalle.precioUnitario).replace('S/.', '')}
          </div>
          <div class="cell">
            <i class="fas fa-calculator" style="color: #f59e0b; margin-right: 0.5rem;"></i>
            <span class="moneda">S/.</span>${formatCurrency(subtotal).replace('S/.', '')}
          </div>
        `;
        fragment.appendChild(row);
      });
      
      modalDetallesContainer.innerHTML = '';
      modalDetallesContainer.appendChild(fragment);
    } else {
      modalDetallesContainer.innerHTML = `
        <div class="empty-state">
          <i class="fas fa-exclamation-triangle"></i>
          <h3>No hay detalles disponibles</h3>
          <p>No se encontraron productos para esta venta.</p>
        </div>
      `;
    }
    
  } catch (error) {
    console.error('Error al cargar los detalles de la venta:', error);
    modalDetallesContainer.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-exclamation-triangle"></i>
        <h3>Error al cargar detalles</h3>
        <p>No se pudieron cargar los detalles de la venta.</p>
        <button class="retry-button" onclick="mostrarDetallesVenta(${ventaId})">
          <i class="fas fa-redo"></i>
          Reintentar
        </button>
      </div>
    `;
  }
}

// Función para cerrar el modal
function cerrarModal() {
  const modal = document.getElementById('venta-details-modal');
  if (modal) {
    modal.classList.remove('show');
  }
}

// Función para cerrar modal al hacer clic fuera de él
function setupModalEvents() {
  const modal = document.getElementById('venta-details-modal');
  const closeButton = document.getElementById('close-modal');
  
  if (closeButton) {
    closeButton.addEventListener('click', cerrarModal);
  }
  
  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        cerrarModal();
      }
    });
    
    // Cerrar con ESC
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && modal.classList.contains('show')) {
        cerrarModal();
      }
    });
  }
}

// Función para cargar todos los datos del dashboard en una sola request
async function cargarDashboardData() {
  try {
    const dashboardData = await fetchWithRetry('/api/ventas/dashboard-data');
    
    if (dashboardData.error) {
      throw new Error(dashboardData.error);
    }
    
    // Actualizar cache con todos los datos
    dataCache.revenue = { data: dashboardData.totalRevenue, timestamp: Date.now() };
    dataCache.sales = { data: dashboardData.totalSales, timestamp: Date.now() };
    dataCache.ventas = { data: dashboardData.recentVentas, timestamp: Date.now() };
    dataCache.products = { data: dashboardData.bestProducts, timestamp: Date.now() };
    
    // Actualizar UI
    const totalRevenueElement = document.getElementById('total-revenue');
    if (totalRevenueElement) {
      totalRevenueElement.textContent = formatCurrency(dashboardData.totalRevenue);
      totalRevenueElement.style.color = '#1e293b';
    }
    
    const totalSalesElement = document.getElementById('total-sales');
    if (totalSalesElement) {
      totalSalesElement.textContent = formatNumber(dashboardData.totalSales);
      totalSalesElement.style.color = '#1e293b';
    }
    
    renderVentasRecientes(dashboardData.recentVentas);
    renderBestSellingProducts(dashboardData.bestProducts);
    
    return dashboardData;
  } catch (error) {
    console.error('Error al cargar datos del dashboard:', error);
    throw error;
  }
}

// Función para actualizar todos los datos optimizada con carga paralela
async function actualizarTodosLosDatos() {
  try {
    // Limpiar cache expirado
    cleanExpiredCache();
    
    // Usar el nuevo endpoint optimizado
    await cargarDashboardData();
    
    // No mostrar notificación aquí, se maneja en setupRefreshButton
  } catch (error) {
    console.error('Error al actualizar los datos:', error);
    
    // Fallback a carga individual si el endpoint optimizado falla
    try {
      console.log('Intentando fallback a carga individual...');
      await Promise.all([
        cargarTotalRevenue(),
        cargarTotalSales(),
        cargarVentasRecientes()
      ]);
      
      setTimeout(() => {
        cargarBestSellingProducts();
      }, 200);
    } catch (fallbackError) {
      console.error('Error en fallback:', fallbackError);
      throw fallbackError; // Re-lanzar el error para que se maneje en setupRefreshButton
    }
  }
}

// Función para actualización rápida (solo ventas)
const actualizarVentasRapido = debounce(async () => {
  try {
    await cargarVentasRecientes();
  } catch (error) {
    console.error('Error al actualizar ventas:', error);
  }
}, CONFIG.debounceDelay);

// Función para manejar el botón de actualización
function setupRefreshButton() {
  const refreshButton = document.getElementById('refresh-data');
  if (refreshButton) {
    // Prevenir múltiples clicks
    let isRefreshing = false;
    
    refreshButton.addEventListener('click', async (event) => {
      event.preventDefault();
      
      // Evitar múltiples clicks simultáneos
      if (isRefreshing) {
        return;
      }
      
      isRefreshing = true;
      
      // Agregar clase de carga
      refreshButton.classList.add('loading');
      refreshButton.disabled = true;
      
      try {
        // Limpiar cache para forzar actualización
        Object.keys(dataCache).forEach(key => {
          if (key !== 'detalles') {
            dataCache[key] = { data: null, timestamp: 0 };
          }
        });
        
        await actualizarTodosLosDatos();
        
        // Mostrar notificación de éxito
        showNotification('Datos actualizados correctamente', 'success');
      } catch (error) {
        console.error('Error al actualizar datos:', error);
        showNotification('Error al actualizar los datos', 'error');
      } finally {
        // Remover clase de carga después de un delay mínimo
        setTimeout(() => {
          refreshButton.classList.remove('loading');
          refreshButton.disabled = false;
          isRefreshing = false;
        }, 800);
      }
    });
    
    // Agregar tooltip mejorado
    refreshButton.setAttribute('title', 'Actualizar datos (Ctrl+R)');
    
    // Agregar atajo de teclado
    document.addEventListener('keydown', (event) => {
      if ((event.ctrlKey || event.metaKey) && event.key === 'r') {
        event.preventDefault();
        refreshButton.click();
      }
    });
  }
}

// Función para mostrar notificación
function showNotification(message, type = 'info') {
  const notification = document.createElement('div');
  notification.className = `notification notification-${type}`;
  notification.innerHTML = `
    <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
    <span>${message}</span>
  `;
  
  document.body.appendChild(notification);
  
  // Animar entrada
  setTimeout(() => notification.classList.add('show'), 100);
  
  // Remover después de 3 segundos
  setTimeout(() => {
    notification.classList.remove('show');
    setTimeout(() => notification.remove(), 300);
  }, 3000);
}

// Función de inicialización optimizada
function initVentasView() {
  console.log('Inicializando vista de ventas optimizada...');
  
  // Verificar que los elementos necesarios existen
  const ventasContainer = document.getElementById('ventas-container');
  const refreshButton = document.getElementById('refresh-data');
  
  if (!ventasContainer) {
    console.error('No se encontró el contenedor de ventas');
    return;
  }
  
  if (!refreshButton) {
    console.error('No se encontró el botón de refresh');
    return;
  }
  
  // Limpiar cache expirado al inicio
  cleanExpiredCache();
  
  // Configurar botón de refresh primero
  setupRefreshButton();
  
  // Cargar todos los datos usando el endpoint optimizado
  cargarDashboardData().catch(error => {
    console.error('Error al cargar datos iniciales:', error);
    // Fallback a carga individual
    cargarVentasRecientes();
    cargarTotalRevenue();
    cargarTotalSales();
    setTimeout(() => {
      cargarBestSellingProducts();
    }, 500);
  });
  
  // Configurar eventos del modal
  setupModalEvents();
  
  // Configurar refresh automático optimizado
  setInterval(actualizarVentasRapido, 15000); // Cada 15 segundos
  setInterval(() => {
    cleanExpiredCache();
    actualizarTodosLosDatos();
  }, 60000); // Cada minuto completo
  
  // Limpiar cache cada 5 minutos
  setInterval(cleanExpiredCache, 300000);
  
  console.log('Vista de ventas optimizada inicializada correctamente');
}

// Función para verificar si estamos en la vista de ventas
function isVentasView() {
  return document.getElementById('ventas-container') !== null;
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
  console.log('DOM cargado, verificando vista de ventas...');
  if (isVentasView()) {
    console.log('Vista de ventas detectada, inicializando...');
    initVentasView();
  } else {
    console.log('No es vista de ventas');
  }
});

// También inicializar inmediatamente si el DOM ya está listo
if (document.readyState !== 'loading') {
  console.log('DOM ya está listo, verificando vista de ventas...');
  if (isVentasView()) {
    console.log('Vista de ventas detectada, inicializando...');
    initVentasView();
  }
}

// Exportar funciones para uso global
window.VentasModule = {
  actualizarTodosLosDatos,
  cargarTotalRevenue,
  cargarTotalSales,
  cargarBestSellingProducts,
  cargarVentasRecientes,
  mostrarDetallesVenta,
  showNotification,
  setupRefreshButton,
  cleanExpiredCache
};

window.cargarTotalRevenue = cargarTotalRevenue;
window.cargarTotalSales = cargarTotalSales;