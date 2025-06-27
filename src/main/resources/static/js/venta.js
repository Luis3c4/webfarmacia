// Función para cargar el total profit desde el backend
function cargarTotalProfit() {
    fetch('/api/detalle-venta/total-profit')
        .then(response => response.json())
        .then(profit => {
            const totalProfitElement = document.getElementById('total-profit');
            if (totalProfitElement) {
                totalProfitElement.textContent = `S/.${profit.toFixed(2)}`;
            }
        })
        .catch(error => {
            console.error('Error al cargar el total profit:', error);
            const totalProfitElement = document.getElementById('total-profit');
            if (totalProfitElement) {
                totalProfitElement.textContent = 'Error';
            }
        });
}

// Función para cargar el total revenue desde el backend
function cargarTotalRevenue() {
    fetch('/api/detalle-venta/total-revenue')
        .then(response => response.json())
        .then(revenue => {
            const totalRevenueElement = document.getElementById('total-revenue');
            if (totalRevenueElement) {
                totalRevenueElement.textContent = `S/.${revenue.toFixed(2)}`;
            }
        })
        .catch(error => {
            console.error('Error al cargar el total revenue:', error);
            const totalRevenueElement = document.getElementById('total-revenue');
            if (totalRevenueElement) {
                totalRevenueElement.textContent = 'Error';
            }
        });
}

// Función para cargar el total sales desde el backend
function cargarTotalSales() {
    fetch('/api/ventas/total-sales')
        .then(response => response.json())
        .then(sales => {
            const totalSalesElement = document.getElementById('total-sales');
            if (totalSalesElement) {
                totalSalesElement.textContent = sales.toString();
            }
        })
        .catch(error => {
            console.error('Error al cargar el total sales:', error);
            const totalSalesElement = document.getElementById('total-sales');
            if (totalSalesElement) {
                totalSalesElement.textContent = 'Error';
            }
        });
}

// Función para cargar el net purchase value desde el backend
function cargarNetPurchaseValue() {
    fetch('/api/detalle-compra/net-purchase-value')
        .then(response => response.json())
        .then(value => {
            const netPurchaseValueElement = document.getElementById('net-purchase-value');
            if (netPurchaseValueElement) {
                netPurchaseValueElement.textContent = `S/.${value.toFixed(2)}`;
            }
        })
        .catch(error => {
            console.error('Error al cargar el net purchase value:', error);
            const netPurchaseValueElement = document.getElementById('net-purchase-value');
            if (netPurchaseValueElement) {
                netPurchaseValueElement.textContent = 'Error';
            }
        });
}

// Función para cargar el net sales value desde el backend
function cargarNetSalesValue() {
    fetch('/api/detalle-venta/net-sales-value')
        .then(response => response.json())
        .then(value => {
            const netSalesValueElement = document.getElementById('net-sales-value');
            if (netSalesValueElement) {
                netSalesValueElement.textContent = `S/.${value.toFixed(2)}`;
            }
        })
        .catch(error => {
            console.error('Error al cargar el net sales value:', error);
            const netSalesValueElement = document.getElementById('net-sales-value');
            if (netSalesValueElement) {
                netSalesValueElement.textContent = 'Error';
            }
        });
}

// Función para cargar las mejores categorías de ventas
function cargarMejoresCategorias() {
    fetch('/api/detalle-venta/mejores-categorias')
        .then(response => response.json())
        .then(categorias => {
            const container = document.getElementById('categorias-container');
            if (container) {
                container.innerHTML = '';
                console.log(categorias);
                if (categorias && categorias.length > 0) {
                    categorias.forEach(categoria => {
                        const row = document.createElement('div');
                        row.className = 'table-row';
                        
                        const increaseClass = categoria.increaseBy >= 0 ? 'increase-positive' : 'increase-negative';
                        const increaseSign = categoria.increaseBy >= 0 ? '+' : '';
                        
                        row.innerHTML = `
                            <div class="cell">${categoria.category || 'Sin categoría'}</div>
                            <div class="cell">S/.${categoria.turnOver ? categoria.turnOver.toFixed(2) : '0.00'}</div>
                            <div class="cell ${increaseClass}">${increaseSign}${categoria.increaseBy ? categoria.increaseBy.toFixed(1) : '0.0'}%</div>
                        `;
                        container.appendChild(row);
                    });
                } else {
                    // Mostrar mensaje cuando no hay datos
                    const row = document.createElement('div');
                    row.className = 'table-row';
                    row.innerHTML = '<div class="cell" colspan="3" style="text-align: center; padding: 20px;">No hay datos de categorías disponibles</div>';
                    container.appendChild(row);
                }
            }
        })
        .catch(error => {
            console.error('Error al cargar las mejores categorías:', error);
            const container = document.getElementById('categorias-container');
            if (container) {
                container.innerHTML = '<div class="table-row"><div class="cell" colspan="3" style="text-align: center; color: red; padding: 20px;">Error al cargar las categorías</div></div>';
            }
        });
}

// Función para cargar los productos más vendidos
function cargarBestSellingProducts() {
    fetch('/api/detalle-venta/best-selling-products')
        .then(response => response.json())
        .then(productos => {
            const container = document.getElementById('best-products-container');
            if (container) {
                container.innerHTML = '';
                if (productos && productos.length > 0) {
                    productos.forEach(prod => {
                        const row = document.createElement('div');
                        row.className = 'table-row';
                        const increaseClass = prod.increaseBy >= 0 ? 'increase-positive' : 'increase-negative';
                        const increaseSign = prod.increaseBy >= 0 ? '+' : '';
                        row.innerHTML = `
                            <div class="cell">${prod.producto}</div>
                            <div class="cell">${prod.productoId}</div>
                            <div class="cell">${prod.category}</div>
                            <div class="cell">-</div>
                            <div class="cell">S/.${prod.turnOver ? prod.turnOver.toFixed(2) : '0.00'}</div>
                            <div class="cell ${increaseClass}">${increaseSign}${prod.increaseBy ? prod.increaseBy.toFixed(1) : '0.0'}%</div>
                        `;
                        container.appendChild(row);
                    });
                } else {
                    const row = document.createElement('div');
                    row.className = 'table-row';
                    row.innerHTML = '<div class="cell" colspan="6" style="text-align: center; padding: 20px;">No hay datos de productos disponibles</div>';
                    container.appendChild(row);
                }
            }
        })
        .catch(error => {
            console.error('Error al cargar los productos más vendidos:', error);
            const container = document.getElementById('best-products-container');
            if (container) {
                container.innerHTML = '<div class="table-row"><div class="cell" colspan="6" style="text-align: center; color: red; padding: 20px;">Error al cargar los productos</div></div>';
            }
        });
}

// Función para inicializar la vista de ventas
function initVentasView() {
    console.log('Inicializando vista de ventas...');
    cargarTotalProfit();
    cargarTotalRevenue();
    cargarTotalSales();
    cargarNetPurchaseValue();
    cargarNetSalesValue();
    cargarMejoresCategorias();
    cargarBestSellingProducts();
}

// Inicializar cuando se carga la vista de ventas
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si estamos en la vista de ventas
    if (window.location.hash === '#/ventas' || document.querySelector('.overview-section')) {
        console.log('Cargando ventas...');
        initVentasView();
    }
});

// Reinicializar cuando cambia la vista (para el router)
window.addEventListener('hashchange', function() {
    if (window.location.hash === '#/ventas') {
        setTimeout(initVentasView, 100); // Pequeño delay para asegurar que el DOM está listo
    }
});