<script setup>
import EmpInput from './EmpInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

// 사원 리스트 조회
const items = ref([]);
const API_BASE_URL = '/api/public/emps';
const toast = useToast();
const fileUploadRef = ref();

// employeeId 자동 생성 함수
const getNextEmployeeId = () => {
    const ids = items.value
        .map((item) => item.employeeId)
        .filter((id) => id?.startsWith('EMP'))
        .map((id) => parseInt(id.replace('EMP', ''), 10))
        .filter((n) => !isNaN(n));

    const max = ids.length > 0 ? Math.max(...ids) : 0;
    const nextId = (max + 1).toString().padStart(4, '0');
    return `EMP${nextId}`;
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
        //compId: '회사코드',
        compName: '회사명',
        //departmentId: '부서코드',
        deptName: '부서명',
        employeeId: '사원코드',
        empName: '사원이름',
        empType: '고용형태',
        email: '이메일',
        //password: '비밀번호',
        phone: '연락처',
        position: '직급'
    },
    rightAligned: []
});

// 입력폼 상태
const inputs = ref({
<<<<<<< HEAD
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
      ]
    },
    { type: 'text', label: '직책', value: '', name: 'position' }
  ]
=======
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
                { name: '안전관리팀', value: '안전관리팀' }
            ]
        },
        { type: 'text', label: '직책', value: '', name: 'position' }
    ]
>>>>>>> origin/main
});

const selectedImageFile = ref(null);
const selectedImageFiles = ref([]);
const uploadedImageUrl = ref('');

// 신규 등록 클릭 시 입력폼 초기화 + 사원ID 자동 생성 함수
const initializeInput = () => {
    inputs.value.inputs.forEach((input) => {
        if (input.name === 'employeeId') {
            input.value = getNextEmployeeId();
        } else {
            input.value = '';
        }
    });
};

const searchData = async (searchOptions) => {
    try {
        const params = {};
        for (const key in searchOptions) {
            if (searchOptions[key]?.trim()) {
                params[key] = searchOptions[key].trim();
            }
        }
        const response = await axios.get(`${API_BASE_URL}/search`, { params });
        items.value = response.data;
    } catch (error) {
        toast.add({ severity: 'error', summary: '검색 실패', detail: error.message, life: 3000 });
    }
};

// 저장 (등록, 수정, 삭제)
const saveData = async (inputData, mode = 'insert') => {
    try {
        if (mode === 'insert') {
            if (!inputData.employeeId) {
                inputData.employeeId = getNextEmployeeId();
            }
            await axios.post(`${API_BASE_URL}`, inputData);
            toast.add({ severity: 'success', summary: '등록 성공', life: 2000 });
        } else if (mode === 'update') {
            await axios.put(`${API_BASE_URL}/${inputData.employeeId}`, inputData);
            toast.add({ severity: 'success', summary: '수정 성공', life: 2000 });
        } else if (mode === 'delete') {
            await axios.delete(`${API_BASE_URL}/${inputData.employeeId}`);
            toast.add({ severity: 'warn', summary: '삭제 성공', life: 2000 });
        }
        await searchData({});
    } catch (error) {
        toast.add({ severity: 'error', summary: '요청 실패', detail: error.message, life: 3000 });
    }
};

// ✅ 부서 목록 초기 로딩용 함수
const loadEmps = async () => {
    await searchData({});
};

// 초기 데이터 조회
onMounted(async () => {
    loadEmps();
    await searchData({});
    initializeInput(); // 페이지 로딩 시 초기 입력폼에 자동ID 생성
});
</script>

<template>
    <EmpInput :items="items" :filters="filters" :header="header" :inputs="inputs" @searchData="searchData" @saveData="saveData" @initializeInput="initializeInput" />
</template>
