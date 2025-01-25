import { defineStore } from 'pinia';
import * as echarts from 'echarts';
import axios from "axios";
import {ElMessage} from "element-plus";

export const useChartStore = defineStore('chart', {
    state: () => ({
        chartOptions: [],
        chartOptionsUser: [],
        xData: [],
        yData: [],
        chartCount: 0,
        chartMap: new Map(),
        chartTitle: [],
        chartType: [],
        maxPoint: 10,
        intervalId: null,
        chartOptionsNode: [],
    }),
    actions: {
        async fetchChartConfig() {
            const response = await fetch('http://localhost:8889/chart');
            const chartList = await response.json();

            chartList.forEach((rawChart) => {
                this.addChart(rawChart);
            });
            //如果失败，1s后重试
            if (chartList.length === 0) {
                setTimeout(() => {
                    this.fetchChartConfig();
                }, 1000);
            }
        },
         addChart(rawChart){
            let seriesList = [];
            this.yData.push([]);
            this.xData.push([]);

            for (let i = 0; i < rawChart.dataLabel.length; i++) {
                this.yData[this.chartCount].push([]);
                let dataName = rawChart.dataLabel[i];
                seriesList.push({
                    type: 'line',
                    data: this.yData[this.chartCount][i].slice(-20),
                    connectNulls: true,
                    name: dataName,
                    // smooth: true,
                });
            }

            let option = {
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
                    data: this.xData[this.chartCount].slice(-20),
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

            if(rawChart.type==="overview") {
                this.chartOptions.push({
                    option: option,
                    chartTitle: rawChart.title,
                });
            }
            if(rawChart.type==="user"){
                this.chartOptionsUser.push({
                    option: option,
                    chartTitle: rawChart.title,
                });
                }
            if(rawChart.type==="node") {
                this.chartOptionsNode.push({
                    option: option,
                    chartTitle: rawChart.title,
                });
            }
            this.chartMap.set(rawChart.title, this.chartCount);
            this.chartTitle.push(rawChart.title);
            this.chartType.push(rawChart.type);
            this.chartCount++;
        },
        async fetchChartData() {
            const response = await fetch('http://localhost:8889/data');
            const data = await response.json();

            data.forEach((rawData) => {
                const chartIndex = this.chartMap.get(rawData.chartName);
                this.xData[chartIndex].push(rawData.xData);
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
        async addNode(selectedAlgorithm, selectedLayer, selectedNode) {

        }
    },
});
