<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';

const API_BASE_URL = '/api/companies';

// 회사 유형 코드 상수 
const COMPANY_TYPES = {
  HEADQUARTERS: '100001', // 본사
  BRANCH: '100002',       // 지점  
  SUPPLIER: '100003',     // 공급업체
  INACTIVE: 'FFFFFF'      // 비활성화
};

// 현재 로그인한 사용자 정보
const currentUser = ref({
  empId: '',
  employeeId: '',
  empName: ''
});

// ✅ 날짜 포맷 함수 (Oracle 호환)
const formatDateForOracle = (dateInput) => {
  if (!dateInput) return null;
  
  try {
    let date;
    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      // YYYY-MM-DD 형식이면 그대로 사용
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateInput.trim())) {
        return dateInput.trim();
      }
      date = new Date(dateInput.trim());
    } else {
      date = new Date(dateInput);
    }
    
    if (isNaN(date.getTime())) {
      throw new Error('유효하지 않은 날짜');
    }
    
    // YYYY-MM-DD 형식으로 변환
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  } catch (error) {
    console.error('날짜 변환 오류:', error);
    return null;
  }
};

// getCurrentUser 함수
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    console.log('사용자 API 전체 응답:', JSON.stringify(response.data, null, 2));
    
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      
      let employeeId = 'admin';
      let empName = '관리자';
      
      const possibleUserSources = [
        userData.user, userData, userData.employee, userData.userInfo, userData.loginUser
      ];
      
      for (const userSource of possibleUserSources) {
        if (userSource && typeof userSource === 'object') {
          const possibleEmployeeIds = [
            userSource.employeeId, userSource.employee_id, userSource.EMPLOYEE_ID
          ];
          const possibleEmpNames = [
            userSource.empName, userSource.emp_name, userSource.EMP_NAME
          ];
          
          const foundEmployeeId = possibleEmployeeIds.find(id => id && id !== 'admin' && String(id).trim() !== '');
          const foundEmpName = possibleEmpNames.find(name => name && name !== '관리자' && String(name).trim() !== '');
          
          if (foundEmployeeId) employeeId = String(foundEmployeeId).trim();
          if (foundEmpName) empName = String(foundEmpName).trim();
          
          if (foundEmployeeId && foundEmpName) break;
        }
      }
      
      currentUser.value = {
        empId: employeeId,
        employeeId: employeeId,
        empName: empName
      };
      
      return currentUser.value;
    } else {
      throw new Error('API 응답에 사용자 데이터가 없습니다');
    }
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error);
    currentUser.value = { empId: 'admin', employeeId: 'admin', empName: '관리자' };
    return currentUser.value;
  }
};

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요', name: 'compName' },
    { type: 'text', label: '사업자번호', value: '', placeholder: '000-00-00000', name: 'bizNumber' },
    { type: 'text', label: '지점장명', value: '', placeholder: '지점장명을 입력하세요', name: 'ceoName' },
    { type: 'text', label: '전화번호', value: '', placeholder: '02-0000-0000', name: 'phone' },
    { type: 'dateRange', label: '등록일', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'dateRange' }
  ]
});

const items = ref([]);
const loading = ref(false);

const header = ref({
  title: '지점 기준정보 관리',
  header: {
    compId: '지점ID', 
    compName: '지점명', 
    bizNumber: '사업자번호', 
    ceoName: '지점장명', 
    phone: '전화번호',
  },
  rightAligned: []
});

const inputs = ref({
  title: '지점 등록/수정',
  inputs: [
    { type: 'text', label: '지점ID', placeholder: '자동생성', name: 'compId', readonly: true },
    { type: 'text', label: '지점명', placeholder: '지점명을 입력하세요', name: 'compName', required: true },
    { type: 'text', label: '사업자번호', placeholder: '000-00-00000', name: 'bizNumber', required: true },
    { type: 'text', label: '지점장명', placeholder: '지점장명을 입력하세요', name: 'ceoName', required: true },
    { type: 'text', label: '전화번호', placeholder: '02-0000-0000', name: 'phone' },
    { type: 'text', label: '우편번호', placeholder: '00000', name: 'zipcode' },
    { type: 'text', label: '주소', placeholder: '주소를 입력하세요', name: 'address' },
    { type: 'text', label: '상세주소', placeholder: '상세주소를 입력하세요', name: 'addressDetail' },
    { type: 'select', label: '정산주기', placeholder: '정산주기를 선택하세요', name: 'settleCycle', options: [
      { name: '월말정산', value: '월말정산' },
      { name: '15일정산', value: '15일정산' },
      { name: '주간정산', value: '주간정산' }
    ]},
    { type: 'text', label: '정산담당자', placeholder: '정산담당자명', name: 'settleMgr' },
    { type: 'text', label: '등록자', placeholder: '등록자 ID', name: 'regUser', readonly: true },
    { type: 'text', label: '등록일', placeholder: '2024-01-01 형식으로 입력하세요', name: 'regDate' },
    { type: 'textarea', label: '비고', placeholder: '특이사항을 입력하세요', name: 'note' }
  ]
});

// StandardInput 컴포넌트 ref
const standardInputRef = ref(null);

// 선택된 지점 정보
const selectedBranch = ref(null);

// 유틸리티 함수들
const formatAddress = (address, addressDetail) => {
  if (!address) return '';
  return addressDetail ? `${address} ${addressDetail}` : address;
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  } catch (error) {
    return dateString;
  }
};

const formatDateForInput = (dateString) => {
  if (!dateString) return '';
  try {
    let dateOnly = dateString;
    if (dateString.includes('T')) {
      dateOnly = dateString.split('T')[0];
    }
    return dateOnly;
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

const getStatusText = (compType) => {
  if (compType === COMPANY_TYPES.INACTIVE) return '비활성';
  return '활성';
};

const getStatusColor = (compType) => {
  if (compType === COMPANY_TYPES.INACTIVE) return 'text-red-500';
  return 'text-green-500';
};

// 지점 목록 조회 (비활성화된 지점 제외)
const loadBranches = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('지점 목록 조회 시작...');
    
    const response = await axios.get(API_BASE_URL, { params: searchParams });
    
    console.log('회사 API 원본 응답:', response.data);
    
    let companies = [];
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      companies = response.data.data;
    } else if (Array.isArray(response.data)) {
      companies = response.data;
    } else {
      console.error('예상하지 못한 응답 구조:', response.data);
      companies = [];
    }
    
    // 활성 지점만 필터링 (비활성화된 지점 제외)
    const branches = companies.filter(item => 
      item.compType === COMPANY_TYPES.BRANCH  // 활성 지점만
    );
    
    items.value = branches.map((item, index) => ({
      id: item.compId || `temp_branch_${Date.now()}_${index}`,
      ...item,
      address: formatAddress(item.address, item.addressDetail),
      regDate: item.regDate ? formatDate(item.regDate) : '',
      updateDate: item.updateDate ? formatDate(item.updateDate) : null,
      status: getStatusText(item.compType)
    }));
    
    console.log('최종 지점 목록:', items.value);
    
  } catch (error) {
    console.error('지점 목록 조회 실패:', error);
    alert('데이터 조회에 실패했습니다.');
    items.value = [];
  } finally {
    loading.value = false;
  }
};

// 지점 사용 여부 확인 함수
const checkBranchUsage = async (compId) => {
  try {
    console.log('지점 사용 여부 확인:', compId);
    
    // 백엔드 통합 API 사용
    const response = await axios.get(`${API_BASE_URL}/${compId}/usage`);
    
    if (response.data?.result_code === 'SUCCESS') {
      const usageData = response.data.data;
      return {
        isUsed: usageData.isUsed,
        purchaseOrderCount: usageData.purchaseOrderCount,
        details: usageData.details
      };
    } else {
      return { isUsed: false, purchaseOrderCount: 0, details: {} };
    }
    
  } catch (error) {
    console.log('사용 여부 확인 실패:', error.message);
    // API 오류 시 안전하게 사용하지 않는 것으로 처리
    return { isUsed: false, purchaseOrderCount: 0, details: {} };
  }
};

// ✅ 검색 함수 수정 (초기화 시 폼 리셋 추가)
const searchData = async (searchOptions) => {
  console.log('지점 검색 조건:', searchOptions);
  
  // ✅ 모든 검색 조건이 비어있는지 확인 (초기화 버튼을 눌렀을 때)
  const hasSearchCondition = Object.values(searchOptions).some(value => {
    if (typeof value === 'string') {
      return value.trim() !== '';
    }
    return value !== null && value !== undefined && value !== '';
  });
  
  // ✅ 검색 조건이 없으면 입력 폼도 함께 초기화
  if (!hasSearchCondition) {
    console.log('검색 조건이 없어서 입력 폼도 초기화합니다.');
    
    // 1. 전체 지점 목록 로드
    await loadBranches();
    
    // 2. 선택된 지점 초기화
    selectedBranch.value = null;
    
    // 3. 입력 폼 초기화 및 기본값 설정
    if (standardInputRef.value?.inputFormRef) {
      standardInputRef.value.inputFormRef.resetInputDatas();
      
      // 등록자, 등록일 다시 설정
      setTimeout(async () => {
        await initializeFormData();
      }, 100);
    }
    
    return;
  }
  
  // 기존 검색 로직
  const searchParams = {};
  
  if (searchOptions.compName?.trim()) searchParams.compName = searchOptions.compName.trim();
  if (searchOptions.bizNumber?.trim()) searchParams.bizNumber = searchOptions.bizNumber.trim();
  if (searchOptions.ceoName?.trim()) searchParams.ceoName = searchOptions.ceoName.trim();
  if (searchOptions.phone?.trim()) searchParams.phone = searchOptions.phone.trim();
  
  // ✅ 날짜 범위 파라미터 처리 (Oracle 호환)
  if (searchOptions.dateRangeFrom && searchOptions.dateRangeTo) {
    try {
      const fromDate = new Date(searchOptions.dateRangeFrom);
      const toDate = new Date(searchOptions.dateRangeTo);
      
      if (!isNaN(fromDate.getTime()) && !isNaN(toDate.getTime())) {
        searchParams.regDateFrom = fromDate.toISOString().split('T')[0];
        searchParams.regDateTo = toDate.toISOString().split('T')[0];
        
        console.log('날짜 범위 설정:', {
          original: { from: searchOptions.dateRangeFrom, to: searchOptions.dateRangeTo },
          converted: { from: searchParams.regDateFrom, to: searchParams.regDateTo }
        });
      } else {
        console.warn('유효하지 않은 날짜:', {
          from: searchOptions.dateRangeFrom,
          to: searchOptions.dateRangeTo
        });
      }
    } catch (dateError) {
      console.error('날짜 변환 오류:', dateError);
    }
  }
  
  console.log('최종 검색 파라미터:', searchParams);
  await loadBranches(searchParams);
};

// 행 선택 처리
const onRowSelect = (branch) => {
  console.log('선택된 지점:', branch);
  selectedBranch.value = branch;
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      if (key !== 'id' && key in branch) {
        let value = branch[key] || '';
        
        if ((key === 'regDate' || key === 'updateDate') && value) {
          value = formatDateForInput(value);
        }
        
        inputFormRef.inputDatas[key] = String(value);
      }
    });
  }
};

// 행 선택 해제 처리
const onRowUnselect = () => {
  selectedBranch.value = null;
};

// ✅ 저장 처리 (날짜 형식 수정)
const saveData = async (inputData) => {
  try {
    console.log('저장할 지점 데이터:', inputData);
    
    const requiredFields = [
      { field: 'compName', label: '지점명' },
      { field: 'bizNumber', label: '사업자번호' },
      { field: 'ceoName', label: '지점장명' }
    ];
    
    for (const req of requiredFields) {
      if (!inputData[req.field]?.trim()) {
        alert(`${req.label}은(는) 필수입력 항목입니다.`);
        return;
      }
    }
    
    const currentUserData = await getCurrentUser();
    const isUpdateMode = selectedBranch.value?.compId;
    
    const branchData = {
      ...inputData,
      compType: isUpdateMode && selectedBranch.value.compType === COMPANY_TYPES.INACTIVE 
        ? COMPANY_TYPES.INACTIVE  // 이미 비활성화된 경우 유지
        : COMPANY_TYPES.BRANCH    // 신규 등록이거나 활성 상태인 경우
    };
    
    let response;
    
    if (isUpdateMode) {
      // ✅ 수정 모드 - 날짜 형식 수정
      let regDate = null;
      if (inputData.regDate?.trim()) {
        regDate = formatDateForOracle(inputData.regDate);
        if (!regDate) {
          alert('등록일 형식이 올바르지 않습니다. (예: 2024-01-01)');
          return;
        }
      }
      
      branchData.compId = selectedBranch.value.compId;
      branchData.updateUser = currentUserData.employeeId;
      branchData.updateDate = formatDateForOracle(new Date());
      branchData.regDate = regDate;
      
      response = await axios.put(`${API_BASE_URL}/${selectedBranch.value.compId}`, branchData);
    } else {
      // ✅ 신규 등록 모드 - 날짜 형식 수정
      let regDate = inputData.regDate?.trim() ? 
        formatDateForOracle(inputData.regDate) : 
        formatDateForOracle(new Date());
      
      if (!regDate) {
        alert('등록일 형식이 올바르지 않습니다. (예: 2024-01-01)');
        return;
      }
      
      branchData.regUser = currentUserData.employeeId;
      branchData.regDate = regDate;
      delete branchData.compId;
      
      response = await axios.post(API_BASE_URL, branchData);
    }
    
    if (response.data.result_code === 'SUCCESS') {
      alert(isUpdateMode ? 
        `지점이 성공적으로 수정되었습니다. (수정자: ${currentUserData.empName})` : 
        `지점이 성공적으로 등록되었습니다. (등록자: ${currentUserData.empName})`
      );
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedBranch.value = null;
      
      await loadBranches();
    } else {
      alert(`저장 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('지점 저장 실패:', error);
    
    if (error.code === 'ERR_NETWORK') {
      alert('네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.');
    } else {
      alert('저장 실패: ' + (error.response?.data?.message || error.message));
    }
  }
};

// ✅ 삭제 처리 → 비활성화 처리로 변경
const deleteData = async () => {
  if (!selectedBranch.value?.compId) {
    alert('비활성화할 지점을 선택해주세요.');
    return;
  }

  // ✅ 비활성화 확인 메시지
  const confirmDeactivate = confirm(
    `지점 "${selectedBranch.value.compName}"을(를) 비활성화하시겠습니까?\n\n비활성화된 지점은 더 이상 사용할 수 없습니다.`
  );
  
  if (!confirmDeactivate) return;

  try {
    console.log('지점 비활성화 시작:', selectedBranch.value.compId);
    
    const currentUserData = await getCurrentUser();
    
    // ✅ 비활성화 API 호출 또는 상태 업데이트
    const response = await axios.put(`${API_BASE_URL}/${selectedBranch.value.compId}/deactivate`, {
      updateUser: currentUserData.employeeId,
      updateDate: formatDateForOracle(new Date())
    });
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`지점 "${selectedBranch.value.compName}"이(가) 비활성화되었습니다. (처리자: ${currentUserData.empName})`);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedBranch.value = null;
      
      await loadBranches();
    } else {
      alert(`비활성화 실패: ${response.data.message || '비활성화 중 오류가 발생했습니다.'}`);
    }
    
  } catch (error) {
    console.error('지점 비활성화 실패:', error);
    
    // ✅ 백엔드에 비활성화 API가 없는 경우 일반 수정 API 사용
    if (error.response?.status === 404) {
      try {
        console.log('비활성화 전용 API가 없어서 일반 수정 API 사용');
        
        const currentUserData = await getCurrentUser();
        
        const updateData = {
          ...selectedBranch.value,
          compType: COMPANY_TYPES.INACTIVE, // 비활성화 상태
          updateUser: currentUserData.employeeId,
          updateDate: formatDateForOracle(new Date())
        };
        
        const fallbackResponse = await axios.put(`${API_BASE_URL}/${selectedBranch.value.compId}`, updateData);
        
        if (fallbackResponse.data.result_code === 'SUCCESS') {
          alert(`지점 "${selectedBranch.value.compName}"이(가) 비활성화되었습니다.`);
          
          if (standardInputRef.value?.inputFormRef) {
            standardInputRef.value.inputFormRef.resetInputDatas();
          }
          selectedBranch.value = null;
          
          await loadBranches();
        } else {
          throw new Error(fallbackResponse.data.message || '지점 비활성화에 실패했습니다.');
        }
      } catch (fallbackError) {
        alert('비활성화 실패: ' + (fallbackError.response?.data?.message || fallbackError.message));
      }
    } else {
      alert('비활성화 실패: ' + (error.response?.data?.message || error.message));
    }
  }
};

// 모달 처리 함수
const openSearchModal = (inputName) => {
  console.log('모달 열기:', inputName);
};

// 초기화 함수
const initializeFormData = async () => {
  const user = await getCurrentUser();
  console.log('폼 초기화 시 사용자 정보:', user);
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    inputFormRef.inputDatas.regUser = user.employeeId;
    
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    inputFormRef.inputDatas.regDate = `${year}-${month}-${day}`;
  }
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await loadBranches();
  
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
  >
    <!-- ✅ 비활성화 버튼 (삭제 대신) -->
    <template #btn>
      <Button 
        label="삭제" 
        severity="danger" 
        class="min-w-fit whitespace-nowrap" 
        outlined
        :disabled="!selectedBranch"
        @click="deleteData"
      />
    </template>
  </StandardInput>
</template>