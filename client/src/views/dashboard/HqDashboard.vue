<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import axios from '@/service/axios.js';
import { Chart, CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend, ArcElement, LineController, DoughnutController } from 'chart.js';

// Chart.js 컴포넌트 등록
Chart.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend, ArcElement, LineController, DoughnutController);

// 반응형 데이터
const kpiData = ref({});
const topSuppliers = ref([]);
const inventoryData = ref({
    totalItems: 0,
    lowStockItems: 0,
    stockoutItems: 0,
    totalValue: '0원'
});
const alerts = ref([]);
const lastUpdated = ref('');
const isLoading = ref(false);
const errorMessage = ref('');

// 차트 참조
const salesTrendChart = ref(null);
const categorySalesChart = ref(null);

// 차트 인스턴스
let trendChartInstance = null;
let categoryChartInstance = null;
let refreshInterval = null;

// API 데이터 저장용
let cachedTrendData = [];

// 공통 API 호출 함수
const fetchData = async (url, dataName) => {
    try {
        console.log(`Fetching ${dataName} from:`, url);

        const response = await axios.get(url);

        console.log(`${dataName} 응답: `, response.data);
        return response.data;
    } catch (error) {
        console.error(`${dataName} API 호출 실패:`, error);
        throw Error;
    }
};

// 매출 트렌드 차트 생성 - 만원 단위로 수정
const createSalesTrendChart = (trendData) => {
    if (!salesTrendChart.value || !trendData || trendData.length === 0) {
        console.log('매출 트렌드 차트 생성 불가: 데이터 없음');
        return;
    }

    try {
        const ctx = salesTrendChart.value.getContext('2d');

        // 기존 차트 파괴
        if (trendChartInstance) {
            trendChartInstance.destroy();
        }

        // 현재 월 기준으로 최근 6개월 생성
        const currentDate = new Date();
        const currentYear = currentDate.getFullYear();
        const currentMonth = currentDate.getMonth() + 1; // 0-based이므로 +1

        const recentMonths = [];
        for (let i = 5; i >= 0; i--) {
            const targetDate = new Date(currentYear, currentMonth - 1 - i, 1);
            const monthStr = `${targetDate.getFullYear()}-${String(targetDate.getMonth() + 1).padStart(2, '0')}`;
            recentMonths.push(monthStr);
        }

        console.log('표시할 월:', recentMonths); // 디버깅용

        const datasets = trendData.map((item, index) => {
            const colors = ['#48bb78', '#4299e1', '#ed8936', '#f56565', '#9f7aea', '#38b2ac'];

            // 각 카테고리별로 최근 6개월 데이터 매핑 (만원 단위로 변환)
            const categoryData = recentMonths.map((month) => {
                const dataIndex = [...new Set(cachedTrendData.map((item) => item.MONTH))].sort().indexOf(month);
                const value = dataIndex >= 0 && item.data[dataIndex] ? item.data[dataIndex] : 0;
                return Math.round(value / 10); // 천원 → 만원 단위로 변환 (1000원 단위를 10으로 나누기)
            });

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
            };
        });

        // 전체 데이터에서 최대값 구하기 (적절한 Y축 범위 설정용)
        const allValues = datasets.flatMap((dataset) => dataset.data);
        const maxValue = Math.max(...allValues);
        const yAxisMax = Math.ceil((maxValue * 1.1) / 10) * 10; // 10 단위로 올림하여 여유 공간 확보

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
                            label: function (context) {
                                const value = context.parsed.y.toLocaleString('ko-KR');
                                return context.dataset.label + ': ' + value + '만원';
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
                            callback: function (value) {
                                return value.toLocaleString('ko-KR') + '만원';
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
        });

        console.log('매출 트렌드 차트 생성 완료 (만원 단위)');
    } catch (error) {
        console.error('라인 차트 생성 실패:', error);
    }
};

// 카테고리별 매출 구성 차트 생성
const createCategorySalesChart = (categoryData) => {
    if (!categorySalesChart.value || !categoryData || categoryData.length === 0) return;

    try {
        const ctx = categorySalesChart.value.getContext('2d');

        // 기존 차트 파괴
        if (categoryChartInstance) {
            categoryChartInstance.destroy();
        }

        const labels = categoryData.map((item) => item.category);
        const data = categoryData.map((item) => item.sales);

        categoryChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [
                    {
                        data: data,
                        backgroundColor: ['#48bb78', '#4299e1', '#ed8936', '#f56565', '#9f7aea', '#38b2ac'],
                        borderWidth: 2,
                        borderColor: '#ffffff'
                    }
                ]
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
                            label: function (context) {
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? ((context.parsed / total) * 100).toFixed(1) : '0.0';
                                const value = context.parsed.toLocaleString('ko-KR');
                                return context.label + ': ' + value + '천원 (' + percentage + '%)';
                            }
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('도넛 차트 생성 실패:', error);
    }
};

// KPI 데이터 조회 - 새로운 KPI 필드명으로 수정
const fetchKpiData = async () => {
    try {
        const data = await fetchData('/api/dashboard/hq/kpi', 'KPI');

        if (data && typeof data === 'object') {
            // 새로운 KPI 필드명으로 매핑
            kpiData.value = {
                // 기존 매출 정보
                totalSales: data.totalSales || '0원',
                salesGrowth: data.salesGrowth || '+0.0%',

                // 새로운 KPI들
                revenueGrowthRate: data.revenueGrowthRate || '+0.0%',
                revenueGrowthChange: data.revenueGrowthChange || '매출 증감율',

                pendingOutboundCount: data.pendingOutboundCount || '0건',
                outboundCountChange: data.outboundCountChange || '+0건',

                pendingPurchaseOrderCount: data.pendingPurchaseOrderCount || '0건',
                poCountChange: data.poCountChange || '+0건'
            };

            console.log('KPI 데이터 매핑 완료:', kpiData.value);
        }
    } catch (error) {
        console.error('KPI 데이터 로딩 실패:', error);
        errorMessage.value = 'KPI 데이터를 불러오는 중 오류가 발생했습니다.';
    }
};

// 매출 트렌드 데이터 조회
const fetchSalesTrend = async () => {
    try {
        const data = await fetchData('/api/dashboard/hq/sales-trend', '매출 트렌드');

        if (data && Array.isArray(data) && data.length > 0) {
            cachedTrendData = data;
            const processedData = processSalesTrendData(data);
            await nextTick();
            createSalesTrendChart(processedData);
        }
    } catch (error) {
        console.error('매출 트렌드 데이터 로딩 실패:', error);
    }
};

// 카테고리별 매출 데이터 조회 - 수정된 버전
const fetchCategorySales = async () => {
    try {
        const data = await fetchData('/api/dashboard/hq/category-sales', '카테고리 매출');

        if (data && Array.isArray(data) && data.length > 0) {
            const processedData = data.map((item) => ({
                category: item.CATEGORY || item.category || '알 수 없음', // 대문자 필드명 매핑
                sales: typeof item.SALES === 'number' ? Math.round(item.SALES / 1000) : 0 // SALES 대문자로 수정
            }));
            console.log('처리된 카테고리 데이터:', processedData);
            await nextTick();
            createCategorySalesChart(processedData);
        } else {
            console.log('카테고리 매출 데이터가 비어있습니다.');
        }
    } catch (error) {
        console.error('카테고리 매출 데이터 로딩 실패:', error);
        // 에러가 발생해도 빈 차트라도 보여주기
        await nextTick();
        createCategorySalesChart([]);
    }
};

// 🔥 공급업체 데이터 조회 - 새로운 쿼리 기준으로 수정
const fetchTopSuppliers = async () => {
    try {
        const data = await fetchData('/api/dashboard/hq/suppliers', '공급업체');

        console.log('공급업체 원본 데이터:', data); // 디버깅용

        if (data && Array.isArray(data) && data.length > 0) {
            topSuppliers.value = data.map((supplier) => ({
                // 대소문자 모두 고려해서 매핑
                supplier_name: supplier.SUPPLIER_NAME || supplier.supplier_name || '알 수 없음',
                order_count: supplier.ORDER_COUNT || supplier.order_count || 0
            }));

            console.log('처리된 공급업체 데이터:', topSuppliers.value);
        } else {
            topSuppliers.value = [];
            console.log('공급업체 데이터가 비어있습니다.');
        }
    } catch (error) {
        console.error('공급업체 데이터 로딩 실패:', error);
        topSuppliers.value = [];
    }
};

// 재고 데이터 조회
const fetchInventoryData = async () => {
    try {
        const data = await fetchData('/api/dashboard/hq/inventory', '재고');

        if (data && typeof data === 'object') {
            inventoryData.value = {
                totalItems: data.totalItems || 0,
                lowStockItems: data.lowStockItems || 0,
                stockoutItems: data.stockoutItems || 0,
                totalValue: data.totalInventoryValue || '0원' // 백엔드에서 이미 formatting됨
            };
        }
    } catch (error) {
        console.error('재고 데이터 로딩 실패:', error);
    }
};

// 알림 데이터 조회 - 새로운 알림 타입 포함
const fetchAlerts = async () => {
    try {
        const data = await fetchData('/api/dashboard/hq/alerts', '알림');

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
            }));
            console.log('처리된 알림 데이터:', alerts.value);
        } else {
            alerts.value = [];
            console.log('알림 데이터가 비어있습니다.');
        }
    } catch (error) {
        console.error('알림 데이터 로딩 실패:', error);
        alerts.value = [];
    }
};

// 매출 트렌드 데이터 처리 - 만원 단위로 수정
const processSalesTrendData = (apiData) => {
    console.log('매출 트렌드 원본 데이터:', apiData);

    if (!apiData || !Array.isArray(apiData) || apiData.length === 0) {
        return [];
    }

    // 월 추출
    const months = [...new Set(apiData.map((item) => item.MONTH))].sort();

    // 카테고리별 데이터 그룹화
    const categoryMap = {};

    apiData.forEach((item) => {
        const categoryName = item.CATEGORY_NAME || '기타';
        const month = item.MONTH || '2024-01';
        const sales = Math.round((item.SALES || 0) / 1000); // 원을 천원으로 변환 (차트에서 다시 만원으로 변환)

        if (!categoryMap[categoryName]) {
            categoryMap[categoryName] = {};
        }
        categoryMap[categoryName][month] = sales;
    });

    // 차트 형식으로 변환
    return Object.keys(categoryMap).map((category) => ({
        category: category,
        data: months.map((month) => categoryMap[category][month] || 0)
    }));
};

// 유틸리티 함수들
const formatCurrency = (amount) => {
    if (typeof amount !== 'number') return '0원';

    if (amount >= 100000000) {
        return (amount / 100000000).toFixed(1) + '억원';
    } else if (amount >= 10000000) {
        return (amount / 10000000).toFixed(1) + '천만원';
    } else if (amount >= 10000) {
        return (amount / 10000).toFixed(0) + '만원';
    } else {
        return amount.toLocaleString('ko-KR') + '원';
    }
};

const formatNumber = (num) => {
    return (num || 0).toLocaleString('ko-KR');
};

const formatGrowth = (value, suffix = '%') => {
    if (value === null || value === undefined) return '계산 중...';
    const formatted = Number(value).toFixed(1);
    return (value >= 0 ? '+' : '') + formatted + suffix;
};

const formatTime = (date) => {
    if (date instanceof Date) {
        return date.toLocaleString('ko-KR');
    }
    return new Date(date).toLocaleString('ko-KR');
};

const getChangeClass = (value, inverse = false) => {
    if (!value || value === '계산 중...') return 'text-muted-color';
    const numValue = parseFloat(value);
    if (inverse) {
        return numValue <= 0 ? 'text-green-500' : 'text-red-500';
    }
    return numValue >= 0 ? 'text-green-500' : 'text-red-500';
};

const getAlertIcon = (alertType) => {
    const icons = {
        PENDING_OUTBOUND: '📦',
        PENDING_PURCHASE_ORDER: '📋',
        STOCKOUT_WARNING: '⚠️',
        DELIVERY_DELAY: '🚚',
        LOW_TURNOVER: '🔄',
        LOW_STOCK: '📉',
        INFO: 'ℹ️'
    };
    return icons[alertType] || '⚠️';
};

// 전체 데이터 로딩
const loadAllData = async () => {
    isLoading.value = true;
    errorMessage.value = '';

    try {
        console.log('대시보드 데이터 로딩 시작');

        // 기본 데이터 병렬 로딩 (에러가 나도 다른 것들은 계속 로딩)
        const results = await Promise.allSettled([fetchKpiData(), fetchInventoryData(), fetchTopSuppliers(), fetchAlerts()]);

        // 결과 확인 (선택적)
        results.forEach((result, index) => {
            const apiNames = ['KPI', '재고', '공급업체', '알림'];
            if (result.status === 'rejected') {
                console.warn(`${apiNames[index]} API 실패:`, result.reason);
            }
        });

        // 차트 데이터 순차 로딩 (DOM 요소가 필요하므로)
        await fetchSalesTrend();
        await fetchCategorySales();

        lastUpdated.value = new Date().toLocaleString('ko-KR');
        console.log('대시보드 데이터 로딩 완료');
    } catch (error) {
        console.error('데이터 로딩 중 오류:', error);
        if (!errorMessage.value) {
            errorMessage.value = '데이터를 불러오는 중 오류가 발생했습니다.';
        }
    } finally {
        isLoading.value = false;
    }
};

// 데이터 새로고침
const refreshData = () => {
    console.log('수동 새로고침 시작');
    loadAllData();
};

// 컴포넌트 마운트
onMounted(async () => {
    console.log('HQ 대시보드 컴포넌트 마운트됨');

    await nextTick();
    await loadAllData();

    // 5분마다 자동 새로고침
    refreshInterval = setInterval(
        () => {
            console.log('자동 새로고침 실행');
            loadAllData();
        },
        5 * 60 * 1000
    );
});

// 컴포넌트 언마운트
onUnmounted(() => {
    console.log('HQ 대시보드 컴포넌트 언마운트됨');

    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
    if (trendChartInstance) {
        trendChartInstance.destroy();
    }
    if (categoryChartInstance) {
        categoryChartInstance.destroy();
    }
});
</script>

<template>
    <div class="surface-ground min-h-screen p-6">
        <!-- 헤더 -->
        <div class="flex justify-between items-center mb-8 pb-6">
            <h1 class="text-surface-900 dark:text-surface-0 text-4xl font-bold">본사 SCM 대시보드</h1>
            <div class="flex items-center gap-4">
                <Button @click="refreshData" :disabled="isLoading" icon="pi pi-refresh" :label="isLoading ? '로딩중...' : '새로고침'" />
                <div class="text-muted-color text-sm">마지막 업데이트: {{ lastUpdated }}</div>
            </div>
        </div>

        <!-- 에러 메시지 -->
        <Message v-if="errorMessage" severity="error" :closable="true" @close="errorMessage = ''">
            {{ errorMessage }}
        </Message>

        <!-- KPI 카드들 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <!-- 1. 월간 총 매출액 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">월간 총 매출액</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.totalSales || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.salesGrowth)]">
                            {{ kpiData.salesGrowth || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-green-100 dark:bg-green-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-dollar text-green-500 text-xl"></i>
                    </div>
                </div>
            </div>

            <!-- 2. 전월 대비 매출 증감율 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">전월 대비 매출 증감율</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.revenueGrowthRate || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.revenueGrowthChange)]">
                            {{ kpiData.revenueGrowthChange || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-blue-100 dark:bg-blue-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-chart-line text-blue-500 text-xl"></i>
                    </div>
                </div>
            </div>

            <!-- 3. 출고 대기 건수 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">출고 대기 건수</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.pendingOutboundCount || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.outboundCountChange, true)]">
                            {{ kpiData.outboundCountChange || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-orange-100 dark:bg-orange-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-box text-orange-500 text-xl"></i>
                    </div>
                </div>
            </div>

            <!-- 4. 대기중인 발주서 수 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">대기중인 발주서 수</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.pendingPurchaseOrderCount || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.poCountChange, true)]">
                            {{ kpiData.poCountChange || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-red-100 dark:bg-red-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-file-edit text-red-500 text-xl"></i>
                    </div>
                </div>
            </div>
        </div>

        <!-- 차트 섹션 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            <!-- 매출 트렌드 차트 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">카테고리별 매출 트렌드</div>
                <div class="h-80">
                    <canvas ref="salesTrendChart"></canvas>
                </div>
            </div>

            <!-- 카테고리별 매출 구성 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">카테고리별 매출 구성</div>
                <div class="h-80">
                    <canvas ref="categorySalesChart"></canvas>
                </div>
            </div>
        </div>

        <!-- 상세 정보 섹션 -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- 상위 공급업체 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">발주 빈도 높은 공급업체</div>
                <div v-if="topSuppliers.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">공급업체 데이터를 불러오는 중입니다...</div>
                <div v-else class="space-y-4">
                    <div v-for="supplier in topSuppliers" :key="supplier.supplier_name" class="flex justify-between items-center p-4 bg-surface-50 dark:bg-surface-800 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-700 transition-colors">
                        <div class="text-surface-900 dark:text-surface-0 font-medium">{{ supplier.supplier_name }}</div>
                        <div class="text-primary font-semibold">{{ supplier.order_count }}건</div>
                    </div>
                </div>
            </div>

            <!-- 재고 현황 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">재고 현황</div>
                <div class="space-y-4">
                    <div class="flex justify-between items-center py-3 border-b border-surface-200 dark:border-surface-700">
                        <span class="text-muted-color">총 품목 수</span>
                        <span class="text-surface-900 dark:text-surface-0 font-semibold">{{ formatNumber(inventoryData.totalItems) }}개</span>
                    </div>
                    <div class="flex justify-between items-center py-3 border-b border-surface-200 dark:border-surface-700">
                        <span class="text-muted-color">안전재고 미달</span>
                        <span class="text-orange-500 font-semibold">{{ formatNumber(inventoryData.lowStockItems) }}개</span>
                    </div>
                    <div class="flex justify-between items-center py-3 border-b border-surface-200 dark:border-surface-700">
                        <span class="text-muted-color">품절 품목</span>
                        <span class="text-red-500 font-semibold">{{ formatNumber(inventoryData.stockoutItems) }}개</span>
                    </div>
                    <div class="flex justify-between items-center py-3">
                        <span class="text-muted-color">총 재고 가치</span>
                        <span class="text-surface-900 dark:text-surface-0 font-semibold">{{ inventoryData.totalValue || '계산 중...' }}</span>
                    </div>
                </div>
            </div>

            <!-- 긴급 알림 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">긴급 알림</div>
                <div v-if="alerts.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">현재 긴급 알림이 없습니다.</div>
                <div v-else class="space-y-4">
                    <div
                        v-for="alert in alerts"
                        :key="alert.id"
                        :class="[
                            'flex items-start p-4 rounded-lg border-l-4',
                            alert.priority === 'high' ? 'bg-red-50 dark:bg-red-950/20 border-l-red-500' : alert.priority === 'medium' ? 'bg-orange-50 dark:bg-orange-950/20 border-l-orange-500' : 'bg-blue-50 dark:bg-blue-950/20 border-l-blue-500'
                        ]"
                    >
                        <div class="text-xl mr-3">{{ getAlertIcon(alert.alert_type) }}</div>
                        <div class="flex-1">
                            <div class="text-surface-900 dark:text-surface-0 font-semibold mb-1">{{ alert.title }}</div>
                            <div class="text-muted-color text-sm mb-2">{{ alert.message }}</div>
                            <div class="text-muted-color text-xs">{{ formatTime(alert.created_at) }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style>
.card {
    margin-bottom: 0px;
}
</style>
