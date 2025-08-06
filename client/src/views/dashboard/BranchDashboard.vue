<template>
  <div class="branch-dashboard">
    <!-- 헤더 -->
    <div class="dashboard-header">
      <h1 class="dashboard-title">
        {{ branchName }} 대시보드
        <span class="branch-badge" v-if="branchType">{{ branchType }}</span>
      </h1>
      
      <!-- 본사용 지점 선택 -->
      <div v-if="isHqUser" class="branch-selector">
        <label for="branchSelect">지점 선택:</label>
        <select id="branchSelect" v-model="selectedBranchId" @change="onBranchChange">
          <option value="">전체 지점 통합</option>
          <option v-for="branch in availableBranches" :key="branch.COMP_ID" :value="branch.COMP_ID">
            {{ branch.COMP_NAME }}
          </option>
        </select>
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
        <div class="kpi-card">
          <div class="kpi-content">
            <h3>금일 매출</h3>
            <div class="kpi-value">{{ kpiData.todaySales || '로딩 중...' }}</div>
            <div :class="['kpi-change', getChangeClass(kpiData.dailyGrowth)]">
              전일 대비 {{ kpiData.dailyGrowth || '계산 중...' }}
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-content">
            <h3>월간 매출</h3>
            <div class="kpi-value">{{ kpiData.monthlySales || '로딩 중...' }}</div>
            <div class="kpi-change">
              목표 달성률: {{ kpiData.monthlyAchievement || '계산 중...' }}
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-content">
            <h3>현재 재고 가치</h3>
            <div class="kpi-value">{{ kpiData.inventoryValue || '로딩 중...' }}</div>
            <div class="kpi-change">
              품목 수: {{ formatNumber(kpiData.totalInventoryItems) }}개
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-content">
            <h3>금일 거래</h3>
            <div class="kpi-value">{{ formatNumber(kpiData.todayTransactions) }}건</div>
            <div class="kpi-change">
              객단가: {{ kpiData.averageOrderValue || '계산 중...' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 차트 섹션 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 주간 매출 트렌드 차트 -->
        <div class="chart-card">
          <h3>주간 매출 트렌드 (최근 7일)</h3>
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
        <!-- 상위 판매 상품 -->
        <div class="detail-card">
          <h3>이번 달 베스트 상품 TOP 5</h3>
          <div class="items-list">
            <div v-if="topProducts.length === 0" class="no-data-message">
              판매 데이터를 불러오는 중입니다...
            </div>
            <div v-for="(product, index) in topProducts" :key="product.PRODUCT_ID" class="item-row top-product">
              <div class="rank-badge">{{ index + 1 }}</div>
              <div class="item-info">
                <div class="item-name">{{ product.PRODUCT_NAME }}</div>
                <div class="item-category">{{ product.CATEGORY_NAME }}</div>
              </div>
              <div class="item-stats">
                <div class="quantity">{{ formatNumber(product.TOTAL_QUANTITY) }}개 판매</div>
                <div class="sales">{{ formatCurrency(product.TOTAL_SALES) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 재고 현황 요약 -->
        <div class="detail-card">
          <h3>재고 현황 요약</h3>
          <div class="inventory-summary">
            <div class="summary-row">
              <span class="summary-label">전체 품목</span>
              <span class="summary-value">{{ formatNumber(kpiData.totalInventoryItems) }}개</span>
            </div>
            <div class="summary-row warning">
              <span class="summary-label">발주 필요</span>
              <span class="summary-value">{{ formatNumber(kpiData.lowStockItems) }}개</span>
            </div>
            <div class="summary-row danger">
              <span class="summary-label">품절 상품</span>
              <span class="summary-value">{{ formatNumber(kpiData.stockoutItems) }}개</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">재고 총액</span>
              <span class="summary-value">{{ kpiData.inventoryValue }}</span>
            </div>
          </div>
        </div>

        <!-- 알림 센터 (발주 필요 상품 포함) -->
        <div class="detail-card">
          <h3>알림 센터</h3>
          <div class="alerts-list">
            <div v-if="alerts.length === 0 && lowStockItems.length === 0" class="no-data-message">
              현재 알림이 없습니다.
            </div>
            
            <!-- 발주 필요 상품 알림 -->
            <div v-for="item in lowStockItems.slice(0, 3)" :key="'stock-' + item.PRODUCT_ID" 
                 :class="['alert-item', 'urgency-' + (item.urgency || 'medium').toLowerCase()]">
              <div class="alert-icon">📦</div>
              <div class="alert-content">
                <div class="alert-title">발주 필요: {{ item.PRODUCT_NAME }}</div>
                <div class="alert-message">
                  현재 재고: {{ formatNumber(item.CURRENT_STOCK) }}개 / 안전재고: {{ formatNumber(item.SAFETY_STOCK) }}개 
                  ({{ item.stockRatio || '0%' }})
                </div>
                <div class="alert-time">{{ item.CATEGORY_NAME }}</div>
              </div>
            </div>
            
            <!-- 기타 시스템 알림 -->
            <div v-for="alert in alerts.slice(0, 5 - Math.min(lowStockItems.length, 3))" 
                 :key="alert.ALERT_TYPE + alert.MESSAGE" 
                 :class="['alert-item', alert.priority?.toLowerCase()]">
              <div class="alert-icon">{{ alert.icon }}</div>
              <div class="alert-content">
                <div class="alert-title">{{ alert.TITLE }}</div>
                <div class="alert-message">{{ alert.MESSAGE }}</div>
                <div class="alert-time">{{ formatTime(alert.CREATED_AT) }}</div>
              </div>
            </div>
            
            <!-- 더 많은 발주 필요 상품이 있을 때 -->
            <div v-if="lowStockItems.length > 3" class="show-more">
              외 {{ lowStockItems.length - 3 }}개 상품 발주 필요
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
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
  Title,
  Tooltip,
  Legend,
  ArcElement,
  LineController,
  DoughnutController
)

// 인증 스토어
const authStore = useAuthStore()

// 반응형 데이터
const kpiData = ref({})
const branchInfo = ref({})
const lowStockItems = ref([])
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

// 본사 사용자 여부 확인
const isHqUser = computed(() => 
  ['system_admin', 'general_manager'].includes(authStore.roleName)
)

// 선택된 지점 (본사용)
const selectedBranchId = ref('')
const availableBranches = ref([])

// 지점 정보
const branchName = computed(() => branchInfo.value?.COMP_NAME || '지점')
const branchType = computed(() => branchInfo.value?.COMP_TYPE_NAME || '')

// API URL 생성 헬퍼
const buildApiUrl = (endpoint) => {
  if (isHqUser.value && selectedBranchId.value) {
    return `http://localhost:3049/api/dashboard/branch${endpoint}?compId=${selectedBranchId.value}`
  }
  return `http://localhost:3049/api/dashboard/branch${endpoint}`
}

// 공통 API 호출 함수
const fetchData = async (url, dataName) => {
  try {
    console.log(`Fetching ${dataName} from:`, url)
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const data = await response.json()
    console.log(`${dataName} 응답:`, data)
    return data
  } catch (error) {
    console.error(`${dataName} 로딩 실패:`, error)
    throw error
  }
}

// 주간 매출 트렌드 차트 생성
const createSalesTrendChart = (trendData) => {
  if (!salesTrendChart.value || !trendData || trendData.length === 0) {
    console.log('매출 트렌드 차트 생성 불가: 데이터 없음')
    return
  }

  try {
    const ctx = salesTrendChart.value.getContext('2d')
    
    if (trendChartInstance) {
      trendChartInstance.destroy()
    }

    const labels = trendData.map(item => item.SALE_DATE)
    const data = trendData.map(item => Math.round(item.DAILY_SALES / 1000))

    trendChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: '일별 매출',
          data: data,
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
          title: { display: false },
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: function(context) {
                return context.parsed.y.toLocaleString('ko-KR') + '천원'
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
            grid: { color: '#e2e8f0' }
          },
          y: {
            display: true,
            title: {
              display: true,
              text: '매출액 (천원)'
            },
            beginAtZero: true,
            grid: { color: '#e2e8f0' },
            ticks: {
              callback: function(value) {
                return value.toLocaleString('ko-KR') + '천원'
              }
            }
          }
        }
      }
    })
    
    console.log('매출 트렌드 차트 생성 완료')
  } catch (error) {
    console.error('라인 차트 생성 실패:', error)
  }
}

// 카테고리별 매출 구성 차트 생성
const createCategorySalesChart = (categoryData) => {
  if (!categorySalesChart.value || !categoryData || categoryData.length === 0) return

  try {
    const ctx = categorySalesChart.value.getContext('2d')
    
    if (categoryChartInstance) {
      categoryChartInstance.destroy()
    }

    const labels = categoryData.map(item => item.CATEGORY)
    const data = categoryData.map(item => item.SALES)

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
          title: { display: false },
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: function(context) {
                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                const percentage = total > 0 ? ((context.parsed / total) * 100).toFixed(1) : '0.0'
                const value = context.parsed.toLocaleString('ko-KR')
                return context.label + ': ' + value + '원 (' + percentage + '%)'
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

// API 호출 함수들
const fetchBranchInfo = async () => {
  try {
    const url = buildApiUrl('/info')
    const data = await fetchData(url, '지점 정보')
    if (data && typeof data === 'object') {
      branchInfo.value = data
    }
  } catch (error) {
    console.error('지점 정보 로딩 실패:', error)
    errorMessage.value = '지점 정보를 불러오는 중 오류가 발생했습니다.'
  }
}

const fetchKpiData = async () => {
  try {
    const url = buildApiUrl('/kpi')
    const data = await fetchData(url, 'KPI')
    if (data && typeof data === 'object') {
      kpiData.value = data
    }
  } catch (error) {
    console.error('KPI 데이터 로딩 실패:', error)
    errorMessage.value = 'KPI 데이터를 불러오는 중 오류가 발생했습니다.'
  }
}

const fetchSalesTrend = async () => {
  try {
    const url = buildApiUrl('/sales-trend')
    const data = await fetchData(url, '매출 트렌드')
    if (data && Array.isArray(data) && data.length > 0) {
      await nextTick()
      createSalesTrendChart(data)
    }
  } catch (error) {
    console.error('매출 트렌드 데이터 로딩 실패:', error)
  }
}

const fetchCategorySales = async () => {
  try {
    const url = buildApiUrl('/category-sales')
    const data = await fetchData(url, '카테고리 매출')
    if (data && Array.isArray(data) && data.length > 0) {
      await nextTick()
      createCategorySalesChart(data)
    }
  } catch (error) {
    console.error('카테고리 매출 데이터 로딩 실패:', error)
    await nextTick()
    createCategorySalesChart([])
  }
}

const fetchLowStockItems = async () => {
  try {
    const url = buildApiUrl('/low-stock')
    const data = await fetchData(url, '발주 필요 상품')
    if (data && Array.isArray(data)) {
      lowStockItems.value = data
    }
  } catch (error) {
    console.error('발주 필요 상품 데이터 로딩 실패:', error)
    lowStockItems.value = []
  }
}

const fetchTopProducts = async () => {
  try {
    const url = buildApiUrl('/top-products')
    const data = await fetchData(url, '상위 판매 상품')
    if (data && Array.isArray(data)) {
      topProducts.value = data
    }
  } catch (error) {
    console.error('상위 판매 상품 데이터 로딩 실패:', error)
    topProducts.value = []
  }
}

const fetchAlerts = async () => {
  try {
    const url = buildApiUrl('/alerts')
    const data = await fetchData(url, '알림')
    if (data && Array.isArray(data)) {
      alerts.value = data
    }
  } catch (error) {
    console.error('알림 데이터 로딩 실패:', error)
    alerts.value = []
  }
}

// 본사용 지점 목록 조회
const fetchAvailableBranches = async () => {
  if (!isHqUser.value) return
  
  try {
    const data = await fetchData('http://localhost:3049/api/dashboard/branch/branches', '지점 목록')
    if (data && Array.isArray(data)) {
      availableBranches.value = data
    }
  } catch (error) {
    console.error('지점 목록 조회 실패:', error)
  }
}

// 지점 변경 이벤트
const onBranchChange = () => {
  loadAllData()
}

// 유틸리티 함수들
const formatCurrency = (amount) => {
  if (typeof amount !== 'number') return '0원'
  
  if (amount >= 100000000) {
    return (amount / 100000000).toFixed(1) + '억원'
  } else if (amount >= 10000) {
    return (amount / 10000).toFixed(0) + '만원'
  } else {
    return amount.toLocaleString('ko-KR') + '원'
  }
}

const formatNumber = (num) => {
  return (num || 0).toLocaleString('ko-KR')
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

// 전체 데이터 로딩
const loadAllData = async () => {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    console.log('지점 대시보드 데이터 로딩 시작')
    
    // 지점 정보를 먼저 로딩
    await fetchBranchInfo()
    
    // 기본 데이터 병렬 로딩
    const results = await Promise.allSettled([
      fetchKpiData(),
      fetchLowStockItems(),
      fetchTopProducts(),
      fetchAlerts()
    ])
    
    // 결과 확인
    results.forEach((result, index) => {
      const apiNames = ['KPI', '발주 필요 상품', '상위 판매 상품', '알림']
      if (result.status === 'rejected') {
        console.warn(`${apiNames[index]} API 실패:`, result.reason)
      }
    })
    
    // 차트 데이터 순차 로딩
    await fetchSalesTrend()
    await fetchCategorySales()
    
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

// 데이터 새로고침
const refreshData = () => {
  console.log('수동 새로고침 시작')
  loadAllData()
}

// 컴포넌트 마운트
onMounted(async () => {
  console.log('지점 대시보드 컴포넌트 마운트됨')
  
  if (isHqUser.value) {
    await fetchAvailableBranches()
  }
  
  await nextTick()
  await loadAllData()
  
  // 3분마다 자동 새로고침
  refreshInterval = setInterval(() => {
    console.log('자동 새로고침 실행')
    loadAllData()
  }, 3 * 60 * 1000)
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
  align-items: center;
  margin-bottom: 30px;
  padding: 20px 0;
}

.dashboard-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.branch-badge {
  background: #4299e1;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.branch-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 20px;
}

.branch-selector label {
  font-weight: 500;
  color: #4a5568;
}

.branch-selector select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  cursor: pointer;
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
.details-section {
  margin-bottom: 30px;
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

.detail-card.urgent {
  border-left: 4px solid #f56565;
}

.detail-card.alerts {
  border-left: 4px solid #4299e1;
}

.detail-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

/* 데이터 없음 메시지 */
.no-data-message {
  text-align: center;
  color: #718096;
  font-style: italic;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 아이템 리스트 */
.items-list {
  space-y: 12px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.item-row:last-child {
  border-bottom: none;
}

.item-row.urgency-high {
  background: linear-gradient(90deg, rgba(245, 101, 101, 0.1) 0%, transparent 100%);
  border-left: 3px solid #f56565;
  padding-left: 12px;
  margin-left: -12px;
}

.item-row.urgency-medium {
  background: linear-gradient(90deg, rgba(255, 136, 0, 0.1) 0%, transparent 100%);
  border-left: 3px solid #ff8800;
  padding-left: 12px;
  margin-left: -12px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 4px;
}

.item-category {
  font-size: 12px;
  color: #718096;
}

.item-stock {
  text-align: right;
  font-size: 12px;
}

.current-stock {
  color: #1a202c;
  font-weight: 500;
}

.safety-stock {
  color: #718096;
  margin: 2px 0;
}

.stock-ratio {
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.stock-ratio.urgency-high {
  background: #fed7d7;
  color: #c53030;
}

.stock-ratio.urgency-medium {
  background: #feebc8;
  color: #c05621;
}

.stock-ratio.urgency-low {
  background: #f0fff4;
  color: #38a169;
}

/* 상위 판매 상품 */
.item-row.top-product {
  align-items: center;
  gap: 12px;
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.item-stats {
  text-align: right;
  font-size: 12px;
}

.quantity {
  color: #718096;
  margin-bottom: 2px;
}

.sales {
  color: #1a202c;
  font-weight: 600;
}

.show-more {
  text-align: center;
  color: #4299e1;
  font-size: 12px;
  font-weight: 500;
  padding: 8px;
  margin-top: 8px;
  border-top: 1px solid #e2e8f0;
}

/* 재고 현황 요약 */
.inventory-summary {
  space-y: 12px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row:hover {
  padding-left: 8px;
}

.summary-row.warning {
  color: #ed8936;
}

.summary-row.danger {
  color: #f56565;
}

.summary-label {
  font-weight: 500;
  color: #718096;
}

.summary-value {
  font-weight: 600;
  color: #1a202c;
}

.summary-value.warning {
  color: #ed8936;
}

.summary-value.danger {
  color: #f56565;
}

/* 알림 센터 */
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

.alert-item.urgency-high {
  background: #fed7d7;
  border-left: 4px solid #f56565;
}

.alert-item.urgency-medium {
  background: #feebc8;
  border-left: 4px solid #ed8936;
}

.alert-item.urgency-low {
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
  
  .dashboard-title {
    font-size: 24px;
  }
  
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  
  .branch-selector {
    margin: 0;
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style> 0.1);
  display: flex;
  align-items: center;
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

.kpi-card.today-sales::before {
  background: #48bb78;
}

.kpi-card.monthly-sales::before {
  background: #4299e1;
}

.kpi-card.inventory::before {
  background: #ed8936;
}

.kpi-card.customer::before {
  background: #f56565;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  box-shadow: 0 2px 4px rgba(0, 0, 0,