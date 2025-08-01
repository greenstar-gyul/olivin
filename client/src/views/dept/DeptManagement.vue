<script setup>
import DeptInput from './DeptInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

<<<<<<< HEAD

const fileUploadRef = ref();



=======
const API_BASE_URL = '/api/depts';
const toast = useToast();
const fileUploadRef = ref();

// windos.location.origin을 computed로 처리
const baseUrl = computed(() => {
  return typeof window !== 'undefined' ? window.location.origin : '';
});

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '회사코드', value: '', name: 'name' },
    { type: 'text', label: '회사명', value: '', name: 'category' },
    { type: 'text', label: '부서명', value: '', name: 'store' },
    { type: 'text', label: '부서정보', value: '', name: 'publisher' }
  ]
});

const items = ref([
  { id: 1, name: '제품 A', category: '카테고리 1', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
  { id: 2, name: '제품 B', category: '카테고리 2', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
  { id: 3, name: '제품 C', category: '카테고리 3', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 }
]);
const selectedDept = ref(null);
const selectDeptId = ref('');

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
    { type: 'text', label: '회사정보', value: '', name: 'name' },
    { type: 'text', label: '회사명', value: '', name: 'category' },
    {
      type: 'select',
      label: '부서명',
      value: '',
      name: 'select1',
      options: [
        { name: '부서 1', value: '부서 1' },
        { name: '부서 2', value: '부서 2' },
        { name: '부서 3', value: '부서 3' }
      ]
    },
    {
      type: 'select',
      label: '직급',
      value: '',
      name: 'select2',
      options: [
        { name: '직급 1', value: '직급 1' },
        { name: '직급 2', value: '직급 2' },
        { name: '직급 3', value: '직급 3' }
      ]
    },
    { type: 'text', label: '등록자', value: '', name: 'store' },
    { type: 'text', label: '연락처', value: '', name: 'id' },
    { type: 'text', label: '기타', value: '', name: 'password' },
    { type: 'textarea', label: '비고', value: '', name: 'note' }
  ]
});

const selectedImageFile = ref(null);
const selectedImageFiles = ref([]);
const uploadedImageUrl = ref('');

const searchData = async (searchOptions) => {
  try {
    const params = {};
    
    if (searchOptions.productName && searchOptions.productName.trim() !== '') {
      params.productName = searchOptions.productName.trim();
    }
    
    if (searchOptions.vendorName && searchOptions.vendorName.trim() !== '') {
      params.vendorName = searchOptions.vendorName.trim();
    }
    
    if (searchOptions.categoryMain && searchOptions.categoryMain.trim() !== '') {
      params.categoryMain = searchOptions.categoryMain.trim();
    }
    
    if (searchOptions.categorySub && searchOptions.categorySub.trim() !== '') {
      params.categorySub = searchOptions.categorySub.trim();
    }

    if (searchOptions.compId && searchOptions.compId.trim() !== '') {
      params.compId = searchOptions.compId.trim();
    }
    
    if (searchOptions.packQty && searchOptions.packQty !== '') {
      params.packQty = searchOptions.packQty;
    }
    
    if (searchOptions.regUser && searchOptions.regUser.trim() !== '') {
      params.regUser = searchOptions.regUser.trim();
    }
    
    if (searchOptions.regDateRange && searchOptions.regDateRange.length === 2) {
      params.regDateFrom = searchOptions.regDateRange[0];
      params.regDateTo = searchOptions.regDateRange[1];
    }
    
    const response = await axios.get(`${API_BASE_URL}/search`, { params });
    
    items.value = response.data.map(product => ({
      ...product,
      regDate: product.regDate ? product.regDate : '',
      updateDate: product.updateDate ? product.updateDate : null
    }));
    
  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
};

const saveData = (inputData) => {
  console.log('Saving data:', inputData);
};
>>>>>>> 4a6f4ee5fefd1335f078ba79245eb0f7cf811c21
</script>

<template>
  <DeptInput
    :filters="filters"
    :items="items"
    :header="header"
    :inputs="inputs"
    :checkType="'multiple'"
    @searchData="searchData"
    @saveData="saveData"
  />
</template>