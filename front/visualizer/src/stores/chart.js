import { defineStore } from 'pinia';
import * as echarts from 'echarts';

export const useChartStore = defineStore('chart', {
    state: () => ({
        chartOptions: [],
        xData: [],
        yData: [],
        chartCount: 0,
        chartMap: new Map(),
        chartTitle: [],
        maxPoint: 10,
        intervalId: null,
    }),
    actions: {
        async fetchChartConfig() {
            const response = await fetch('http://localhost:8889/chart');
            const chartList = await response.json();

            chartList.forEach((rawChart) => {
                let seriesList = [];
                this.yData.push([]);
                this.xData.push([]);

                for (let i = 0; i < rawChart.dataLabel.length; i++) {
                    this.yData[this.chartCount].push([]);
                    let dataName = rawChart.dataLabel[i];
                    seriesList.push({
                        type: 'line',
                        data: this.yData[this.chartCount][i],
                        connectNulls: true,
                        name: dataName,
                        // smooth: true,
                    });
                }

                let options = {
                    // dataZoom: [
                    //     {
                    //         type: "slider",
                    //         xAxisIndex: [0],
                    //         start: 0,
                    //         end: 1,
                    //         minSpan: 0,
                    //         maxSpan: 100,
                    //     },
                    // ],
                    animationThreshold: 100,
                    animationDuration: 300,
                    animationEasing: 'linear',
                    grid: {
                        left: '45px',
                        right: '60px',
                        bottom: '3%',
                        containLabel: true,
                    },
                    xAxis: {
                        type: 'category',
                        name: rawChart.xLabel,
                        data: this.xData[this.chartCount],
                        splitLine: {
                            show: false
                        }
                    },
                    tooltip: {
                        axisPointer: {
                            animation: false, //很重要！
                        },
                    },
                    toolbox: {
                        feature: { saveAsImage: {} },
                    },
                    legend: {
                        data: rawChart.dataLabel,
                    },
                    yAxis: {
                        type: 'value',
                        nameLocation: 'center',
                        nameGap: 45,
                        name: rawChart.yLabel,
                    },
                    series: seriesList,
                };

                this.chartOptions.push(options);
                this.chartMap.set(rawChart.title, this.chartCount);
                this.chartTitle.push(rawChart.title);
                this.chartCount++;
            });
        },
        async fetchChartData() {
            const response = await fetch('http://localhost:8889/data');
            const data = await response.json();

            data.forEach((rawData) => {
                const chartIndex = this.chartMap.get(rawData.chartName);
                this.xData[chartIndex].push(rawData.xData);

                while (this.xData[chartIndex].length > this.maxPoint) {
                    this.xData[chartIndex].shift();
                    for (let i = 0; i < this.yData[chartIndex].length; i++) {
                        this.yData[chartIndex][i].shift();
                    }
                }

                rawData.yData.forEach((yData, i) => {
                    this.yData[chartIndex][i].push(yData);
                });
            });
        },
        startFetching() {
            if (!this.intervalId) {
                this.intervalId = setInterval(() => {
                    this.fetchChartData();
                }, 1000);
            }
        },
        stopFetching() {
            if (this.intervalId) {
                clearInterval(this.intervalId);
                this.intervalId = null;
            }
        },
    },
});
