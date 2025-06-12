<template>
  <div class="about-container fade-in">
    <!-- 头部信息 -->
    <div class="hero-section slide-in-up">
      <div class="hero-content">
        <div class="logo-section">
          <div class="logo-wrapper">
            <img src="../components/icons/teq.svg" alt="TEQ Logo" class="hero-logo" />
          </div>
          <div class="hero-text">
            <h1 class="hero-title gradient-text">T.E.Q.</h1>
            <p class="hero-subtitle">To Ease Query-algorithm-developers</p>
          </div>
        </div>
        <div class="hero-description">
          <h2 class="description-title">Open and Developer-Friendly Testbed for Edge-Based Query Processing Algorithms</h2>
          <p class="description-text">
            Edge computing is ideally suited for querying populations of fast data streams. However, developing and evaluating edge-based query processing algorithms is challenging due to the decentralized setting and the lack of effective tools. For example, current simulators fail to execute real queries. To rectify these shortcomings, we propose TEQ, a Testbed for Edge-based Query processing algorithms, designed expressly to be open, enabling extensible simulation and algorithm deployment, and to be developer-friendly, simplifying otherwise cumbersome simulation, complex algorithm management, and tedious evaluation.
          </p>
        </div>
      </div>
    </div>

    <!-- 特性介绍 -->
    <div class="features-section slide-in-up" style="animation-delay: 0.2s;">
      <div class="section-header">
        <h2 class="section-title">Core Features</h2>
        <p class="section-subtitle">TEQ provides comprehensive solutions for edge-based query processing</p>
      </div>
      
      <div class="features-grid">
        <div class="feature-card hover-lift">
          <div class="feature-icon simulation-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <h3 class="feature-title">Extensible Simulation</h3>
          <p class="feature-description">
            Supports extensible simulation environments that can simulate real edge computing scenarios,
            providing authentic and reliable testing environments for algorithm development.
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon algorithm-icon">
            <el-icon><Setting /></el-icon>
          </div>
          <h3 class="feature-title">Algorithm Deployment</h3>
          <p class="feature-description">
            Provides simplified algorithm deployment mechanisms, converting decentralized algorithm modules 
            into executable Flink scripts, greatly simplifying the development process.
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon query-icon">
            <el-icon><Search /></el-icon>
          </div>
          <h3 class="feature-title">Real Query Execution</h3>
          <p class="feature-description">
            Unlike traditional simulators, TEQ can execute real query operations,
            providing more accurate and reliable test results.
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon data-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <h3 class="feature-title">Data Playback Control</h3>
          <p class="feature-description">
            Provides data and query playback control functionality, ensuring 
            data stream reproducibility during evaluation and guaranteeing test result consistency.
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon metrics-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <h3 class="feature-title">Metrics Transformation</h3>
          <p class="feature-description">
            Transforms Flink metrics into desired metrics related to user-defined
            edge computing runtime, providing comprehensive performance evaluation.
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon opensource-icon">
            <el-icon><Share /></el-icon>
          </div>
          <h3 class="feature-title">Open Source Collaboration</h3>
          <p class="feature-description">
            TEQ is currently in the open-source process, aiming to attract external contributions
            and drive innovation in edge-based query processing technology.
          </p>
        </div>
      </div>
    </div>

    <!-- 技术架构 -->
    <div class="architecture-section slide-in-up" style="animation-delay: 0.4s;">
      <div class="section-header">
        <h2 class="section-title">Technical Architecture</h2>
        <p class="section-subtitle">Scalable platform built on modern technology stack</p>
      </div>
      
      <div class="tech-stack">
        <div class="tech-category">
          <h4 class="tech-title">Frontend Technologies</h4>
          <div class="tech-items">
            <div class="tech-item">
              <span class="tech-name">Vue.js 3</span>
              <span class="tech-desc">Reactive frontend framework</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Element Plus</span>
              <span class="tech-desc">UI component library</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">ECharts</span>
              <span class="tech-desc">Data visualization</span>
            </div>
          </div>
        </div>

        <div class="tech-category">
          <h4 class="tech-title">Backend Technologies</h4>
          <div class="tech-items">
            <div class="tech-item">
              <span class="tech-name">Apache Flink</span>
              <span class="tech-desc">Stream processing engine</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Spring Boot</span>
              <span class="tech-desc">Backend framework</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Docker</span>
              <span class="tech-desc">Containerized deployment</span>
            </div>
          </div>
        </div>

        <div class="tech-category">
          <h4 class="tech-title">Core Features</h4>
          <div class="tech-items">
            <div class="tech-item">
              <span class="tech-name">Edge Computing</span>
              <span class="tech-desc">Distributed processing</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Stream Processing</span>
              <span class="tech-desc">Real-time data processing</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Algorithm Testing</span>
              <span class="tech-desc">Performance evaluation</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 版本信息 -->
    <div class="version-section slide-in-up" style="animation-delay: 0.6s;">
      <el-card class="version-card hover-lift">
        <div class="version-content">
          <div class="version-info">
            <h3 class="version-title">Version Information</h3>
            <div class="version-details">
              <div class="version-item">
                <span class="version-label">Current Version:</span>
                <el-tag type="primary" size="large">v0.1.0</el-tag>
              </div>
              <div class="version-item">
                <span class="version-label">Release Date:</span>
                <span class="version-value">January 2024</span>
              </div>
              <div class="version-item">
                <span class="version-label">Development Status:</span>
                <el-tag type="warning" size="small">In Development</el-tag>
              </div>
            </div>
          </div>
          <div class="contact-info">
            <h4 class="contact-title">Contact Us</h4>
            <p class="contact-text">
              If you are interested in the TEQ project, please feel free to contact us or participate in open source contributions.
            </p>
            <div class="contact-buttons">
              <el-button type="primary" size="large" @click="openEmail">
                <el-icon><MessageBox /></el-icon>
                Contact Us
              </el-button>
              <el-button type="success" size="large" @click="openGitHub">
                <el-icon><Share /></el-icon>
                Contribute
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
const openGitHub = () => {
  window.open('https://github.com/leiyu-dev/docker-TEQ', '_blank')
}

const openEmail = () => {
  window.open('mailto:devleiyu@gmail.com', '_blank')
}
</script>

<style scoped>
.about-container {
  min-height: 100vh;
  padding: 20px;
}

.hero-section {
  margin-bottom: 60px;
}

.hero-content {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 24px;
  padding: 60px 40px;
  text-align: center;
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
  gap: 30px;
}

.logo-wrapper {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.hero-logo {
  width: 60px;
  height: 60px;
  filter: brightness(0) invert(1);
}

.hero-text {
  text-align: left;
}

.hero-title {
  font-size: 48px;
  font-weight: 900;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 500;
  font-style: italic;
}

.hero-description {
  max-width: 800px;
  margin: 0 auto;
}

.description-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.description-text {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-secondary);
  margin: 0;
  text-align: justify;
}

.features-section,
.architecture-section,
.version-section {
  margin-bottom: 60px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.section-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 30px;
}

.feature-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border-radius: 20px;
  padding: 30px;
  text-align: center;
  border: 1px solid rgba(102, 126, 234, 0.1);
  backdrop-filter: blur(10px);
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 36px;
  color: white;
}

.simulation-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.algorithm-icon {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.query-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.data-icon {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.metrics-icon {
  background: linear-gradient(135deg, #fa709a, #fee140);
}

.opensource-icon {
  background: linear-gradient(135deg, #a8edea, #fed6e3);
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.feature-description {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin: 0;
}

.tech-stack {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.tech-category {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border-radius: 16px;
  padding: 30px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.tech-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  text-align: center;
}

.tech-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tech-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  border-left: 3px solid var(--primary-color);
}

.tech-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.tech-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.version-card {
  border-radius: 20px;
  overflow: hidden;
}

.version-content {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.version-info {
  flex: 1;
}

.version-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
}

.version-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.version-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.version-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.contact-info {
  flex: 1;
}

.contact-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.contact-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin: 0 0 20px 0;
}

.contact-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-container {
    padding: 15px;
  }
  
  .hero-content {
    padding: 40px 20px;
  }
  
  .logo-section {
    flex-direction: column;
    gap: 20px;
  }
  
  .hero-text {
    text-align: center;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .description-title {
    font-size: 24px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .tech-stack {
    grid-template-columns: 1fr;
  }
  
  .version-content {
    flex-direction: column;
    gap: 30px;
  }
  
  .contact-buttons {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .hero-content {
    padding: 30px 15px;
  }
  
  .hero-title {
    font-size: 28px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .feature-card {
    padding: 20px;
  }
  
  .tech-category {
    padding: 20px;
  }
}
</style>
