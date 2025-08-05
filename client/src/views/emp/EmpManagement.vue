<script setup>
import EmpInput from './EmpInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

const items = ref([]);
const API_BASE_URL = '/api/emps';
const toast = useToast();
const fileUploadRef = ref();

// windos.location.origin을 computed로 처리
const baseUrl = computed(() => {
  return typeof window !== 'undefined' ? window.location.origin : '';
});

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '회사코드', value: '', name: 'compId' },
    { type: 'text', label: '회사명', value: '', name: 'compName' },
    { type: 'text', label: '부서코드', value: '', name: 'departmentId' },
    { type: 'text', label: '부서명', value: '', name: 'EmpName' }
  ]
});

const selectedEmp = ref(null);
const selectEmpId = ref('');

const header = ref({
  title: '조회 결과',
  header: {
    compId: '회사코드',
    compName: '회사명',
    departmentId: '부서코드',
    deptName: '부서명',
    employeeId: '사원코드',
    empName: '사원이름',
    empType: '고용형태',
    email: '이메일',
    password: '비밀번호',
    phone: '연락처',
    position: '직급',
  },
  rightAligned: []
});

// 입력필드
    const inputs = ref({
      title: '입력 폼',
      inputs: [
        { type: 'text', label: '회사코드', value: '', name: 'compId' },
        { type: 'text', label: '회사명', value: '', name: 'compName' },
        { type: 'text', label: '사원번호', value: '', name: 'employeeId' },
        {
          type: 'select',
          label: '고용형태',
          value: '',
          name: 'empType',
          options: [
            { name: '정규직', value: '정규직' },
            { name: '비정규직', value: '비정규직' }
          ]
        },
        { type: 'text', label: '이메일', value: '', name: 'email' },
        { type: 'text', label: '비밀번호', value: '', name: 'password' },
        { type: 'text', label: '연락처', value: '', name: 'phone' },
        {
          type: 'select',
          label: '부서명',
          value: '',
          name: 'EmpName',
          options: [
            { name: '경영지원팀', value: '경영지원팀' },
            { name: '영업팀', value: '영업팀' },
            { name: '물류팀', value: '물류팀' },
            { name: '구매팀', value: '구매팀' },
            { name: '회계팀', value: '회계팀' },
            { name: '마케팅팀', value: '마케팅팀' },
            { name: '품질관리팀', value: '품질관리팀' },
            { name: '연구개발팀', value: '연구개발팀' },
            { name: '고객서비스팀', value: '고객서비스팀' },
            { name: 'IT팀', value: 'IT팀' },
            { name: '전략기획팀', value: '전략기획팀' },
            { name: '인사팀', value: '인사팀' },
            { name: '법무팀', value: '법무팀' },
            { name: '감사팀', value: '감사팀' },
            { name: '해외사업팀', value: '해외사업팀' },
            { name: '디지털마케팅팀', value: '디지털마케팅팀' },
            { name: '브랜드팀', value: '브랜드팀' },
            { name: '상품기획팀', value: '상품기획팀' },
            { name: '교육팀', value: '교육팀' },
            { name: '안전관리팀', value: '안전관리팀' },
          ]},
          { type: 'text', label: '직책', value: '', name: 'position' },
        ]
      });

const selectedImageFile = ref(null);
const selectedImageFiles = ref([]);
const uploadedImageUrl = ref('');

const searchData = async (searchOptions) => {
  try {
    const params = {};

    if (searchOptions.compId?.trim()) {
      params.compId = searchOptions.compId.trim();
    }

    if (searchOptions.compName?.trim()) {
      params.compName = searchOptions.compName.trim();
    }

    if (searchOptions.departmentId?.trim()) {
      params.departmentId = searchOptions.departmentId.trim();
    }

    if (searchOptions.EmpName?.trim()) {
      params.EmpName = searchOptions.EmpName.trim();
    }

    if (searchOptions.employeeId?.trim()) {
      params.employeeId = searchOptions.employeeId.trim();
    }

    if (searchOptions.empName?.trim()) {
      params.empName = searchOptions.empName.trim();
    }

    if (searchOptions.empType?.trim()) {
      params.empType = searchOptions.empType.trim();
    }

    if (searchOptions.email?.trim()) {
      params.email = searchOptions.email.trim();
    }

    if (searchOptions.password?.trim()) {
      params.password = searchOptions.password.trim();
    }

    if (searchOptions.phone?.trim()) {
      params.phone = searchOptions.phone.trim();
    }

    if (searchOptions.position?.trim()) {
      params.position = searchOptions.position.trim();
    }

    const response = await axios.get(`${API_BASE_URL}/search`, { params });

    items.value = response.data;

  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
};

// ✅ 부서 목록 초기 로딩용 함수
const loadEmps = async () => {
  await searchData({});
};

// ✅ 컴포넌트 마운트 시 실행
onMounted(() => {
  loadEmps();
});

const saveData = (inputData) => {
  console.log('Saving data:', inputData);
};
</script>

<template>
  <EmpInput
  :items="items"
    :filters="filters"
    :header="header"
    :inputs="inputs"
    :checkType="'multiple'"
    @searchData="searchData"
    @saveData="saveData"
  />
</template>