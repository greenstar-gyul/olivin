<script setup>
import SearchTable from '@/components/common/SearchTable.vue';
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import axios from '@/service/axios';

const header = ref({
  title: '매출 계획 조회',
  header: {
    planId: 'ID',
    year: '연도',
    quarter: '분기',
    compName: '지점명',
    targetAmount: '목표 금액',
    regUserName: '등록자',
    regDate: '등록일',
    reason: '사유'
  },
  rightAligned: ['year', 'quarter', 'totalAmount']
});

const items = ref([]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
  { type: 'text', label: 'ID', value: '', placeholder: '매출계획 ID를 입력하세요.', name: 'planId' },
  { type: 'text', label: '연도', value: '', placeholder: '연도를 입력하세요.', name: 'year' },
  { type: 'text', label: '분기', value: '', placeholder: '분기를 입력하세요.', name: 'quarter' },
  { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요.', name: 'regUserName' },
  { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요.', name: 'compName' },
  { type: 'text', label: '사유', value: '', placeholder: '사유를 입력하세요.', name: 'reason' },
  { type: 'dateRange', label: '등록일', value: '', name: 'regDate',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
];

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
  getOrdersData(searchOptions);
};

const getOrdersData = async (options) => {
  const req = await axios.get('/api/salesPlans', {
    params: {
      ...options
    }
  });
  const result = req.data;
  const viewData = result.map((item) => {
    return {
      ...item,
      regDate: convertDate(item.regDate),
      targetAmount: Number(item.targetAmount).toLocaleString()+'원'
    }
  });
  items.value = viewData;
  // console.log(req);
}

onMounted(() => {
  getOrdersData();
});
</script>
<template>
  <SearchTable ref="searchOptions" :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal"></SearchTable>
</template>