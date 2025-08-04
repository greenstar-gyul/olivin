<script setup>
import DeptInput from './DeptInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

const items = ref([]);
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

const selectedDept = ref(null);
const selectDeptId = ref('');

const header = ref({
  title: '조회 결과',
  header: {
    compId: '회사명',
    compName: '직급',
    departmentId: '부서코드',
    deptName: '부서명',
    position: '직책'
  },
  rightAligned: []
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
    }
  ]
});

const selectedImageFile = ref(null);
const selectedImageFiles = ref([]);
const uploadedImageUrl = ref('');

const searchData = async (searchOptions) => {
  try {
    const params = {};

    if (searchOptions.compId && searchOptions.compId.trim() !== '') {
      params.compId = searchOptions.compId.trim();
    }

    if (searchOptions.compName && searchOptions.compName.trim() !== '') {
      params.compName = searchOptions.compName.trim();
    }

    if (searchOptions.departId && searchOptions.departId.trim() !== '') {
      params.departId = searchOptions.departId.trim();
    }

    if (searchOptions.deptName && searchOptions.deptName.trim() !== '') {
      params.deptName = searchOptions.deptName.trim();
    }

    const response = await axios.get(`${API_BASE_URL}/search`, { params });

    // 조회 결과를 items에 저장 (날짜 정리 등 필요시 여기에)
    items.value = response.data;

  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
};

// ✅ 부서 목록 초기 로딩용 함수
const loadDepts = async () => {
  await searchData({});
};

// ✅ 컴포넌트 마운트 시 실행
onMounted(() => {
  loadDepts();
});

const saveData = (inputData) => {
  console.log('Saving data:', inputData);
};
</script>

<template>
  <DeptInput
  :items="items"
    :filters="filters"
    :header="header"
    :inputs="inputs"
    :checkType="'multiple'"
    @searchData="searchData"
    @saveData="saveData"
  />
</template>