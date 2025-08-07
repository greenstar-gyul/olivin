<script setup>
import { onMounted, ref } from 'vue';
import BasicTable from '@/components/table/BasicTable.vue';
import axios from '@/service/axios';


// 마스터 테이블
const masterHeader = ref({
  title: '조회 테스트',
  header: {
    soId: 'ID', 
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
  title: '상세 조회 테스트',
  header: {
    sodId: 'ID', 
    productName: '상품명', 
    unitPrice: '판매가', 
    quantity: '수량', 
    price: '합계',
  },
  rightAligned: ['unitPrice', 'quantity', 'price']
});

const detailItems = ref([]);

const onRowSelect = (row) => {
  console.log('Selected Row:', row);
};

// 검색 조건
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: 'ID', value: '', placeholder: '', name: 'id' },
  ]
});

const searchData = () => {

};

const resetSearchOptions = () => {

};

onMounted(async () => {
  const masterRes = await axios.get('/api/sales/orders');
  masterItems.value = masterRes.data.map((e) => {
    return {
      ...e,
      totalPrice: Number(e.totalPrice).toLocaleString() + '원',
    }
  });
});
</script>
<template>
  <SearchForm ref="searchFormRef" :filters="filters" @searchData="searchData" @resetSearchOptions="resetSearchOptions"></SearchForm>
  <BasicTable :data="masterItems" :header="masterHeader" :checked="true" :dataKey="'soId'" @rowSelect="onRowSelect"></BasicTable>
  <BasicTable :data="detailItems" :header="detailHeader"></BasicTable>
</template>