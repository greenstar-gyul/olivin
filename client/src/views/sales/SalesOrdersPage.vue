<script setup>
import { onMounted, ref } from 'vue';
import BasicTable from '@/components/table/BasicTable.vue';
import axios from '@/service/axios';
import SearchForm from '@/components/inputForm/SearchForm.vue';

const searchFormRef = ref(null);

// 마스터 테이블
const masterHeader = ref({
  title: '주문 조회',
  header: {
    soId: '주문코드', 
    compName: '지점명', 
    totalPrice: '총 가격', 
    paymentTypeName: '결제 방식', 
    soDate: '주문일자',
    empName: '등록자',
    statusName: '상태',
  },
  rightAligned: ['totalPrice']
});

const masterItems = ref([]);

// 상세 테이블
const detailHeader = ref({
  title: '상품 목록',
  header: {
    productId: '상품코드',
    productName: '상품명', 
    unitPrice: '판매가', 
    quantity: '수량', 
    price: '합계',
  },
  rightAligned: ['unitPrice', 'quantity', 'price']
});

const detailItems = ref([]);

const onRowSelect = async (row) => {
  const res = await axios.get(`/api/sales/orders/${row.soId}`);
  if (res.data?.detail?.length > 0) {
    detailItems.value = res.data.detail.map((e) => {
      return {
        ...e,
        price: Number(e.price).toLocaleString() + '원',
        unitPrice: Number(e.unitPrice).toLocaleString() + '원',
      }
    });
  } else {
    detailItems.value = [];
  }
  // console.log('Selected Row:', row);
};

const onRowUnselect = (row) => {
  // TODO : 초기화 로직 추가
  console.log('Unselected Row:', row);
};

// 검색 조건
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '주문코드', value: '', placeholder: '주문코드를 입력하세요.', name: 'soId' },
    { type: 'select', label: '결제방식', value: '', placeholder: '결제방식을 선택하세요.', name: 'paymentType',
        options: [
        { name: '현금', value: '160001' },
        { name: '계좌이체', value: '160002' },
        { name: '카드결제', value: '160003' },
      ]
    },
    { type: 'dateRange', label: '주문일자', value: '', name: 'soDate',
      fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
    { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요.', name: 'compName' },
    { type: 'select', label: '상태', value: '', placeholder: '주문상태를 선택하세요.', name: 'status',
      options: [
        { name: '판매됨', value: '020001' },
        { name: '취소됨', value: '020002' },
      ]
    },
    { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요.', name: 'empName' },
  ]
});

const searchData = async (searchValue) => {
  getSalesOrders(searchValue);
};

const resetSearchOptions = () => {

};

const getSalesOrders = async (searchValue) => {
  const res = await axios.get('/api/sales/orders', {
    params: {
      ...searchValue
    }
  });
  if (res.data?.length > 0) {
    masterItems.value = res.data.map((e) => {
      return {
        ...e,
        totalPrice: Number(e.totalPrice).toLocaleString() + '원',
      }
    });
  } else {
    masterItems.value = [];
  }
};

onMounted(async () => {
  // 1년 전부터 조회하기 위해 기본 날짜 설정
  const searchOptions = searchFormRef.value.searchOptions;
  if (searchOptions) {
    const defaultOrderDateFrom = new Date();
    defaultOrderDateFrom.setFullYear(defaultOrderDateFrom.getFullYear() - 1);

    searchOptions.soDateFrom = defaultOrderDateFrom;
    getSalesOrders(searchOptions);
  }
});
</script>
<template>
  <SearchForm ref="searchFormRef" :filters="filters" @searchData="searchData" @resetSearchOptions="resetSearchOptions"></SearchForm>
  <BasicTable :data="masterItems" :header="masterHeader" :checked="true" :dataKey="'soId'" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect"></BasicTable>
  <BasicTable :data="detailItems" :header="detailHeader"></BasicTable>
</template>