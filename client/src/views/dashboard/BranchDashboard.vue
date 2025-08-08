<template>
  <div class="branch-dashboard">
    <!-- 헤더 -->
    <div class="dashboard-header">
      <div class="header-left">
        <h1 class="dashboard-title">
          {{ branchInfo.COMP_NAME || '지점' }} 대시보드
        </h1>
        <p class="branch-subtitle">
          {{ branchInfo.COMP_TYPE_NAME || '' }} | {{ branchInfo.ADDRESS || '' }}
        </p>
      </div>
      <div class="header-actions">
        <button @click="refreshData" class="refresh-button" :disabled="isLoading">
          <span v-if="!isLoading">🔄 새로고침</span>
          <span v-else>로딩중...</span>
        </button>
        <div class="last-updated">
          마지막 업데이트: {{ lastUpdated }}
        </div>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="errorMessage" class="error-banner">
      <span>⚠️ {{ errorMessage }}</span>
      <button @click="errorMessage = ''" class="close-error">✕</button>
    </div>

    <!-- KPI 카드들 -->
    <div class="kpi-section">
      <div class="kpi-grid">
        <!-- 당일 매출 -->
        <div class="kpi-card">
          <div class="kpi-content">
            <h3>당일 매출</h3>
            <div class="kpi-value">{{ kpiData.todaySales || '로딩 중...' }}</div>
            <div :class="['kpi-change', getChangeClass(kpiData.dailyGrowth)]">
              전일대비 {{ kpiData.dailyGrowth || '계산 중...' }}
            </div>
          </div>
        </div>

        <!-- 월간 매출 -->
        <div class="kpi-card">
          <div class="kpi-content">
            <h3>월간 매출</h3>
            <div class="kpi-value">{{ kpiData.monthlySales || '로딩 중...' }}</div>
            <div class="kpi-change">
              목표달성률 {{ kpiData.monthlyAchievement || '계산 중...' }}
            </div>
          </div>
        </div>

        <!-- 재고 현황 -->
        <div class="kpi-card">
          <div class="kpi-content">
            <h3>재고 현황</h3>
            <div class="kpi-value">{{ kpiData.inventoryValue || '로딩 중...' }}</div>
            <div class="kpi-change">
              총 {{ formatNumber(kpiData.totalInventoryItems) }}개 품목
            </div>
          </div>
        </div>

        <!-- 거래 건수 -->
        <div class="kpi-card">
          <div class="kpi-content">
            <h3>당일 거래</h3>
            <div class="kpi-value">{{ formatNumber(kpiData.todayTransactions) }}건</div>
            <div class="kpi-change">
              평균 객단가 {{ kpiData.averageOrderValue || '계산 중...' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 차트 섹션 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 주간 매출 트렌드 -->
        <div class="chart-card">
          <h3>최근 7일 매출 트렌드</h3>
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
        <!-- 발주 필요 상품 -->
        <div class="detail-card">
          <h3>발주 필요 상품 ({{ lowStockItems.length }}개)</h3>
          <div class="items-list">
            <div v-if="lowStockItems.length === 0" class="no-data-message">
              발주가 필요한 상품이 없습니다.
            </div>
            <div 
              v-for="(item, index) in lowStockItems.slice(0, 5)" 
              :key="index" 
              :class="['item-row', getUrgencyClass(item.urgency)]"
            >
              <div class="item-info">
                <div class="item-name">{{ item.PRODUCT_NAME }}</div>
                <div class="item-detail">
                  현재: {{ formatNumber(item.CURRENT_STOCK) }}개 / 
                  안전재고: {{ formatNumber(item.SAFETY_STOCK) }}개
                </div>
              </div>
              <div class="item-stock-ratio">
                {{ item.stockRatio }}
              </div>
            </div>
          </div>
        </div>

        <!-- 상위 판매 상품 -->
        <div class="detail-card">
          <h3>이달 상위 판매 상품 TOP 5</h3>
          <div class="items-list">
            <div v-if="topProducts.length === 0" class="no-data-message">
              판매 데이터를 불러오는 중입니다...
            </div>
            <div 
              v-for="(product, index) in topProducts" 
              :key="index" 
              class="item-row"
            >
              <div class="item-rank">{{ index + 1 }}</div>
              <div class="item-info">
                <div class="item-name">{{ product.PRODUCT_NAME }}</div>
                <div class="item-detail">
                  판매량: {{ formatNumber(product.TOTAL_QUANTITY) }}개 | 
                  매출: {{ formatSales(product.TOTAL_SALES) }}만원
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 재고 알림 -->
        <div class="detail-card alerts">
          <h3>재고 알림 ({{ alerts.length }}개)</h3>
          <div class="alerts-list">
            <div v-if="alerts.length === 0" class="no-data-message">
              현재 알림이 없습니다.
            </div>
            <div 
              v-for="alert in alerts.slice(0, 5)" 
              :key="alert.id" 
              :class="['alert-item', alert.priority]"
            >
              <div class="alert-icon">{{ getAlertIcon(alert.alert_type) }}</div>
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

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import axios from '@/service/axios.js'
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

// Chart.js 컴포넌트 등록
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

// 반응형 데이터
const branchInfo = ref({})
const kpiData = ref({})
const lowStockItems = ref([])
const criticalStockItems = ref([])
const topProducts = ref([])
const alerts = ref([])
const lastUpdated = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

// 차트 참조
const salesTrendChart = ref(null)
const categorySalesChart = ref(null)

// 차트 인스턴스
let trendChartInstance = null
let categoryChartInstance = null
let refreshInterval = null

// API 기본 URL
const API_BASE_URL = 'http://localhost:3049/api/dashboard/branch'

// 공통 API 호출 함수
const fetchData = async (url, dataName) => {
  try {
    console.log(`Fetching ${dataName} from:`, url)
    const response = await axios.get(url)
    console.log(`${dataName} 응답:`, response.data)
    return response.data
  } catch (error) {
    console.error(`${dataName} API 호출 실패:`, error)
    throw error
  }
}

// 지점 정보 조회
const fetchBranchInfo = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/info`, '지점 정보')
    branchInfo.value = data || {}
  } catch (error) {
    console.error('지점 정보 로딩 실패:', error)
    branchInfo.value = {
      COMP_NAME: '지점 정보 로딩 실패',
      COMP_TYPE_NAME: '오류',
      error: true
    }
  }
}

// KPI 데이터 조회
const fetchKpiData = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/kpi`, 'KPI')
    if (data && typeof data === 'object') {
      kpiData.value = data
    }
  } catch (error) {
    console.error('KPI 데이터 로딩 실패:', error)
    errorMessage.value = 'KPI 데이터를 불러오는 중 오류가 발생했습니다.'
  }
}

// 주간 매출 트렌드 차트 생성
const createSalesTrendChart = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/sales-trend`, '매출 트렌드')
    
    if (!salesTrendChart.value || !data || !Array.isArray(data)) {
      console.log('매출 트렌드 차트 생성 불가: 데이터 없음')
      return
    }

    const ctx = salesTrendChart.value.getContext('2d')
    
    // 기존 차트 파괴
    if (trendChartInstance) {
      trendChartInstance.destroy()
    }

    const labels = data.map(item => item.SALE_DATE)
    const salesData = data.map(item => Math.round((item.DAILY_SALES || 0) / 10000)) // 만원 단위

    trendChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: '일별 매출',
          data: salesData,
          borderColor: '#4299e1',
          backgroundColor: '#4299e1' + '20',
          tension: 0.4,
          fill: true,
          pointRadius: 6,
          pointHoverRadius: 8,
          borderWidth: 3
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
            display: false
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                return context.parsed.y.toLocaleString('ko-KR') + '만원'
              }
            }
          }
        },
        scales: {
          x: {
            display: true,
            title: {
              display: true,
              text: '날짜'
            },
            grid: {
              color: '#e2e8f0'
            }
          },
          y: {
            display: true,
            title: {
              display: true,
              text: '매출액 (만원)'
            },
            beginAtZero: true,
            grid: {
              color: '#e2e8f0'
            },
            ticks: {
              callback: function(value) {
                return value.toLocaleString('ko-KR') + '만원'
              }
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('매출 트렌드 차트 생성 실패:', error)
  }
}

// 카테고리별 매출 구성 차트 생성
const createCategorySalesChart = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/category-sales`, '카테고리 매출')
    
    if (!categorySalesChart.value || !data || !Array.isArray(data)) {
      console.log('카테고리 매출 차트 생성 불가: 데이터 없음')
      return
    }

    const ctx = categorySalesChart.value.getContext('2d')
    
    // 기존 차트 파괴
    if (categoryChartInstance) {
      categoryChartInstance.destroy()
    }

    const labels = data.map(item => item.CATEGORY || '기타')
    const salesData = data.map(item => Math.round((item.SALES || 0) / 1000)) // 천원 단위

    categoryChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: salesData,
          backgroundColor: [
            '#48bb78',
            '#4299e1', 
            '#ed8936',
            '#f56565',
            '#9f7aea',
            '#38b2ac'
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
                const value = context.raw.toLocaleString('ko-KR')
                return context.label + ': ' + value + '천원'
              }
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('카테고리 매출 차트 생성 실패:', error)
  }
}

// 발주 필요 상품 조회
const fetchLowStockItems = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/low-stock`, '발주 필요 상품')
    lowStockItems.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('발주 필요 상품 로딩 실패:', error)
    lowStockItems.value = []
  }
}

// 품절 임박 상품 조회
const fetchCriticalStockItems = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/critical-stock`, '품절 임박 상품')
    criticalStockItems.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('품절 임박 상품 로딩 실패:', error)
    criticalStockItems.value = []
  }
}

// 상위 판매 상품 조회
const fetchTopProducts = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/top-products`, '상위 판매 상품')
    topProducts.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('상위 판매 상품 로딩 실패:', error)
    topProducts.value = []
  }
}

// 알림 조회
const fetchAlerts = async () => {
  try {
    const data = await fetchData(`${API_BASE_URL}/alerts`, '알림')
    if (data && Array.isArray(data)) {
      const processedAlerts = data.map((alert, index) => ({
        ...alert,
        id: alert.id || `alert-${index}`,
        title: alert.TITLE || alert.title || '알림',
        message: alert.MESSAGE || alert.message || '',
        created_at: alert.CREATED_AT || alert.created_at || new Date(),
        priority: (alert.PRIORITY || alert.priority || 'LOW').toLowerCase(),
        alert_type: alert.ALERT_TYPE || alert.alert_type || 'INFO'
      }))
      alerts.value = processedAlerts
    } else {
      alerts.value = []
    }
  } catch (error) {
    console.error('알림 데이터 로딩 실패:', error)
    alerts.value = []
  }
}

// 전체 데이터 로딩
const loadAllData = async () => {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    console.log('지점 대시보드 데이터 로딩 시작')
    
    // 기본 데이터 병렬 로딩
    const results = await Promise.allSettled([
      fetchBranchInfo(),
      fetchKpiData(),
      fetchLowStockItems(),
      fetchCriticalStockItems(),
      fetchTopProducts(),
      fetchAlerts()
    ])
    
    // 결과 확인
    results.forEach((result, index) => {
      const apiNames = ['지점정보', 'KPI', '발주필요상품', '품절임박상품', '상위판매상품', '알림']
      if (result.status === 'rejected') {
        console.warn(`${apiNames[index]} API 실패:`, result.reason)
      }
    })
    
    // 차트 데이터 로딩
    await nextTick()
    await createSalesTrendChart()
    await createCategorySalesChart()
    
    lastUpdated.value = new Date().toLocaleString('ko-KR')
    console.log('지점 대시보드 데이터 로딩 완료')
  } catch (error) {
    console.error('데이터 로딩 중 오류:', error)
    if (!errorMessage.value) {
      errorMessage.value = '데이터를 불러오는 중 오류가 발생했습니다.'
    }
  } finally {
    isLoading.value = false
  }
}

// 유틸리티 함수들
const formatNumber = (num) => {
  return (num || 0).toLocaleString('ko-KR')
}

const formatSales = (amount) => {
  return Math.round((amount || 0) / 10000).toLocaleString()
}

const formatTime = (date) => {
  if (date instanceof Date) {
    return date.toLocaleString('ko-KR')
  }
  return new Date(date).toLocaleString('ko-KR')
}

const getChangeClass = (value) => {
  if (!value || value === '계산 중...') return ''
  const numValue = parseFloat(value)
  return numValue >= 0 ? 'positive' : 'negative'
}

const getAlertIcon = (alertType) => {
  const icons = {
    'STOCKOUT': '📦',
    'CRITICAL_STOCK': '⚠️',
    'LOW_STOCK': '📉',
    'ORDER_REQUIRED': '🛒',
    'INFO': 'ℹ️'
  }
  return icons[alertType] || '⚠️'
}

const getUrgencyClass = (urgency) => {
  switch (urgency) {
    case 'HIGH': return 'high'
    case 'MEDIUM': return 'medium'
    case 'LOW': return 'low'
    default: return 'low'
  }
}

// 데이터 새로고침
const refreshData = () => {
  console.log('수동 새로고침 시작')
  loadAllData()
}

// 컴포넌트 마운트
onMounted(async () => {
  console.log('지점 대시보드 컴포넌트 마운트됨')
  await loadAllData()
  
  // 5분마다 자동 새로고침
  refreshInterval = setInterval(() => {
    console.log('자동 새로고침 실행')
    loadAllData()
  }, 5 * 60 * 1000)
})

// 컴포넌트 언마운트
onUnmounted(() => {
  console.log('지점 대시보드 컴포넌트 언마운트됨')
  
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
  if (trendChartInstance) {
    trendChartInstance.destroy()
  }
  if (categoryChartInstance) {
    categoryChartInstance.destroy()
  }
})
</script>

<style scoped>
.branch-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding: 20px 0;
}

.header-left h1.dashboard-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px 0;
}

.branch-subtitle {
  color: #718096;
  font-size: 16px;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.refresh-button {
  padding: 8px 16px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.refresh-button:hover:not(:disabled) {
  background: #3182ce;
  transform: translateY(-1px);
}

.refresh-button:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}

.last-updated {
  color: #718096;
  font-size: 14px;
}

.error-banner {
  background: #fed7d7;
  border: 1px solid #fc8181;
  border-radius: 8px;
  padding: 12px 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #c53030;
}

.close-error {
  background: none;
  border: none;
  color: #c53030;
  font-size: 18px;
  cursor: pointer;
  padding: 0 5px;
}

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
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.kpi-card:nth-child(1)::before {
  background: #48bb78;
}

.kpi-card:nth-child(2)::before {
  background: #4299e1;
}

.kpi-card:nth-child(3)::before {
  background: #ed8936;
}

.kpi-card:nth-child(4)::before {
  background: #9f7aea;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  color: #718096;
}

.kpi-change.positive {
  color: #48bb78;
}

.kpi-change.negative {
  color: #f56565;
}

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

.no-data-message {
  text-align: center;
  color: #718096;
  font-style: italic;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.items-list {
  space-y: 12px;
}

.item-row {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

.item-row:hover {
  background: #e2e8f0;
  transform: translateX(4px);
}

.item-row.high {
  background: #fed7d7;
  border-left: 4px solid #f56565;
}

.item-row.medium {
  background: #feebc8;
  border-left: 4px solid #ed8936;
}

.item-row.low {
  background: #f0fff4;
  border-left: 4px solid #48bb78;
}

.item-rank {
  background: #4299e1;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 12px;
  margin-right: 12px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 4px;
}

.item-detail {
  color: #718096;
  font-size: 14px;
}

.item-stock-ratio {
  font-weight: 600;
  color: #4299e1;
  font-size: 16px;
}

.alerts-list {
  space-y: 12px;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

.alert-item:hover {
  transform: translateX(4px);
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
  font-size: 14px;
}

.alert-time {
  font-size: 12px;
  color: #718096;
}

/* 반응형 디자인 */
@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .details-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .branch-dashboard {
    padding: 15px;
  }
  
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
    gap: 10px;
  }
  
  .refresh-button {
    width: 100%;
  }
  
  .header-left h1.dashboard-title {
    font-size: 24px;
  }
  
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  
  .kpi-card {
    padding: 20px;
  }
  
  .kpi-value {
    font-size: 24px;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .details-grid {
    grid-template-columns: 1fr;
  }
  
  .error-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .close-error {
    align-self: flex-end;
  }
}

/* 애니메이션 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.kpi-card,
.chart-card,
.detail-card {
  animation: fadeIn 0.5s ease-out;
}

/* 로딩 상태 */
.kpi-value:empty::after {
  content: '로딩 중...';
  color: #a0aec0;
  font-size: 16px;
  font-weight: normal;
}

/* 인쇄 스타일 */
@media print {
  .branch-dashboard {
    background: white;
  }
  
  .refresh-button,
  .close-error {
    display: none;
  }
  
  .kpi-card,
  .chart-card,
  .detail-card {
    box-shadow: none;
    border: 1px solid #e2e8f0;
    break-inside: avoid;
  }
  
  .chart-container {
    height: 200px;
  }
}

/* 다크 모드 지원 (선택적) */
@media (prefers-color-scheme: dark) {
  .branch-dashboard {
    background: #1a202c;
  }
  
  .header-left h1.dashboard-title {
    color: #f7fafc;
  }
  
  .kpi-card,
  .chart-card,
  .detail-card {
    background: #2d3748;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
  
  .kpi-value,
  .item-name,
  .alert-title {
    color: #f7fafc;
  }
  
  .kpi-content h3,
  .item-detail,
  .alert-message {
    color: #a0aec0;
  }
  
  .item-row,
  .no-data-message {
    background: #374151;
  }
  
  .item-row:hover {
    background: #4a5568;
  }
}
</style>