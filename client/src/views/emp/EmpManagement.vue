<script setup>
import { ref, onMounted } from 'vue';
import SearchForm from '@/components/inputForm/SearchForm.vue';
import BasicTable from '@/components/table/BasicTable.vue';
import InputForm from '@/components/inputForm/InputForm.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import axios from '@/service/axios';

// API 경로
const EMP_API_URL = '/api/public/emps';

// 사원 목록 데이터
const items = ref([]);
const loading = ref(false);

// 선택된 사원
const selectedEmp = ref(null);

// 모달 관련
const companyModalVisible = ref(false);
const departmentModalVisible = ref(false);

// ✅ 사원 ID 자동생성 함수 (olivin + 5자리 순번)
const getNextEmployeeId = () => {
  const ids = items.value
    .map(item => item.employeeId)
    .filter(id => id?.startsWith('olivin'))
    .map(id => parseInt(id.replace('olivin', ''), 10))
    .filter(n => !isNaN(n));

  const max = ids.length > 0 ? Math.max(...ids) : 10000;
  let nextId = max + 1;
  
  // 명세서 범위 확인
  if (nextId < 10001) nextId = 10001;
  if (nextId > 99999) nextId = 10001; // 범위 초과시 처음으로
  
  return `olivin${nextId.toString().padStart(5, '0')}`;
};

// 검색 조건 정의
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '사원명', value: '', placeholder: '사원명을 입력하세요', name: 'empName' },
    { type: 'text', label: '이메일', value: '', placeholder: 'example@company.com', name: 'email' },
    { type: 'text', label: '전화번호', value: '', placeholder: '010-0000-0000', name: 'phone' },
    { type: 'select', label: '고용형태', value: '', placeholder: '고용형태 선택', name: 'empType', options: [
      { name: '정규직', value: '정규직' },
      { name: '비정규직', value: '비정규직' },
      { name: '계약직', value: '계약직' },
      { name: '인턴', value: '인턴' }
    ]},
    { type: 'dateRange', label: '입사일', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'dateRange' }
  ]
});

// 테이블 헤더 정의
const header = ref({
  title: '사원 기준정보 관리',
  header: {
    employeeId: '사원ID', 
    empName: '사원명', 
    compName: '회사명', 
    deptName: '부서명',
    empType: '고용형태',
    email: '이메일',
    phone: '전화번호',
    position: '직급'
  },
  rightAligned: []
});

// 입력 폼 정의
const inputs = ref({
  title: '사원 등록/수정',
  inputs: [
    { type: 'text', label: '사원ID', placeholder: '자동생성', name: 'employeeId', readonly: true },
    { type: 'text', label: '사원명', placeholder: '사원명을 입력하세요', name: 'empName', required: true },
    { type: 'item-search', label: '회사선택', placeholder: '회사를 선택하세요', name: 'compId', required: true },
    { type: 'text', label: '회사명', placeholder: '회사명 (자동입력)', name: 'compName', readonly: true },
    { type: 'item-search', label: '부서선택', placeholder: '부서를 선택하세요', name: 'departmentId', required: true },
    { type: 'text', label: '부서명', placeholder: '부서명 (자동입력)', name: 'deptName', readonly: true },
    { type: 'select', label: '고용형태', placeholder: '고용형태를 선택하세요', name: 'empType', required: true, options: [
      { name: '정규직', value: '정규직' },
      { name: '비정규직', value: '비정규직' },
      { name: '계약직', value: '계약직' },
      { name: '인턴', value: '인턴' }
    ]},
    { type: 'text', label: '이메일', placeholder: 'example@company.com', name: 'email' },
    { type: 'text', label: '비밀번호', placeholder: '비밀번호를 입력하세요', name: 'password' },
    { type: 'text', label: '전화번호', placeholder: '010-0000-0000', name: 'phone' },
    { type: 'text', label: '직급', placeholder: '직급을 입력하세요', name: 'position' },
    { type: 'date', label: '입사일', placeholder: '입사일을 선택하세요', name: 'hireDate' }
  ]
});

// 모달 데이터
const companyModalHeaders = ref([
  { field: 'compId', header: '회사ID' },
  { field: 'compName', header: '회사명' },
  { field: 'compType', header: '회사유형' },
  { field: 'ceoName', header: '대표자명' },
  { field: 'phone', header: '전화번호' }
]);
const companyModalItems = ref([]);

const departmentModalHeaders = ref([
  { field: 'departmentId', header: '부서ID' },
  { field: 'deptName', header: '부서명' }
]);
const departmentModalItems = ref([]);

// 컴포넌트 ref
const inputFormRef = ref(null);

// 사원 목록 조회
const loadEmps = async () => {
  try {
    loading.value = true;
    console.log('사원 목록 조회 시작...');
    
    const response = await axios.get(`${EMP_API_URL}/search`);
    console.log('사원 API 응답:', response.data);
    
    let employees = [];
    if (Array.isArray(response.data)) {
      employees = response.data;
    } else if (response.data && response.data.data) {
      employees = response.data.data;
    }
    
    items.value = employees.map((item, index) => ({
      id: item.employeeId || `temp_emp_${Date.now()}_${index}`,
      ...item
    }));
    
    console.log('최종 사원 목록 (수량:', items.value.length, '):', items.value);
    
  } catch (error) {
    console.error('사원 목록 조회 실패:', error);
    
    // 테스트용 데이터
    items.value = [
      {
        id: 'olivin10001',
        employeeId: 'olivin10001',
        empName: '홍길동',
        compName: '올리브인 본사',
        deptName: '개발팀',
        empType: '정규직',
        email: 'hong@olivin.com',
        phone: '010-1234-5678',
        position: '대리'
      }
    ];
    console.log('테스트 데이터 사용');
  } finally {
    loading.value = false;
  }
};

// 검색 처리
const searchData = async (searchOptions) => {
  console.log('검색 조건:', searchOptions);
  await loadEmps();
};

// 검색 모달 처리
const openSearchModal = (filterName) => {
  console.log('검색 모달 열기:', filterName);
};

// 검색 조건 초기화
const resetSearchOptions = () => {
  console.log('검색 조건 초기화');
};

// 행 선택 처리
const onRowSelect = (emp) => {
  console.log('선택된 사원:', emp);
  selectedEmp.value = emp;
  
  if (inputFormRef.value) {
    // 선택된 사원 데이터로 입력 폼 채우기
    Object.keys(inputFormRef.value.inputDatas).forEach(key => {
      if (key in emp) {
        inputFormRef.value.inputDatas[key] = emp[key] || '';
      }
    });
  }
};

// 행 선택 해제 처리
const onRowUnselect = () => {
  selectedEmp.value = null;
  if (inputFormRef.value) {
    inputFormRef.value.resetInputDatas();
    initializeFormData();
  }
};

// 저장 처리
const saveData = async (inputData) => {
  try {
    console.log('저장할 사원 데이터:', inputData);
    
    const requiredFields = [
      { field: 'empName', label: '사원명' },
      { field: 'compId', label: '회사' },
      { field: 'departmentId', label: '부서' },
      { field: 'empType', label: '고용형태' }
    ];
    
    for (const req of requiredFields) {
      if (!inputData[req.field]?.trim()) {
        alert(`${req.label}은(는) 필수입력 항목입니다.`);
        return;
      }
    }
    
    const isUpdateMode = selectedEmp.value?.employeeId;
    const empData = { ...inputData };
    
    if (!isUpdateMode && !empData.employeeId) {
      empData.employeeId = getNextEmployeeId();
    }
    
    let response;
    
    if (isUpdateMode) {
      response = await axios.put(`${EMP_API_URL}/${selectedEmp.value.employeeId}`, empData);
    } else {
      response = await axios.post(EMP_API_URL, empData);
    }
    
    if (response.status === 200 || response.data > 0) {
      alert(isUpdateMode ? '수정되었습니다.' : '등록되었습니다.');
      
      if (inputFormRef.value) {
        inputFormRef.value.resetInputDatas();
        initializeFormData();
      }
      selectedEmp.value = null;
      
      await loadEmps();
    } else {
      alert('저장에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('저장 실패:', error);
    alert('저장 실패: ' + error.message);
  }
};

// 삭제 처리
const deleteData = async () => {
  if (!selectedEmp.value?.employeeId) {
    alert('삭제할 사원을 선택해주세요.');
    return;
  }

  if (!confirm(`사원 "${selectedEmp.value.empName}"을(를) 삭제하시겠습니까?`)) {
    return;
  }

  try {
    const response = await axios.delete(`${EMP_API_URL}?employeeId=${selectedEmp.value.employeeId}`);
    
    if (response.status === 200 || response.data > 0) {
      alert('삭제되었습니다.');
      
      if (inputFormRef.value) {
        inputFormRef.value.resetInputDatas();
        initializeFormData();
      }
      selectedEmp.value = null;
      
      await loadEmps();
    } else {
      alert('삭제에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('삭제 실패:', error);
    alert('삭제 실패: ' + error.message);
  }
};

// 입력 폼 모달 처리
const openInputModal = (inputName) => {
  console.log('입력 모달 열기:', inputName);
  
  if (inputName === 'compId') {
    // 회사 목록 조회 후 모달 열기
    companyModalItems.value = [
      { compId: 'COM10001', compName: '올리브인 본사', compType: '본사', ceoName: '김대표', phone: '02-1234-5678' },
      { compId: 'COM20001', compName: '올리브인 강남점', compType: '지점', ceoName: '이점장', phone: '02-2345-6789' },
      { compId: 'COM20002', compName: '올리브인 홍대점', compType: '지점', ceoName: '박점장', phone: '02-3456-7890' }
    ];
    companyModalVisible.value = true;
    
  } else if (inputName === 'departmentId') {
    // 부서 목록 조회 후 모달 열기
    departmentModalItems.value = [
      { departmentId: 'DEPT001', deptName: '경영지원팀' },
      { departmentId: 'DEPT002', deptName: '재무회계팀' },
      { departmentId: 'DEPT003', deptName: '구매팀' },
      { departmentId: 'DEPT004', deptName: '물류팀' },
      { departmentId: 'DEPT005', deptName: '영업팀' },
      { departmentId: 'DEPT006', deptName: '마케팅팀' },
      { departmentId: 'DEPT007', deptName: '상품기획팀' },
      { departmentId: 'DEPT008', deptName: 'IT팀' },
      { departmentId: 'DEPT009', deptName: '고객서비스팀' },
      { departmentId: 'DEPT010', deptName: '매장관리팀' }
    ];
    departmentModalVisible.value = true;
  }
};

// 회사 선택 처리
const confirmCompany = (selectedCompany) => {
  if (inputFormRef.value && selectedCompany) {
    inputFormRef.value.inputDatas.compId = selectedCompany.compId;
    inputFormRef.value.inputDatas.compName = selectedCompany.compName;
    
    // 회사 선택 시 부서 초기화
    inputFormRef.value.inputDatas.departmentId = '';
    inputFormRef.value.inputDatas.deptName = '';
  }
  companyModalVisible.value = false;
};

// 부서 선택 처리
const confirmDepartment = (selectedDepartment) => {
  if (inputFormRef.value && selectedDepartment) {
    inputFormRef.value.inputDatas.departmentId = selectedDepartment.departmentId;
    inputFormRef.value.inputDatas.deptName = selectedDepartment.deptName;
  }
  departmentModalVisible.value = false;
};

// 초기화 함수
const initializeFormData = () => {
  if (inputFormRef.value) {
    inputFormRef.value.inputDatas.employeeId = getNextEmployeeId();
    
    // 오늘 날짜를 입사일 기본값으로 설정
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    inputFormRef.value.inputDatas.hireDate = `${year}-${month}-${day}`;
  }
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  console.log('EmpManagement 컴포넌트 마운트됨');
  await loadEmps();
  
  setTimeout(() => {
    initializeFormData();
  }, 1000);
});
</script>

<template>
  <div>
    <SearchForm 
      :filters="filters" 
      @searchData="searchData" 
      @openSearchModal="openSearchModal" 
      @resetSearchOptions="resetSearchOptions" 
    />
    
    <div class="grid grid-cols-7 gap-4 mb-4 items-stretch">
      <BasicTable
        :data="items"
        :header="header"
        :checked="true"
        dataKey="employeeId"
        @rowSelect="onRowSelect"
        @rowUnselect="onRowUnselect"
        class="col-span-4"
      >
        <template #btn>
          <Button 
            label="삭제" 
            severity="danger" 
            class="min-w-fit whitespace-nowrap" 
            outlined
            :disabled="!selectedEmp"
            @click="deleteData"
          />
        </template>
      </BasicTable>

      <InputForm
        ref="inputFormRef"
        :inputs="inputs"
        @saveData="saveData"
        @openSearchModal="openInputModal"
        class="col-span-3"
      />
    </div>

    <!-- 회사 선택 모달 -->
    <DialogModal 
      :display="companyModalVisible"
      title="회사 선택"
      :headers="companyModalHeaders"
      :items="companyModalItems"
      selectionMode="single"
      @close="companyModalVisible = false"
      @confirm="confirmCompany"
    />

    <!-- 부서 선택 모달 -->
    <DialogModal 
      :display="departmentModalVisible"
      title="부서 선택"
      :headers="departmentModalHeaders"
      :items="departmentModalItems"
      selectionMode="single"
      @close="departmentModalVisible = false"
      @confirm="confirmDepartment"
    />
  </div>
</template>