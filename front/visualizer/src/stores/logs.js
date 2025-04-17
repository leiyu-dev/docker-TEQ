import {defineStore} from "pinia";

export const useLogsStore = defineStore('choose', {
    state: () => ({
        logs: ""
    }),
    actions: {
        pollLogs() {
            setInterval(async () => {
                try {
                    const response = await fetch("http://localhost:8889/log");
                    if (response.ok) {
                        const logData = await response.text();
                        if (logData && logData.trim() !== "") {
                            this.logs += logData
                        }
                    } else {
                        console.error("Error fetching logs: ", response.status);
                    }
                } catch (error) {
                    console.error("Polling Error: ", error);
                }
            }, 2000); // 每1秒轮询一次
        },
    },

});