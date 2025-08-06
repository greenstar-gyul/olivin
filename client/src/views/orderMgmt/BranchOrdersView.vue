<script setup>
import SearchDetailTable from '@/components/common/SearchDetailTable.vue';
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import { useRouter } from 'vue-router';
import axios from '@/service/axios';
import { useAuth } from '@/composables/useAuth';

const router = useRouter();

const searchDetailTableRef = ref(null);

const header = ref({
  title: '발주서 조회',
  header: {
    orderId: '발주서코드', 
    orderTitle: '발주명', 
    reasonName: '발주사유', 
    orderDate: '발주요청일', 
    creatorName: '등록자', 
    orderFrom: '지점명', 
    totalAmount: '총 금액', 
    dueDate: '납기예정일',
    orderStatusName: '발주상태'
  },
  rightAligned: ['totalAmount']
});

const items = ref([]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
  { type: 'text', label: '발주서코드', value: '', placeholder: '발주서코드를 입력하세요.', name: 'orderId' },
  { type: 'select', label: '발주상태', value: '', placeholder: '발주상태를 선택하세요.', name: 'orderStatus',
    options: [
      { name: '발주상태를 선택하세요.', value: '' },
      { name: '승인대기', value: '030001' },
      { name: '승인', value: '030002' },
      { name: '출고완료', value: '030003' },
      { name: '반려', value: '030004' }
    ]
  },
  { type: 'dateRange', label: '발주요청일', value: '', name: 'orderDate',
    fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
  { type: 'text', label: '발주명', value: '', placeholder: '발주명을 입력하세요.', name: 'orderTitle' },
  { type: 'select', label: '발주사유', value: '', placeholder: '발주사유를 선택하세요.', name: 'reason',
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
  { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요.', name: 'orderFrom' },

];

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
  getOrdersData(searchOptions);
};

const getOrdersData = async (options) => {
  const req = await axios.get('/api/orders/branch', {
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
  // console.log(req);
}

const actionHandler = (rowData) => {
  router.push(`/orders/view/${rowData.orderId}`);
}

const getBranchInfo = async (empId) => {
  const req = await axios.get('/api/orders/user/compInfo', {
    params: {
      empId: empId
    }
  });

  //지점 정보
  if (req.data.compType == '100002')
    return req.data;
  return undefined;
}

onMounted(async () => {
  // 1년 전부터 조회하기 위해 기본 날짜 설정
  const searchOptions = searchDetailTableRef.value.searchFormRef.searchOptions;
  if (searchOptions) {
    const defaultOrderDateFrom = new Date();
    defaultOrderDateFrom.setFullYear(defaultOrderDateFrom.getFullYear() - 1);

    const branchInfo = await getBranchInfo(useAuth().user.value.employeeId);
    searchOptions.orderFrom = branchInfo.compName || '';

    searchOptions.orderDateFrom = defaultOrderDateFrom;
    getOrdersData(searchOptions);
  }
});
</script>
<template>
  <SearchDetailTable ref="searchDetailTableRef" :filters="filters" :items="items" :header="header" @searchData="searchData" @actionHandler="actionHandler"></SearchDetailTable>
</template>