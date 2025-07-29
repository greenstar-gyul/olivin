<script setup>
import { ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';

const header = ref({
  title: '발주제안 조회',
  header: {
    order_id: '제안코드', 
    order_name: '제안명', 
    reason: '제안사유', 
    order_date: '제안요청일', 
    user_name: '등록자', 
    order_to: '공급업체명', 
    total_amount: '총 금액', 
    due_date: '납기예정일',
    order_status: '제안상태'
  },
  rightAligned: ['total_amount']
});

const items = ref([
  {
    order_id: 'PO202507230001',
    order_name: '20250723-재고부족 발주건',
    reason: '재고보충',
    order_date: '2025-07-23',
    user_name: '홍길동',
    order_to: '(주)케이서플라이',
    total_amount: '1,200,000',
    due_date: '2025-07-30',
    order_status: '승인대기'
  },
  {
    order_id: 'PO202507230002',
    order_name: '20250723-정기발주',
    reason: '정기발주',
    order_date: '2025-07-23',
    user_name: '이영희',
    order_to: '(주)디스트리뷰션코리아',
    total_amount: '3,450,000',
    due_date: '2025-08-01',
    order_status: '승인완료'
  },
  {
    order_id: 'PO202507230003',
    order_name: '20250723-재고부족 발주건',
    reason: '재고보충',
    order_date: '2025-07-23',
    user_name: '김철수',
    order_to: '에이플러스상사',
    total_amount: '860,000',
    due_date: '2025-07-25',
    order_status: '반려'
  }
]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
  { type: 'text', label: '제안코드', value: '', placeholder: '제안코드를 입력하세요.', name: 'order_id' },
  { type: 'select', label: '제안상태', value: '', placeholder: '제안상태를 선택하세요.', name: 'order_status',
    options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ]
  },
  { type: 'dateRange', label: '제안요청일', value: '', name: 'order_date',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '제안명', value: '', placeholder: '제안명을 입력하세요.', name: 'order_name' },
  { type: 'select', label: '제안사유', value: '', placeholder: '제안사유를 선택하세요.', name: 'reason',
    options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ]
  },
  { type: 'dateRange', label: '납기예정일', value: '', name: 'due_date',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '공급업체명', value: '', placeholder: '공급업체명을 입력하세요.', name: 'order_to' },

];

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};
</script>
<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData"></SearchTable>
</template>