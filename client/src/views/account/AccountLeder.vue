<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';

import SearchForm from '@/components/inputForm/SearchForm.vue';
import AccountTable from './AccountTable.vue'; // 수정된 AccountTable 컴포넌트 임포트
import ToggleButton from 'primevue/togglebutton';

const filters = ref({});
filters.value.title = '조회 조건'; // 검색 조건 폼 제목
filters.value.filters = [
    // 검색 조건 필터 목록
    // type: 'text'는 일반 텍스트 입력 필드
    // type: 'dateRange'는 날짜 범위 선택 필드
    // type: 'select'는 드롭다운 선택 필드
    // type: 'item-search'는 아이템 검색 모달을 여는 필드
    // type: 'number'는 숫자 입력 필드
    // type: 'textarea'는 다중 행 텍스트 입력 필드
    // type: 'date'는 단일 날짜 선택 필드
    // label: 필드의 라벨. 사용자에게 보여지는 이름.
    // value: 필드의 초기 값. 특별한 경우가 아니면 일반적으로 빈 문자열.
    // placeholder: 필드에 대한 플레이스홀더 텍스트. 사용자가 입력하기 전에 보여지는 안내 텍스트.
    // name: 필드의 고유 이름. 데이터 바인딩에 사용됨.
    {
        type: 'select',
        label: '회계단위',
        value: '',
        placeholder: '',
        name: 'select2',
        options: [
            { name: '전년도', value: '전기' },
            { name: '올해', value: '당해' }
        ]
    },
    { type: 'text', label: '계정코드', value: '', placeholder: '', name: 'accountId' },
    { type: 'text', label: '계정과목', value: '', fromPlaceholder: '', name: 'publisher' },
    { type: 'dateRange', label: '회계기간', value: '', fromPlaceholder: '', name: 'publisher' },
    { type: 'text', label: '거래처코드', value: '', placeholder: '', name: 'compId' },
    { type: 'text', label: '거래처명', value: '', placeholder: '', name: 'compName' }
];

// 테이블에 표시할 데이터를 담을 ref
const items = ref([]);
// 'Balance' 컬럼의 고정 여부를 제어할 ref
const balanceFrozen = ref(false);

// 데이터 포맷팅 함수
function formatCurrency(value) {
    if (value === null || value === undefined) return '';
    // 숫자형으로 변환 시도
    const numberValue = Number(value);
    if (isNaN(numberValue)) return value; // 숫자가 아니면 원래 값 반환

    return numberValue.toLocaleString('ko-KR', { style: 'currency', currency: 'KRW' });
}

// 💡 테이블 컬럼 구조를 데이터로 정의 (computed 사용으로 반응성 유지)
const tableColumns = computed(() => [
    { field: 'accountlederId', header: '거래처원장ID', style: 'min-width: 150px', frozen: true, class: 'font-bold' },
    { field: 'compName', header: '회사명', style: 'min-width: 120px' },
    { field: 'accountId', header: '계정코드', style: 'min-width: 120px' },
    { field: 'accountName', header: '계정명', style: 'min-width: 120px' },
    { field: 'increase', header: '차변', style: 'min-width: 150px' },
    { field: 'decrease', header: '대변', style: 'min-width: 150px' },
    { field: 'writeDate', header: '작성일', style: 'min-width: 150px' },
    { field: 'detail', header: '상세', style: 'min-width: 200px' },
    { field: 'productName', header: '제품명', style: 'min-width: 120px' },
    { field: 'productId', header: '제품코드', style: 'min-width: 120px' },
    // balanceFrozen 값에 따라 `frozen` 속성이 동적으로 바뀝니다.
    { field: 'balance', header: '잔액', style: 'min-width: 150px', alignFrozen: 'right', frozen: balanceFrozen.value }
]);

// 컴포넌트가 마운트될 때 API를 호출하여 데이터를 가져옵니다.
onMounted(async () => {
    try {
        const result = await axios.get('/api/account');
        items.value = result.data;
    } catch (error) {
        console.error('데이터를 불러오는 데 실패했습니다:', error);
        items.value = []; // 에러 발생 시 빈 배열로 초기화
    }
});
</script>

<template>
    <div class="card">
        <SearchForm :filters="filters" @searchData="handleSearchData"></SearchForm>
        <div class="font-semibold text-xl mb-4">거래처원장</div>
        <ToggleButton v-model="balanceFrozen" onIcon="pi pi-lock" offIcon="pi pi-lock-open" onLabel="잔액고정" offLabel="잔액고정해제" />

        <AccountTable :data="items" :columns="tableColumns" :headerInfo="{ title: '' }" dataKey="accountlederId">
            <template #body-balance="{ data }">
                <span class="font-bold">{{ formatCurrency(data.balance) }}</span>
            </template>
            <template #body-increase="{ data }">
                <span class="text-blue-600">{{ formatCurrency(data.increase) }}</span>
            </template>
            <template #body-decrease="{ data }">
                <span class="text-red-600">{{ formatCurrency(data.decrease) }}</span>
            </template>
        </AccountTable>
    </div>
</template>
