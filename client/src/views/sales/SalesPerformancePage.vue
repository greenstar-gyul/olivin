<script setup>
import { onMounted, ref, computed } from 'vue';
import axios from '@/service/axios.js';
import { useAuth } from '@/composables/useAuth';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const { user } = useAuth();

// 기간 선택 타입
const periodTypes = [
    { label: '일/주', value: 'daily' },
    { label: '월', value: 'monthly' },
    { label: '분기', value: 'quarterly' },
    { label: '직접선택', value: 'custom' }
];

const selectedPeriodType = ref('daily');

// 세분화 옵션들
const dailyOptions = [
    { label: '오늘', value: 'today' },
    { label: '어제', value: 'yesterday' },
    { label: '이번 주', value: 'thisWeek' },
    { label: '지난 주', value: 'lastWeek' }
];

const monthlyOptions = [
    { label: '이번 달', value: 'thisMonth' },
    { label: '지난 달', value: 'lastMonth' },
    { label: '3개월', value: 'last3Months' },
    { label: '6개월', value: 'last6Months' }
];

const quarterlyOptions = [
    { label: '이번 분기', value: 'thisQuarter' },
    { label: '지난 분기', value: 'lastQuarter' },
    { label: '올해', value: 'thisYear' },
    { label: '작년', value: 'lastYear' }
];

const selectedDetailOption = ref('today');

// 직접 선택 날짜
const customDateRange = ref();

// 현재 적용된 기간
const fromDate = ref();
const toDate = ref();

// 지점 선택 (본사만)
const selectedBranch = ref();
const availableBranches = ref([]);
const isHeadquarter = computed(() => user.value?.compId === 'COM10001');

// 매출 데이터
const salesSummary = ref({
    grossSales: 0, // 총매출
    returns: 0, // 반품액
    netSales: 0, // 실매출
    totalOrders: 0, // 총 주문건수
    averageOrder: 0, // 평균 주문금액
    totalCustomers: 0, // 총 고객수
    compareData: {
        // 전기간 대비 데이터
        grossSalesChange: 0,
        netSalesChange: 0,
        ordersChange: 0
    }
});

// 로딩 상태
const isLoading = ref(false);
const errorMessage = ref('');

// 상세 데이터 관련 변수들
const detailData = ref([]);
const isDetailLoading = ref(false);
const detailErrorMessage = ref('');

// 본사인 경우 지점 목록 로드
const loadBranches = async () => {
    if (!isHeadquarter.value) return;

    try {
        const response = await axios.get('/api/search/branches/all');
        availableBranches.value = response.data.map((branch) => ({
            label: branch.compName,
            value: branch.compId
        }));

        // "전지점 요약" 옵션 추가
        availableBranches.value.unshift({
            label: '전 지점 요약',
            value: 'ALL_BRANCHES'
        });

        // 기본값으로 첫 번째 지점 선택
        if (availableBranches.value.length > 0) {
            selectedBranch.value = availableBranches.value[0].value;
        }
    } catch (error) {
        console.error('지점 목록 로드 실패:', error);
        errorMessage.value = '지점 목록을 불러오는 중 오류가 발생했습니다.';
    }
};

// 날짜 계산 함수들
const getDateRange = (type, option) => {
    const today = new Date();
    const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());

    switch (type) {
        case 'daily':
            switch (option) {
                case 'today':
                    return { from: startOfToday, to: startOfToday };
                case 'yesterday':
                    const yesterday = new Date(startOfToday.getTime() - 24 * 60 * 60 * 1000);
                    return { from: yesterday, to: new Date(yesterday.getTime() + 24 * 60 * 60 * 1000 - 1) };
                case 'thisWeek':
                    const thisWeekStart = new Date(startOfToday);
                    const dayOfWeek = startOfToday.getDay();
                    // 월요일 시작: 일요일(0)이면 -6, 다른 날은 -1
                    const daysToSubtract = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
                    thisWeekStart.setDate(startOfToday.getDate() - daysToSubtract);
                    return { from: thisWeekStart, to: today };
                case 'lastWeek':
                    const lastWeekStart = new Date(startOfToday);
                    const lastDayOfWeek = startOfToday.getDay();
                    // 지난주 월요일 계산: 현재 요일에 따라 조정
                    const daysToLastMonday = lastDayOfWeek === 0 ? 13 : lastDayOfWeek + 6;
                    lastWeekStart.setDate(startOfToday.getDate() - daysToLastMonday);
                    const lastWeekEnd = new Date(lastWeekStart);
                    lastWeekEnd.setDate(lastWeekStart.getDate() + 6);
                    return { from: lastWeekStart, to: lastWeekEnd };
            }
            break;
        case 'monthly':
            switch (option) {
                case 'thisMonth':
                    return {
                        from: new Date(today.getFullYear(), today.getMonth(), 1),
                        to: today
                    };
                case 'lastMonth':
                    const lastMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1);
                    const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0);
                    return { from: lastMonth, to: lastMonthEnd };
                case 'last3Months':
                    return {
                        from: new Date(today.getFullYear(), today.getMonth() - 2, 1),
                        to: today
                    };
                case 'last6Months':
                    return {
                        from: new Date(today.getFullYear(), today.getMonth() - 5, 1),
                        to: today
                    };
            }
            break;
        case 'quarterly':
            const quarter = Math.floor(today.getMonth() / 3);
            switch (option) {
                case 'thisQuarter':
                    return {
                        from: new Date(today.getFullYear(), quarter * 3, 1),
                        to: today
                    };
                case 'lastQuarter':
                    const lastQuarterStart =
                        quarter === 0
                            ? new Date(today.getFullYear() - 1, 9, 1) // 작년 4분기
                            : new Date(today.getFullYear(), (quarter - 1) * 3, 1);
                    const lastQuarterEnd = quarter === 0 ? new Date(today.getFullYear() - 1, 11, 31) : new Date(today.getFullYear(), quarter * 3, 0);
                    return { from: lastQuarterStart, to: lastQuarterEnd };
                case 'thisYear':
                    return {
                        from: new Date(today.getFullYear(), 0, 1),
                        to: today
                    };
                case 'lastYear':
                    return {
                        from: new Date(today.getFullYear() - 1, 0, 1),
                        to: new Date(today.getFullYear() - 1, 11, 31)
                    };
            }
            break;
    }
    return { from: startOfToday, to: today };
};

// 기간 선택 변경 처리
const handlePeriodTypeChange = async () => {
    if (selectedPeriodType.value === 'daily') {
        selectedDetailOption.value = 'today';
    } else if (selectedPeriodType.value === 'monthly') {
        selectedDetailOption.value = 'thisMonth';
    } else if (selectedPeriodType.value === 'quarterly') {
        selectedDetailOption.value = 'thisQuarter';
    }

    if (selectedPeriodType.value !== 'custom') {
        updateDateRange();
        // 새로운 기간으로 데이터 자동 조회
        await searchSalesPerformance();
    }
};

// 세부 옵션 변경 처리
const handleDetailOptionChange = () => {
    updateDateRange();
};

// 날짜 범위 업데이트
const updateDateRange = () => {
    if (selectedPeriodType.value === 'custom') {
        if (customDateRange.value && customDateRange.value.length === 2) {
            fromDate.value = customDateRange.value[0];
            toDate.value = customDateRange.value[1];
        }
    } else {
        const dateRange = getDateRange(selectedPeriodType.value, selectedDetailOption.value);
        fromDate.value = dateRange.from;
        toDate.value = dateRange.to;
    }
};

// 직접 선택 날짜 변경 처리
const handleCustomDateChange = () => {
    updateDateRange();
};

// 매출 데이터 조회
const searchSalesPerformance = async () => {
    if (!fromDate.value || !toDate.value) {
        errorMessage.value = '조회 기간을 선택해주세요.';
        return;
    }

    isLoading.value = true;
    errorMessage.value = '';

    try {
        let summaryData;

        // 전지점 요약인 경우 테이블 데이터를 합산하여 사용
        if (selectedBranch.value === 'ALL_BRANCHES') {
            // 지점별 데이터를 먼저 조회
            await loadDetailData();

            // 지점별 데이터를 합산
            const totalGrossSales = detailData.value.reduce((sum, item) => sum + (item.GROSSSALES || 0), 0);
            const totalReturns = detailData.value.reduce((sum, item) => sum + (item.RETURNS || 0), 0);
            const totalNetSales = detailData.value.reduce((sum, item) => sum + (item.NETSALES || 0), 0);
            const totalOrders = detailData.value.reduce((sum, item) => sum + (item.TOTALORDERS || 0), 0);

            summaryData = {
                grossSales: totalGrossSales,
                returns: totalReturns,
                netSales: totalNetSales,
                totalOrders: totalOrders,
                averageOrder: totalOrders > 0 ? Math.round(totalGrossSales / totalOrders) : 0,
                totalCustomers: 0, // 전지점 고객수는 계산하지 않음
                compareData: {
                    grossSalesChange: 0,
                    netSalesChange: 0,
                    ordersChange: 0
                }
            };
        } else {
            // 특정 지점 조회
            const params = {
                fromDate: formatDateForAPI(fromDate.value),
                toDate: formatDateForAPI(toDate.value)
            };

            // 본사가 아닌 경우 자신의 지점 ID 추가
            if (!isHeadquarter.value) {
                params.compId = user.value.compId;
            } else if (selectedBranch.value) {
                params.compId = selectedBranch.value;
            }

            const response = await axios.get('/api/sales/performance/summary', { params });

            // 응답 데이터 검증
            if (response.data && typeof response.data === 'object') {
                summaryData = response.data;
            } else {
                throw new Error('서버에서 올바르지 않은 데이터를 받았습니다.');
            }
        }

        salesSummary.value = summaryData;
    } catch (error) {
        console.error('매출 실적 조회 실패:', error);

        // 에러 메시지 설정
        if (error.response?.status === 404) {
            errorMessage.value = '해당 기간의 매출 데이터를 찾을 수 없습니다.';
        } else if (error.response?.status === 500) {
            errorMessage.value = '서버 오류로 인해 데이터를 불러올 수 없습니다.';
        } else if (error.message) {
            errorMessage.value = error.message;
        } else {
            errorMessage.value = '매출 실적을 조회할 수 없습니다. 잠시 후 다시 시도해주세요.';
        }

        // 데이터 초기화 (임시 데이터 절대 사용하지 않음)
        salesSummary.value = {
            grossSales: 0,
            returns: 0,
            netSales: 0,
            totalOrders: 0,
            averageOrder: 0,
            totalCustomers: 0,
            compareData: {
                grossSalesChange: 0,
                netSalesChange: 0,
                ordersChange: 0
            }
        };
    } finally {
        isLoading.value = false;
    }
};

// 날짜 포맷팅
const formatDateForAPI = (date) => {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

const formatCurrency = (amount) => {
    return Number(amount || 0).toLocaleString('ko-KR') + '원';
};

const formatNumber = (num) => {
    return Number(num || 0).toLocaleString('ko-KR');
};

const formatPercent = (num) => {
    const value = Number(num || 0);
    return (value >= 0 ? '+' : '') + value.toFixed(1) + '%';
};

const getChangeClass = (value) => {
    if (!value) return 'text-muted-color';
    return value >= 0 ? 'text-green-500' : 'text-red-500';
};

// 현재 선택된 세분화 옵션들
const currentDetailOptions = computed(() => {
    switch (selectedPeriodType.value) {
        case 'daily':
            return dailyOptions;
        case 'monthly':
            return monthlyOptions;
        case 'quarterly':
            return quarterlyOptions;
        default:
            return [];
    }
});

// 상세 데이터 조회
const loadDetailData = async () => {
    if (!fromDate.value || !toDate.value) {
        detailErrorMessage.value = '조회 기간을 선택해주세요.';
        return;
    }

    isDetailLoading.value = true;
    detailErrorMessage.value = '';

    try {
        const params = {
            fromDate: formatDateForAPI(fromDate.value),
            toDate: formatDateForAPI(toDate.value)
        };

        let endpoint = '';
        if (selectedBranch.value === 'ALL_BRANCHES') {
            // 지점별 매출 조회
            endpoint = '/api/sales/performance/branches';
            console.log('지점별 데이터 조회:', endpoint, params);
        } else {
            // 제품별 매출 조회
            endpoint = '/api/sales/performance/products';
            if (!isHeadquarter.value) {
                params.compId = user.value.compId;
            } else {
                params.compId = selectedBranch.value;
            }
            console.log('제품별 데이터 조회:', endpoint, params);
        }

        const response = await axios.get(endpoint, { params });
        console.log('상세 데이터 응답:', response.data);
        detailData.value = response.data || [];
    } catch (error) {
        console.error('상세 데이터 조회 실패:', error);

        if (error.response?.status === 404) {
            detailErrorMessage.value = '해당 기간의 데이터를 찾을 수 없습니다.';
        } else if (error.response?.status === 500) {
            detailErrorMessage.value = '서버 오류로 인해 데이터를 불러올 수 없습니다.';
        } else {
            detailErrorMessage.value = '데이터를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.';
        }

        detailData.value = [];
    } finally {
        isDetailLoading.value = false;
    }
};

// 지점 변경 핸들러
const handleBranchChange = async () => {
    await searchSalesPerformance();
    // 지점 변경 시 상세 데이터도 함께 새로고침
    await loadDetailData();
};

// 초기화
onMounted(async () => {
    updateDateRange(); // 기본값: 오늘
    await loadBranches();
    await searchSalesPerformance();
    await loadDetailData();
});
</script>

<template>
    <div class="surface-ground min-h-screen p-6">
        <!-- 헤더 -->
        <div class="flex justify-between items-center mb-8">
            <div>
                <h1 class="text-surface-900 dark:text-surface-0 text-4xl font-bold mb-2">매출 실적 조회</h1>
                <p class="text-muted-color text-lg">선택한 기간 동안의 매출 현황을 확인하세요</p>
            </div>
            <div class="flex items-center gap-4">
                <!-- 지점 선택 (본사만) -->
                <div v-if="isHeadquarter" class="flex items-center gap-2">
                    <label class="text-muted-color text-sm font-medium">지점:</label>
                    <Dropdown v-model="selectedBranch" @change="handleBranchChange" :options="availableBranches" optionLabel="label" optionValue="value" placeholder="지점 선택" class="w-48" />
                </div>
                <Button @click="searchSalesPerformance" :loading="isLoading" icon="pi pi-refresh" label="새로고침" outlined />
            </div>
        </div>

        <!-- 기간 선택 -->
        <div class="card mb-6 rounded-xl shadow-sm">
            <div class="mb-4">
                <!-- 기간 타입 선택 -->
                <div class="grid grid-cols-4 gap-3 mb-4">
                    <Button
                        v-for="type in periodTypes"
                        :key="type.value"
                        :label="type.label"
                        :severity="selectedPeriodType === type.value ? 'primary' : 'secondary'"
                        :outlined="selectedPeriodType !== type.value"
                        size="large"
                        class="h-12 rounded-lg shadow-sm"
                        @click="
                            selectedPeriodType = type.value;
                            handlePeriodTypeChange();
                        "
                    />
                </div>

                <!-- 세분화 옵션 (직접선택 제외) -->
                <div v-if="selectedPeriodType !== 'custom'" class="grid gap-3 mb-4" :class="`grid-cols-${currentDetailOptions.length}`">
                    <Button
                        v-for="option in currentDetailOptions"
                        :key="option.value"
                        :label="option.label"
                        :severity="selectedDetailOption === option.value ? 'primary' : 'secondary'"
                        :outlined="selectedDetailOption !== option.value"
                        size="large"
                        class="h-12 rounded-lg shadow-sm"
                        @click="
                            selectedDetailOption = option.value;
                            handleDetailOptionChange();
                            searchSalesPerformance();
                            loadDetailData();
                        "
                    />
                </div>

                <!-- 직접 선택 날짜 -->
                <div v-if="selectedPeriodType === 'custom'" class="flex items-center gap-4">
                    <label class="text-muted-color text-sm font-medium">조회 기간:</label>
                    <Calendar
                        v-model="customDateRange"
                        @update:modelValue="
                            handleCustomDateChange();
                            searchSalesPerformance();
                            loadDetailData();
                        "
                        selectionMode="range"
                        dateFormat="yy-mm-dd"
                        :showIcon="true"
                        :showButtonBar="true"
                        placeholder="기간을 선택하세요"
                        class="w-80"
                    />
                </div>

                <!-- 현재 선택된 기간 표시 -->
                <div class="text-center mt-4 p-3 bg-surface-50 dark:bg-surface-800 rounded-lg shadow-sm">
                    <div class="text-muted-color text-sm">조회 기간</div>
                    <div class="text-surface-900 dark:text-surface-0 font-semibold">{{ fromDate ? fromDate.toLocaleDateString('ko-KR') : '' }} ~ {{ toDate ? toDate.toLocaleDateString('ko-KR') : '' }}</div>
                </div>
            </div>
        </div>

        <!-- 매출 요약 카드 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
            <!-- 로딩 상태 -->
            <template v-if="isLoading">
                <div v-for="n in 3" :key="n" class="card rounded-xl shadow-md h-32">
                    <div class="flex justify-center items-center h-full">
                        <i class="pi pi-spin pi-spinner text-2xl text-muted-color"></i>
                        <span class="ml-2 text-muted-color">로딩 중...</span>
                    </div>
                </div>
            </template>
            <!-- 에러 상태 -->
            <template v-else-if="errorMessage && !isLoading">
                <div class="col-span-full">
                    <div class="card rounded-xl shadow-md p-6 text-center">
                        <i class="pi pi-exclamation-triangle text-4xl text-red-500 mb-4"></i>
                        <h3 class="text-surface-900 dark:text-surface-0 text-xl font-semibold mb-2">데이터를 불러올 수 없습니다</h3>
                        <p class="text-muted-color mb-4">{{ errorMessage }}</p>
                        <Button @click="searchSalesPerformance" icon="pi pi-refresh" label="다시 시도" outlined class="rounded-lg" />
                    </div>
                </div>
            </template>
            <!-- 정상 데이터 표시 -->
            <template v-else>
                <!-- 총매출 -->
                <div class="card rounded-xl shadow-md h-48">
                    <div class="flex justify-between items-start h-full">
                        <div>
                            <div class="text-muted-color text-lg font-semibold mb-2 uppercase tracking-wide">총매출</div>
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-bold mb-2">
                                {{ formatCurrency(salesSummary.grossSales) }}
                            </div>
                            <div :class="['text-sm font-medium', getChangeClass(salesSummary.compareData.grossSalesChange)]">전기간 대비 {{ formatPercent(salesSummary.compareData.grossSalesChange) }}</div>
                        </div>
                    </div>
                </div>

                <!-- 반품액 -->
                <div class="card rounded-xl shadow-md h-48">
                    <div class="flex justify-between items-start h-full">
                        <div>
                            <div class="text-muted-color text-lg font-semibold mb-2 uppercase tracking-wide">반품액</div>
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-bold mb-2">
                                {{ formatCurrency(salesSummary.returns) }}
                            </div>
                            <div class="text-muted-color text-sm">총매출의 {{ salesSummary.grossSales > 0 ? ((salesSummary.returns / salesSummary.grossSales) * 100).toFixed(1) : '0.0' }}%</div>
                        </div>
                    </div>
                </div>

                <!-- 실매출 -->
                <div class="card rounded-xl shadow-md h-48">
                    <div class="flex justify-between items-start h-full">
                        <div>
                            <div class="text-muted-color text-lg font-semibold mb-2 uppercase tracking-wide">실매출</div>
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-bold mb-2">
                                {{ formatCurrency(salesSummary.netSales) }}
                            </div>
                            <div :class="['text-sm font-medium', getChangeClass(salesSummary.compareData.netSalesChange)]">전기간 대비 {{ formatPercent(salesSummary.compareData.netSalesChange) }}</div>
                        </div>
                    </div>
                </div>
            </template>
        </div>

        <!-- 상세 매출 데이터 테이블 -->
        <div class="card rounded-xl shadow-sm">
            <div class="flex justify-between items-center mb-6">
                <h3 class="text-surface-900 dark:text-surface-0 text-2xl font-bold">
                    {{ selectedBranch === 'ALL_BRANCHES' ? '지점별 매출 현황' : '제품별 매출 현황' }}
                </h3>
                <div class="flex items-center gap-4">
                    <Button @click="loadDetailData" :loading="isDetailLoading" icon="pi pi-refresh" label="새로고침" outlined size="small" />
                </div>
            </div>

            <!-- 로딩 상태 -->
            <div v-if="isDetailLoading" class="flex justify-center items-center h-64">
                <i class="pi pi-spin pi-spinner text-2xl text-muted-color"></i>
                <span class="ml-2 text-muted-color">데이터를 불러오는 중...</span>
            </div>

            <!-- 에러 상태 -->
            <div v-else-if="detailErrorMessage" class="text-center py-16">
                <i class="pi pi-exclamation-triangle text-4xl text-red-500 mb-4"></i>
                <h4 class="text-surface-900 dark:text-surface-0 text-lg font-semibold mb-2">데이터를 불러올 수 없습니다</h4>
                <p class="text-muted-color mb-4">{{ detailErrorMessage }}</p>
                <Button @click="loadDetailData" icon="pi pi-refresh" label="다시 시도" outlined class="rounded-lg" />
            </div>

            <!-- 데이터 테이블 -->
            <div v-else class="overflow-x-auto">
                <DataTable :value="detailData" :paginator="detailData.length > 10" :rows="10" class="p-datatable-lg" stripedRows>
                    <!-- 지점별 보기 -->
                    <Column v-if="selectedBranch === 'ALL_BRANCHES'" field="BRANCHNAME" header="지점명" sortable>
                        <template #body="slotProps">
                            <span class="text-lg font-semibold">{{ slotProps.data.BRANCHNAME || '데이터 없음' }}</span>
                        </template>
                    </Column>

                    <!-- 제품별 보기 -->
                    <Column v-else field="PRODUCTNAME" header="제품명" sortable>
                        <template #body="slotProps">
                            <span class="text-lg font-semibold">{{ slotProps.data.PRODUCTNAME || '데이터 없음' }}</span>
                        </template>
                    </Column>

                    <Column field="GROSSSALES" header="총매출" sortable>
                        <template #body="slotProps">
                            <span class="text-lg font-semibold">{{ formatCurrency(slotProps.data.GROSSSALES) }}</span>
                        </template>
                    </Column>

                    <Column field="NETSALES" header="실매출" sortable>
                        <template #body="slotProps">
                            <span class="text-lg font-bold text-green-600">{{ formatCurrency(slotProps.data.NETSALES) }}</span>
                        </template>
                    </Column>

                    <Column v-if="selectedBranch === 'ALL_BRANCHES'" field="TOTALORDERS" header="주문건수" sortable>
                        <template #body="slotProps">
                            <span class="text-lg font-semibold">{{ formatNumber(slotProps.data.TOTALORDERS) }}건</span>
                        </template>
                    </Column>

                    <Column v-else field="QUANTITY" header="판매수량" sortable>
                        <template #body="slotProps">
                            <span class="text-lg font-semibold">{{ formatNumber(slotProps.data.QUANTITY) }}개</span>
                        </template>
                    </Column>
                </DataTable>
            </div>

            <!-- 데이터 없음 (이 조건은 현재 사용되지 않음) -->
            <div v-if="false" class="text-center py-16">
                <i class="pi pi-info-circle text-4xl text-muted-color mb-4"></i>
                <h4 class="text-surface-900 dark:text-surface-0 text-lg font-semibold mb-2">조회된 데이터가 없습니다</h4>
                <p class="text-muted-color">선택한 기간에 해당하는 매출 데이터가 없습니다.</p>
            </div>
        </div>
    </div>
</template>
