<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import axios from '@/service/axios.js';
import { Chart, CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend, ArcElement, LineController, DoughnutController } from 'chart.js';

// Chart.js 컴포넌트 등록
Chart.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend, ArcElement, LineController, DoughnutController);

// 반응형 데이터
const supplierInfo = ref({});
const kpiData = ref({});
const activeOrders = ref([]);
const topProducts = ref([]);
const alerts = ref([]);
const lastUpdated = ref('');
const isLoading = ref(false);
const errorMessage = ref('');

// 공급업체 선택 관련
const currentUser = ref({});
const availableSuppliers = ref([]);
const selectedSupplierId = ref('');
const isSystemAdmin = ref(false);

// 차트 참조
const orderTrendChart = ref(null);
const categorySupplyChart = ref(null);

// 차트 인스턴스
let trendChartInstance = null;
let categoryChartInstance = null;
let refreshInterval = null;

// API 기본 URL
const API_BASE_URL = 'http://localhost:3049/api/dashboard/supplier';

// 공통 API 호출 함수
const fetchData = async (url, dataName) => {
    try {
        console.log(`Fetching ${dataName} from:`, url);

        // 시스템 관리자인 경우 공급업체 ID를 쿼리 파라미터로 추가
        let finalUrl = url;
        if (isSystemAdmin.value && selectedSupplierId.value) {
            const separator = url.includes('?') ? '&' : '?';
            finalUrl = `${url}${separator}supplierId=${selectedSupplierId.value}`;
        }

        console.log(`Final URL: ${finalUrl}`);

        const response = await axios.get(finalUrl);

        console.log(`${dataName} 응답: `, response.data);
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

        // 시스템 관리자 권한 체크
        const userRole = response.data.data.role?.roleName || '';
        const userCompId = currentUser.value.compId || '';

        console.log('사용자 역할:', userRole);
        console.log('사용자 compId:', userCompId);

        // 시스템 관리자 또는 본사 권한 체크
        isSystemAdmin.value = userRole.toUpperCase().includes('SYSTEM_ADMIN') || userRole.toUpperCase().includes('GENERAL_MANAGER') || userCompId === 'COM10001' || userRole.toUpperCase().includes('HEADQUARTERS');

        console.log('현재 사용자:', currentUser.value);
        console.log('시스템 관리자 권한:', isSystemAdmin.value);

        if (!isSystemAdmin.value) {
            // 공급업체 사용자인 경우 자신의 회사 ID 사용
            selectedSupplierId.value = userCompId;
        }
    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
        errorMessage.value = '사용자 정보를 불러올 수 없습니다.';
    }
};

// 공급업체 목록 조회 (시스템 관리자 전용)
const fetchAvailableSuppliers = async () => {
    if (!isSystemAdmin.value) return;

    try {
        const response = await axios.get(`${API_BASE_URL}/suppliers`);
        availableSuppliers.value = response.data || [];
        console.log('사용 가능한 공급업체들:', availableSuppliers.value);

        if (availableSuppliers.value.length > 0 && !selectedSupplierId.value) {
            selectedSupplierId.value = availableSuppliers.value[0].COMP_ID;
        } else if (availableSuppliers.value.length === 0) {
            errorMessage.value = '등록된 공급업체가 없습니다. 시스템 관리자에게 문의하세요.';
        }
    } catch (error) {
        console.error('공급업체 목록 조회 실패:', error);
        availableSuppliers.value = [];
        errorMessage.value = '공급업체 목록을 불러올 수 없습니다. 네트워크 연결을 확인하세요.';
    }
};

// 공급업체 변경 시 호출
const onSupplierChange = () => {
    console.log('선택된 공급업체 변경:', selectedSupplierId.value);
    if (selectedSupplierId.value) {
        loadAllData();
    }
};

// 공급업체 정보 조회
const fetchSupplierInfo = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/info`, '공급업체 정보');
        supplierInfo.value = data || {};
    } catch (error) {
        console.error('공급업체 정보 로딩 실패:', error);
        supplierInfo.value = {
            COMP_NAME: '공급업체 정보 로딩 실패',
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
            kpiData.value = {
                monthlyOrders: data.monthlyOrders || '0개',
                ordersGrowth: data.ordersGrowth || '+0개',

                completionRate: data.completionRate || '0%',
                completionRateChange: data.completionRateChange || '+0.0%',

                avgDeliveryTime: data.avgDeliveryTime || '0일',
                deliveryTimeChange: data.deliveryTimeChange || '0일',

                qualityScore: data.qualityScore || '0건',
                qualityScoreChange: data.qualityScoreChange || '+0건'
            };

            console.log('공급업체 KPI 데이터 매핑 완료:', kpiData.value);
        }
    } catch (error) {
        console.error('KPI 데이터 로딩 실패:', error);
        errorMessage.value = 'KPI 데이터를 불러오는 중 오류가 발생했습니다.';
    }
};

// 진행 중인 발주 현황 조회
const fetchActiveOrders = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/active-orders`, '진행 중인 발주');

        if (data && Array.isArray(data) && data.length > 0) {
            activeOrders.value = data.map((order) => ({
                orderId: order.ORDER_ID || order.orderId || '알 수 없음',
                orderDate: order.ORDER_DATE || order.orderDate || '',
                totalAmount: order.TOTAL_AMOUNT || order.totalAmount || '0원',
                status: order.STATUS || order.status || 'UNKNOWN'
            }));

            console.log('처리된 진행 중인 발주 데이터:', activeOrders.value);
        } else {
            activeOrders.value = [];
            console.log('진행 중인 발주 데이터가 비어있습니다.');
        }
    } catch (error) {
        console.error('진행 중인 발주 데이터 로딩 실패:', error);
        activeOrders.value = [];
    }
};

// 상위 공급 상품 조회
const fetchTopProducts = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/top-products`, '상위 공급 상품');

        if (data && Array.isArray(data) && data.length > 0) {
            topProducts.value = data.map((product) => ({
                productId: product.PRODUCT_ID || product.productId || 'unknown',
                productName: product.PRODUCT_NAME || product.productName || '알 수 없음',
                totalQuantity: product.TOTAL_QUANTITY || product.totalQuantity || 0,
                totalAmount: product.TOTAL_AMOUNT || product.totalAmount || 0
            }));

            console.log('처리된 상위 공급 상품 데이터:', topProducts.value);
        } else {
            topProducts.value = [];
            console.log('상위 공급 상품 데이터가 비어있습니다.');
        }
    } catch (error) {
        console.error('상위 공급 상품 데이터 로딩 실패:', error);
        topProducts.value = [];
    }
};

// 알림 데이터 조회
const fetchAlerts = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/alerts`, '알림');

        if (data && Array.isArray(data) && data.length > 0) {
            alerts.value = data.map((alert, index) => ({
                ...alert,
                id: alert.id || `alert-${index}`,
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

// 수주 트렌드 차트 생성
const createOrderTrendChart = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/order-trend`, '수주 트렌드');

        if (!orderTrendChart.value) {
            console.error('수주 트렌드 차트 요소를 찾을 수 없습니다.');
            return;
        }

        if (trendChartInstance) {
            trendChartInstance.destroy();
        }

        const ctx = orderTrendChart.value.getContext('2d');

        if (!data || !Array.isArray(data) || data.length === 0) {
            console.warn('수주 트렌드 데이터가 없습니다.');
            createEmptyOrderTrendChart(ctx);
            return;
        }

        // 현재 월 기준으로 최근 6개월 생성
        const currentDate = new Date();
        const currentYear = currentDate.getFullYear();
        const currentMonth = currentDate.getMonth() + 1;

        const recentMonths = [];
        for (let i = 5; i >= 0; i--) {
            const targetDate = new Date(currentYear, currentMonth - 1 - i, 1);
            const monthStr = `${targetDate.getFullYear()}-${String(targetDate.getMonth() + 1).padStart(2, '0')}`;
            recentMonths.push(monthStr);
        }

        // 데이터 매핑
        const orderCounts = recentMonths.map((month) => {
            const monthData = data.find((item) => item.MONTH === month || item.month === month);
            return monthData ? monthData.ORDER_COUNT || monthData.orderCount || 0 : 0;
        });

        trendChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: recentMonths,
                datasets: [
                    {
                        label: '월간 발주 수량',
                        data: orderCounts,
                        borderColor: '#48bb78',
                        backgroundColor: '#48bb78' + '20',
                        tension: 0.4,
                        fill: false,
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
                    legend: { position: 'bottom' },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return context.dataset.label + ': ' + context.parsed.y + '개';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        display: true,
                        title: { display: true, text: '월' },
                        grid: { color: '#e2e8f0' }
                    },
                    y: {
                        display: true,
                        title: { display: true, text: '발주 수량 (개)' },
                        beginAtZero: true,
                        grid: { color: '#e2e8f0' },
                        ticks: {
                            callback: function (value) {
                                return value + '개';
                            }
                        }
                    }
                }
            }
        });

        console.log('수주 트렌드 차트 생성 완료');
    } catch (error) {
        console.error('수주 트렌드 차트 생성 실패:', error);
        if (orderTrendChart.value) {
            createEmptyOrderTrendChart(orderTrendChart.value.getContext('2d'));
        }
    }
};

// 빈 수주 트렌드 차트 생성
const createEmptyOrderTrendChart = (ctx) => {
    trendChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['데이터 없음'],
            datasets: [
                {
                    label: '월간 수주량',
                    data: [0],
                    borderColor: '#a0aec0',
                    backgroundColor: '#a0aec0' + '20',
                    tension: 0.4,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { display: true, title: { display: true, text: '월' } },
                y: { display: true, title: { display: true, text: '수주량 (건)' }, beginAtZero: true }
            }
        }
    });
};

// 카테고리별 공급 비중 차트 생성
const createCategorySupplyChart = async () => {
    try {
        const data = await fetchData(`${API_BASE_URL}/category-supply`, '카테고리 공급');

        if (!categorySupplyChart.value) {
            console.error('카테고리 공급 차트 요소를 찾을 수 없습니다.');
            return;
        }

        if (categoryChartInstance) {
            categoryChartInstance.destroy();
        }

        const ctx = categorySupplyChart.value.getContext('2d');

        if (!data || !Array.isArray(data) || data.length === 0) {
            console.warn('카테고리 공급 데이터가 없습니다.');
            createEmptyCategorySupplyChart(ctx);
            return;
        }

        const labels = data.map((item) => item.CATEGORY || item.category || '기타');
        const supplyData = data.map((item) => item.SUPPLY_AMOUNT || item.supplyAmount || 0);

        categoryChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [
                    {
                        data: supplyData,
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
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? ((context.parsed / total) * 100).toFixed(1) : '0.0';
                                const value = context.parsed.toLocaleString('ko-KR');
                                return context.label + ': ' + value + '원 (' + percentage + '%)';
                            }
                        }
                    }
                }
            }
        });

        console.log('카테고리 공급 차트 생성 완료');
    } catch (error) {
        console.error('카테고리 공급 차트 생성 실패:', error);
        if (categorySupplyChart.value) {
            createEmptyCategorySupplyChart(categorySupplyChart.value.getContext('2d'));
        }
    }
};

// 빈 카테고리 공급 차트 생성
const createEmptyCategorySupplyChart = (ctx) => {
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
    if (isSystemAdmin.value && !selectedSupplierId.value) {
        console.log('공급업체가 선택되지 않음');
        errorMessage.value = '본사 계정으로 접속했으나 공급업체가 선택되지 않았습니다. 공급업체를 선택해주세요.';
        return;
    }

    isLoading.value = true;
    errorMessage.value = '';

    try {
        console.log('공급업체 대시보드 데이터 로딩 시작');

        // 기본 데이터 병렬 로딩
        const results = await Promise.allSettled([fetchSupplierInfo(), fetchKpiData(), fetchActiveOrders(), fetchTopProducts(), fetchAlerts()]);

        // 결과 확인
        results.forEach((result, index) => {
            const apiNames = ['공급업체정보', 'KPI', '진행중인발주', '상위공급상품', '알림'];
            if (result.status === 'rejected') {
                console.warn(`${apiNames[index]} API 실패:`, result.reason);
            }
        });

        // 차트 데이터 순차 로딩
        await nextTick();
        await createOrderTrendChart();
        await createCategorySupplyChart();

        lastUpdated.value = new Date().toLocaleString('ko-KR');
        console.log('공급업체 대시보드 데이터 로딩 완료');
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
const formatNumber = (num) => {
    return (num || 0).toLocaleString('ko-KR');
};

const formatAmount = (amount) => {
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

const getStatusClass = (status) => {
    switch (status) {
        case 'COMPLETED':
            return 'bg-green-100 text-green-800';
        case 'IN_PROGRESS':
            return 'bg-blue-100 text-blue-800';
        case 'PENDING':
            return 'bg-orange-100 text-orange-800';
        case 'CANCELLED':
            return 'bg-red-100 text-red-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
};

const getStatusText = (status) => {
    const statusMap = {
        COMPLETED: '완료',
        IN_PROGRESS: '진행중',
        PENDING: '대기',
        CANCELLED: '취소',
        UNKNOWN: '알 수 없음'
    };
    return statusMap[status] || '알 수 없음';
};

const getAlertIcon = (alertType) => {
    const icons = {
        DELIVERY_DELAY: '🚚',
        QUALITY_ISSUE: '⚠️',
        ORDER_URGENT: '📋',
        STOCK_REQUEST: '📦',
        PAYMENT_DUE: '💰',
        INFO: 'ℹ️'
    };
    return icons[alertType] || '⚠️';
};

// 데이터 새로고침
const refreshData = () => {
    console.log('수동 새로고침 시작');
    loadAllData();
};

// 컴포넌트 마운트
onMounted(async () => {
    console.log('공급업체 대시보드 컴포넌트 마운트됨');

    await fetchCurrentUser();

    if (isSystemAdmin.value) {
        await fetchAvailableSuppliers();
    }

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
    console.log('공급업체 대시보드 컴포넌트 언마운트됨');

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
        <div class="flex justify-between items-start mb-8">
            <div>
                <h1 class="text-surface-900 dark:text-surface-0 text-4xl font-bold mb-2">{{ supplierInfo.COMP_NAME || '공급업체' }} SCM 대시보드</h1>
                <p class="text-muted-color text-lg mb-4">
                    {{ supplierInfo.COMP_TYPE_NAME || '' }} | {{ supplierInfo.ADDRESS || '' }}
                    <span v-if="supplierInfo.TOTAL_PURCHASE_COUNT !== undefined" class="ml-4 text-sm"> (총 발주 건수: {{ supplierInfo.TOTAL_PURCHASE_COUNT }}건) </span>
                </p>

                <!-- 본사용 공급업체 선택 드롭다운 -->
                <div v-if="isSystemAdmin" class="mb-4">
                    <label for="supplierSelect" class="block text-muted-color text-sm font-medium mb-2">조회할 공급업체:</label>
                    <Dropdown
                        id="supplierSelect"
                        v-model="selectedSupplierId"
                        @change="onSupplierChange"
                        :options="availableSuppliers"
                        optionLabel="COMP_NAME"
                        optionValue="COMP_ID"
                        :placeholder="availableSuppliers.length === 0 ? '공급업체를 로딩 중...' : '공급업체를 선택하세요'"
                        :disabled="availableSuppliers.length === 0 || isLoading"
                        class="w-80"
                    />
                    <p v-if="availableSuppliers.length === 0 && isSystemAdmin" class="text-red-500 text-sm mt-2">등록된 공급업체가 없거나 로딩 중입니다.</p>
                    <p v-else-if="availableSuppliers.length > 0 && selectedSupplierId" class="text-green-600 text-sm mt-2">{{ availableSuppliers.find((s) => s.COMP_ID === selectedSupplierId)?.COMP_NAME }} 데이터를 표시하고 있습니다.</p>
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
            <!-- 1. 월간 발주 수량 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">월간 발주 수량</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.monthlyOrders || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.ordersGrowth)]">
                            {{ kpiData.ordersGrowth || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-green-100 dark:bg-green-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-shopping-bag text-green-500 text-xl"></i>
                    </div>
                </div>
            </div>

            <!-- 2. 발주 완료율 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">발주 완료율</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.completionRate || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.completionRateChange)]">
                            {{ kpiData.completionRateChange || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-blue-100 dark:bg-blue-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-check-circle text-blue-500 text-xl"></i>
                    </div>
                </div>
            </div>

            <!-- 3. 평균 납기일 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">평균 납기일</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.avgDeliveryTime || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.deliveryTimeChange, true)]">
                            {{ kpiData.deliveryTimeChange || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-orange-100 dark:bg-orange-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-clock text-orange-500 text-xl"></i>
                    </div>
                </div>
            </div>

            <!-- 4. 월간 발주 건수 -->
            <div class="card">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="text-muted-color text-sm font-medium mb-2 uppercase tracking-wide">월간 발주 건수</div>
                        <div class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                            {{ kpiData.qualityScore || '로딩 중...' }}
                        </div>
                        <div :class="['text-sm font-medium mt-1', getChangeClass(kpiData.qualityScoreChange)]">
                            {{ kpiData.qualityScoreChange || '계산 중...' }}
                        </div>
                    </div>
                    <div class="flex items-center justify-center bg-purple-100 dark:bg-purple-400/10 rounded-lg w-12 h-12">
                        <i class="pi pi-file-o text-purple-500 text-xl"></i>
                    </div>
                </div>
            </div>
        </div>

        <!-- 차트 섹션 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            <!-- 발주 수량 트렌드 차트 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">월간 발주 수량 트렌드</div>
                <div class="h-80">
                    <canvas ref="orderTrendChart"></canvas>
                </div>
            </div>

            <!-- 카테고리별 발주 비중 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">카테고리별 발주 비중</div>
                <div class="h-80">
                    <canvas ref="categorySupplyChart"></canvas>
                </div>
            </div>
        </div>

        <!-- 상세 정보 섹션 -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- 최근 발주 현황 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">최근 발주 현황</div>
                <div v-if="activeOrders.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">최근 발주 내역이 없습니다.</div>
                <div v-else class="space-y-4">
                    <div v-for="order in activeOrders" :key="order.orderId" class="flex justify-between items-center p-4 bg-surface-50 dark:bg-surface-800 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-700 transition-colors">
                        <div>
                            <div class="text-surface-900 dark:text-surface-0 font-medium">발주번호: {{ order.orderId }}</div>
                            <div class="text-muted-color text-sm">{{ order.orderDate }} | {{ order.totalAmount }}</div>
                        </div>
                        <div :class="['px-3 py-1 rounded-full text-xs font-semibold', getStatusClass(order.status)]">
                            {{ getStatusText(order.status) }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- 주요 발주 상품 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">주요 발주 상품</div>
                <div v-if="topProducts.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">발주 상품 데이터를 불러오는 중입니다...</div>
                <div v-else class="space-y-4">
                    <div v-for="(product, index) in topProducts" :key="product.productId" class="flex items-center p-4 bg-surface-50 dark:bg-surface-800 rounded-lg">
                        <div class="flex items-center justify-center bg-primary text-primary-contrast rounded-full w-8 h-8 mr-4 text-sm font-bold">
                            {{ index + 1 }}
                        </div>
                        <div class="flex-1">
                            <div class="text-surface-900 dark:text-surface-0 font-medium mb-1">{{ product.productName }}</div>
                            <div class="text-muted-color text-sm">발주량: {{ formatNumber(product.totalQuantity) }}개 | 발주액: {{ formatAmount(product.totalAmount) }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 발주/납기 알림 -->
            <div class="card">
                <div class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-6">발주/납기 알림</div>
                <div v-if="alerts.length === 0" class="text-center text-muted-color p-8 bg-surface-50 dark:bg-surface-800 rounded-lg">현재 알림이 없습니다.</div>
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
