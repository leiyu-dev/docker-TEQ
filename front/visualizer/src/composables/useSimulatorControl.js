import { ElMessage } from 'element-plus'

export function useSimulatorControl(statusStore) {
  const start = () => {
    if (statusStore.status === 'RUNNING') {
      ElMessage.warning('Simulator is already running')
      return
    }
    if (statusStore.status === 'RESTARTING') {
      ElMessage.warning('Simulator is currently restarting')
      return
    }
    if (statusStore.status === 'DISCONNECTED') {
      ElMessage.warning('Simulator connection is disconnected')
      return
    }
    if (statusStore.status === 'STOPPING') {
      ElMessage.warning('Simulator is currently stopping')
      return
    }

    fetch('http://localhost:8889/start', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    })
      .then(response => {
        if (response.ok) {
          return response.json()
        }
        throw new Error('Network response was not ok')
      })
      .then(data => {
        if (data.code === 0) {
          ElMessage.success('Simulator started successfully')
          statusStore.status = 'RUNNING'
        } else {
          ElMessage.error(data.message)
        }
      })
      .catch(error => {
        ElMessage.error('Start failed: ' + error.message)
      })
  }

  const stop = () => {
    if (statusStore.status === 'STOPPED') {
      ElMessage.warning('Simulator is already stopped')
      return
    }
    if (statusStore.status === 'DISCONNECTED') {
      ElMessage.warning('Simulator connection is disconnected')
      return
    }
    if (statusStore.status === 'RESTARTING') {
      ElMessage.warning('Simulator is currently restarting')
      return
    }
    if (statusStore.status === 'STOPPING') {
      ElMessage.warning('Simulator is currently stopping')
      return
    }

    fetch('http://localhost:8889/stop', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    })
      .then(response => {
        if (response.ok) {
          return response.json()
        }
        throw new Error('Network response was not ok')
      })
      .then(data => {
        if (data.code === 0) {
          ElMessage.success('Simulator stopped')
          statusStore.status = 'STOPPED'
        } else {
          ElMessage.error(data.message)
        }
      })
      .catch(error => {
        ElMessage.error('Stop failed: ' + error.message)
      })
  }

  const restart = () => {
    if (statusStore.status === 'RESTARTING') {
      ElMessage.warning('Simulator is currently restarting')
      return
    }
    if (statusStore.status === 'DISCONNECTED') {
      ElMessage.warning('Simulator connection is disconnected')
      return
    }
    if (statusStore.status === 'STOPPED') {
      ElMessage.warning('Simulator is stopped')
      return
    }
    if (statusStore.status === 'STOPPING') {
      ElMessage.warning('Simulator is currently stopping')
      return
    }

    fetch('http://localhost:8889/restart', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    })
      .then(response => {
        if (response.ok) {
          return response.json()
        }
        throw new Error('Network response was not ok')
      })
      .then(data => {
        if (data.code === 0) {
          ElMessage.success('Simulator restarting')
          statusStore.status = 'RESTARTING'
        } else {
          ElMessage.error(data.message)
        }
      })
      .catch(error => {
        ElMessage.error('Restart failed: ' + error.message)
      })
  }

  return {
    start,
    stop,
    restart
  }
} 