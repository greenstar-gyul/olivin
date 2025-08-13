<script setup>
import { ref, onMounted } from 'vue';
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import axios from '@/service/axios';

// API 경로
const DEPT_API_URL = '/api/depts';

// 데이터
const items = ref([]);
const loading = ref(false);
const selectedDept = ref(null);

// ✅ 현재 로그인한 사용자 정보 (emp와 동일)
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      return {
        empId: userData.employeeId || 'admin',
        employeeId: userData.employeeId || 'admin',
        empName: userData.empName || '관리자'
      };
    }
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error);
  }
  return { empId: 'admin', employeeId: 'admin', empName: '관리자' };
};

// ✅ departmentId 자동 생성 함수 - 올바른 필드명
const getNextDepartmentId = () => {
  const ids = items.value
    .map((item) => item.departmentId)  // departmentId 사용
    .filter((id) => id?.startsWith('DEPT'))
    .map((id) => parseInt(id.replace('DEPT', ''), 10))
    .filter((n) => !isNaN(n));

  const max = ids.length > 0 ? Math.max(...ids) : 0;
  const nextId = (max + 1).toString().padStart(3, '0');
  return `DEPT${nextId}`;
};

// 검색 조건 정의 (부서만 관리) - 올바른 필드명
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '부서ID', value: '', placeholder: '부서ID를 입력하세요', name: 'departmentId' },
    { type: 'text', label: '부서명', value: '', placeholder: '부서명을 입력하세요', name: 'deptName' }
  ]
});

// 테이블 헤더 정의 (부서만 관리) - 올바른 필드명
const header = ref({
  title: '부서 기준정보 관리',
  header: {
    departmentId: '부서ID',
    deptName: '부서명'
  },
  rightAligned: []
});

// ✅ 입력 폼 정의 수정 - 등록시에만 ID 생성
const inputs = ref({
  title: '부서 등록/수정',
  inputs: [
    { 
      type: 'text', 
      label: '부서ID', 
      placeholder: '등록 시 자동생성됩니다', 
      name: 'departmentId', 
      readonly: true 
    },
    { 
      type: 'text', 
      label: '부서명', 
      placeholder: '부서명을 입력하세요', 
      name: 'deptName', 
      required: true 
    }
  ]
});

// StandardInput 컴포넌트 ref
const standardInputRef = ref(null);

// ✅ 부서 목록 조회 (emp와 동일한 패턴)
const loadDepts = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('부서 목록 조회 시작...');
    
    const response = await axios.get(DEPT_API_URL, { params: searchParams });
    console.log('부서 API 응답:', response.data);
    
    let departments = [];
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      departments = response.data.data;
    } else if (Array.isArray(response.data)) {
      departments = response.data;
    } else {
      console.error('예상하지 못한 응답 구조:', response.data);
      departments = [];
    }
    
    items.value = departments.map((item, index) => ({
      id: item.departmentId || `temp_dept_${Date.now()}_${index}`,  // 올바른 필드명
      ...item
    }));
    
    console.log('최종 부서 목록 (수량:', items.value.length, '):', items.value);
    
  } catch (error) {
    console.error('부서 목록 조회 실패:', error);
    items.value = [];
  } finally {
    loading.value = false;
  }
};

// 검색 처리 (emp와 동일한 패턴)
const searchData = async (searchOptions) => {
  console.log('부서 검색 조건:', searchOptions);
  
  // 모든 검색 조건이 비어있는지 확인 (초기화 버튼 클릭 시)
  const hasSearchCondition = Object.values(searchOptions).some(value => {
    if (typeof value === 'string') {
      return value.trim() !== '';
    }
    return value !== null && value !== undefined && value !== '';
  });
  
  // 검색 조건이 없으면 입력 폼도 함께 초기화
  if (!hasSearchCondition) {
    console.log('검색 조건이 없어서 입력 폼도 초기화합니다.');
    
    // ✅ 선택된 부서 초기화 (여기가 핵심!)
    selectedDept.value = null;
    console.log('selectedDept.value 초기화됨:', selectedDept.value);
    
    // ✅ 데이터를 다시 로드하여 테이블 선택 상태도 초기화
    await loadDepts();
    
    // ✅ 추가: 데이터 배열을 강제로 업데이트하여 Vue의 반응성 시스템이 테이블을 다시 렌더링하도록 함
    const currentItems = [...items.value];
    items.value = [];
    await new Promise(resolve => setTimeout(resolve, 10)); // 아주 짧은 지연
    items.value = currentItems;
    
    if (standardInputRef.value?.inputFormRef) {
      standardInputRef.value.inputFormRef.resetInputDatas();
      
      setTimeout(async () => {
        await initializeFormData();
      }, 100);
    }
    return;
  }
  
  // 기존 검색 로직 - 올바른 필드명
  const searchParams = {};
  
  if (searchOptions.departmentId?.trim()) searchParams.departmentId = searchOptions.departmentId.trim();
  if (searchOptions.deptName?.trim()) searchParams.deptName = searchOptions.deptName.trim();
  
  console.log('최종 검색 파라미터:', searchParams);
  await loadDepts(searchParams);
};

// 행 선택 처리 (emp와 동일한 패턴)
const onRowSelect = (dept) => {
  console.log('선택된 부서:', dept);
  selectedDept.value = dept;
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      if (key !== 'id' && key in dept) {
        let value = dept[key] || '';
        inputFormRef.inputDatas[key] = String(value);
      }
    });
  }
};

// 행 선택 해제 처리 (emp와 동일한 패턴)
const onRowUnselect = () => {
  console.log('행 선택 해제됨');
  selectedDept.value = null;
};

// ✅ 저장 처리 - 등록시에만 ID 생성
const saveData = async (inputData) => {
  try {
    console.log('저장할 부서 데이터:', inputData);
    console.log('현재 selectedDept.value:', selectedDept.value);
    
    // 기본값 설정 및 검증
    const requiredFields = [
      { field: 'deptName', label: '부서명' }
    ];
    
    for (const req of requiredFields) {
      if (!inputData[req.field]?.trim()) {
        alert(`${req.label}은(는) 필수입력 항목입니다.`);
        return;
      }
    }
    
    const currentUserData = await getCurrentUser();
    const isUpdateMode = selectedDept.value?.departmentId;  // 올바른 필드명
    
    console.log('수정 모드 여부 (isUpdateMode):', isUpdateMode);
    
    const deptData = { ...inputData };
    
    // ✅ 등록 모드일 때만 부서 ID 자동생성
    if (!isUpdateMode) {
      deptData.departmentId = getNextDepartmentId();
      console.log('자동 생성된 부서 ID:', deptData.departmentId);
    }
    
    let response;
    
    if (isUpdateMode) {
      deptData.departmentId = selectedDept.value.departmentId;  // 올바른 필드명
      console.log('수정 모드: 기존 부서 ID 사용:', deptData.departmentId);
      response = await axios.put(`${DEPT_API_URL}/${selectedDept.value.departmentId}`, deptData);  // 올바른 필드명
    } else {
      console.log('등록 모드: 새 부서 생성');
      response = await axios.post(DEPT_API_URL, deptData);
    }
    
    if (response.data.result_code === 'SUCCESS') {
      const successMessage = isUpdateMode ? 
        `부서 정보가 수정되었습니다. (수정자: ${currentUserData.empName})` : 
        `부서가 등록되었습니다. (부서ID: ${deptData.departmentId}, 등록자: ${currentUserData.empName})`;  // 올바른 필드명
      
      alert(successMessage);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedDept.value = null;
      
      await loadDepts();
      setTimeout(async () => {
        await initializeFormData();
      }, 100);
    } else {
      alert(`저장 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('부서 저장 실패:', error);
    alert('저장 실패: ' + (error.response?.data?.message || error.message));
  }
};

// ✅ 삭제 처리 (emp와 동일한 패턴) - 올바른 필드명
const deleteData = async () => {
  if (!selectedDept.value?.departmentId) {  // 올바른 필드명
    alert('삭제할 부서를 선택해주세요.');
    return;
  }

  const confirmDelete = confirm(
    `부서 "${selectedDept.value.deptName}"을(를) 삭제하시겠습니까?`
  );
  
  if (!confirmDelete) return;

  try {
    const currentUserData = await getCurrentUser();
    
    const response = await axios.delete(`${DEPT_API_URL}/${selectedDept.value.departmentId}`);  // 올바른 필드명
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`부서 "${selectedDept.value.deptName}"이(가) 삭제되었습니다. (처리자: ${currentUserData.empName})`);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedDept.value = null;
      
      await loadDepts();
      setTimeout(async () => {
        await initializeFormData();
      }, 100);
    } else {
      alert(`삭제 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('부서 삭제 실패:', error);
    alert('삭제 실패: ' + (error.response?.data?.message || error.message));
  }
};

// ✅ 초기화 이벤트 핸들러 추가 (StandardInput에서 호출되는 경우)
const onFormReset = () => {
  console.log('폼 초기화 이벤트 발생');
  selectedDept.value = null;
  // 필요시 추가 초기화 로직
};

// ✅ 초기화 함수 수정 - 선택된 부서도 함께 초기화
const initializeFormData = async () => {
  console.log('부서 폼 초기화');
  
  // ✅ 선택된 부서 정보 초기화 (중요!)
  selectedDept.value = null;
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    // 부서 ID를 미리 생성하지 않고 빈 값으로 설정
    inputFormRef.inputDatas.departmentId = '';
    inputFormRef.inputDatas.deptName = '';
  }
};

// 컴포넌트 마운트 시 데이터 로드 (emp와 동일)
onMounted(async () => {
  await loadDepts();
  
  setTimeout(async () => {
    await initializeFormData();
  }, 100);
});
</script>

<template>
  <StandardInput
    ref="standardInputRef"
    :filters="filters"
    :items="items"
    :header="header"
    :inputs="inputs"
    @searchData="searchData"
    @saveData="saveData"
    @openSearchModal="openSearchModal"
    @rowSelect="onRowSelect"
    @rowUnselect="onRowUnselect"
    @formReset="onFormReset"
  >
    <!-- 삭제 버튼 -->
    <template #btn>
      <Button 
        label="삭제" 
        severity="danger" 
        class="min-w-fit whitespace-nowrap" 
        outlined
        :disabled="!selectedDept"
        @click="deleteData"
      />
    </template>
  </StandardInput>
</template>