document.addEventListener("DOMContentLoaded", function () {

    const canvas = document.getElementById("graficProductesMensual");

    if (!canvas) {
        return;
    }

    const labelsProductesMensuals = JSON.parse(canvas.dataset.labels || "[]");
    const datasetsProductesMensuals = JSON.parse(canvas.dataset.datasets || "[]");

    const colorsGrafic = [
        "#8B5E34",
        "#C07A3F",
        "#D9A441",
        "#6F8F72",
        "#7A9E9F",
        "#6D7FA3",
        "#9A7AA0",
        "#B86B6B",
        "#A68A64",
        "#5F6F52"
    ];

    datasetsProductesMensuals.forEach((dataset, index) => {
        const color = colorsGrafic[index % colorsGrafic.length];

        dataset.backgroundColor = color;
        dataset.borderColor = color;
        dataset.borderWidth = 1;
        dataset.borderRadius = 8;
        dataset.maxBarThickness = 46;
    });

    new Chart(canvas, {
        type: "bar",
        data: {
            labels: labelsProductesMensuals,
            datasets: datasetsProductesMensuals
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
                        label: function (context) {
                            return context.dataset.label + ": " + context.parsed.y;
                        }
                    }
                }
            },

            scales: {
                x: {
                    grid: {
                        display: true,
                        color: "rgba(60, 47, 40, 0.18)",
                        lineWidth: 1,
                        drawTicks: true
                    },
                    ticks: {
                        padding: 8
                    }
                },
                y: {
                    beginAtZero: true,
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