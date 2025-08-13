<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';

import SearchForm from '@/components/inputForm/SearchForm.vue';
import AccountTable from './AccountTable.vue';
import ToggleButton from 'primevue/togglebutton';

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
    // {
    //     type: 'select',
    //     label: '회계단위',
    //     value: '',
    //     placeholder: '회계단위를 선택하세요',
    //     name: 'accountUnit',
    //     options: [
    //         { name: '전년도', value: '전기' },
    //         { name: '올해', value: '당해' }
    //     ]
    // },
    { type: 'text', label: '계정코드', value: '', placeholder: '계정코드를 입력하세요', name: 'accountId' },
    { type: 'text', label: '계정과목', value: '', placeholder: '계정과목을 입력하세요', name: 'accountName' },
    { type: 'dateRange', label: '회계기간', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'dateRange' },
    // { type: 'text', label: '거래처코드', value: '', placeholder: '거래처코드를 입력하세요', name: 'compId' },
    { type: 'text', label: '회사명', value: '', placeholder: '회사명을 입력하세요', name: 'compName' }
];

// 테이블에 표시할 데이터를 담을 ref
const items = ref([]);
const loading = ref(false);
// 'Balance' 컬럼의 고정 여부를 제어할 ref
const balanceFrozen = ref(false);

// 데이터 포맷팅 함수
function formatCurrency(value) {
    if (value === null || value === undefined || value === '') return '0';
    const numberValue = Number(value);
    if (isNaN(numberValue)) return value;

    return new Intl.NumberFormat('ko-KR').format(numberValue) + '원';
}

// 날짜 포맷팅 함수
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
}

// 💡 테이블 컬럼 구조를 데이터로 정의 (computed 사용으로 반응성 유지)
const tableColumns = computed(() => [
    { field: 'accountLederId', header: '거래처원장ID', style: 'min-width: 150px', frozen: true, class: 'font-bold' },
    { field: 'compId', header: '회사코드', style: 'min-width: 120px' },
    { field: 'compName', header: '회사명', style: 'min-width: 120px' },
    { field: 'accountId', header: '계정코드', style: 'min-width: 120px' },
    { field: 'accountName', header: '계정명', style: 'min-width: 120px' },
    { field: 'increase', header: '차변', style: 'min-width: 150px' },
    { field: 'decrease', header: '대변', style: 'min-width: 150px' },
    { field: 'writeDate', header: '작성일', style: 'min-width: 150px' },
    { field: 'detail', header: '상세', style: 'min-width: 200px' },
    // { field: 'productName', header: '제품명', style: 'min-width: 120px' },
    // { field: 'productId', header: '제품코드', style: 'min-width: 120px' },
    { field: 'balance', header: '잔액', style: 'min-width: 150px', alignFrozen: 'right', frozen: balanceFrozen.value }
]);

// 데이터 조회 함수
async function fetchData(searchParams = {}) {
    loading.value = true;
    try {
        const result = await axios.get('/api/account', { params: searchParams });
        items.value = result.data || [];
        console.log('데이터 조회 성공:', items.value.length, '건');
    } catch (error) {
        console.error('데이터를 불러오는 데 실패했습니다:', error);
        items.value = [];
        // 에러 토스트 메시지 (PrimeVue Toast 사용 시)
        // toast.add({severity:'error', summary: '오류', detail: '데이터 조회에 실패했습니다.', life: 3000});
    } finally {
        loading.value = false;
    }
}

// 검색 버튼 클릭 시 실행될 함수
function handleSearchData(searchData) {
    console.log('검색 조건:', searchData);
    fetchData(searchData);
}

// 컴포넌트가 마운트될 때 초기 데이터를 가져옵니다.
onMounted(() => {
    fetchData();
});
</script>

<template>
    <div class="card">
        <SearchForm :filters="filters" @searchData="handleSearchData"></SearchForm>
        <div class="font-semibold text-xl mb-4">거래처원장</div>
        <ToggleButton 
            v-model="balanceFrozen" 
            onIcon="pi pi-lock" 
            offIcon="pi pi-lock-open" 
            onLabel="잔액고정" 
            offLabel="잔액고정해제" 
            class="mb-4"
        />

        <AccountTable 
            :data="items" 
            :columns="tableColumns" 
            :headerInfo="{ title: '거래처원장 목록' }" 
            :loading="loading"
            dataKey="accountLederId"
        >
            <!-- 잔액 컬럼 커스텀 포맷 -->
            <template #body-balance="{ data }">
                <span class="font-bold text-green-600">{{ formatCurrency(data.balance) }}</span>
            </template>
            <!-- 차변 컬럼 커스텀 포맷 -->
            <template #body-increase="{ data }">
                <span class="text-blue-600">{{ formatCurrency(data.increase) }}</span>
            </template>
            <!-- 대변 컬럼 커스텀 포맷 -->
            <template #body-decrease="{ data }">
                <span class="text-red-600">{{ formatCurrency(data.decrease) }}</span>
            </template>
            <!-- 작성일 컬럼 커스텀 포맷 -->
            <template #body-writeDate="{ data }">
                <span>{{ formatDate(data.writeDate) }}</span>
            </template>
        </AccountTable>
    </div>
</template>