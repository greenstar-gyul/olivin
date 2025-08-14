<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';

import SearchForm from '@/components/inputForm/SearchForm.vue';
import AccountTable from './AccountTable.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import ToggleButton from 'primevue/togglebutton';

// 검색 조건 필터 설정
const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
    // { type: 'item-search', label: '계정코드', value: '', placeholder: '계정코드를 선택하세요', name: 'accountId' },
    { type: 'item-search', label: '계정과목', value: '', placeholder: '계정과목을 선택하세요', name: 'accountName' },
    { type: 'dateRange', label: '회계기간', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'dateRange' },
    { type: 'text', label: '회사명', value: '', placeholder: '회사명을 입력하세요', name: 'compName' }
];

// 테이블에 표시할 데이터를 담을 ref
const items = ref([]);
const loading = ref(false);
// 'Balance' 컬럼의 고정 여부를 제어할 ref
const balanceFrozen = ref(false);

// 모달창의 테이블 헤더 정보
const accountHeaders = ref([
    { field: 'accountId', header: '계정코드' },
    { field: 'accountName', header: '계정과목' },
    { field: 'normalBalance', header: '대차구분' }
]);

// 모달창의 데이터 아이템
const accountItems = ref([]);

// 모달의 visible 상태를 관리하는 ref 변수
const accountModalVisible = ref(false);

// SearchForm의 ref 추가
const searchFormRef = ref(null);

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
    { field: 'balance', header: '잔액', style: 'min-width: 150px', alignFrozen: 'right', frozen: balanceFrozen.value }
]);

// 계정 데이터 조회 함수
const loadAccountItems = async () => {
    try {
        const response = await axios.get('/api/dbaccounts');
        accountItems.value = response.data || [];
        console.log('Account items loaded:', accountItems.value);
    } catch (error) {
        console.error('Error loading account items:', error);
        accountItems.value = [];
    }
};

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

// 검색 폼에서 검색 버튼 클릭 시 호출되는 함수
const searchData = async (searchOptions) => {
    console.log('Searching with options:', searchOptions);
    
    // 검색 파라미터 변환
    const params = {
        accountId: searchOptions.accountId || '',
        accountName: searchOptions.accountName || '',
        compName: searchOptions.compName || ''
    };
    
    // 날짜 범위 처리
    if (searchOptions.dateRangeFrom) {
        params.startDate = searchOptions.dateRangeFrom;
    }
    if (searchOptions.dateRangeTo) {
        params.endDate = searchOptions.dateRangeTo;
    }
    
    await fetchData(params);
};

// 검색 모달을 열 때 호출되는 함수
const handleOpenModal = (filterName) => {
    console.log('Open modal for filter:', filterName);
    switch (filterName) {
        case 'accountId':
        case 'accountName':
            loadAccountItems();
            accountModalVisible.value = true;
            break;
        default:
            console.warn('No modal defined for filter:', filterName);
    }
};

// 모달창 닫기 함수
const closeAccountModal = () => {
    accountModalVisible.value = false;
};

// SearchForm의 필터 값을 업데이트하는 함수
const updateFilterValue = (filterName, selectedItem) => {
    if (searchFormRef.value && searchFormRef.value.searchOptions) {
        searchFormRef.value.searchOptions[filterName] = selectedItem;
    }
};

// 모달창 확인 버튼 클릭 시 호출되는 함수
const confirmAccountModal = (selectedItems) => {
    console.log('Selected items from account modal:', selectedItems);
    if (selectedItems) {
        // 어떤 필드에서 모달이 호출되었는지에 따라 처리
        // 여기서는 둘 다 업데이트하되, 실제로는 호출한 필드에 맞게 처리
        updateFilterValue('accountId', selectedItems.accountId);
        updateFilterValue('accountName', selectedItems.accountName);
        
        // 표시용 값도 업데이트
        const accountIdFilter = filters.value.filters.find(f => f.name === 'accountId');
        const accountNameFilter = filters.value.filters.find(f => f.name === 'accountName');
        
        if (accountIdFilter) {
            accountIdFilter.value = `${selectedItems.accountId} (${selectedItems.accountName})`;
        }
        if (accountNameFilter) {
            accountNameFilter.value = `${selectedItems.accountName} (${selectedItems.accountId})`;
        }
    }
    accountModalVisible.value = false;
};

// 계정 검색 함수
const searchAccounts = async (searchValue) => {
    try {
        console.log('Searching accounts with value:', searchValue);
        const response = await axios.get('/api/accounts', {
            params: {
                searchValue: searchValue
            }
        });
        accountItems.value = response.data || [];
    } catch (error) {
        console.error('Error searching accounts:', error);
        accountItems.value = [];
    }
};

// 초기화 함수
const resetList = () => {
    fetchData();
    // 필터 값들도 초기화
    filters.value.filters.forEach(filter => {
        if (filter.type === 'item-search') {
            filter.value = '';
        }
    });
};

// 컴포넌트가 마운트될 때 초기 데이터를 가져옵니다.
onMounted(() => {
    fetchData();
});
</script>

<template>
    <div class="card">
        <SearchForm 
            ref="searchFormRef" 
            :filters="filters" 
            @searchData="searchData" 
            @openSearchModal="handleOpenModal" 
            @resetSearchOptions="resetList"
        />
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

        <!-- 계정 선택 모달 -->
        <DialogModal
            v-model:display="accountModalVisible"
            :items="accountItems"
            :headers="accountHeaders"
            title="계정 선택"
            placeholder="계정코드 또는 계정과목을 입력하세요"
            selectionMode="single"
            @close="closeAccountModal"
            @confirm="confirmAccountModal"
            @searchModal="searchAccounts"
        />
    </div>
</template>