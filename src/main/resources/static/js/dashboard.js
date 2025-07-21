function initDashboard() {
    console.log('[dashboard] Inicializando dashboard...');
    
    // Cargar todos los datos del dashboard desde el nuevo endpoint consolidado
    cargarDashboardDataCompleto();
}

// Función principal para cargar todos los datos del dashboard
async function cargarDashboardDataCompleto() {
    try {
        mostrarSkeletonMetricas();
        console.log('[dashboard] Cargando datos completos del dashboard...');
        
        const response = await fetch('/api/dashboard/data');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const dashboardData = await response.json();
        
        if (dashboardData.error) {
            throw new Error(dashboardData.error);
        }
        
        console.log('[dashboard] Datos recibidos:', dashboardData);
        
        // Actualizar métricas principales
        actualizarMetricas(dashboardData);
        
        // Inicializar gráficos con datos reales
        inicializarGraficos(dashboardData);
        
        // Actualizar actividad reciente
        actualizarActividadReciente(dashboardData.recentActivity);
        
        console.log('[dashboard] Dashboard actualizado exitosamente');
        
    } catch (error) {
        console.error('[dashboard] Error al cargar datos del dashboard:', error);
        mostrarErrorDashboard();
    } finally {
        quitarSkeletonMetricas();
    }
}

// Función para actualizar las métricas principales
function actualizarMetricas(dashboardData) {
    // Ingresos
    const totalRevenueElement = document.getElementById('total-revenue');
    if (totalRevenueElement) {
        const revenue = dashboardData.totalRevenue || 0;
        totalRevenueElement.textContent = formatCurrency(revenue);
        totalRevenueElement.style.color = '#1a1a1a';
    }
    
    // Egresos
    const totalExpensesElement = document.getElementById('total-purchase-revenue');
    if (totalExpensesElement) {
        const expenses = dashboardData.totalExpenses || 0;
        totalExpensesElement.textContent = formatCurrency(expenses);
        totalExpensesElement.style.color = '#1a1a1a';
    }
    
    // Productos
    const totalProductsElement = document.getElementById('total-products');
    if (totalProductsElement) {
        const products = dashboardData.totalProducts || 0;
        totalProductsElement.textContent = formatNumber(products);
        totalProductsElement.style.color = '#1a1a1a';
    }
    
    // Usuarios
    const totalUsersElement = document.getElementById('total-users');
    if (totalUsersElement) {
        const users = dashboardData.totalUsers || 0;
        totalUsersElement.textContent = formatNumber(users);
        totalUsersElement.style.color = '#1a1a1a';
    }
    
    // Compras
    const totalPurchasesElement = document.getElementById('total-purchases');
    if (totalPurchasesElement) {
        const purchases = dashboardData.totalPurchases || 0;
        totalPurchasesElement.textContent = formatNumber(purchases);
        totalPurchasesElement.style.color = '#1a1a1a';
    }
    
    // Ventas
    const totalSalesElement = document.getElementById('total-sales');
    if (totalSalesElement) {
        const sales = dashboardData.totalSales || 0;
        totalSalesElement.textContent = formatNumber(sales);
        totalSalesElement.style.color = '#1a1a1a';
    }
}

// Función para inicializar todos los gráficos con datos reales
function inicializarGraficos(dashboardData) {
    // Gráfico de Ingresos vs Egresos
    if (dashboardData.revenueChartData) {
        initRevenueChart(dashboardData.revenueChartData);
    }
    
    // Gráfico de Distribución de Ventas
    if (dashboardData.salesDistributionData) {
        initSalesDistributionChart(dashboardData.salesDistributionData);
    }
    
    // Gráfico de Productos Más Vendidos
    if (dashboardData.productPerformanceData) {
        initProductPerformanceChart(dashboardData.productPerformanceData);
    }
    
    // Gráfico de Tendencias Mensuales
    if (dashboardData.monthlyTrendsData) {
        initMonthlyTrendsChart(dashboardData.monthlyTrendsData);
    }
}

// Gráfico de Ingresos vs Egresos con datos reales
function initRevenueChart(chartData) {
    const ctx = document.getElementById("revenueChart");
    if (!ctx || !chartData.labels) return;
    
    const revenueChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: chartData.labels,
            datasets: [
                {
                    label: "Ingresos",
                    data: chartData.revenue || [],
                    backgroundColor: "#10b981",
                    borderRadius: 8,
                    borderSkipped: false,
                },
                {
                    label: "Egresos",
                    data: chartData.expenses || [],
                    backgroundColor: "#ef4444",
                    borderRadius: 8,
                    borderSkipped: false,
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': S/. ' + context.parsed.y.toLocaleString('es-PE');
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: "#f3f4f6",
                        drawBorder: false,
                    },
                    ticks: {
                        callback: function(value) {
                            return 'S/. ' + value.toLocaleString('es-PE');
                        },
                        color: "#6b7280",
                        font: {
                            size: 12
                        }
                    }
                },
                x: {
                    grid: {
                        display: false,
                    },
                    ticks: {
                        color: "#6b7280",
                        font: {
                            size: 12
                        }
                    }
                }
            }
        }
    });
}

// Gráfico de Distribución de Ventas con datos reales
function initSalesDistributionChart(chartData) {
    const ctx = document.getElementById("salesDistributionChart");
    if (!ctx || !chartData.labels) return;
    
    const salesDistributionChart = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: chartData.labels,
            datasets: [{
                data: chartData.data || [],
                backgroundColor: chartData.colors || [
                    "#3b82f6",
                    "#8b5cf6", 
                    "#06b6d4",
                    "#10b981",
                    "#f59e0b"
                ],
                borderWidth: 0,
                cutout: "70%"
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        font: {
                            size: 12
                        },
                        color: "#6b7280"
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((context.parsed / total) * 100).toFixed(1);
                            return context.label + ': ' + percentage + '%';
                        }
                    }
                }
            }
        }
    });
}

// Gráfico de Productos Más Vendidos con datos reales
function initProductPerformanceChart(chartData) {
    const ctx = document.getElementById("productPerformanceChart");
    if (!ctx || !chartData.labels) return;
    
    const productPerformanceChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: chartData.labels,
            datasets: [{
                label: "Unidades Vendidas",
                data: chartData.data || [],
                backgroundColor: [
                    "#3b82f6",
                    "#8b5cf6",
                    "#06b6d4", 
                    "#10b981",
                    "#f59e0b"
                ],
                borderRadius: 6,
                borderSkipped: false,
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.parsed.x + ' unidades';
                        }
                    }
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    grid: {
                        color: "#f3f4f6",
                        drawBorder: false,
                    },
                    ticks: {
                        color: "#6b7280",
                        font: {
                            size: 12
                        }
                    }
                },
                y: {
                    grid: {
                        display: false,
                    },
                    ticks: {
                        color: "#6b7280",
                        font: {
                            size: 12
                        }
                    }
                }
            }
        }
    });
}

// Gráfico de Tendencias Mensuales con datos reales
function initMonthlyTrendsChart(chartData) {
    const ctx = document.getElementById("monthlyTrendsChart");
    if (!ctx || !chartData.labels) return;
    
    const monthlyTrendsChart = new Chart(ctx, {
        type: "line",
        data: {
            labels: chartData.labels,
            datasets: [{
                label: "Ventas",
                data: chartData.data || [],
                borderColor: "#3b82f6",
                backgroundColor: "rgba(59, 130, 246, 0.1)",
                tension: 0.4,
                borderWidth: 3,
                pointRadius: 6,
                pointBackgroundColor: "#3b82f6",
                pointBorderColor: "#ffffff",
                pointBorderWidth: 2,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Ventas: ' + context.parsed.y + ' unidades';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: "#f3f4f6",
                        drawBorder: false,
                    },
                    ticks: {
                        color: "#6b7280",
                        font: {
                            size: 12
                        }
                    }
                },
                x: {
                    grid: {
                        display: false,
                    },
                    ticks: {
                        color: "#6b7280",
                        font: {
                            size: 12
                        }
                    }
                }
            }
        }
    });
}

// Función para actualizar la actividad reciente con datos reales
function actualizarActividadReciente(activities) {
    const activityList = document.getElementById('recent-activity-list');
    if (!activityList || !activities) return;
    
    activityList.innerHTML = '';
    
    activities.forEach(actividad => {
        const activityItem = document.createElement('div');
        activityItem.className = 'activity-item';
        activityItem.innerHTML = `
            <div class="activity-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    ${getActivityIcon(actividad.icon)}
                </svg>
            </div>
            <div class="activity-content">
                <p class="activity-text">${actividad.text}</p>
                <span class="activity-time">${actividad.time}</span>
            </div>
        `;
        activityList.appendChild(activityItem);
    });
}

// Función para mostrar error en el dashboard
function mostrarErrorDashboard() {
    const metricElements = [
        'total-revenue',
        'total-purchase-revenue', 
        'total-products',
        'total-users',
        'total-purchases',
        'total-sales'
    ];
    
    metricElements.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = 'Error';
            element.style.color = '#ef4444';
        }
    });
    
    // Mostrar mensaje de error en la consola
    console.error('[dashboard] No se pudieron cargar los datos del dashboard');
}

// Función para obtener el icono SVG según el tipo de actividad
function getActivityIcon(icono) {
    const icons = {
        'check-circle': '<path d="M9 12l2 2 4-4"/><path d="M21 12c-1 0-2-1-2-2s1-2 2-2 2 1 2 2-1 2-2 2z"/>',
        'package': '<path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>',
        'user': '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>',
        'shopping-cart': '<circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>'
    };
    return icons[icono] || icons['check-circle'];
}

// Función para formatear números con separadores de miles
function formatNumber(num) {
    return new Intl.NumberFormat('es-PE').format(num || 0);
}

// Función para formatear moneda en soles
function formatCurrency(amount) {
    return `S/.${parseFloat(amount || 0).toFixed(2)}`;
}

// Función para actualizar el dashboard (para compatibilidad con el sistema existente)
async function cargarMetricasDashboard() {
    console.log('[dashboard] Función de compatibilidad llamada');
    await cargarDashboardDataCompleto();
}

// Mostrar skeletons en las tarjetas de métricas
function mostrarSkeletonMetricas() {
    const metricElements = [
        'total-revenue',
        'total-purchase-revenue',
        'total-products',
        'total-users',
        'total-purchases',
        'total-sales'
    ];
    metricElements.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = '';
            element.classList.add('skeleton-metric');
        }
    });
}
// Quitar skeletons de las tarjetas de métricas
function quitarSkeletonMetricas() {
    const metricElements = [
        'total-revenue',
        'total-purchase-revenue',
        'total-products',
        'total-users',
        'total-purchases',
        'total-sales'
    ];
    metricElements.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.classList.remove('skeleton-metric');
        }
    });
}

// Exportar funciones para uso global
window.initDashboard = initDashboard;
window.cargarMetricasDashboard = cargarMetricasDashboard;
window.formatNumber = formatNumber;
window.formatCurrency = formatCurrency;

// Inicialización del dashboard y botón de recarga
window.addEventListener('DOMContentLoaded', function() {
    initDashboard();
    var refreshBtn = document.getElementById('refresh-dashboard-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            cargarDashboardDataCompleto();
        });
    }
});