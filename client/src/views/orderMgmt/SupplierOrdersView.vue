<script setup>
import SearchDetailTable from '@/components/common/SearchDetailTable.vue';
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import { useRouter } from 'vue-router';
import axios from '@/service/axios';

const router = useRouter();

const header = ref({
  title: '발주제안 조회',
  header: {
    orderId: '제안코드', 
    orderTitle: '제안명', 
    reasonName: '제안사유', 
    orderDate: '제안요청일', 
    creatorName: '등록자', 
    orderFrom: '공급업체명', 
    totalAmount: '총 금액', 
    dueDate: '납기예정일',
    orderStatusName: '제안상태'
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
      { name: '발주상태를 선택하세요.', value: '' },
      { name: '승인대기', value: '030001' },
      { name: '승인', value: '030002' },
      { name: '출고완료', value: '030003' },
      { name: '반려', value: '030004' }
    ]
  },
  { type: 'dateRange', label: '제안요청일', value: '', name: 'orderDate',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '제안명', value: '', placeholder: '제안명을 입력하세요.', name: 'orderTitle' },
  { type: 'select', label: '제안사유', value: '', placeholder: '제안사유를 선택하세요.', name: 'reason',
    options: [
      { name: '발주사유를 선택하세요.', value: '' },
      { name: '정기 발주', value: '140001' },
      { name: '수요 예측 발주', value: '140002' },
      { name: '재고 부족', value: '140003' },
      { name: '신상품 발주', value: '140004' },
      { name: '긴급 발주', value: '140005' },
      { name: '행사상품 발주', value: '140006' }
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

const actionHandler = (rowData) => {
  router.push(`/orders/view/${rowData.orderId}`);
}

onMounted(() => {
  getOrdersData();
});
</script>
<template>
  <SearchDetailTable :filters="filters" :items="items" :header="header" @searchData="searchData" @actionHandler="actionHandler"></SearchDetailTable>
</template>