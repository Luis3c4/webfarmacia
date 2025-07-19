function initDashboard() {
    // Line Chart
    const lineCtx = document.getElementById("lineChart").getContext("2d")
    const lineChart = new Chart(lineCtx, {
        type: "line",
        data: {
            labels: ["Jan 1", "", "Jan 7", "", "Jan 14", "", "Jan 21", "", "Jan 28"],
            datasets: [
                {
                    label: "Sessions",
                    data: [20, 40, 15, 35, 25, 50, 10, 30, 20, 45, 15, 35, 25],
                    borderColor: "#1e88e5",
                    backgroundColor: "rgba(30, 136, 229, 0.1)",
                    tension: 0.4,
                    borderWidth: 2,
                    pointRadius: 0,
                },
            ],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    enabled: true,
                },
            },
            scales: {
                y: {
                    min: -20,
                    max: 60,
                    ticks: {
                        stepSize: 20,
                    },
                    grid: {
                        color: "#e5e7eb",
                    },
                },
                x: {
                    grid: {
                        display: false,
                    },
                },
            },
        },
    })

    // Pie Chart
    const pieCtx = document.getElementById("pieChart").getContext("2d")
    const pieChart = new Chart(pieCtx, {
        type: "doughnut",
        data: {
            labels: ["Email", "Referral", "Paid Search", "(Other)", "Direct", "Social", "Display", "Organic Search"],
            datasets: [
                {
                    data: [40, 40, 37, 35, 32, 28, 27, 10],
                    backgroundColor: [
                        "#a5c8d0", // Email
                        "#90caf9", // Referral
                        "#ffe082", // Paid Search
                        "#e6c07b", // Other
                        "#4fc3f7", // Direct
                        "#81c784", // Social
                        "#d4d4d4", // Display
                        "#b9f6ca", // Organic Search
                    ],
                    borderWidth: 0,
                },
            ],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "70%",
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    enabled: true,
                },
            },
        },
    })

    // Añadir texto en el centro del gráfico de donut
    Chart.register({
        id: "centerText",
        beforeDraw: (chart) => {
            if (chart.config.type === "doughnut") {
                const width = chart.width
                const height = chart.height
                const ctx = chart.ctx

                ctx.restore()
                const fontSize = (height / 114).toFixed(2)
                ctx.font = fontSize + "em sans-serif"
                ctx.textBaseline = "middle"

                const text = "249"
                const textX = Math.round((width - ctx.measureText(text).width) / 2)
                const textY = height / 2 - 10

                ctx.fillText(text, textX, textY)

                ctx.font = fontSize * 0.5 + "em sans-serif"
                const subText = "Sessions"
                const subTextX = Math.round((width - ctx.measureText(subText).width) / 2)
                const subTextY = height / 2 + 10

                ctx.fillText(subText, subTextX, subTextY)
                ctx.save()
            }
        },
    })

    // Cargar todas las métricas del dashboard
    cargarMetricasDashboard();
}

// Función para cargar todas las métricas del dashboard
async function cargarMetricasDashboard() {
    console.log('[dashboard] Cargando métricas del dashboard...');
    
    try {
        // Cargar egresos (ya existe en compra.js)
        if (typeof window.cargarTotalPurchaseRevenue === 'function') {
            console.log('[dashboard] Llamando a cargarTotalPurchaseRevenue');
            window.cargarTotalPurchaseRevenue();
        } else {
            console.error('[dashboard] cargarTotalPurchaseRevenue no está definida');
        }

        // Cargar ingresos (ya existe en venta.js)
        if (typeof window.cargarTotalRevenue === 'function') {
            console.log('[dashboard] Llamando a cargarTotalRevenue');
            window.cargarTotalRevenue();
        } else {
            console.error('[dashboard] cargarTotalRevenue no está definida');
        }

        // Cargar total de ventas (ya existe en venta.js)
        if (typeof window.cargarTotalSales === 'function') {
            console.log('[dashboard] Llamando a cargarTotalSales');
            window.cargarTotalSales();
        } else {
            console.error('[dashboard] cargarTotalSales no está definida');
        }

        // Cargar total de compras (ya existe en compra.js)
        if (typeof window.cargarTotalPurchases === 'function') {
            console.log('[dashboard] Llamando a cargarTotalPurchases');
            window.cargarTotalPurchases();
        } else {
            console.error('[dashboard] cargarTotalPurchases no está definida');
        }

        // Cargar total de productos
        await cargarTotalProductos();

        // Cargar total de usuarios
        await cargarTotalUsuarios();

    } catch (error) {
        console.error('[dashboard] Error al cargar métricas:', error);
    }
}

// Función para cargar total de productos
async function cargarTotalProductos() {
    try {
        console.log('[dashboard] Cargando total de productos...');
        const response = await fetch('/api/productos?size=1');
        if (response.ok) {
            const data = await response.json();
            const totalProductos = data.totalElements || 0;
            
            const totalProductsElement = document.getElementById('total-products');
            if (totalProductsElement) {
                totalProductsElement.textContent = formatNumber(totalProductos);
                totalProductsElement.style.color = '#1e293b';
            }
            console.log('[dashboard] Total productos:', totalProductos);
        } else {
            throw new Error(`HTTP ${response.status}`);
        }
    } catch (error) {
        console.error('[dashboard] Error al cargar total de productos:', error);
        const totalProductsElement = document.getElementById('total-products');
        if (totalProductsElement) {
            totalProductsElement.textContent = 'Error';
            totalProductsElement.style.color = '#ef4444';
        }
    }
}

// Función para cargar total de usuarios
async function cargarTotalUsuarios() {
    try {
        console.log('[dashboard] Cargando total de usuarios...');
        const response = await fetch('/api/usuarios');
        if (response.ok) {
            const usuarios = await response.json();
            const totalUsuarios = usuarios.length || 0;
            
            const totalUsersElement = document.getElementById('total-users');
            if (totalUsersElement) {
                totalUsersElement.textContent = formatNumber(totalUsuarios);
                totalUsersElement.style.color = '#1e293b';
            }
            console.log('[dashboard] Total usuarios:', totalUsuarios);
        } else {
            throw new Error(`HTTP ${response.status}`);
        }
    } catch (error) {
        console.error('[dashboard] Error al cargar total de usuarios:', error);
        const totalUsersElement = document.getElementById('total-users');
        if (totalUsersElement) {
            totalUsersElement.textContent = 'Error';
            totalUsersElement.style.color = '#ef4444';
        }
    }
}

// Función para formatear números
function formatNumber(num) {
    return new Intl.NumberFormat('es-PE').format(num || 0);
}

window.initDashboard = initDashboard;