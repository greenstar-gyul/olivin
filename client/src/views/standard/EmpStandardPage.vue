<script setup>
import { ref, onMounted, nextTick } from 'vue';
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import axios from '@/service/axios';

// API 경로
const EMP_API_URL = '/api/emps';

// ✅ 기본 암호화된 비밀번호 (자동 설정용)
const DEFAULT_ENCRYPTED_PASSWORD = '$2a$12$Q7opGUilXhMM.4r7kdL2L.636YSbuKpYwp0KwEGeTBeZNQKb4/nvy';

// 데이터
const items = ref([]);
const loading = ref(false);
const selectedEmp = ref(null);

// 모달 관련
const companyModalVisible = ref(false);
const departmentModalVisible = ref(false);

// ✅ 현재 로그인한 사용자 정보
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

// 검색 조건 정의
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '사원명', value: '', placeholder: '사원명을 입력하세요', name: 'empName' },
    { type: 'text', label: '이메일', value: '', placeholder: 'example@company.com', name: 'email' },
    { type: 'text', label: '전화번호', value: '', placeholder: '010-0000-0000', name: 'phone' },
    { type: 'select', label: '상태', value: '', placeholder: '상태 선택', name: 'status', options: [
      { name: '재직중', value: '050001' },
      { name: '퇴사', value: '050002' }
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
    status: '상태',
  },
  rightAligned: []
});

// 입력 폼 정의
const inputs = ref({
  title: '사원 등록/수정',
  inputs: [
    { type: 'text', label: '사원ID', placeholder: '등록 시 자동생성', name: 'employeeId', readonly: true },
    { type: 'text', label: '사원명', placeholder: '사원명을 입력하세요', name: 'empName', required: true },
    { type: 'item-search', label: '회사선택', placeholder: '회사를 선택하세요', name: 'compId', required: true },
    { type: 'text', label: '회사명', placeholder: '회사명 (자동입력)', name: 'compName', readonly: true },
    { type: 'item-search', label: '부서선택', placeholder: '부서를 선택하세요', name: 'departmentId', required: true },
    { type: 'text', label: '부서명', placeholder: '부서명 (자동입력)', name: 'deptName', readonly: true },
    { type: 'select', label: '고용형태', placeholder: '고용형태를 선택하세요', name: 'empType', required: true, options: [
      { name: '정규직', value: '정규직' },
      { name: '비정규직', value: '비정규직' },
      { name: '임원', value: '임원' }
    ]},
    { type: 'select', label: '상태', placeholder: '상태를 선택하세요', name: 'status', required: true, options: [
      { name: '재직중', value: '050001' },
      { name: '퇴사', value: '050002' }
    ]},
    { type: 'text', label: '이메일', placeholder: 'example@company.com', name: 'email' },
    { type: 'text', label: '비밀번호', placeholder: '기본 비밀번호 자동설정', name: 'password', readonly: true },
    { type: 'text', label: '전화번호', placeholder: '010-0000-0000', name: 'phone' },
    { type: 'text', label: '직급', placeholder: '직급을 입력하세요', name: 'position' },
    { type: 'select', label: '성별', placeholder: '성별을 선택하세요', name: 'gender', options: [
      { name: '남성', value: 'M' },
      { name: '여성', value: 'F' }
    ]},
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

// StandardInput 컴포넌트 ref
const standardInputRef = ref(null);

// ✅ 사원 목록 조회
const loadEmps = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('사원 목록 조회 시작...');
    
    const response = await axios.get(EMP_API_URL, { params: searchParams });
    console.log('사원 API 응답:', response.data);
    
    let employees = [];
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      employees = response.data.data;
    } else if (Array.isArray(response.data)) {
      employees = response.data;
    } else {
      console.error('예상하지 못한 응답 구조:', response.data);
      employees = [];
    }
    
    // 상태 코드를 한글명으로 변환
    items.value = employees.map((item, index) => ({
      id: item.employeeId || `temp_emp_${Date.now()}_${index}`,
      ...item,
      status: getStatusName(item.status), // 상태 코드를 한글명으로 변환 (null 안전)
      gender: getGenderName(item.gender) // 성별 코드를 한글명으로 변환 (null 안전)
    }));
    
    console.log('최종 사원 목록 (수량:', items.value.length, '):', items.value);
    
  } catch (error) {
    console.error('사원 목록 조회 실패:', error);
    items.value = [];
  } finally {
    loading.value = false;
  }
};

// 상태 코드를 한글명으로 변환하는 함수
const getStatusName = (status) => {
  if (!status) {
    return '재직중'; // 기본값
  }
  switch (status) {
    case '050001':
      return '재직중';
    case '050002':
      return '퇴사';
    default:
      return status;
  }
};

// 성별 코드를 한글명으로 변환하는 함수  
const getGenderName = (gender) => {
  if (!gender) {
    return '미지정'; // 기본값
  }
  switch (gender) {
    case 'M':
      return '남성';
    case 'F':
      return '여성';
    default:
      return '미지정';
  }
};

// 검색 처리
const searchData = async (searchOptions) => {
  console.log('사원 검색 조건:', searchOptions);
  
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
    
    await loadEmps();
    selectedEmp.value = null;
    
    if (standardInputRef.value?.inputFormRef) {
      standardInputRef.value.inputFormRef.resetInputDatas();
      setTimeout(async () => {
        await initializeFormData();
      }, 100);
    }
    return;
  }
  
  // 기존 검색 로직
  const searchParams = {};
  
  if (searchOptions.empName?.trim()) searchParams.empName = searchOptions.empName.trim();
  if (searchOptions.email?.trim()) searchParams.email = searchOptions.email.trim();
  if (searchOptions.phone?.trim()) searchParams.phone = searchOptions.phone.trim();
  if (searchOptions.status?.trim()) searchParams.status = searchOptions.status.trim();
  if (searchOptions.gender?.trim()) searchParams.gender = searchOptions.gender.trim();
  
  // 날짜 범위 처리 (타임존 문제 해결)
  if (searchOptions.dateRangeFrom && searchOptions.dateRangeTo) {
    try {
      // 로컬 날짜로 처리하여 타임존 문제 해결
      searchParams.hireDateFrom = formatDateForBackend(searchOptions.dateRangeFrom);
      searchParams.hireDateTo = formatDateForBackend(searchOptions.dateRangeTo);
    } catch (dateError) {
      console.error('날짜 변환 오류:', dateError);
    }
  }
  
  console.log('최종 검색 파라미터:', searchParams);
  await loadEmps(searchParams);
};

// 행 선택 처리
const onRowSelect = (emp) => {
  console.log('선택된 사원:', emp);
  selectedEmp.value = emp;
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      if (key !== 'id' && key in emp) {
        let value = emp[key] || '';
        
        if ((key === 'hireDate' || key === 'createDate') && value) {
          value = formatDateForInput(value);
        }
        
        // 상태는 원본 코드값으로 설정 (화면 표시용 한글명이 아닌)
        if (key === 'status') {
          value = getStatusCode(emp.status) || '050001'; // 기본값 추가
        }
        
        // 성별도 원본 코드값으로 설정
        if (key === 'gender') {
          value = getGenderCode(emp.gender) || 'M'; // 기본값 추가
        }
        
        inputFormRef.inputDatas[key] = String(value);
      }
    });
    
    // 비밀번호는 항상 기본값으로 설정
    inputFormRef.inputDatas.password = DEFAULT_ENCRYPTED_PASSWORD;
  }
};

// 한글명을 상태 코드로 변환하는 함수
const getStatusCode = (statusName) => {
  if (!statusName) {
    return '050001'; // 기본값
  }
  switch (statusName) {
    case '재직중':
      return '050001';
    case '퇴사':
      return '050002';
    default:
      return statusName;
  }
};

// 한글명을 성별 코드로 변환하는 함수
const getGenderCode = (genderName) => {
  if (!genderName) {
    return 'M'; // 기본값
  }
  switch (genderName) {
    case '남성':
      return 'M';
    case '여성':
      return 'F';
    default:
      return genderName;
  }
};

// 회사 유형 코드를 한글명으로 변환하는 함수
const getCompanyTypeName = (compType) => {
  switch (compType) {
    case '100001':
      return '본사';
    case '100002':
      return '지점';
    case '100003':
      return '공급업체';
    default:
      return compType;
  }
};

// 행 선택 해제 처리
const onRowUnselect = () => {
  selectedEmp.value = null;
};

// ✅ 폼 초기화 이벤트 핸들러 추가
const onFormReset = () => {
  console.log('EmpStandardPage - 폼 초기화 이벤트 받음');
  
  // 선택된 사원 데이터 초기화
  selectedEmp.value = null;
  
  console.log('EmpStandardPage - 선택된 사원 초기화 완료');
};

// ✅ 저장 처리
const saveData = async (inputData) => {
  try {
    console.log('저장할 사원 데이터:', inputData);
    
    // 기본값 설정 및 검증
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
    
    const currentUserData = await getCurrentUser();
    const isUpdateMode = selectedEmp.value?.employeeId;
    
    const empData = { ...inputData };
    
    // 상태가 없거나 비어있으면 기본값(재직중) 설정
    if (!empData.status || empData.status.trim() === '') {
      empData.status = '050001';
    }
    
    // 성별이 없거나 비어있으면 기본값(남성) 설정
    if (!empData.gender || empData.gender.trim() === '') {
      empData.gender = 'M';
    }
    
    // 비밀번호가 없거나 비어있으면 기본 암호화된 비밀번호 설정
    if (!empData.password || empData.password.trim() === '') {
      empData.password = DEFAULT_ENCRYPTED_PASSWORD;
    }
    
    // 날짜 형식 변환 (타임존 문제 해결)
    if (empData.hireDate) {
      empData.hireDate = formatDateForBackend(empData.hireDate);
    }
    
    let response;
    
    if (isUpdateMode) {
      empData.employeeId = selectedEmp.value.employeeId;
      response = await axios.put(`${EMP_API_URL}/${selectedEmp.value.employeeId}`, empData);
    } else {
      // 신규 등록 시 employeeId는 서버에서 회사 유형별로 자동 생성됨
      delete empData.employeeId; // employeeId 제거
      response = await axios.post(EMP_API_URL, empData);
    }
    
    if (response.data.result_code === 'SUCCESS') {
      const successMessage = isUpdateMode ? 
        `사원 정보가 수정되었습니다. (수정자: ${currentUserData.empName})` : 
        `사원이 등록되었습니다. (사원ID: ${response.data.data?.employeeId || '자동생성'}, 등록자: ${currentUserData.empName})`;
      
      alert(successMessage);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedEmp.value = null;
      
      await loadEmps();
      setTimeout(async () => {
        await initializeFormData();
      }, 100);
    } else {
      alert(`저장 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('사원 저장 실패:', error);
    alert('저장 실패: ' + (error.response?.data?.message || error.message));
  }
};

// ✅ 퇴사 처리
const deleteData = async () => {
  if (!selectedEmp.value?.employeeId) {
    alert('퇴사 처리할 사원을 선택해주세요.');
    return;
  }

  const confirmResign = confirm(
    `사원 "${selectedEmp.value.empName}"을(를) 퇴사 처리하시겠습니까?`
  );
  
  if (!confirmResign) return;

  try {
    const currentUserData = await getCurrentUser();
    
    const response = await axios.delete(`${EMP_API_URL}/${selectedEmp.value.employeeId}`);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`사원 "${selectedEmp.value.empName}"이(가) 퇴사 처리되었습니다. (처리자: ${currentUserData.empName})`);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedEmp.value = null;
      
      await loadEmps();
      setTimeout(async () => {
        await initializeFormData();
      }, 100);
    } else {
      alert(`퇴사 처리 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('퇴사 처리 실패:', error);
    alert('퇴사 처리 실패: ' + (error.response?.data?.message || error.message));
  }
};

// ✅ 입력 폼 모달 처리 - 부서 데이터 매핑 문제 완전 해결
const openSearchModal = async (inputName) => {
  console.log('🎯 입력 모달 열기:', inputName);
  
  if (inputName === 'compId') {
    try {
      const response = await axios.get('/api/companies/active');
      
      if (response.data.result_code === 'SUCCESS' && response.data.data) {
        companyModalItems.value = response.data.data.map(company => ({
          compId: company.compId,
          compName: company.compName,
          compType: getCompanyTypeName(company.compType),
          ceoName: company.ceoName,
          phone: company.phone || ''
        }));
      }
      
      companyModalVisible.value = true;
      
    } catch (error) {
      console.error('회사 목록 조회 실패:', error);
      alert('회사 목록을 불러오는 중 오류가 발생했습니다.');
    }
    
  } else if (inputName === 'departmentId') {
    try {
      console.log('🏢 부서 API 호출 시작');
      
      const response = await axios.get('/api/emps/departments');
      console.log('📡 부서 API 전체 응답:', response);
      console.log('📊 부서 API 응답 데이터:', response.data);
      
      if (response.data && response.data.result_code === 'SUCCESS') {
        if (response.data.data && Array.isArray(response.data.data) && response.data.data.length > 0) {
          console.log('✅ 부서 원본 데이터 (처리 전):', response.data.data);
          console.log('🔍 첫 번째 부서 샘플:', response.data.data[0]);
          console.log('🔑 첫 번째 부서의 모든 키들:', Object.keys(response.data.data[0]));
          
          // ✅ 핵심 수정: 가능한 모든 필드명 케이스 처리
          departmentModalItems.value = response.data.data.map((dept, index) => {
            console.log(`📄 처리 중인 부서 #${index}:`, dept);
            
            // 다양한 케이스의 필드명을 모두 확인
            let departmentId = null;
            let deptName = null;
            
            // departmentId 찾기 (가능한 모든 케이스)
            if (dept.departmentId) departmentId = dept.departmentId;
            else if (dept.DEPARTMENT_ID) departmentId = dept.DEPARTMENT_ID;
            else if (dept.department_id) departmentId = dept.department_id;
            else if (dept.departmentid) departmentId = dept.departmentid;
            else if (dept.DEPARTMENTID) departmentId = dept.DEPARTMENTID;
            
            // deptName 찾기 (가능한 모든 케이스)
            if (dept.deptName) deptName = dept.deptName;
            else if (dept.DEPT_NAME) deptName = dept.DEPT_NAME;
            else if (dept.dept_name) deptName = dept.dept_name;
            else if (dept.deptname) deptName = dept.deptname;
            else if (dept.DEPTNAME) deptName = dept.DEPTNAME;
            else if (dept.departmentName) deptName = dept.departmentName;
            else if (dept.DEPARTMENT_NAME) deptName = dept.DEPARTMENT_NAME;
            else if (dept.name) deptName = dept.name;
            else if (dept.NAME) deptName = dept.NAME;
            
            const processedDept = {
              departmentId: departmentId || `DEPT${String(index + 1).padStart(3, '0')}`,
              deptName: deptName || `부서${index + 1}`
            };
            
            console.log(`✨ 처리된 부서 #${index}:`, processedDept);
            return processedDept;
          });
          
          console.log('🎯 최종 부서 모달 Items:', departmentModalItems.value);
          console.log('📋 부서 모달 Headers:', departmentModalHeaders.value);
          
          // 데이터 유효성 최종 검증
          if (departmentModalItems.value.length > 0) {
            const hasValidData = departmentModalItems.value.some(item => 
              item.departmentId && item.deptName
            );
            
            if (hasValidData) {
              console.log('✅ 유효한 부서 데이터가 있음, 모달 표시');
              departmentModalVisible.value = true;
            } else {
              console.error('❌ 변환된 데이터에 유효한 필드가 없음');
              alert('부서 데이터를 처리하는 중 문제가 발생했습니다.');
            }
          } else {
            console.error('❌ 변환된 데이터가 비어있음');
            alert('변환된 부서 데이터가 없습니다.');
          }
          
        } else {
          console.error('❌ 부서 데이터가 비어있거나 배열이 아닙니다:', response.data.data);
          alert('등록된 부서가 없습니다.');
        }
      } else {
        console.error('❌ 부서 API 응답 구조 문제:', response.data);
        alert('부서 데이터를 불러오는 중 오류가 발생했습니다.');
      }
      
    } catch (error) {
      console.error('❌ 부서 API 호출 실패:', error);
      alert('부서 목록을 불러오는 중 오류가 발생했습니다: ' + (error.response?.data?.message || error.message));
    }
  }
};

// 회사 선택 처리
const confirmCompany = (selectedCompany) => {
  console.log('🏢 회사 선택:', selectedCompany);
  if (standardInputRef.value?.inputFormRef && selectedCompany) {
    standardInputRef.value.inputFormRef.inputDatas.compId = selectedCompany.compId;
    standardInputRef.value.inputFormRef.inputDatas.compName = selectedCompany.compName;
    
    // 회사 선택 시 부서 초기화
    standardInputRef.value.inputFormRef.inputDatas.departmentId = '';
    standardInputRef.value.inputFormRef.inputDatas.deptName = '';
  }
  companyModalVisible.value = false;
};

// 부서 선택 처리
const confirmDepartment = (selectedDepartment) => {
  console.log('🏢 부서 선택:', selectedDepartment);
  if (standardInputRef.value?.inputFormRef && selectedDepartment) {
    standardInputRef.value.inputFormRef.inputDatas.departmentId = selectedDepartment.departmentId;
    standardInputRef.value.inputFormRef.inputDatas.deptName = selectedDepartment.deptName;
  }
  departmentModalVisible.value = false;
};

// ✅ 백엔드로 보낼 날짜 형식 변환 함수 (타임존 문제 해결)
const formatDateForBackend = (dateValue) => {
  if (!dateValue) return null;
  
  try {
    let date;
    
    if (typeof dateValue === 'string') {
      // YYYY-MM-DD 형식이면 그대로 반환
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateValue)) {
        return dateValue;
      }
      // 로컬 시간으로 날짜 생성 (타임존 오프셋 없이)
      date = new Date(dateValue + 'T00:00:00');
    } else if (dateValue instanceof Date) {
      date = dateValue;
    } else {
      return null;
    }
    
    // 유효한 날짜인지 확인
    if (isNaN(date.getTime())) {
      console.error('유효하지 않은 날짜:', dateValue);
      return null;
    }
    
    // 로컬 날짜 기준으로 YYYY-MM-DD 형식 반환
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  } catch (error) {
    console.error('날짜 변환 오류:', error, dateValue);
    return null;
  }
};

// 날짜 포맷 함수 (타임존 문제 해결)
const formatDateForInput = (dateString) => {
  if (!dateString) return '';
  try {
    // 이미 YYYY-MM-DD 형식이면 그대로 반환
    if (typeof dateString === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
      return dateString;
    }
    
    let dateOnly = dateString;
    if (dateString.includes('T')) {
      dateOnly = dateString.split('T')[0];
    }
    
    // 날짜 유효성 검사
    if (/^\d{4}-\d{2}-\d{2}$/.test(dateOnly)) {
      return dateOnly;
    }
    
    // 날짜 객체로 변환 후 로컬 날짜 기준으로 포맷
    const date = new Date(dateString + 'T00:00:00');
    if (!isNaN(date.getTime())) {
      return formatDateForBackend(date);
    }
    
    return dateOnly;
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

// ✅ 초기화 함수
const initializeFormData = async () => {
  const user = await getCurrentUser();
  console.log('폼 초기화 시 사용자 정보:', user);
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    inputFormRef.inputDatas.employeeId = '';
    inputFormRef.inputDatas.password = DEFAULT_ENCRYPTED_PASSWORD;
    inputFormRef.inputDatas.status = '050001'; // 기본 상태를 재직중으로
    inputFormRef.inputDatas.gender = 'M'; // 기본 성별을 남성으로
    
    // 오늘 날짜를 입사일 기본값으로 설정 (타임존 문제 해결)
    const now = new Date();
    const todayStr = formatDateForBackend(now);
    inputFormRef.inputDatas.hireDate = todayStr;
  }
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await loadEmps();
  
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
    <!-- 퇴사처리 버튼 -->
    <template #btn>
      <Button 
        label="퇴사" 
        severity="danger" 
        class="min-w-fit whitespace-nowrap" 
        outlined
        :disabled="!selectedEmp"
        @click="deleteData"
      />
    </template>
  </StandardInput>

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
</template>