<template>
  <div class="hq-dashboard">
    <!-- 헤더 -->
    <div class="dashboard-header">
      <h1 class="dashboard-title">본사 SCM 대시보드</h1>
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
            <h3>월간 총 매출액</h3>
            <div class="kpi-value">{{ kpiData.totalSales || '로딩 중...' }}</div>
            <div :class="['kpi-change', getChangeClass(kpiData.salesGrowth)]">
              {{ kpiData.salesGrowth || '계산 중...' }}
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-content">
            <h3>전체 재고 회전율</h3>
            <div class="kpi-value">{{ kpiData.inventoryTurnover || '로딩 중...' }}</div>
            <div :class="['kpi-change', getChangeClass(kpiData.turnoverChange)]">
              {{ kpiData.turnoverChange || '계산 중...' }}
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-content">
            <h3>공급업체 납기준수율</h3>
            <div class="kpi-value">{{ kpiData.deliveryRate || '로딩 중...' }}</div>
            <div :class="['kpi-change', getChangeClass(kpiData.deliveryRateChange, true)]">
              {{ kpiData.deliveryRateChange || '계산 중...' }}
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-content">
            <h3>품절률</h3>
            <div class="kpi-value">{{ kpiData.stockoutRate || '로딩 중...' }}</div>
            <div :class="['kpi-change', getChangeClass(kpiData.stockoutRateChange, true)]">
              {{ kpiData.stockoutRateChange || '계산 중...' }}
            </div>
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
            <div v-if="topSuppliers.length === 0" class="no-data-message">
              공급업체 데이터를 불러오는 중입니다...
            </div>
            <div v-for="supplier in topSuppliers" :key="supplier.supplier_name" class="supplier-item">
              <div class="supplier-info">
                <div class="supplier-name">{{ supplier.supplier_name }}</div>
                <div class="supplier-revenue">{{ formatCurrency(supplier.revenue) }}</div>
              </div>
              <div class="supplier-metrics">
                <span class="metric">납기: {{ supplier.delivery_rate || 0 }}%</span>
                <span class="metric">품질: {{ supplier.quality_score || 0 }}점</span>
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
              <span class="stat-value">{{ formatNumber(inventoryData.totalItems) }}개</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">안전재고 미달</span>
              <span class="stat-value warning">{{ formatNumber(inventoryData.lowStockItems) }}개</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">품절 품목</span>
              <span class="stat-value danger">{{ formatNumber(inventoryData.stockoutItems) }}개</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">총 재고 가치</span>
              <span class="stat-value">{{ inventoryData.totalValue || '계산 중...' }}</span>
            </div>
          </div>
        </div>

        <!-- 긴급 알림 -->
        <div class="detail-card alerts">
          <h3>긴급 알림</h3>
          <div class="alerts-list">
            <div v-if="alerts.length === 0" class="no-data-message">
              현재 긴급 알림이 없습니다.
            </div>
            <div v-for="alert in alerts" :key="alert.id" :class="['alert-item', alert.priority.toLowerCase()]">
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
const kpiData = ref({})
const topSuppliers = ref([])
const inventoryData = ref({
  totalItems: 0,
  lowStockItems: 0,
  stockoutItems: 0,
  totalValue: '0원'
})
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

// API 데이터 저장용
let cachedTrendData = []

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

// 매출 트렌드 차트 생성 - 만원 단위로 수정
const createSalesTrendChart = (trendData) => {
  if (!salesTrendChart.value || !trendData || trendData.length === 0) {
    console.log('매출 트렌드 차트 생성 불가: 데이터 없음')
    return
  }

  try {
    const ctx = salesTrendChart.value.getContext('2d')
    
    // 기존 차트 파괴
    if (trendChartInstance) {
      trendChartInstance.destroy()
    }

    // 현재 월 기준으로 최근 6개월 생성
    const currentDate = new Date()
    const currentYear = currentDate.getFullYear()
    const currentMonth = currentDate.getMonth() + 1 // 0-based이므로 +1
    
    const recentMonths = []
    for (let i = 5; i >= 0; i--) {
      const targetDate = new Date(currentYear, currentMonth - 1 - i, 1)
      const monthStr = `${targetDate.getFullYear()}-${String(targetDate.getMonth() + 1).padStart(2, '0')}`
      recentMonths.push(monthStr)
    }
    
    console.log('표시할 월:', recentMonths) // 디버깅용
    
    const datasets = trendData.map((item, index) => {
      const colors = ['#48bb78', '#4299e1', '#ed8936', '#f56565', '#9f7aea', '#38b2ac']
      
      // 각 카테고리별로 최근 6개월 데이터 매핑 (만원 단위로 변환)
      const categoryData = recentMonths.map(month => {
        const dataIndex = [...new Set(cachedTrendData.map(item => item.MONTH))].sort().indexOf(month)
        const value = dataIndex >= 0 && item.data[dataIndex] ? item.data[dataIndex] : 0
        return Math.round(value / 10) // 천원 → 만원 단위로 변환 (1000원 단위를 10으로 나누기)
      })
      
      return {
        label: item.category,
        data: categoryData,
        borderColor: colors[index % colors.length],
        backgroundColor: colors[index % colors.length] + '20',
        tension: 0.4,
        fill: false,
        pointRadius: 6,
        pointHoverRadius: 8,
        borderWidth: 3
      }
    })

    // 전체 데이터에서 최대값 구하기 (적절한 Y축 범위 설정용)
    const allValues = datasets.flatMap(dataset => dataset.data)
    const maxValue = Math.max(...allValues)
    const yAxisMax = Math.ceil(maxValue * 1.1 / 10) * 10 // 10 단위로 올림하여 여유 공간 확보

    trendChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: recentMonths,
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
                const value = context.parsed.y.toLocaleString('ko-KR')
                return context.dataset.label + ': ' + value + '만원'
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
            max: yAxisMax,
            grid: {
              color: '#e2e8f0'
            },
            ticks: {
              stepSize: 10, // 10만원 간격으로 고정
              callback: function(value) {
                return value.toLocaleString('ko-KR') + '만원'
              }
            }
          }
        },
        interaction: {
          mode: 'nearest',
          axis: 'x',
          intersect: false
        }
      }
    })
    
    console.log('매출 트렌드 차트 생성 완료 (만원 단위)')
  } catch (error) {
    console.error('라인 차트 생성 실패:', error)
  }
}

// 카테고리별 매출 구성 차트 생성
const createCategorySalesChart = (categoryData) => {
  if (!categorySalesChart.value || !categoryData || categoryData.length === 0) return

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
                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                const percentage = total > 0 ? ((context.parsed / total) * 100).toFixed(1) : '0.0'
                const value = context.parsed.toLocaleString('ko-KR')
                return context.label + ': ' + value + '천원 (' + percentage + '%)'
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

// KPI 데이터 조회
const fetchKpiData = async () => {
  try {
    const data = await fetchData('http://localhost:3049/api/dashboard/hq/kpi', 'KPI')
    
    if (data && typeof data === 'object') {
      // 백엔드에서 이미 formatting된 문자열로 보내므로 그대로 사용
      kpiData.value = {
        totalSales: data.totalSales || '0원',
        salesGrowth: data.salesGrowth || '+0.0%',
        inventoryTurnover: data.inventoryTurnover || '0.0회',
        turnoverChange: data.turnoverChange || '+0.0%',
        deliveryRate: data.deliveryRate || '0.0%',
        deliveryRateChange: data.deliveryRateChange || '+0.0%',
        stockoutRate: data.stockoutRate || '0.0%',
        stockoutRateChange: data.stockoutRateChange || '+0.0%'
      }
    }
  } catch (error) {
    console.error('KPI 데이터 로딩 실패:', error)
    errorMessage.value = 'KPI 데이터를 불러오는 중 오류가 발생했습니다.'
  }
}

// 매출 트렌드 데이터 조회
const fetchSalesTrend = async () => {
  try {
    const data = await fetchData('http://localhost:3049/api/dashboard/hq/sales-trend', '매출 트렌드')
    
    if (data && Array.isArray(data) && data.length > 0) {
      cachedTrendData = data
      const processedData = processSalesTrendData(data)
      await nextTick()
      createSalesTrendChart(processedData)
    }
  } catch (error) {
    console.error('매출 트렌드 데이터 로딩 실패:', error)
  }
}

// 카테고리별 매출 데이터 조회 - 수정된 버전
const fetchCategorySales = async () => {
  try {
    const data = await fetchData('http://localhost:3049/api/dashboard/hq/category-sales', '카테고리 매출')
    
    if (data && Array.isArray(data) && data.length > 0) {
      const processedData = data.map(item => ({
        category: item.CATEGORY || item.category || '알 수 없음',  // 대문자 필드명 매핑
        sales: typeof item.SALES === 'number' ? Math.round(item.SALES / 1000) : 0 // SALES 대문자로 수정
      }))
      console.log('처리된 카테고리 데이터:', processedData)
      await nextTick()
      createCategorySalesChart(processedData)
    } else {
      console.log('카테고리 매출 데이터가 비어있습니다.')
    }
  } catch (error) {
    console.error('카테고리 매출 데이터 로딩 실패:', error)
    // 에러가 발생해도 빈 차트라도 보여주기
    await nextTick()
    createCategorySalesChart([])
  }
}

// 공급업체 데이터 조회
const fetchTopSuppliers = async () => {
  try {
    const data = await fetchData('http://localhost:3049/api/dashboard/hq/suppliers', '공급업체')
    
    if (data && Array.isArray(data) && data.length > 0) {
      topSuppliers.value = data.map(supplier => ({
        ...supplier,
        supplier_name: supplier.supplier_name || '알 수 없음',
        revenue: supplier.revenue || 0,
        delivery_rate: supplier.delivery_rate || 0,
        quality_score: supplier.quality_score || 0
      }))
    } else {
      topSuppliers.value = []
    }
  } catch (error) {
    console.error('공급업체 데이터 로딩 실패:', error)
    topSuppliers.value = []
  }
}

// 재고 데이터 조회
const fetchInventoryData = async () => {
  try {
    const data = await fetchData('http://localhost:3049/api/dashboard/hq/inventory', '재고')
    
    if (data && typeof data === 'object') {
      inventoryData.value = {
        totalItems: data.totalItems || 0,
        lowStockItems: data.lowStockItems || 0,
        stockoutItems: data.stockoutItems || 0,
        totalValue: data.totalInventoryValue || '0원'  // 백엔드에서 이미 formatting됨
      }
    }
  } catch (error) {
    console.error('재고 데이터 로딩 실패:', error)
  }
}

// 알림 데이터 조회 - 수정된 버전
const fetchAlerts = async () => {
  try {
    const data = await fetchData('http://localhost:3049/api/dashboard/hq/alerts', '알림')
    
    if (data && Array.isArray(data) && data.length > 0) {
      alerts.value = data.map((alert, index) => ({
        ...alert,
        id: alert.id || `alert-${index}`,
        // 백엔드 대문자 필드를 소문자로 매핑
        title: alert.TITLE || alert.title || '알림',
        message: alert.MESSAGE || alert.message || '',
        created_at: alert.CREATED_AT || alert.created_at ? new Date(alert.CREATED_AT || alert.created_at) : new Date(),
        priority: (alert.PRIORITY || alert.priority || 'LOW').toLowerCase(),
        alert_type: alert.ALERT_TYPE || alert.alert_type || 'INFO'
      }))
      console.log('처리된 알림 데이터:', alerts.value)
    } else {
      alerts.value = []
      console.log('알림 데이터가 비어있습니다.')
    }
  } catch (error) {
    console.error('알림 데이터 로딩 실패:', error)
    alerts.value = []
  }
}

// 매출 트렌드 데이터 처리 - 만원 단위로 수정
const processSalesTrendData = (apiData) => {
  console.log('매출 트렌드 원본 데이터:', apiData)
  
  if (!apiData || !Array.isArray(apiData) || apiData.length === 0) {
    return []
  }
  
  // 월 추출
  const months = [...new Set(apiData.map(item => item.MONTH))].sort()
  
  // 카테고리별 데이터 그룹화
  const categoryMap = {}
  
  apiData.forEach(item => {
    const categoryName = item.CATEGORY_NAME || '기타'
    const month = item.MONTH || '2024-01'
    const sales = Math.round((item.SALES || 0) / 1000) // 원을 천원으로 변환 (차트에서 다시 만원으로 변환)
    
    if (!categoryMap[categoryName]) {
      categoryMap[categoryName] = {}
    }
    categoryMap[categoryName][month] = sales
  })
  
  // 차트 형식으로 변환
  return Object.keys(categoryMap).map(category => ({
    category: category,
    data: months.map(month => categoryMap[category][month] || 0)
  }))
}

// 유틸리티 함수들
const formatCurrency = (amount) => {
  if (typeof amount !== 'number') return '0원'
  
  if (amount >= 100000000) {
    return (amount / 100000000).toFixed(1) + '억원'
  } else if (amount >= 10000000) {
    return (amount / 10000000).toFixed(1) + '천만원'
  } else if (amount >= 10000) {
    return (amount / 10000).toFixed(0) + '만원'
  } else {
    return amount.toLocaleString('ko-KR') + '원'
  }
}

const formatNumber = (num) => {
  return (num || 0).toLocaleString('ko-KR')
}

const formatGrowth = (value, suffix = '%') => {
  if (value === null || value === undefined) return '계산 중...'
  const formatted = Number(value).toFixed(1)
  return (value >= 0 ? '+' : '') + formatted + suffix
}

const formatTime = (date) => {
  if (date instanceof Date) {
    return date.toLocaleString('ko-KR')
  }
  return new Date(date).toLocaleString('ko-KR')
}

const getChangeClass = (value, inverse = false) => {
  if (!value || value === '계산 중...') return ''
  const numValue = parseFloat(value)
  if (inverse) {
    return numValue <= 0 ? 'positive' : 'negative'
  }
  return numValue >= 0 ? 'positive' : 'negative'
}

const getAlertIcon = (alertType) => {
  const icons = {
    'STOCKOUT_WARNING': '📦',
    'DELIVERY_DELAY': '🚚',
    'LOW_TURNOVER': '🔄',
    'LOW_STOCK': '⚠️',
    'INFO': 'ℹ️'
  }
  return icons[alertType] || '⚠️'
}

// 전체 데이터 로딩
const loadAllData = async () => {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    console.log('대시보드 데이터 로딩 시작')
    
    // 기본 데이터 병렬 로딩 (에러가 나도 다른 것들은 계속 로딩)
    const results = await Promise.allSettled([
      fetchKpiData(),
      fetchInventoryData(),
      fetchTopSuppliers(),
      fetchAlerts()
    ])
    
    // 결과 확인 (선택적)
    results.forEach((result, index) => {
      const apiNames = ['KPI', '재고', '공급업체', '알림']
      if (result.status === 'rejected') {
        console.warn(`${apiNames[index]} API 실패:`, result.reason)
      }
    })
    
    // 차트 데이터 순차 로딩 (DOM 요소가 필요하므로)
    await fetchSalesTrend()
    await fetchCategorySales()
    
    lastUpdated.value = new Date().toLocaleString('ko-KR')
    console.log('대시보드 데이터 로딩 완료')
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
  console.log('HQ 대시보드 컴포넌트 마운트됨')
  
  await nextTick()
  await loadAllData()
  
  // 5분마다 자동 새로고침
  refreshInterval = setInterval(() => {
    console.log('자동 새로고침 실행')
    loadAllData()
  }, 5 * 60 * 1000)
})

// 컴포넌트 언마운트
onUnmounted(() => {
  console.log('HQ 대시보드 컴포넌트 언마운트됨')
  
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

/* 에러 배너 */
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

.kpi-card.sales::before {
  background: #48bb78;
}

.kpi-card.inventory::before {
  background: #4299e1;
}

.kpi-card.delivery::before {
  background: #ed8936;
}

.kpi-card.stockout::before {
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

/* 데이터 없음 메시지 */
.no-data-message {
  text-align: center;
  color: #718096;
  font-style: italic;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
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
  transition: all 0.2s ease;
}

.supplier-item:hover {
  background: #e2e8f0;
  transform: translateX(4px);
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
  transition: all 0.2s ease;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-item:hover {
  padding-left: 8px;
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

/* 로딩 상태 */
.kpi-value:empty::after,
.stat-value:empty::after {
  content: '로딩 중...';
  color: #a0aec0;
  font-size: 16px;
  font-weight: normal;
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
  .hq-dashboard {
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
  
  .dashboard-title {
    font-size: 24px;
  }
  
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  
  .kpi-card {
    padding: 20px;
  }
  
  .kpi-icon {
    font-size: 24px;
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
  
  .supplier-metrics {
    flex-direction: column;
    gap: 4px;
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

/* 인쇄 스타일 */
@media print {
  .hq-dashboard {
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
  .hq-dashboard {
    background: #1a202c;
  }
  
  .dashboard-title {
    color: #f7fafc;
  }
  
  .kpi-card,
  .chart-card,
  .detail-card {
    background: #2d3748;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
  
  .kpi-value,
  .stat-value,
  .supplier-name,
  .alert-title {
    color: #f7fafc;
  }
  
  .kpi-content h3,
  .stat-label,
  .metric,
  .alert-message {
    color: #a0aec0;
  }
  
  .supplier-item,
  .no-data-message {
    background: #374151;
  }
  
  .supplier-item:hover {
    background: #4a5568;
  }
  
  .stat-item {
    border-bottom-color: #4a5568;
  }
}
</style>