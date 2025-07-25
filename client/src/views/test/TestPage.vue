<!-- 조회 테스트 페이지 -->
<script setup>
import { onMounted, ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import { StockService } from '@/service/StockService';
import { Dialog } from 'primevue';
import axios from 'axios';

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

const testModalVisible = ref(false);

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

const handleOpenModal = (filterName) => {
  console.log('Open modal for filter:', filterName);
  switch (filterName) {
    case 'dialog':
      testModalVisible.value = true;
      break;
    default:
      console.warn('No modal defined for filter:', filterName);
  }
};

const getSampleData = async () => {
  const result = await axios.get('/api/test');
  const data = await result.data;
  console.log('Sample data:', data);
}

onMounted(() => {
  getSampleData();
});

</script>
<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData"></SearchTable>
  <div class="font-semibold text-xl mb-4">Dialog</div>
  <Dialog header="Dialog" v-model:visible="display" :breakpoints="{ '960px': '75vw' }" :style="{ width: '30vw' }"
    :modal="true">
    <p class="leading-normal m-0">
      Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore
      magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo
      consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
      Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
    </p>
    <template #footer>
      <Button label="Save" @click="close" />
    </template>
  </Dialog>
</template>