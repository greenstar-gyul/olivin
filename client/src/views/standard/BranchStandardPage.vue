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
  SUPPLIER: '100003'      // 공급업체
};

// 현재 로그인한 사용자 정보
const currentUser = ref({
  empId: '',
  employeeId: '',
  empName: ''
});

// 수정된 getCurrentUser 함수 - employeeId 기반
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    console.log('사용자 API 전체 응답:', JSON.stringify(response.data, null, 2));
    
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      console.log('userData 구조:', JSON.stringify(userData, null, 2));
      
      let employeeId = 'admin';
      let empName = '관리자';
      
      // 다양한 경우에 대한 더 포괄적인 처리
      const possibleUserSources = [
        userData.user,           // user 객체
        userData,               // userData 직접
        userData.employee,      // employee 객체
        userData.userInfo,      // userInfo 객체
        userData.loginUser      // loginUser 객체
      ];
      
      for (const userSource of possibleUserSources) {
        if (userSource) {
          console.log('처리 중인 userSource:', typeof userSource, userSource);
          
          if (typeof userSource === 'object' && userSource !== null) {
            // 객체인 경우 - employeeId 우선 검색
            const possibleEmployeeIds = [
              userSource.employeeId,        // employeeId 우선
              userSource.employee_id,       // employee_id
              userSource.EMPLOYEE_ID,       // EMPLOYEE_ID (DB 컬럼명)
            ];
            
            const possibleEmpNames = [
              userSource.empName,
              userSource.emp_name,
              userSource.EMP_NAME,
            ];
            
            // 첫 번째로 유효한 값 찾기
            const foundEmployeeId = possibleEmployeeIds.find(id => id && id !== 'admin' && String(id).trim() !== '');
            const foundEmpName = possibleEmpNames.find(name => name && name !== '관리자' && String(name).trim() !== '');
            
            if (foundEmployeeId) {
              employeeId = String(foundEmployeeId).trim();
            }
            if (foundEmpName) {
              empName = String(foundEmpName).trim();
            }
            
            // 유효한 사용자 정보를 찾았으면 중단
            if (foundEmployeeId && foundEmpName) {
              break;
            }
          } else if (typeof userSource === 'string' && userSource.trim() !== '') {
            // 문자열인 경우
            empName = userSource.trim();
            employeeId = userSource.trim();
            break;
          }
        }
      }
      
      currentUser.value = {
        empId: employeeId,          // 호환성을 위해 empId로도 저장
        employeeId: employeeId,     // employeeId 추가
        empName: empName
      };
      
      console.log('최종 설정된 사용자 정보:', currentUser.value);
      
      // 사용자 정보가 기본값이면 경고 로그
      if (employeeId === 'admin' && empName === '관리자') {
        console.warn('사용자 정보를 찾지 못해 기본값을 사용합니다. API 응답 구조를 확인해주세요.');
      }
      
      return currentUser.value;
    } else {
      console.warn('API 응답에 사용자 데이터가 없음:', response.data);
      throw new Error('API 응답에 사용자 데이터가 없습니다');
    }
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error);
    
    // API 실패 시 기본값 사용
    currentUser.value = {
      empId: 'admin',
      employeeId: 'admin',
      empName: '관리자'
    };
    
    console.warn('사용자 정보 API 실패로 기본값 사용:', currentUser.value);
    return currentUser.value;
  }
};

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요', name: 'compName' },
    { type: 'text', label: '사업자번호', value: '', placeholder: '000-00-00000', name: 'bizNumber' },
    { type: 'text', label: 'CEO명', value: '', placeholder: 'CEO명을 입력하세요', name: 'ceoName' },
    { type: 'text', label: '전화번호', value: '', placeholder: '02-0000-0000', name: 'phone' },
    { type: 'dateRange', label: '등록일 범위', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'dateRange' }
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
    ceoName: 'CEO명', 
    phone: '전화번호', 
    regUser: '등록자',
    regDate: '등록일'
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

// 주소 포맷 함수
const formatAddress = (address, addressDetail) => {
  if (!address) return '';
  return addressDetail ? `${address} ${addressDetail}` : address;
};

// 날짜 포맷 함수들
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

// 지점 목록 조회 (고유키 추가)
const loadBranches = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('지점 목록 조회 시작...');
    
    // 모든 회사 조회 후 지점만 필터링
    const response = await axios.get(API_BASE_URL, { params: searchParams });
    
    console.log('회사 API 원본 응답:', response.data);
    
    // API 응답 구조 처리
    let companies = [];
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      companies = response.data.data;
    } else if (Array.isArray(response.data)) {
      companies = response.data;
    } else {
      console.error('예상하지 못한 응답 구조:', response.data);
      companies = [];
    }
    
    console.log('전체 회사 데이터:', companies);
    console.log('COMP_TYPE 값들:', companies.map(c => ({ id: c.compId, type: c.compType, name: c.compName })));
    
    // 지점만 필터링 (compType이 '100002'인 것)
    const branches = companies.filter(item => item.compType === COMPANY_TYPES.BRANCH);
    
    console.log('필터링된 지점:', branches);
    
    // 데이터 가공 (고유 ID 추가)
    items.value = branches.map((item, index) => ({
      id: item.compId || `temp_branch_${Date.now()}_${index}`, // 고유 ID 추가
      ...item,
      address: formatAddress(item.address, item.addressDetail),
      regDate: item.regDate ? formatDate(item.regDate) : '',
      updateDate: item.updateDate ? formatDate(item.updateDate) : null
    }));
    
    console.log('최종 지점 목록:', items.value);
    
  } catch (error) {
    console.error('지점 목록 조회 실패:', error);
    console.error('에러 응답:', error.response);
    alert('데이터 조회에 실패했습니다.');
    items.value = [];
  } finally {
    loading.value = false;
  }
};

// 검색 실행
const searchData = async (searchOptions) => {
  console.log('지점 검색 조건:', searchOptions);
  
  const searchParams = {};
  
  // 검색 조건 매핑
  if (searchOptions.compName && searchOptions.compName.trim() !== '') {
    searchParams.compName = searchOptions.compName.trim();
  }
  if (searchOptions.bizNumber && searchOptions.bizNumber.trim() !== '') {
    searchParams.bizNumber = searchOptions.bizNumber.trim();
  }
  if (searchOptions.ceoName && searchOptions.ceoName.trim() !== '') {
    searchParams.ceoName = searchOptions.ceoName.trim();
  }
  if (searchOptions.phone && searchOptions.phone.trim() !== '') {
    searchParams.phone = searchOptions.phone.trim();
  }
  if (searchOptions.dateRangeFrom && searchOptions.dateRangeTo) {
    searchParams.regDateFrom = searchOptions.dateRangeFrom;
    searchParams.regDateTo = searchOptions.dateRangeTo;
  }
  
  await loadBranches(searchParams);
};

// 행 선택 처리
const onRowSelect = (branch) => {
  console.log('선택된 지점:', branch);
  selectedBranch.value = branch;
  
  // StandardInput의 inputForm에 데이터 설정
  if (standardInputRef.value && standardInputRef.value.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    
    // 날짜 포맷 함수
    const formatDateForInput = (dateString) => {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return dateString;
        
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        
        return `${year}-${month}-${day}`;
      } catch (error) {
        console.error('날짜 포맷 오류:', error);
        return dateString;
      }
    };
    
    // 폼 데이터 업데이트 (id 필드는 제외)
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      if (key !== 'id' && key in branch) {
        let value = branch[key] || '';
        
        // 날짜 필드를 위한 포맷팅
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

// 저장 처리
const saveData = async (inputData) => {
  try {
    console.log('저장할 지점 데이터:', inputData);
    
    // 필수 필드 검증
    const requiredFields = [
      { field: 'compName', label: '지점명' },
      { field: 'bizNumber', label: '사업자번호' },
      { field: 'ceoName', label: 'CEO명' }
    ];
    
    for (const req of requiredFields) {
      if (!inputData[req.field] || inputData[req.field].trim() === '') {
        alert(`${req.label}은(는) 필수입력 항목입니다.`);
        return;
      }
    }
    
    // 등록일 처리
    let regDate = null;
    if (inputData.regDate && inputData.regDate.trim() !== '') {
      try {
        const dateStr = inputData.regDate.trim();
        regDate = new Date(dateStr + 'T00:00:00');
        
        if (isNaN(regDate.getTime())) {
          throw new Error('유효하지 않은 날짜 형식');
        }
      } catch (error) {
        alert('등록일 형식이 올바르지 않습니다. (예: 2024-01-01)');
        return;
      }
    }
    
    // 현재 사용자 정보를 저장 전에 다시 확인
    const currentUserData = await getCurrentUser();
    console.log('저장 시점의 현재 사용자 정보:', currentUserData);
    
    // 선택된 지점이 있으면 수정 모드, 없으면 신규 등록
    const isUpdateMode = selectedBranch.value && selectedBranch.value.compId;
    
    // 기본 지점 데이터 구성
    const branchData = {
      ...inputData,
      compType: COMPANY_TYPES.BRANCH // 지점 유형 고정
    };
    
    let response;
    
    if (isUpdateMode) {
      // 수정 모드 - 수정자 정보 명확히 설정
      const now = new Date();
      
      branchData.compId = selectedBranch.value.compId;
      branchData.updateUser = currentUserData.employeeId; // employeeId 사용
      branchData.updateDate = now; // 수정일시
      branchData.regDate = regDate; // 등록일은 기존 값 유지 또는 입력된 값
      
      console.log('수정 모드 - 전송할 데이터:', {
        compId: branchData.compId,
        updateUser: branchData.updateUser,
        updateDate: branchData.updateDate,
        regUser: branchData.regUser,
        regDate: branchData.regDate
      });
      
      response = await axios.put(`${API_BASE_URL}/${selectedBranch.value.compId}`, branchData);
    } else {
      // 신규 등록 모드
      branchData.regUser = currentUserData.employeeId; // employeeId 사용
      branchData.regDate = regDate; // 등록일
      delete branchData.compId; // 백엔드에서 자동 생성
      
      console.log('등록 모드 - 전송할 데이터:', {
        regUser: branchData.regUser,
        regDate: branchData.regDate,
        compType: branchData.compType
      });
      
      response = await axios.post(API_BASE_URL, branchData);
    }
    
    // 응답 처리
    console.log('서버 응답:', response.data);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(isUpdateMode ? 
        `지점이 성공적으로 수정되었습니다. (수정자: ${currentUserData.empName})` : 
        `지점이 성공적으로 등록되었습니다. (등록자: ${currentUserData.empName})`
      );
      
      // 폼 초기화
      if (standardInputRef.value && standardInputRef.value.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedBranch.value = null;
      
      await loadBranches();
    } else {
      alert(`저장 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('지점 저장 실패:', error);
    console.error('에러 상세:', {
      code: error.code,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    });
    
    if (error.code === 'ERR_NETWORK') {
      alert('네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.');
    } else {
      alert('저장 실패: ' + (error.response?.data?.message || error.message));
    }
  }
};

// 삭제 처리
const deleteData = async () => {
  if (!selectedBranch.value || !selectedBranch.value.compId) {
    alert('삭제할 지점을 선택해주세요.');
    return;
  }

  // 삭제 확인
  const confirmDelete = confirm(
    `지점 "${selectedBranch.value.compName}"을(를) 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`
  );
  
  if (!confirmDelete) {
    return;
  }

  try {
    console.log('지점 삭제 시작:', selectedBranch.value.compId);
    
    const response = await axios.delete(`${API_BASE_URL}/${selectedBranch.value.compId}`);
    
    console.log('삭제 응답:', response.data);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`지점 "${selectedBranch.value.compName}"이(가) 성공적으로 삭제되었습니다.`);
      
      // 폼 초기화
      if (standardInputRef.value && standardInputRef.value.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedBranch.value = null;
      
      await loadBranches();
    } else {
      alert(`삭제 실패: ${response.data.message || '삭제 중 오류가 발생했습니다.'}`);
    }
    
  } catch (error) {
    console.error('지점 삭제 실패:', error);
    console.error('에러 상세:', {
      code: error.code,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    });
    
    let errorMessage = '삭제 중 오류가 발생했습니다.';
    
    if (error.code === 'ERR_NETWORK') {
      errorMessage = '네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.';
    } else if (error.response?.status === 404) {
      errorMessage = '삭제할 지점을 찾을 수 없습니다.';
    } else if (error.response?.status === 409) {
      errorMessage = '다른 데이터에서 참조 중인 지점은 삭제할 수 없습니다.';
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    alert('삭제 실패: ' + errorMessage);
  }
};

// 모달 처리 함수
const openSearchModal = (inputName) => {
  console.log('모달 열기:', inputName);
  // 필요한 경우 모달 로직 구현
};

// 초기화 함수
const initializeFormData = async () => {
  const user = await getCurrentUser();
  console.log('폼 초기화 시 사용자 정보:', user);
  
  // StandardInput의 inputForm에 초기값 설정
  if (standardInputRef.value && standardInputRef.value.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    inputFormRef.inputDatas.regUser = user.employeeId; // employeeId 사용
    
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    inputFormRef.inputDatas.regDate = `${year}-${month}-${day}`;
  }
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  // 사용자 정보 디버깅
  try {
    const response = await axios.get('/api/auth/me');
    console.log('=== 사용자 API 디버깅 ===');
    console.log('전체 응답:', JSON.stringify(response.data, null, 2));
    console.log('response.data.data:', JSON.stringify(response.data.data, null, 2));
    if (response.data.data?.user) {
      console.log('user 객체:', JSON.stringify(response.data.data.user, null, 2));
    }
    console.log('========================');
  } catch (error) {
    console.error('디버깅 중 오류:', error);
  }
  
  await loadBranches();
  
  // 약간의 지연 후 초기화 (StandardInput이 완전히 마운트된 후)
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
    <!-- 삭제 버튼 추가 -->
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