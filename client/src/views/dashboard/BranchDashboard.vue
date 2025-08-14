<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import axios from '@/service/axios.js';
import { Chart, CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend, ArcElement, LineController, DoughnutController } from 'chart.js';

// Chart.js 등록
Chart.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend, ArcElement, LineController, DoughnutController);

// 반응형 데이터
const branchInfo = ref({});
const kpiData = ref({});
const lowStockItems = ref([]);
const criticalStockItems = ref([]);
const topProducts = ref([]);
const alerts = ref([]);
const lastUpdated = ref('');
const isLoading = ref(false);
const errorMessage = ref('');

// 지점 선택 관련
const currentUser = ref({});
const availableBranches = ref([]);
const selectedBranchId = ref('');
const isHeadquarter = ref(false);

// 차트 참조
const salesTrendChart = ref(null);
const categorySalesChart = ref(null);

// 차트 인스턴스
let trendChartInstance = null;
let categoryChartInstance = null;
let refreshInterval = null;

// API 기본 URL
const API_BASE_URL = '/api/dashboard/branch';

// 공통 API 호출 함수
const fetchData = async (url, dataName) => {
    try {
        console.log(`Fetching ${dataName} from:`, url);

        // 본사인 경우 지점 ID를 쿼리 파라미터로 추가
        let finalUrl = url;
        if (isHeadquarter.value && selectedBranchId.value) {
            const separator = url.includes('?') ? '&' : '?';
            finalUrl = `${url}${separator}compId=${selectedBranchId.value}`;
        }

        console.log(`Final URL: ${finalUrl}`);

        const response = await axios.get(finalUrl);
        console.log(`${dataName} 응답:`, response.data);
        return response.data;
    } catch (error) {
        console.error(`${dataName} API 호출 실패:`, error);
        throw error;
    }
};

// 현재 사용자 정보 조회
const fetchCurrentUser = async () => {
    try {
        const response = await axios.get('/api/auth/me');
        currentUser.value = response.data.data.user;

        // 본사 권한 체크 - 실제 로그에서 확인된 형태로 체크
        const userRole = response.data.data.role?.roleName || '';
        const userCompId = currentUser.value.compId || '';

        console.log('사용자 역할:', userRole); // 디버깅용
        console.log('사용자 compId:', userCompId); // 디버깅용

        // 실제 권한 체크 - 대소문자 구분 없이, 포함 여부로 체크
        isHeadquarter.value = userRole.toUpperCase().includes('SYSTEM_ADMIN') || userRole.toUpperCase().includes('GENERAL_MANAGER') || userCompId === 'COM10001';

        console.log('현재 사용자:', currentUser.value);
        console.log('본사 권한:', isHeadquarter.value);

        if (!isHeadquarter.value) {
            selectedBranchId.value = userCompId;
        }
    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
        errorMessage.value = '사용자 정보를 불러올 수 없습니다.';
    }
};

// 지점 목록 조회 (본사 전용)
const fetchAvailableBranches = async () => {
    if (!isHeadquarter.value) return;

    try {
        const response = await axios.get(`${API_BASE_URL}/branches`);
        availableBranches.value = response.data || [];
        console.log('사용 가능한 지점들:', availableBranches.value);

        if (availableBranches.value.length > 0 && !selectedBranchId.value) {
            selectedBranchId.value = availableBranches.value[0].COMP_ID;
        }
    } catch (error) {
        console.error('지점 목록 조회 실패:', error);
        availableBranches.value = [];
    }
};

// 지점 변경 시 호출
const onBranchChange = () => {
    console.log('선택된 지점 변경:', selectedBranchId.value);
    if (selectedBranchId.value) {
        loadAllData();
    }
};

// 지점 정보 조회
const fetchBranchInfo = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/info`, '지점 정보');
        branchInfo.value = data || {};
    } catch (error) {
        console.error('지점 정보 로딩 실패:', error);
        branchInfo.value = {
            COMP_NAME: '지점 정보 로딩 실패',
            COMP_TYPE_NAME: '오류',
            error: true
        };
    }
};

// KPI 데이터 조회
const fetchKpiData = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/kpi`, 'KPI');
        if (data && typeof data === 'object') {
            kpiData.value = data;
        }
    } catch (error) {
        console.error('KPI 데이터 로딩 실패:', error);
        errorMessage.value = 'KPI 데이터를 불러오는 중 오류가 발생했습니다.';
    }
};

// 발주 필요 상품 조회
const fetchLowStockItems = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/low-stock`, '발주 필요 상품');
        lowStockItems.value = Array.isArray(data) ? data : [];
    } catch (error) {
        console.error('발주 필요 상품 로딩 실패:', error);
        lowStockItems.value = [];
    }
};

// 품절 임박 상품 조회
const fetchCriticalStockItems = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/critical-stock`, '품절 임박 상품');
        criticalStockItems.value = Array.isArray(data) ? data : [];
    } catch (error) {
        console.error('품절 임박 상품 로딩 실패:', error);
        criticalStockItems.value = [];
    }
};

// 상위 판매 상품 조회
const fetchTopProducts = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/top-products`, '상위 판매 상품');
        topProducts.value = Array.isArray(data) ? data : [];
    } catch (error) {
        console.error('상위 판매 상품 로딩 실패:', error);
        topProducts.value = [];
    }
};

// 알림 조회
const fetchAlerts = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/alerts`, '알림');
        if (data && Array.isArray(data)) {
            const processedAlerts = data.map((alert, index) => ({
                ...alert,
                id: alert.id || `alert-${index}`,
                title: alert.TITLE || alert.title || '알림',
                message: alert.MESSAGE || alert.message || '',
                created_at: alert.CREATED_AT || alert.created_at || new Date(),
                priority: (alert.PRIORITY || alert.priority || 'LOW').toLowerCase(),
                alert_type: alert.ALERT_TYPE || alert.alert_type || 'INFO'
            }));
            alerts.value = processedAlerts;
        } else {
            alerts.value = [];
        }
    } catch (error) {
        console.error('알림 데이터 로딩 실패:', error);
        alerts.value = [];
    }
};

// 매출 트렌드 차트 생성
const createSalesTrendChart = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/sales-trend`, '매출 트렌드');

        if (!salesTrendChart.value) {
            console.error('매출 트렌드 차트 Canvas 요소를 찾을 수 없습니다.');
            return;
        }

        if (!data || !Array.isArray(data) || data.length === 0) {
            console.warn('매출 트렌드 데이터가 없습니다:', data);
            createEmptyTrendChart();
            return;
        }

        const ctx = salesTrendChart.value.getContext('2d');

        if (trendChartInstance) {
            trendChartInstance.destroy();
        }

        const labels = data.map((item) => item.SALE_DATE || '날짜 미상');
        const salesData = data.map((item) => Math.round((item.DAILY_SALES || 0) / 10000));

        console.log('차트 라벨:', labels);
        console.log('차트 데이터:', salesData);

        trendChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: '일별 매출',
                        data: salesData,
                        borderColor: '#4299e1',
                        backgroundColor: '#4299e1' + '20',
                        tension: 0.4,
                        fill: true,
                        pointRadius: 6,
                        pointHoverRadius: 8,
                        borderWidth: 3
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: { display: false },
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return context.parsed.y.toLocaleString('ko-KR') + '만원';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        display: true,
                        title: { display: true, text: '날짜' },
                        grid: { color: '#e2e8f0' }
                    },
                    y: {
                        display: true,
                        title: { display: true, text: '매출액 (만원)' },
                        beginAtZero: true,
                        grid: { color: '#e2e8f0' },
                        ticks: {
                            callback: function (value) {
                                return value.toLocaleString('ko-KR') + '만원';
                            }
                        }
                    }
                }
            }
        });

        console.log('매출 트렌드 차트 생성 완료');
    } catch (error) {
        console.error('매출 트렌드 차트 생성 실패:', error);
        createEmptyTrendChart();
    }
};

// 빈 차트 생성
const createEmptyTrendChart = () => {
    if (!salesTrendChart.value) return;

    const ctx = salesTrendChart.value.getContext('2d');

    if (trendChartInstance) {
        trendChartInstance.destroy();
    }

    trendChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['데이터 없음'],
            datasets: [
                {
                    label: '일별 매출',
                    data: [0],
                    borderColor: '#a0aec0',
                    backgroundColor: '#a0aec0' + '20',
                    tension: 0.4,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { display: true, title: { display: true, text: '날짜' } },
                y: { display: true, title: { display: true, text: '매출액 (만원)' }, beginAtZero: true }
            }
        }
    });
};

// 카테고리 매출 차트 생성
const createCategorySalesChart = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/category-sales`, '카테고리 매출');

        if (!categorySalesChart.value) {
            console.error('카테고리 매출 차트 Canvas 요소를 찾을 수 없습니다.');
            return;
        }

        if (!data || !Array.isArray(data) || data.length === 0) {
            console.warn('카테고리 매출 데이터가 없습니다:', data);
            createEmptyCategoryChart();
            return;
        }

        const ctx = categorySalesChart.value.getContext('2d');

        if (categoryChartInstance) {
            categoryChartInstance.destroy();
        }

        const labels = data.map((item) => item.CATEGORY || '기타');
        const salesData = data.map((item) => Math.round((item.SALES || 0) / 1000));

        console.log('카테고리 라벨:', labels);
        console.log('카테고리 데이터:', salesData);

        categoryChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [
                    {
                        data: salesData,
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
                    title: { display: false },
                    legend: { position: 'bottom' },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const value = context.raw.toLocaleString('ko-KR');
                                return context.label + ': ' + value + '천원';
                            }
                        }
                    }
                }
            }
        });

        console.log('카테고리 매출 차트 생성 완료');
    } catch (error) {
        console.error('카테고리 매출 차트 생성 실패:', error);
        createEmptyCategoryChart();
    }
};

// 빈 카테고리 차트 생성
const createEmptyCategoryChart = () => {
    if (!categorySalesChart.value) return;

    const ctx = categorySalesChart.value.getContext('2d');

    if (categoryChartInstance) {
        categoryChartInstance.destroy();
    }

    categoryChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['데이터 없음'],
            datasets: [
                {
                    data: [1],
                    backgroundColor: ['#a0aec0'],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom' } }
        }
    });
};

// 전체 데이터 로딩
const loadAllData = async () => {
    if (isHeadquarter.value && !selectedBranchId.value) {
        console.log('지점이 선택되지 않음');
        return;
    }

    isLoading.value = true;
    errorMessage.value = '';

    try {
        console.log('지점 대시보드 데이터 로딩 시작');

        const results = await Promise.allSettled([fetchBranchInfo(), fetchKpiData(), fetchLowStockItems(), fetchCriticalStockItems(), fetchTopProducts(), fetchAlerts()]);

        results.forEach((result, index) => {
            const apiNames = ['지점정보', 'KPI', '발주필요상품', '품절임박상품', '상위판매상품', '알림'];
            if (result.status === 'rejected') {
                console.warn(`${apiNames[index]} API 실패:`, result.reason);
            }
        });

        await nextTick();
        await createSalesTrendChart();
        await createCategorySalesChart();

        lastUpdated.value = new Date().toLocaleString('ko-KR');
        console.log('지점 대시보드 데이터 로딩 완료');
    } catch (error) {
        console.error('데이터 로딩 중 오류:', error);
        if (!errorMessage.value) {
            errorMessage.value = '데이터를 불러오는 중 오류가 발생했습니다.';
        }
    } finally {
        isLoading.value = false;
    }
};

// 유틸리티 함수들
const formatNumber = (num) => (num || 0).toLocaleString('ko-KR');
const formatSales = (amount) => (amount || 0).toLocaleString('ko-KR') + '원';
const formatTime = (date) => {
    if (date instanceof Date) return date.toLocaleString('ko-KR');
    return new Date(date).toLocaleString('ko-KR');
};
const getChangeClass = (value) => {
    if (!value || value === '데이터 없음') return 'text-muted-color';
    const numValue = parseFloat(value);
    return numValue >= 0 ? 'text-green-500' : 'text-red-500';
};
const getAlertIcon = (alertType) => {
    const icons = {
        STOCKOUT: '📦',
        CRITICAL_STOCK: '⚠️',
        LOW_STOCK: '📉',
        ORDER_REQUIRED: '🛒',
        INFO: 'ℹ️'
    };
    return icons[alertType] || '⚠️';
};
const getUrgencyClass = (urgency) => {
    switch (urgency) {
        case 'HIGH':
            return 'bg-red-50 dark:bg-red-950/20 border-l-red-500';
        case 'MEDIUM':
            return 'bg-orange-50 dark:bg-orange-950/20 border-l-orange-500';
        case 'LOW':
            return 'bg-green-50 dark:bg-green-950/20 border-l-green-500';
        default:
            return 'bg-blue-50 dark:bg-blue-950/20 border-l-blue-500';
    }
};

// 데이터 새로고침
const refreshData = () => {
    console.log('수동 새로고침 시작');
    loadAllData();
};

// 컴포넌트 마운트
onMounted(async () => {
    console.log('지점 대시보드 컴포넌트 마운트됨');

    await fetchCurrentUser();

    if (isHeadquarter.value) {
        await fetchAvailableBranches();
    }

    await loadAllData();

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
    console.log('지점 대시보드 컴포넌트 언마운트됨');

    if (refreshInterval) clearInterval(refreshInterval);
    if (trendChartInstance) trendChartInstance.destroy();
    if (categoryChartInstance) categoryChartInstance.destroy();
});
</script>

<template>
    <div class="surface-ground min-h-screen p-6">
        <!-- 헤더 -->
        <div class="flex justify-between items-start mb-8">
            <div>
                <h1 class="text-surface-900 dark:text-surface-0 text-4xl font-bold mb-2">{{ branchInfo.COMP_NAME || '지점' }} 대시보드</h1>
                <p class="text-muted-color text-lg mb-4">{{ branchInfo.COMP_TYPE_NAME || '' }} | {{ branchInfo.ADDRESS || '' }}</p>

                <!-- 본사용 지점 선택 드롭다운 -->
                <div v-if="isHeadquarter && availableBranches.length > 0" class="mb-4">
                    <label for="branchSelect" class="block text-muted-color text-sm font-medium mb-2">조회할 지점:</label>
                    <Dropdown id="branchSelect" v-model="selectedBranchId" @change="onBranchChange" :options="availableBranches" optionLabel="COMP_NAME" optionValue="COMP_ID" placeholder="지점을 선택하세요" class="w-64" />
                </div>
            </div>
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
            <!-- 당일 매출 -->
            <div class="card">
                <div class="mb-4">
                    <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">당일 매출</div>
                    <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                        {{ kpiData.todaySales || '로딩 중...' }}
                    </div>
                    <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.dailyGrowth)]">전일대비 {{ kpiData.dailyGrowth || '데이터 없음' }}</div>
                </div>
            </div>

            <!-- 월간 매출 -->
            <div class="card">
                <div class="mb-4">
                    <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">월간 매출</div>
                    <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                        {{ kpiData.monthlySales || '로딩 중...' }}
                    </div>
                    <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.monthlyGrowth)]">전월대비 {{ kpiData.monthlyGrowth || '데이터 없음' }}</div>
                </div>
            </div>

            <!-- 재고 현황 -->
            <div class="card">
                <div class="mb-4">
                    <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">재고 현황</div>
                    <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                        {{ kpiData.inventoryValue || '로딩 중...' }}
                    </div>
                    <div class="text-muted-color text-sm font-medium mt-1">총 {{ formatNumber(kpiData.totalInventoryItems) }}개 품목</div>
                </div>
            </div>

            <!-- 거래 건수 -->
            <div class="card">
                <div class="mb-4">
                    <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">당일 거래</div>
                    <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">{{ formatNumber(kpiData.todayTransactions) }}건</div>
                    <div class="text-muted-color text-sm font-medium mt-1">평균 객단가 {{ kpiData.averageOrderValue || '데이터 없음' }}</div>
                </div>
            </div>
        </div>

        <!-- 차트 섹션 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            <!-- 주간 매출 트렌드 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">최근 7일 매출 트렌드</div>
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
            <!-- 발주 필요 상품 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">발주 필요 상품 ({{ lowStockItems.length }}개)</div>
                <div v-if="lowStockItems.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">발주가 필요한 상품이 없습니다.</div>
                <div v-else class="space-y-4">
                    <div v-for="(item, index) in lowStockItems.slice(0, 5)" :key="index" :class="['flex items-center justify-between p-4 rounded-lg border-l-4', getUrgencyClass(item.urgency)]">
                        <div class="flex-1">
                            <div class="text-surface-900 dark:text-surface-0 font-medium mb-1">{{ item.PRODUCT_NAME }}</div>
                            <div class="text-muted-color text-sm">현재: {{ formatNumber(item.CURRENT_STOCK) }}개 / 안전재고: {{ formatNumber(item.SAFETY_STOCK) }}개</div>
                        </div>
                        <div class="text-primary font-semibold">
                            {{ item.stockRatio }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- 상위 판매 상품 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">이달 상위 판매 상품</div>
                <div v-if="topProducts.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">판매 데이터를 불러오는 중입니다...</div>
                <div v-else class="space-y-4">
                    <div v-for="(product, index) in topProducts" :key="index" class="flex items-center p-4 bg-surface-50 dark:bg-surface-800 rounded-lg">
                        <div class="flex items-center justify-center bg-primary text-primary-contrast rounded-full w-8 h-8 mr-4 text-sm font-bold">
                            {{ index + 1 }}
                        </div>
                        <div class="flex-1">
                            <div class="text-surface-900 dark:text-surface-0 font-medium mb-1">{{ product.PRODUCT_NAME }}</div>
                            <div class="text-muted-color text-sm">판매량: {{ formatNumber(product.TOTAL_QUANTITY) }}개 | 매출: {{ formatSales(product.TOTAL_SALES) }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 재고 알림 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">재고 알림 ({{ alerts.length }}개)</div>
                <div v-if="alerts.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">현재 알림이 없습니다.</div>
                <div v-else class="space-y-4">
                    <div
                        v-for="alert in alerts.slice(0, 5)"
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
