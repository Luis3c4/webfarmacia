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
}
window.initDashboard=initDashboard;