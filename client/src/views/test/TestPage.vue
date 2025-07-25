<!-- 조회 테스트 페이지 -->
<script setup>
import { onMounted, ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import { StockService } from '@/service/StockService';
import { Dialog } from 'primevue';
import axios from 'axios';
import DialogModal from '@/components/overray/DialogModal.vue';

// 조회 폼의 헤더 정보 (조회 테이블 컬럼 이름)
const header = ref({
  title: '조회 테스트', // 조회 폼 제목
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
  rightAligned: ['quantity', 'safe'] // 오른쪽 정렬할 컬럼
});

// 조회할 데이터
const items = ref([]);
items.value = StockService.getStockList();

// 
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
  ] },
  { type: 'item-search', label: '제품 검색', value: '', placeholder: '검색', name: 'dialog' },
  { type: 'item-search', label: '제품 검색2', value: '', placeholder: '검색2', name: 'dialog2' },
];

const modalHeaders = ref([
  { field: 'id', header: 'ID' },
  { field: 'name', header: '제품명' },
  { field: 'category', header: '제품분류' },
  { field: 'publisher', header: '공급사' },
  { field: 'store', header: '지점' },
  { field: 'size', header: '규격' },
  { field: 'quantity', header: '현재 재고' },
  { field: 'safe', header: '안전 재고' }
]);

const modalItems = ref([
  { id: 1, name: '제품 A', category: '분류 1', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
  { id: 2, name: '제품 B', category: '분류 2', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
  { id: 3, name: '제품 C', category: '분류 3', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 }
]);

const modalHeaders2 = ref([
  { field: 'id', header: 'ID' },
  { field: 'name', header: '회사명' },
  { field: 'category', header: '업종' },
  { field: 'publisher', header: '대표자' },
  { field: 'store', header: '주소' },
  { field: 'size', header: '전화번호' },
  { field: 'quantity', header: '이메일' },
  { field: 'safe', header: '비고' }
]);

const modalItems2 = ref([
  { id: 1, name: '회사 A', category: 'IT', publisher: '대표 A', store: '서울', size: '규모 A', quantity: 10, safe: '비고 A' },
  { id: 2, name: '회사 B', category: '제조업', publisher: '대표 B', store: '부산', size: '규모 B', quantity: 20, safe: '비고 B' },
  { id: 3, name: '회사 C', category: '서비스업', publisher: '대표 C', store: '대구', size: '규모 C', quantity: 30, safe: '비고 C' }
]);

const testModalVisible = ref(false);
const testModalVisible2 = ref(false);

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

const handleOpenModal = (filterName) => {
  console.log('Open modal for filter:', filterName);
  switch (filterName) {
    case 'dialog':
      testModalVisible.value = true;
      break;
    case 'dialog2':
      testModalVisible2.value = true;
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

const closeModal = () => {
  testModalVisible.value = false;
}

const confirmModal = (selectedItems) => {
  console.log('Selected items from modal:', selectedItems);
  testModalVisible2.value = false;
};

const closeModal2 = () => {
  testModalVisible2.value = false;
}

const confirmModal2 = (selectedItems) => {
  console.log('Selected items from modal:', selectedItems);
  testModalVisible2.value = false;
};

onMounted(() => {
  getSampleData();
});

</script>
<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal"></SearchTable>
  <DialogModal title="테스트 모달 1" :display="testModalVisible" :headers="modalHeaders" :items="modalItems" :selectionMode="'multiple'" @close="closeModal" @confirm="confirmModal"></DialogModal>
  <DialogModal title="테스트 모달 2" :display="testModalVisible2" :headers="modalHeaders2" :items="modalItems2" :selectionMode="'single'" @close="closeModal2" @confirm="confirmModal2"></DialogModal>
</template>