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

// Función para inicializar la vista de ventas
function initVentasView() {
    console.log('Inicializando vista de ventas...');
    cargarTotalProfit();
    cargarTotalRevenue();
    cargarTotalSales();
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