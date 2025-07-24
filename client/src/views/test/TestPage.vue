<!-- 조회 테스트 페이지 -->
<script setup>
import { ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import { StockService } from '@/service/StockService';

const header = ref({
  title: '조회 테스트',
  header: {
    id: 'ID', 
    name: '제품명', 
    category: '제품분류', 
    publisher: '공급사', 
    store: '지점', 
    size: '규격', 
    quantity: '현재 재고', 
    safe: '안전 재고', 
  },
  rightAligned: ['quantity', 'safe']
});

const items = ref([]);
items.value = StockService.getStockList();

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
  { type: 'text', label: '제품명', value: '', placeholder: '', name: 'name' },
  { type: 'text', label: '제품분류', value: '', placeholder: '', name: 'category' },
  { type: 'text', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'text', label: '지점', value: '', placeholder: '', name: 'store' },
  { type: 'dateRange', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'dateRange', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'dateRange', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'select', label: '선택', value: '', placeholder: '', name: 'select', options: [
    { name: '옵션 1', value: 'Option 1' },
    { name: '옵션 2', value: 'Option 2' },
    { name: '옵션 3', value: 'Option 3' }
  ] }
];

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

function searchData(options) {
  
}

</script>
<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData"></SearchTable>
</template>