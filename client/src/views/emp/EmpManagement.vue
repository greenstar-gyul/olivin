<script setup>
import EmpInput from './EmpInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

const items = ref([]);
const API_BASE_URL = '/api/public/emps';
const toast = useToast();
const fileUploadRef = ref();

// departmentId 자동 생성
const getNextDepartmentId = () => {
  const ids = items.value
    .map(item => item.departmentId)
    .filter(id => id?.startsWith('DEPT'))
    .map(id => parseInt(id.replace('DEPT', ''), 10))
    .filter(n => !isNaN(n));

  const max = ids.length > 0 ? Math.max(...ids) : 0;
  const nextId = (max + 1).toString().padStart(3, '0');
  return `DEPT${nextId}`;
};

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
    //password: '비밀번호',
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
        { type: 'text', label: '사원이름', value: '', name: 'empName' },
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
          name: 'deptName',
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

// 입력값 초기화 + 자동 departmentId 생성
const initializeInput = () => {
  inputs.value.inputs.forEach(input => {
    input.value = '';
  });

  const deptIdInput = inputs.value.inputs.find(input => input.name === 'departmentId');
  if (deptIdInput) {
    deptIdInput.value = getNextDepartmentId();
  }
};

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

    // 조회 결과를 items에 저장 (날짜 정리 등 필요시 여기에)
    items.value = response.data;

  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
};

// ✅ 등록/수정/삭제
const saveData = async (inputData, mode = 'insert') => {
  try {
    if (mode === 'insert') {
      await axios.post(`${API_BASE_URL}`, inputData);
      toast.add({ severity: 'success', summary: '등록 성공', life: 2000 });
    } else if (mode === 'update') {
      await axios.put(`${API_BASE_URL}/${inputData.employeeId}`, inputData);
      toast.add({ severity: 'success', summary: '수정 성공', life: 2000 });
    } else if (mode === 'delete') {
      await axios.delete(`${API_BASE_URL}/${inputData.employeeId}`);
      toast.add({ severity: 'warn', summary: '삭제 성공', life: 2000 });
    }
    await searchData({}); // 리스트 갱신
  } catch (error) {
    toast.add({ severity: 'error', summary: '요청 실패', detail: error.message, life: 3000 });
  }
};



// ✅ 부서 목록 초기 로딩용 함수
const loadEmps = async () => {
  await searchData({});
};

// ✅ 컴포넌트 마운트 시 실행
onMounted(() => {
  loadEmps();
  searchData({});
});

</script>

<template>
  <EmpInput
    :items="items"
    :filters="filters"
    :header="header"
    :inputs="inputs"
    @searchData="searchData"
    @saveData="saveData"
  />
</template>