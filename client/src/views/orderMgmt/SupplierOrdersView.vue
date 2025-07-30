<script setup>
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import SearchTable from '../../components/common/SearchTable.vue';
import axios from 'axios';

const header = ref({
  title: '발주제안 조회',
  header: {
    orderId: '제안코드', 
    orderTitle: '제안명', 
    reason: '제안사유', 
    orderDate: '제안요청일', 
    userId: '등록자', 
    orderFrom: '공급업체명', 
    totalAmount: '총 금액', 
    dueDate: '납기예정일',
    orderStatus: '제안상태'
  },
  rightAligned: ['totalAmount']
});

const items = ref([]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
  { type: 'text', label: '제안코드', value: '', placeholder: '제안코드를 입력하세요.', name: 'orderId' },
  { type: 'select', label: '제안상태', value: '', placeholder: '제안상태를 선택하세요.', name: 'orderStatus',
    options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ]
  },
  { type: 'dateRange', label: '제안요청일', value: '', name: 'orderDate',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '제안명', value: '', placeholder: '제안명을 입력하세요.', name: 'orderTitle' },
  { type: 'select', label: '제안사유', value: '', placeholder: '제안사유를 선택하세요.', name: 'reason',
    options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ]
  },
  { type: 'dateRange', label: '납기예정일', value: '', name: 'dueDate',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '공급업체명', value: '', placeholder: '공급업체명을 입력하세요.', name: 'orderForm' },

];

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
  getOrdersData(searchOptions);
};

const getOrdersData = async (options) => {
  const req = await axios.get('/api/orders/supplier', {
    params: {
      ...options
    }
  });
  const result = req.data;
  const viewData = result.map((item) => {
    return {
      ...item,
      orderDate: convertDate(item.orderDate),
      totalAmount: Number(item.totalAmount).toLocaleString()
    }
  });
  items.value = viewData;
  console.log(req);
}

onMounted(() => {
  getOrdersData();
});
</script>
<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData"></SearchTable>
</template>