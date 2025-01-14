import {defineStore} from "pinia";

export const useChooseStore = defineStore('choose', {
    state: () => ({
        selectedAlgorithm: '',
        selectedLayer: '',
        selectedNode: '',
        selectedConfig: '',
    }),
});