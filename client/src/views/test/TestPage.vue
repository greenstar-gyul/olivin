<!-- 조회 테스트 페이지 -->
<script setup>
import { ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import { StockService } from '@/service/StockService';

const header = ref({});

const items = ref([]);
items.value = StockService.getStockList();

const filters = ref([]);
filters.value = [
  { type: 'text', label: '이름', value: '', placeholder: '이름을 입력하세요', name: 'name' },
  { type: 'text', label: '이름', value: '', placeholder: '이름을 입력하세요', name: 'name' },
  { type: 'dateRange', label: '가나다라마바사', value: '', fromPlaceholder: '날짜 범위를 선택하세요', name: 'dateRange' },
  { type: 'text', label: '이름', value: '', placeholder: '이름을 입력하세요', name: 'name' },
  { type: 'date', label: '날짜', value: '', placeholder: '날짜를 선택하세요', name: 'date' },
  { type: 'date', label: '날짜', value: '', placeholder: '날짜를 선택하세요', name: 'date' },
  { type: 'date', label: '날짜', value: '', placeholder: '날짜를 선택하세요', name: 'date' },
  { type: 'textarea', label: '설명', value: '', placeholder: '설명을 입력하세요', name: 'description' },
];

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

onMounted(() => {
  // 초기화 작업
  header.value = {
    title: '조회 테스트',
    description: '이 페이지는 조회 테스트를 위한 페이지입니다.'
  };
});

</script>
<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData"></SearchTable>
</template>