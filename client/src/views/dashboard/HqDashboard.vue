<template>
  <div class="hq-dashboard">
    <!-- 헤더 -->
    <div class="dashboard-header">
      <h1 class="dashboard-title">올리브영 본사 SCM 대시보드</h1>
      <div class="last-updated">
        마지막 업데이트: {{ lastUpdated }}
      </div>
    </div>

    <!-- KPI 카드들 -->
    <div class="kpi-section">
      <div class="kpi-grid">
        <div class="kpi-card sales">
          <div class="kpi-icon">💰</div>
          <div class="kpi-content">
            <h3>월간 총 매출액</h3>
            <div class="kpi-value">{{ kpiData.totalSales || '로딩 중...' }}</div>
            <div class="kpi-change positive">{{ kpiData.salesGrowth || '계산 중...' }}</div>
          </div>
        </div>

        <div class="kpi-card inventory">
          <div class="kpi-icon">🔄</div>
          <div class="kpi-content">
            <h3>전체 재고 회전율</h3>
            <div class="kpi-value">{{ kpiData.inventoryTurnover || '로딩 중...' }}</div>
            <div class="kpi-change positive">{{ kpiData.turnoverChange || '계산 중...' }}</div>
          </div>
        </div>

        <div class="kpi-card delivery">
          <div class="kpi-icon">🚚</div>
          <div class="kpi-content">
            <h3>공급업체 납기준수율</h3>
            <div class="kpi-value">{{ kpiData.deliveryRate || '로딩 중...' }}</div>
            <div class="kpi-change negative">{{ kpiData.deliveryRateChange || '계산 중...' }}</div>
          </div>
        </div>

        <div class="kpi-card stockout">
          <div class="kpi-icon">📦</div>
          <div class="kpi-content">
            <h3>품절률</h3>
            <div class="kpi-value">{{ kpiData.stockoutRate || '로딩 중...' }}</div>
            <div class="kpi-change positive">{{ kpiData.stockoutRateChange || '계산 중...' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 차트 섹션 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 매출 트렌드 차트 -->
        <div class="chart-card">
          <h3>카테고리별 매출 트렌드</h3>
          <div class="chart-container">
            <canvas ref="salesTrendChart"></canvas>
          </div>
        </div>

        <!-- 카테고리별 매출 구성 -->
        <div class="chart-card">
          <h3>카테고리별 매출 구성</h3>
          <div class="chart-container">
            <canvas ref="categorySalesChart"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- 상세 정보 섹션 -->
    <div class="details-section">
      <div class="details-grid">
        <!-- 상위 공급업체 -->
        <div class="detail-card">
          <h3>상위 공급업체 성과 TOP 5</h3>
          <div class="suppliers-list">
            <div v-for="supplier in topSuppliers" :key="supplier.supplier_name" class="supplier-item">
              <div class="supplier-info">
                <div class="supplier-name">{{ supplier.supplier_name }}</div>
                <div class="supplier-revenue">{{ formatCurrency(supplier.revenue) }}</div>
              </div>
              <div class="supplier-metrics">
                <span class="metric">납기: {{ supplier.delivery_rate }}%</span>
                <span class="metric">품질: {{ supplier.quality_score }}점</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 재고 현황 -->
        <div class="detail-card">
          <h3>재고 현황</h3>
          <div class="inventory-stats">
            <div class="stat-item">
              <span class="stat-label">총 품목 수</span>
              <span class="stat-value">{{ inventoryData.totalItems || '로딩 중...' }}{{ inventoryData.totalItems ? '개' : '' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">안전재고 미달</span>
              <span class="stat-value warning">{{ inventoryData.lowStockItems || '로딩 중...' }}{{ inventoryData.lowStockItems ? '개' : '' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">품절 품목</span>
              <span class="stat-value danger">{{ inventoryData.stockoutItems || '로딩 중...' }}{{ inventoryData.stockoutItems ? '개' : '' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">총 재고 가치</span>
              <span class="stat-value">{{ inventoryData.totalValue || '로딩 중...' }}</span>
            </div>
          </div>
        </div>

        <!-- 긴급 알림 -->
        <div class="detail-card alerts">
          <h3>긴급 알림</h3>
          <div class="alerts-list">
            <div v-for="alert in alerts" :key="alert.id" :class="['alert-item', alert.priority.toLowerCase()]">
              <div class="alert-icon">⚠️</div>
              <div class="alert-content">
                <div class="alert-title">{{ alert.title }}</div>
                <div class="alert-message">{{ alert.message }}</div>
                <div class="alert-time">{{ formatTime(alert.created_at) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  LineController,
  DoughnutController
} from 'chart.js'

// Chart.js 컴포넌트 등록 (컨트롤러 포함)
Chart.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  LineController,
  DoughnutController
)

export default {
  name: 'HqDashboard',
  setup() {
    // 반응형 데이터
    const kpiData = ref({})
    const topSuppliers = ref([])
    const inventoryData = ref({})
    const alerts = ref([])
    const lastUpdated = ref('')
    
    // 차트 참조
    const salesTrendChart = ref(null)
    const categorySalesChart = ref(null)
    
    // 차트 인스턴스
    let trendChartInstance = null
    let categoryChartInstance = null

    // 매출 트렌드 차트 생성
    const createSalesTrendChart = (trendData) => {
      if (!salesTrendChart.value || !trendData) return

      try {
        const ctx = salesTrendChart.value.getContext('2d')
        
        // 기존 차트 파괴
        if (trendChartInstance) {
          trendChartInstance.destroy()
        }

        // 차트 데이터 변환
        const datasets = trendData.map((item, index) => {
          const colors = ['#48bb78', '#4299e1', '#ed8936', '#f56565', '#9f7aea', '#38b2ac']
          return {
            label: item.category,
            data: item.data,
            borderColor: colors[index % colors.length],
            backgroundColor: colors[index % colors.length] + '20',
            tension: 0.4,
            fill: false
          }
        })

        trendChartInstance = new Chart(ctx, {
          type: 'line',
          data: {
            labels: ['2024-02', '2024-03', '2024-04', '2024-05', '2024-06', '2024-07', '2025-08'],
            datasets: datasets
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              title: {
                display: false
              },
              legend: {
                position: 'bottom'
              },
              tooltip: {
                mode: 'index',
                intersect: false,
                callbacks: {
                  label: function(context) {
                    return context.dataset.label + ': ' + context.parsed.y + '억원'
                  }
                }
              }
            },
            scales: {
              x: {
                display: true,
                title: {
                  display: true,
                  text: '월'
                }
              },
              y: {
                display: true,
                title: {
                  display: true,
                  text: '매출액 (억원)'
                },
                beginAtZero: true
              }
            },
            interaction: {
              mode: 'nearest',
              axis: 'x',
              intersect: false
            }
          }
        })
      } catch (error) {
        console.error('라인 차트 생성 실패:', error)
      }
    }

    // 카테고리별 매출 구성 차트 생성
    const createCategorySalesChart = (categoryData) => {
      if (!categorySalesChart.value || !categoryData) return

      try {
        const ctx = categorySalesChart.value.getContext('2d')
        
        // 기존 차트 파괴
        if (categoryChartInstance) {
          categoryChartInstance.destroy()
        }

        const labels = categoryData.map(item => item.category)
        const data = categoryData.map(item => item.sales)

        categoryChartInstance = new Chart(ctx, {
          type: 'doughnut',
          data: {
            labels: labels,
            datasets: [{
              data: data,
              backgroundColor: [
                '#48bb78',
                '#4299e1',
                '#ed8936',
                '#f56565',
                '#9f7aea'
              ],
              borderWidth: 2,
              borderColor: '#ffffff'
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              title: {
                display: false
              },
              legend: {
                position: 'bottom'
              },
              tooltip: {
                callbacks: {
                  label: function(context) {
                    const total = context.dataset.data.reduce((a, b) => a + b, 0)
                    const percentage = total > 0 ? ((context.parsed / total) * 100).toFixed(1) : '0.0'
                    return context.label + ': ' + context.parsed + '억원 (' + percentage + '%)'
                  }
                }
              }
            }
          }
        })
      } catch (error) {
        console.error('도넛 차트 생성 실패:', error)
      }
    }

    // 실제 API 호출 함수들
    const fetchKpiData = async () => {
      try {
        const response = await fetch('http://localhost:3049/api/dashboard/hq/kpi')
        const data = await response.json()
        console.log('KPI API 응답:', data)
        
        if (data && Object.keys(data).length > 0) {
          kpiData.value = data
          lastUpdated.value = new Date().toLocaleString('ko-KR')
          console.log('KPI 데이터 업데이트 완료')
        }
      } catch (error) {
        console.error('KPI 데이터 로딩 실패:', error)
      }
    }

    const fetchSalesTrend = async () => {
      try {
        const response = await fetch('http://localhost:3049/api/dashboard/hq/sales-trend')
        const data = await response.json()
        console.log('매출 트렌드 API 응답:', data)
        
        if (data && data.length > 0) {
          // API 데이터를 차트 형식으로 변환
          const processedData = processSalesTrendData(data)
          createSalesTrendChart(processedData)
          console.log('매출 트렌드 차트 업데이트 완료')
        }
      } catch (error) {
        console.error('매출 트렌드 데이터 로딩 실패:', error)
      }
    }

    const fetchCategorySales = async () => {
      try {
        const response = await fetch('http://localhost:3049/api/dashboard/hq/category-sales')
        const data = await response.json()
        console.log('카테고리 매출 API 응답:', data)
        
        if (data && data.length > 0) {
          // API 데이터를 차트 형식으로 변환
          const processedData = data.map(item => ({
            category: item.category,
            sales: Math.round(item.sales / 100000000) // 원을 억원으로 변환
          }))
          createCategorySalesChart(processedData)
          console.log('카테고리 매출 차트 업데이트 완료')
        }
      } catch (error) {
        console.error('카테고리 매출 데이터 로딩 실패:', error)
      }
    }

    const fetchTopSuppliers = async () => {
      try {
        const response = await fetch('http://localhost:3049/api/dashboard/hq/suppliers')
        const data = await response.json()
        console.log('공급업체 API 응답:', data)
        
        if (data && data.length > 0) {
          topSuppliers.value = data
          console.log('공급업체 데이터 업데이트 완료')
        }
      } catch (error) {
        console.error('공급업체 데이터 로딩 실패:', error)
      }
    }

    const fetchInventoryData = async () => {
      try {
        const response = await fetch('http://localhost:3049/api/dashboard/hq/inventory')
        const data = await response.json()
        console.log('재고 API 응답:', data)
        
        if (data && Object.keys(data).length > 0) {
          // 숫자 형식 변환
          inventoryData.value = {
            totalItems: data.totalItems || 0,
            lowStockItems: data.lowStockItems || 0,
            stockoutItems: data.stockoutItems || 0,
            totalValue: data.totalInventoryValue ? formatCurrency(data.totalInventoryValue) : '0억원'
          }
          console.log('재고 데이터 업데이트 완료')
        }
      } catch (error) {
        console.error('재고 데이터 로딩 실패:', error)
      }
    }

    const fetchAlerts = async () => {
      try {
        const response = await fetch('http://localhost:3049/api/dashboard/hq/alerts')
        const data = await response.json()
        console.log('알림 API 응답:', data)
        
        if (data && data.length > 0) {
          alerts.value = data.map(alert => ({
            ...alert,
            created_at: new Date(alert.created_at)
          }))
          console.log('알림 데이터 업데이트 완료')
        }
      } catch (error) {
        console.error('알림 데이터 로딩 실패:', error)
      }
    }

    // 매출 트렌드 데이터 처리 함수
    const processSalesTrendData = (apiData) => {
      // 카테고리 코드를 실제 이름으로 매핑
      const categoryMapping = {
        '110001': '스킨케어',
        '110002': '메이크업', 
        '110003': '헤어케어',
        '110004': '바디케어',
        '110005': '향수',
        '110006': '기타'
      }
      
      // API 데이터를 카테고리별로 그룹화
      const categoryMap = {}
      
      apiData.forEach(item => {
        const categoryCode = item.CATEGORY_NAME
        const categoryName = categoryMapping[categoryCode] || categoryCode
        const month = item.MONTH
        const sales = Math.round(item.SALES / 100000000) // 원을 억원으로 변환
        
        if (!categoryMap[categoryName]) {
          categoryMap[categoryName] = {}
        }
        categoryMap[categoryName][month] = sales
      })
      
      // 차트 형식으로 변환
      const months = ['2024-02', '2024-03', '2024-04', '2024-05', '2024-06', '2024-07', '2025-08']
      return Object.keys(categoryMap).map(category => ({
        category: category,
        data: months.map(month => categoryMap[category][month] || 0)
      }))
    }

    // 유틸리티 함수들
    const formatCurrency = (amount) => {
      if (typeof amount === 'number') {
        return (amount / 100000000).toFixed(1) + '억원'
      }
      return amount
    }

    const formatTime = (date) => {
      if (date instanceof Date) {
        return date.toLocaleString('ko-KR')
      }
      return new Date(date).toLocaleString('ko-KR')
    }

    // 컴포넌트 마운트 시 데이터 로딩
    onMounted(async () => {
      console.log('대시보드 컴포넌트 마운트 시작 - 실제 데이터만 로딩')
      
      // DOM이 완전히 렌더링된 후 API 호출
      await nextTick()
      
      // 모든 데이터를 실제 API에서 로딩
      console.log('실제 API 호출 시작')
      
      try {
        // 모든 API를 병렬로 호출하여 속도 향상
        await Promise.all([
          fetchKpiData(),
          fetchInventoryData(), 
          fetchTopSuppliers(),
          fetchAlerts()
        ])
        
        // 차트 데이터는 순차적으로 (데이터 처리 필요)
        await fetchSalesTrend()
        await fetchCategorySales()
        
        console.log('모든 API 호출 완료')
        lastUpdated.value = new Date().toLocaleString('ko-KR')
        
      } catch (error) {
        console.error('API 호출 중 오류 발생:', error)
      }
    })

    return {
      kpiData,
      topSuppliers,
      inventoryData,
      alerts,
      lastUpdated,
      salesTrendChart,
      categorySalesChart,
      formatCurrency,
      formatTime
    }
  }
}
</script>

<style scoped>
.hq-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px 0;
}

.dashboard-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
}

.last-updated {
  color: #718096;
  font-size: 14px;
}

/* KPI 카드 스타일 */
.kpi-section {
  margin-bottom: 40px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.kpi-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.kpi-card.sales {
  border-left: 4px solid #48bb78;
}

.kpi-card.inventory {
  border-left: 4px solid #4299e1;
}

.kpi-card.delivery {
  border-left: 4px solid #ed8936;
}

.kpi-card.stockout {
  border-left: 4px solid #f56565;
}

.kpi-icon {
  font-size: 32px;
  margin-right: 16px;
}

.kpi-content h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #718096;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 4px;
}

.kpi-change {
  font-size: 14px;
  font-weight: 600;
}

.kpi-change.positive {
  color: #48bb78;
}

.kpi-change.negative {
  color: #f56565;
}

/* 차트 섹션 */
.charts-section {
  margin-bottom: 40px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chart-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

.chart-container {
  height: 300px;
  position: relative;
}

/* 상세 정보 섹션 */
.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.detail-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.detail-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

/* 공급업체 목록 */
.suppliers-list {
  space-y: 12px;
}

.supplier-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.supplier-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.supplier-name {
  font-weight: 600;
  color: #1a202c;
}

.supplier-revenue {
  font-weight: 600;
  color: #48bb78;
}

.supplier-metrics {
  display: flex;
  gap: 16px;
}

.metric {
  font-size: 14px;
  color: #718096;
}

/* 재고 현황 */
.inventory-stats {
  space-y: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-weight: 500;
  color: #718096;
}

.stat-value {
  font-weight: 600;
  color: #1a202c;
}

.stat-value.warning {
  color: #ed8936;
}

.stat-value.danger {
  color: #f56565;
}

/* 알림 */
.alerts-list {
  space-y: 12px;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.alert-item.high {
  background: #fed7d7;
  border-left: 4px solid #f56565;
}

.alert-item.medium {
  background: #feebc8;
  border-left: 4px solid #ed8936;
}

.alert-item.low {
  background: #f0f4f8;
  border-left: 4px solid #718096;
}

.alert-icon {
  margin-right: 12px;
  font-size: 18px;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 4px;
}

.alert-message {
  color: #4a5568;
  margin-bottom: 4px;
}

.alert-time {
  font-size: 12px;
  color: #718096;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .hq-dashboard {
    padding: 15px;
  }
  
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .dashboard-title {
    font-size: 24px;
  }
  
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .details-grid {
    grid-template-columns: 1fr;
  }
}
</style>