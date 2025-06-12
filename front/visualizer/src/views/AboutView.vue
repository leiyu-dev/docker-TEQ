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
          <h2 class="description-title">开放且开发者友好的边缘计算查询处理算法测试平台</h2>
          <p class="description-text">
            边缘计算非常适合查询快速数据流的群体。然而，虽然边缘计算算法由于其分散性质而难以开发和评估，但缺乏支持工具。
            例如，当前的模拟器无法执行真实查询。为了纠正这些缺点，我们提出了TEQ，一个专门为边缘计算查询处理算法设计的测试平台，
            它被明确设计为开放的，支持可扩展的模拟和算法部署，并且对开发者友好，简化了原本广泛的模拟、复杂的算法管理和繁琐的评估。
          </p>
        </div>
      </div>
    </div>

    <!-- 特性介绍 -->
    <div class="features-section slide-in-up" style="animation-delay: 0.2s;">
      <div class="section-header">
        <h2 class="section-title">核心特性</h2>
        <p class="section-subtitle">TEQ 为边缘计算查询处理提供全面的解决方案</p>
      </div>
      
      <div class="features-grid">
        <div class="feature-card hover-lift">
          <div class="feature-icon simulation-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <h3 class="feature-title">可扩展模拟</h3>
          <p class="feature-description">
            支持可扩展的模拟环境，能够模拟真实的边缘计算场景，
            为算法开发提供真实可靠的测试环境。
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon algorithm-icon">
            <el-icon><Setting /></el-icon>
          </div>
          <h3 class="feature-title">算法部署</h3>
          <p class="feature-description">
            提供简化的算法部署机制，将分散的算法模块转换为可执行的
            Flink 脚本，大大简化开发流程。
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon query-icon">
            <el-icon><Search /></el-icon>
          </div>
          <h3 class="feature-title">真实查询执行</h3>
          <p class="feature-description">
            不同于传统模拟器，TEQ 能够执行真实的查询操作，
            提供更加准确和可靠的测试结果。
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon data-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <h3 class="feature-title">数据回放控制</h3>
          <p class="feature-description">
            提供数据和查询回放控制功能，确保在评估过程中
            数据流的可重现性，保证测试结果的一致性。
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon metrics-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <h3 class="feature-title">指标转换</h3>
          <p class="feature-description">
            将 Flink 指标转换为与用户自定义边缘计算运行时相关的
            所需指标，提供全面的性能评估。
          </p>
        </div>

        <div class="feature-card hover-lift">
          <div class="feature-icon opensource-icon">
            <el-icon><Share /></el-icon>
          </div>
          <h3 class="feature-title">开源协作</h3>
          <p class="feature-description">
            TEQ 正在开源过程中，目标是吸引外部贡献，
            推进基于边缘计算的查询技术发展。
          </p>
        </div>
      </div>
    </div>

    <!-- 技术架构 -->
    <div class="architecture-section slide-in-up" style="animation-delay: 0.4s;">
      <div class="section-header">
        <h2 class="section-title">技术架构</h2>
        <p class="section-subtitle">基于现代化技术栈构建的可扩展平台</p>
      </div>
      
      <div class="tech-stack">
        <div class="tech-category">
          <h4 class="tech-title">前端技术</h4>
          <div class="tech-items">
            <div class="tech-item">
              <span class="tech-name">Vue.js 3</span>
              <span class="tech-desc">响应式前端框架</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Element Plus</span>
              <span class="tech-desc">UI 组件库</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">ECharts</span>
              <span class="tech-desc">数据可视化</span>
            </div>
          </div>
        </div>

        <div class="tech-category">
          <h4 class="tech-title">后端技术</h4>
          <div class="tech-items">
            <div class="tech-item">
              <span class="tech-name">Apache Flink</span>
              <span class="tech-desc">流处理引擎</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Spring Boot</span>
              <span class="tech-desc">后端框架</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Docker</span>
              <span class="tech-desc">容器化部署</span>
            </div>
          </div>
        </div>

        <div class="tech-category">
          <h4 class="tech-title">核心特性</h4>
          <div class="tech-items">
            <div class="tech-item">
              <span class="tech-name">边缘计算</span>
              <span class="tech-desc">分布式处理</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">流处理</span>
              <span class="tech-desc">实时数据处理</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">算法测试</span>
              <span class="tech-desc">性能评估</span>
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
            <h3 class="version-title">版本信息</h3>
            <div class="version-details">
              <div class="version-item">
                <span class="version-label">当前版本：</span>
                <el-tag type="primary" size="large">v0.1.0</el-tag>
              </div>
              <div class="version-item">
                <span class="version-label">发布日期：</span>
                <span class="version-value">2024年1月</span>
              </div>
              <div class="version-item">
                <span class="version-label">开发状态：</span>
                <el-tag type="warning" size="small">开发中</el-tag>
              </div>
            </div>
          </div>
          <div class="contact-info">
            <h4 class="contact-title">联系我们</h4>
            <p class="contact-text">
              如果您对 TEQ 项目感兴趣，欢迎与我们联系或参与到开源贡献中来。
            </p>
            <div class="contact-buttons">
              <el-button type="primary" size="large">
                <el-icon><MessageBox /></el-icon>
                联系我们
              </el-button>
              <el-button type="success" size="large">
                <el-icon><Share /></el-icon>
                参与贡献
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

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
