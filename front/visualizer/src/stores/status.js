import {defineStore} from "pinia";
import {ElMessage} from "element-plus";

export const useStatusStore = defineStore('status', {
    state: () => ({
        status: 'DISCONNECTED',
        algorithms: 0,
        layers: 0,
        nodes: 0,
        cpuUsage: "",
        memoryUsage: "",
        uptime: "",
        connected: false,
        intervalId: null,
    }),
    actions: {
         async fetchStatus(){
            // ask the server for the status every second (GET /status)
            fetch('http://localhost:8889/app/status').then((response) => {
                if (response.status !== 200) {
                    throw Error('Failed to fetch configurations');
                }
                return response.json();
            }).then((data) => {
                this.status = data.status;
                this.algorithms = data.algorithmCount;
                this.layers = data.layerCount;
                this.nodes = data.nodeCount;
                this.cpuUsage = data.cpuUsage;
                this.memoryUsage = data.memoryUsage;
                this.uptime = data.upTime;

            }).catch((error) => {
                this.status = 'DISCONNECTED';
                if(this.connected!==false) {
                    ElMessage.error(error.message);
                    this.connected = false;
                }
            });
        },
        startFetching() {
             console.log('????')
            if (!this.intervalId) {
                this.intervalId = setInterval(() => {
                    this.fetchStatus();
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