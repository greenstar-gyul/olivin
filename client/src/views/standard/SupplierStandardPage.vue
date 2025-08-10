<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted } from 'vue';
import axios from '@/service/axios';

// ================================
// 상수 및 기본 설정
// ================================
const API_BASE_URL = '/api/companies';

const COMPANY_TYPES = {
  HEADQUARTERS: '100001', // 본사
  BRANCH: '100002',       // 지점  
  SUPPLIER: '100003',     // 공급업체
  INACTIVE: 'FFFFFF'      // 비활성화
};

// ================================
// 반응형 데이터
// ================================
const items = ref([]);
const loading = ref(false);
const selectedSupplier = ref(null);
const standardInputRef = ref(null);

const currentUser = ref({
  empId: '',
  employeeId: '',
  empName: ''
});

// ================================
// 폼 스키마 정의
// ================================
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '업체명', value: '', placeholder: '업체명을 입력하세요', name: 'compName' },
    { type: 'text', label: '사업자번호', value: '', placeholder: '000-00-00000', name: 'bizNumber' },
    { type: 'text', label: 'CEO명', value: '', placeholder: 'CEO명을 입력하세요', name: 'ceoName' },
    { type: 'text', label: '전화번호', value: '', placeholder: '02-0000-0000', name: 'phone' },
    { type: 'dateRange', label: '등록일', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'dateRange' }
  ]
});

const header = ref({
  title: '공급업체 기준정보 관리',
  header: {
    compId: '업체ID', 
    compName: '업체명', 
    bizNumber: '사업자번호', 
    ceoName: 'CEO명', 
    phone: '전화번호', 
  },
  rightAligned: []
});

const inputs = ref({
  title: '공급업체 등록/수정',
  inputs: [
    { type: 'text', label: '업체ID', placeholder: '자동생성', name: 'compId', readonly: true },
    { type: 'text', label: '업체명', placeholder: '업체명을 입력하세요', name: 'compName', required: true },
    { type: 'text', label: '사업자번호', placeholder: '000-00-00000', name: 'bizNumber', required: true },
    { type: 'text', label: 'CEO명', placeholder: 'CEO명을 입력하세요', name: 'ceoName', required: true },
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

// ================================
// 유틸리티 함수들
// ================================
const formatAddress = (address, addressDetail) => {
  if (!address) return '';
  return addressDetail ? `${address} ${addressDetail}` : address;
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    let dateOnly = dateString;
    if (dateString.includes('T')) {
      dateOnly = dateString.split('T')[0];
    }
    const [year, month, day] = dateOnly.split('-');
    return `${year}.${month.padStart(2, '0')}.${day.padStart(2, '0')}`;
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
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
    return dateOnly; // YYYY-MM-DD 형식 반환
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

// ⭐ 핵심 추가: 백엔드로 보낼 날짜 형식 변환 함수
const formatDateForBackend = (dateValue) => {
  if (!dateValue) return null;
  
  try {
    let date;
    
    if (dateValue instanceof Date) {
      date = dateValue;
    } else if (typeof dateValue === 'string') {
      date = new Date(dateValue);
    } else {
      return null;
    }
    
    // 유효한 날짜인지 확인
    if (isNaN(date.getTime())) {
      console.error('잘못된 날짜:', dateValue);
      return null;
    }
    
    // YYYY-MM-DD 형식으로 변환 (로컬 타임존 기준)
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  } catch (error) {
    console.error('날짜 변환 오류:', error, dateValue);
    return null;
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

// ================================
// 사용자 정보 관리
// ================================
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
      
      console.log('최종 설정된 사용자 정보:', currentUser.value);
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

// ================================
// 데이터 조회 함수들
// ================================
const loadSuppliers = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('공급업체 목록 조회 시작...', searchParams);
    
    const params = { ...searchParams };
    const response = await axios.get(API_BASE_URL, { params });
    
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
    
    // 활성 공급업체만 필터링 (비활성화된 공급업체 제외)
    const suppliers = companies.filter(item => 
      item.compType === COMPANY_TYPES.SUPPLIER  // 활성 공급업체만
    );
    
    items.value = suppliers.map((item, index) => ({
      id: item.compId || `temp_supplier_${Date.now()}_${index}`,
      ...item,
      address: formatAddress(item.address, item.addressDetail),
      regDate: item.regDate ? formatDate(item.regDate) : '',
      updateDate: item.updateDate ? formatDate(item.updateDate) : null,
      status: '활성'  // 활성 공급업체만 표시하므로 항상 활성
    }));
    
    console.log('최종 공급업체 목록:', items.value);
    
  } catch (error) {
    console.error('공급업체 목록 조회 실패:', error);
    alert('데이터 조회에 실패했습니다.');
    items.value = [];
  } finally {
    loading.value = false;
  }
};

const checkSupplierUsage = async (compId) => {
  try {
    console.log('공급업체 사용 여부 확인:', compId);
    
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

// ================================
// 이벤트 핸들러들 - ⭐ 핵심 수정: 날짜 형식 변환 + 폼 리셋 추가
// ================================
const searchData = async (searchOptions) => {
  console.log('공급업체 검색 조건:', searchOptions);
  
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
    
    // 1. 전체 공급업체 목록 로드
    await loadSuppliers();
    
    // 2. 선택된 공급업체 초기화
    selectedSupplier.value = null;
    
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
  
  // ⭐ 핵심 수정: 날짜를 YYYY-MM-DD 형식으로 변환
  if (searchOptions.dateRangeFrom || searchOptions.dateRangeTo) {
    const fromDate = formatDateForBackend(searchOptions.dateRangeFrom);
    const toDate = formatDateForBackend(searchOptions.dateRangeTo);
    
    if (fromDate) searchParams.regDateFrom = fromDate;
    if (toDate) searchParams.regDateTo = toDate;
    
    console.log('날짜 변환 결과:', {
      원본: { 
        from: searchOptions.dateRangeFrom, 
        to: searchOptions.dateRangeTo 
      },
      변환후: { 
        regDateFrom: fromDate, 
        regDateTo: toDate 
      }
    });
  }
  
  console.log('최종 검색 파라미터:', searchParams);
  await loadSuppliers(searchParams);
};

const resetSearchOptions = async () => {
  console.log('검색 조건 초기화');
  
  if (standardInputRef.value?.searchFormRef) {
    const searchFormRef = standardInputRef.value.searchFormRef;
    Object.keys(searchFormRef.searchOptions).forEach(key => {
      searchFormRef.searchOptions[key] = '';
    });
  }
  
  filters.value.filters.forEach(filter => {
    filter.value = '';
  });
  
  await loadSuppliers();
};

const onRowSelect = (supplier) => {
  console.log('선택된 공급업체:', supplier);
  selectedSupplier.value = supplier;
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      if (key !== 'id' && key in supplier) {
        let value = supplier[key] || '';
        
        if ((key === 'regDate' || key === 'updateDate') && value) {
          value = formatDateForInput(value);
        }
        
        inputFormRef.inputDatas[key] = String(value);
      }
    });
  }
};

const onRowUnselect = () => {
  selectedSupplier.value = null;
};

const saveData = async (inputData) => {
  try {
    console.log('저장할 공급업체 데이터:', inputData);
    
    // 필수 필드 검증
    const requiredFields = [
      { field: 'compName', label: '업체명' },
      { field: 'bizNumber', label: '사업자번호' },
      { field: 'ceoName', label: 'CEO명' }
    ];
    
    for (const req of requiredFields) {
      if (!inputData[req.field]?.trim()) {
        alert(`${req.label}은(는) 필수입력 항목입니다.`);
        return;
      }
    }
    
    // 등록일 처리
    let regDate = null;
    if (inputData.regDate?.trim()) {
      try {
        const dateStr = inputData.regDate.trim();
        const [year, month, day] = dateStr.split('-');
        regDate = new Date(parseInt(year), parseInt(month) - 1, parseInt(day), 12, 0, 0);
        
        if (isNaN(regDate.getTime())) {
          throw new Error('유효하지 않은 날짜 형식');
        }
      } catch (error) {
        alert('등록일 형식이 올바르지 않습니다. (예: 2024-01-01)');
        return;
      }
    }
    
    const currentUserData = await getCurrentUser();
    const isUpdateMode = selectedSupplier.value?.compId;
    
    const supplierData = {
      ...inputData,
      compType: isUpdateMode && selectedSupplier.value.compType === COMPANY_TYPES.INACTIVE 
        ? COMPANY_TYPES.INACTIVE  // 이미 비활성화된 경우 유지
        : COMPANY_TYPES.SUPPLIER  // 신규 등록이거나 활성 상태인 경우
    };
    
    let response;
    
    if (isUpdateMode) {
      // 수정 모드
      supplierData.compId = selectedSupplier.value.compId;
      supplierData.updateUser = currentUserData.employeeId;
      supplierData.updateDate = new Date();
      supplierData.regDate = regDate;
      
      response = await axios.put(`${API_BASE_URL}/${selectedSupplier.value.compId}`, supplierData);
    } else {
      // 신규 등록 모드
      supplierData.regUser = currentUserData.employeeId;
      supplierData.regDate = regDate;
      delete supplierData.compId;
      
      response = await axios.post(API_BASE_URL, supplierData);
    }
    
    if (response.data.result_code === 'SUCCESS') {
      alert(isUpdateMode ? 
        `공급업체가 성공적으로 수정되었습니다. (수정자: ${currentUserData.empName})` : 
        `공급업체가 성공적으로 등록되었습니다. (등록자: ${currentUserData.empName})`
      );
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedSupplier.value = null;
      
      await loadSuppliers();
    } else {
      alert(`저장 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('공급업체 저장 실패:', error);
    
    if (error.code === 'ERR_NETWORK') {
      alert('네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.');
    } else {
      alert('저장 실패: ' + (error.response?.data?.message || error.message));
    }
  }
};

const deleteData = async () => {
  if (!selectedSupplier.value?.compId) {
    alert('삭제할 공급업체를 선택해주세요.');
    return;
  }

  try {
    console.log('공급업체 삭제 시작:', selectedSupplier.value.compId);
    
    // 이미 비활성화된 회사인지 확인
    if (selectedSupplier.value.compType.startsWith('FFFF')) {
      alert('이미 비활성화된 공급업체입니다.');
      return;
    }
    
    // 발주서에서 사용 여부 확인
    const usageInfo = await checkSupplierUsage(selectedSupplier.value.compId);
    
    if (usageInfo.isUsed) {
      let message = `공급업체 "${selectedSupplier.value.compName}"은(는) 다음과 같이 사용 중이어서 삭제할 수 없습니다:\n\n`;
      
      if (usageInfo.details?.hasPurchaseOrders) {
        message += `• 발주서: ${usageInfo.purchaseOrderCount}건\n`;
      }
      
      message += '\n대신 상태를 "비활성"으로 변경하시겠습니까?';
      
      const confirmDeactivate = confirm(message);
      
      if (confirmDeactivate) {
        // 비활성화 처리
        await deactivateSupplier();
      }
      
      return;
    }
    
    // 실제 삭제 확인
    const confirmDelete = confirm(
      `공급업체 "${selectedSupplier.value.compName}"을(를) 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`
    );
    
    if (!confirmDelete) return;

    // 삭제 실행
    const response = await axios.delete(`${API_BASE_URL}/${selectedSupplier.value.compId}`);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`공급업체 "${selectedSupplier.value.compName}"이(가) 성공적으로 삭제되었습니다.`);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedSupplier.value = null;
      
      await loadSuppliers();
    } else {
      alert(`삭제 실패: ${response.data.message || '삭제 중 오류가 발생했습니다.'}`);
    }
    
  } catch (error) {
    console.error('공급업체 삭제 실패:', error);
    
    let errorMessage = '삭제 중 오류가 발생했습니다.';
    
    if (error.code === 'ERR_NETWORK') {
      errorMessage = '네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.';
    } else if (error.response?.status === 404) {
      errorMessage = '삭제할 공급업체를 찾을 수 없습니다.';
    } else if (error.response?.status === 409) {
      errorMessage = '다른 데이터에서 참조 중인 공급업체는 삭제할 수 없습니다.';
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    
    alert('삭제 실패: ' + errorMessage);
  }
};

// 비활성화 처리 함수
const deactivateSupplier = async () => {
  try {
    const response = await axios.put(`${API_BASE_URL}/${selectedSupplier.value.compId}/deactivate`);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`공급업체 "${selectedSupplier.value.compName}"이(가) 비활성화되었습니다.`);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedSupplier.value = null;
      
      await loadSuppliers();
    } else {
      alert(`비활성화 실패: ${response.data.message || '비활성화 중 오류가 발생했습니다.'}`);
    }
    
  } catch (error) {
    console.error('공급업체 비활성화 실패:', error);
    alert('비활성화 실패: ' + (error.response?.data?.message || error.message));
  }
};

// 재활성화 처리 함수
const reactivateSupplier = async () => {
  try {
    const currentUserData = await getCurrentUser();
    
    const supplierData = {
      ...selectedSupplier.value,
      compType: COMPANY_TYPES.SUPPLIER, // 공급업체로 다시 활성화
      updateUser: currentUserData.employeeId,
      updateDate: new Date()
    };
    
    const response = await axios.put(`${API_BASE_URL}/${selectedSupplier.value.compId}`, supplierData);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`공급업체 "${selectedSupplier.value.compName}"이(가) 다시 활성화되었습니다.`);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedSupplier.value = null;
      
      await loadSuppliers();
    } else {
      alert(`활성화 실패: ${response.data.message || '활성화 중 오류가 발생했습니다.'}`);
    }
    
  } catch (error) {
    console.error('공급업체 활성화 실패:', error);
    alert('활성화 실패: ' + (error.response?.data?.message || error.message));
  }
};

const openSearchModal = (inputName) => {
  console.log('모달 열기:', inputName);
};

// ================================
// 초기화 및 라이프사이클
// ================================
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

onMounted(async () => {
  console.log('=== 공급업체 페이지 마운트 시작 ===');
  
  await loadSuppliers();
  
  setTimeout(async () => {
    await initializeFormData();
  }, 100);
  
  console.log('=== 공급업체 페이지 마운트 완료 ===');
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
    <template #btn>
      <Button 
        label="삭제" 
        severity="danger" 
        class="min-w-fit whitespace-nowrap" 
        outlined
        :disabled="!selectedSupplier"
        @click="deleteData"
      />
    </template>
  </StandardInput>
</template>