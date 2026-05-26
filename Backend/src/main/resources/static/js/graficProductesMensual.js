document.addEventListener("DOMContentLoaded", function () {

    const canvas = document.getElementById("graficProductesMensual");

    if (!canvas) {
        return;
    }

    const labelsDiesMes = JSON.parse(canvas.dataset.labels || "[]");
    const dadesVendesDiaries = JSON.parse(canvas.dataset.dades || "[]");
    const labelSerie = canvas.dataset.serie || (document.documentElement.lang === "es" ? "Todos los productos" : "Tots els productes");

    new Chart(canvas, {
        type: "line",
        data: {
            labels: labelsDiesMes,
            datasets: [{
                label: labelSerie,
                data: dadesVendesDiaries,
                backgroundColor: "rgba(139, 94, 52, 0.12)",
                borderColor: "#8B5E34",
                borderWidth: 2,
                pointBackgroundColor: "#8B5E34",
                pointBorderColor: "#8B5E34",
                pointRadius: 3,
                pointHoverRadius: 5,
                tension: 0.25,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,

            layout: {
                padding: {
                    top: 10,
                    right: 12,
                    bottom: 4,
                    left: 4
                }
            },

            plugins: {
                legend: {
                    display: true
                },
                tooltip: {
                    callbacks: {
                        title: function (context) {
                            return canvas.dataset.day + " " + context[0].label;
                        },
                        label: function (context) {
                            return context.dataset.label + ": " + context.parsed.y + " " + canvas.dataset.units;
                        }
                    }
                }
            },

            scales: {
                x: {
                    title: {
                        display: true,
                        text: canvas.dataset.dayAxis
                    },
                    grid: {
                        display: true,
                        color: "rgba(60, 47, 40, 0.10)",
                        lineWidth: 1,
                        drawTicks: true
                    },
                    ticks: {
                        autoSkip: false,
                        padding: 8
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: canvas.dataset.unitsAxis
                    },
                    grid: {
                        color: "rgba(60, 47, 40, 0.10)"
                    },
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });

});
