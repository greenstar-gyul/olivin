<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref } from 'vue';

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '제품명', value: '', placeholder: '', name: 'name' },
    { type: 'text', label: '제품분류', value: '', placeholder: '', name: 'category' },
    { type: 'text', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
    { type: 'text', label: '지점', value: '', placeholder: '', name: 'store' },
    { type: 'dateRange', label: '날짜 범위', value: '', fromPlaceholder: '', name: 'dateRange' },
    { type: 'select', label: '선택', value: '', placeholder: '', name: 'select', options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ] },
    { type: 'item-search', label: '제품 검색', value: '', placeholder: '검색', name: 'dialog' },
    { type: 'item-search', label: '제품 검색2', value: '', placeholder: '검색2', name: 'dialog2' },
  ]
});

const items = ref([
  { id: 1, name: '제품 A', category: '카테고리 1', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
  { id: 2, name: '제품 B', category: '카테고리 2', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
  { id: 3, name: '제품 C', category: '카테고리 3', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 },
  
]);

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
    safe: '안전 재고'
  },
  rightAligned: ['quantity', 'safe']
});

const inputs = ref({
  title: '입력 폼',
  inputs: [
    { type: 'text', label: '제품명', value: '', placeholder: '', name: 'name' },
    { type: 'text', label: '사업자 번호', value: '', placeholder: '', name: 'category' },
    { type: 'text', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
    { type: 'text', label: '지점', value: '', placeholder: '', name: 'store' },
    { type: 'number', label: '숫자', value: '', placeholder: '', name: 'nums' },
    { type: 'textarea', label: '비고', value: '', fromPlaceholder: '', name: 'note' },
    { type: 'select', label: '선택', value:'', placeholder: '', name: 'select', options: [
      { name: '옵션 1', value: 'Option 1' },
      { name: '옵션 2', value: 'Option 2' },
      { name: '옵션 3', value: 'Option 3' }
    ] }
  ]
})

const visible = ref(false);
const visible2 = ref(false);

const modalHeaders1 = ref([
  { field: 'id', header: 'ID' },
  { field: 'name', header: '제품명' },
  { field: 'category', header: '제품분류' },
  { field: 'publisher', header: '공급사' },
  { field: 'store', header: '지점' },
  { field: 'size', header: '규격' },
  { field: 'quantity', header: '현재 재고' },
  { field: 'safe', header: '안전 재고' }
]);

const modalItems1 = ref([
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
  { id: 1, name: '회사 A', category: '업종 A', publisher: '대표자 A', store: '주소 A', size: '전화번호 A', quantity: '이메일 A', safe: '비고 A' },
  { id: 2, name: '회사 B', category: '업종 B', publisher: '대표자 B', store: '주소 B', size: '전화번호 B', quantity: '이메일 B', safe: '비고 B' },
  { id: 3, name: '회사 C', category: '업종 C', publisher: '대표자 C', store: '주소 C', size: '전화번호 C', quantity: '이메일 C', safe: '비고 C' }
]);


const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

const saveData = (inputData) => {
  console.log('Saving data:', inputData);
};

const openSearchModal = (inputName) => {
  if (inputName === 'dialog') {
    visible.value = true;
  } else if (inputName === 'dialog2') {
    visible2.value = true;
  }
};

const close1 = () => {
  visible.value = false;
}

const close2 = () => {
  visible2.value = false;
}

const confirm1 = (selectedItems) => {
  console.log('Confirming data from modal 1', selectedItems);
  visible.value = false;
};

const confirm2 = (selectedItems) => {
  console.log('Confirming data from modal 2', selectedItems);
  visible2.value = false;
};

</script>
<template>
  <StandardInput :filters="filters" :items="items" :header="header" :inputs="inputs" @searchData="searchData" @saveData="saveData" @openSearchModal="openSearchModal"></StandardInput>
  <DialogModal :visible="visible" :headers="modalHeaders1" :items="modalItems1" @close="close1" @confirm="confirm1" title="제품 검색"></DialogModal>
  <DialogModal :visible="visible2" :headers="modalHeaders2" :items="modalItems2" @close="close2" @confirm="confirm2" title="제품 검색2"></DialogModal>
</template>