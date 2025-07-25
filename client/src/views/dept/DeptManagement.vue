<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import { ref } from 'vue';

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '회사코드', value: '', placeholder: '', name: 'name' },
    { type: 'text', label: '회사명', value: '', placeholder: '', name: 'category' },
    { type: 'text', label: '부서명', value: '', placeholder: '', name: 'store' },
    { type: 'text', label: '부서정보', value: '', fromPlaceholder: '', name: 'publisher' },
  ]
});

const items = ref([
  { id: 1, name: '제품 A', category: '카테고리 1', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
  { id: 2, name: '제품 B', category: '카테고리 2', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
  { id: 3, name: '제품 C', category: '카테고리 3', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 },
  
]);

const header = ref({
  title: '조회 결과',
  header: {
    id: '회사명', 
    name: '직급', 
    category: '사원이름', 
    publisher: '아이디', 
    store: '비밀번호', 
    size: '규격', 
    quantity: '현재 재고', 
    safe: '안전 재고'
  },
  rightAligned: ['quantity', 'safe']
});

const inputs = ref({
  title: '입력 폼',
  inputs: [
    { type: 'text', label: '회사정보', value: '', placeholder: '', name: 'name' },
    { type: 'text', label: '회사명', value: '', placeholder: '', name: 'category' },
    { type: 'select', label: '부서명', value:'', placeholder: '', name: 'select1', options: [
      { name: '부서 1', value: '부서 1' },
      { name: '부서 2', value: '부서 2' },
      { name: '부서 3', value: '부서 3' }
    ]},
    { type: 'select', label: '직급', value:'', placeholder: '', name: 'select2', options: [
      { name: '직급 1', value: '직급 1' },
      { name: '지급 2', value: '직급 2' },
      { name: '직급 3', value: '직급 3' }
    ]},
    { type: 'text', label: '등록자', value: '', placeholder: '', name: 'store' },
    { type: 'text', label: '연락처', value: '', fromPlaceholder: '', name: 'id' },
    { type: 'text', label: '기타', value: '', fromPlaceholder: '', name: 'password' },
    { type: 'textarea', label: '비고', value: '', fromPlaceholder: '', name: 'note' },
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