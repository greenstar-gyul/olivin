<script setup>
import { ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';

const header = ref({
  title: '발주서 조회',
  header: {
    order_id: '발주서코드', 
    order_name: '발주명', 
    reason: '발주사유', 
    order_date: '발주요청일', 
    user_name: '등록자', 
    order_to: '지점명', 
    total_amount: '총 금액', 
    due_date: '납기예정일',
    order_status: '발주상태'
  },
  rightAligned: ['total_amount']
});

const items = ref([
  {
    order_id: 'ORD20250725001',
    order_name: '2025년 7월 정기 발주',
    reason: '재고 보충',
    order_date: '2025-07-20',
    user_name: '홍길동',
    order_to: '강남지점',
    total_amount: '1,200,000',
    due_date: '2025-07-30',
    order_status: '요청 완료'
  },
  {
    order_id: 'ORD20250725002',
    order_name: '2025년 7월 긴급 발주',
    reason: '긴급 요청',
    order_date: '2025-07-22',
    user_name: '김민수',
    order_to: '부산지점',
    total_amount: '850,000',
    due_date: '2025-07-28',
    order_status: '검토 중'
  },
  {
    order_id: 'ORD20250725003',
    order_name: '화장품 3차 발주',
    reason: '신제품 입고',
    order_date: '2025-07-18',
    user_name: '이수진',
    order_to: '대전지점',
    total_amount: '2,100,000',
    due_date: '2025-07-27',
    order_status: '배송 중'
  },
  {
    order_id: 'ORD20250725004',
    order_name: '7월 마감 전 추가 발주',
    reason: '월말 재고 확보',
    order_date: '2025-07-24',
    user_name: '박재현',
    order_to: '광주지점',
    total_amount: '980,000',
    due_date: '2025-07-31',
    order_status: '완료'
  }
]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
  { type: 'text', label: '발주서코드', value: '', placeholder: '발주서코드를 입력하세요.', name: 'order_id' },
  { type: 'select', label: '발주상태', value: '', placeholder: '발주상태를 선택하세요.', name: 'order_status',
    options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ]
  },
  { type: 'dateRange', label: '발주요청일', value: '', name: 'order_date',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '발주명', value: '', placeholder: '발주명을 입력하세요.', name: 'order_name' },
  { type: 'select', label: '발주사유', value: '', placeholder: '발주사유를 선택하세요.', name: 'reason',
    options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ]
  },
  { type: 'dateRange', label: '납기예정일', value: '', name: 'due_date',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요.', name: 'order_to' },

];

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};
</script>
<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData"></SearchTable>
</template>