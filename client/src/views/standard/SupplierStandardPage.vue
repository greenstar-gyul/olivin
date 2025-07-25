<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import { ref } from 'vue';

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '제품명', value: '', placeholder: '', name: 'name' },
    { type: 'text', label: '제품분류', value: '', placeholder: '', name: 'category' },
    { type: 'text', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
    { type: 'text', label: '지점', value: '', placeholder: '', name: 'store' },
    { type: 'dateRange', label: '날짜 범위', value: '', fromPlaceholder: '', name: 'dateRange' }
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

const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

const saveData = (inputData) => {
  console.log('Saving data:', inputData);
};

</script>
<template>
  <StandardInput :filters="filters" :items="items" :header="header" :inputs="inputs" @searchData="searchData" @saveData="saveData"></StandardInput>
</template>