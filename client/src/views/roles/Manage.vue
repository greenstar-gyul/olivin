<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Checkbox from 'primevue/checkbox';

const API_BASE_URL = '/api/roles';

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
    { type: 'text', label: '권한명', value: '', placeholder: '권한명을 입력하세요', name: 'roleName' },
    { type: 'text', label: '권한설명', value: '', placeholder: '권한설명을 입력하세요', name: 'roleDesc' },
    { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요', name: 'regUser' },
    { type: 'dateRange', label: '등록일 범위', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'dateRange' }
  ]
});

const items = ref([]);
const loading = ref(false);

const header = ref({
  title: '권한 기준정보 관리',
  header: {
    roleId: '권한ID', 
    roleName: '권한명', 
    roleDesc: '권한설명', 
    permissionNames: '보유권한',
    employeeCount: '직원수',
    permissionCount: '권한수',
    regUser: '등록자',
    regDate: '등록일'
  },
  rightAligned: ['employeeCount', 'permissionCount']
});

const inputs = ref({
  title: '권한 등록/수정',
  inputs: [
    { type: 'text', label: '권한ID', placeholder: '자동생성', name: 'roleId', readonly: true },
    { type: 'text', label: '권한명', placeholder: '권한명을 입력하세요', name: 'roleName', required: true },
    { type: 'textarea', label: '권한설명', placeholder: '권한설명을 입력하세요', name: 'roleDesc', required: true },
    { type: 'text', label: '등록자', placeholder: '등록자 ID', name: 'regUser', readonly: true },
    { type: 'text', label: '등록일', placeholder: '2024-01-01 형식으로 입력하세요', name: 'regDate' }
  ]
});

// StandardInput 컴포넌트 ref
const standardInputRef = ref(null);

// 선택된 권한 정보
const selectedRole = ref(null);

// 권한 할당 모달 관련
const permissionModalVisible = ref(false);
const allPermissions = ref([]);
const selectedPermissions = ref([]);

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

// 권한 목록 조회 (고유키 추가)
const loadRoles = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('권한 목록 조회 시작...');
    
    const response = await axios.get(API_BASE_URL, { params: searchParams });
    
    console.log('권한 API 원본 응답:', response.data);
    
    // API 응답 구조 처리
    let roles = [];
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      roles = response.data.data;
    } else if (Array.isArray(response.data)) {
      roles = response.data;
    } else {
      console.error('예상하지 못한 응답 구조:', response.data);
      roles = [];
    }
    
    console.log('권한 데이터:', roles);
    
    // 데이터 가공 (고유 ID 추가)
    items.value = roles.map((item, index) => ({
      id: item.roleId || `temp_role_${Date.now()}_${index}`, // 고유 ID 추가
      ...item,
      regDate: item.regDate ? formatDate(item.regDate) : '',
      updateDate: item.updateDate ? formatDate(item.updateDate) : null,
      employeeCount: item.employeeCount || 0,
      permissionCount: item.permissionCount || 0,
      permissionNames: item.permissionNames || '' // 조인으로 가져온 권한명들
    }));
    
    console.log('최종 권한 목록:', items.value);
    
  } catch (error) {
    console.error('권한 목록 조회 실패:', error);
    console.error('에러 응답:', error.response);
    alert('데이터 조회에 실패했습니다.');
    items.value = [];
  } finally {
    loading.value = false;
  }
};

// 모든 권한 목록 조회 (Map 형태)
const loadAllPermissions = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/permissions`);
    
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      allPermissions.value = response.data.data.map(perm => ({
        permId: perm.PERM_ID,
        permName: perm.PERM_NAME,
        permDescription: perm.PERM_DESCRIPTION,
        selected: false
      }));
    }
  } catch (error) {
    console.error('권한 목록 조회 실패:', error);
    alert('권한 목록 조회에 실패했습니다.');
  }
};

// 특정 권한의 권한 ID 목록 조회
const loadRolePermissions = async (roleId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/${roleId}/permissions`);
    
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      const rolePermissionIds = response.data.data;
      
      // 모든 권한에서 선택 상태 업데이트
      allPermissions.value.forEach(perm => {
        perm.selected = rolePermissionIds.includes(perm.permId);
      });
      
      selectedPermissions.value = rolePermissionIds;
    }
  } catch (error) {
    console.error('권한별 권한 조회 실패:', error);
  }
};

// 검색 실행
const searchData = async (searchOptions) => {
  console.log('권한 검색 조건:', searchOptions);
  
  const searchParams = {};
  
  // 검색 조건 매핑
  if (searchOptions.roleName && searchOptions.roleName.trim() !== '') {
    searchParams.roleName = searchOptions.roleName.trim();
  }
  if (searchOptions.roleDesc && searchOptions.roleDesc.trim() !== '') {
    searchParams.roleDesc = searchOptions.roleDesc.trim();
  }
  if (searchOptions.regUser && searchOptions.regUser.trim() !== '') {
    searchParams.regUser = searchOptions.regUser.trim();
  }
  if (searchOptions.dateRangeFrom && searchOptions.dateRangeTo) {
    searchParams.regDateFrom = searchOptions.dateRangeFrom;
    searchParams.regDateTo = searchOptions.dateRangeTo;
  }
  
  await loadRoles(searchParams);
};

// 행 선택 처리
const onRowSelect = (role) => {
  console.log('선택된 권한:', role);
  selectedRole.value = role;
  
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
      if (key !== 'id' && key in role) {
        let value = role[key] || '';
        
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
  selectedRole.value = null;
};

// 저장 처리
const saveData = async (inputData) => {
  try {
    console.log('저장할 권한 데이터:', inputData);
    
    // 필수 필드 검증
    const requiredFields = [
      { field: 'roleName', label: '권한명' },
      { field: 'roleDesc', label: '권한설명' }
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
    
    // 선택된 권한이 있으면 수정 모드, 없으면 신규 등록
    const isUpdateMode = selectedRole.value && selectedRole.value.roleId;
    
    // 기본 권한 데이터 구성
    const roleData = {
      ...inputData
    };
    
    let response;
    
    if (isUpdateMode) {
      // 수정 모드 - 수정자 정보 명확히 설정
      const now = new Date();
      
      roleData.roleId = selectedRole.value.roleId;
      roleData.updateUser = currentUserData.employeeId; // employeeId 사용
      roleData.updateDate = now; // 수정일시
      roleData.regDate = regDate; // 등록일은 기존 값 유지 또는 입력된 값
      
      console.log('수정 모드 - 전송할 데이터:', {
        roleId: roleData.roleId,
        updateUser: roleData.updateUser,
        updateDate: roleData.updateDate,
        regUser: roleData.regUser,
        regDate: roleData.regDate
      });
      
      response = await axios.put(`${API_BASE_URL}/${selectedRole.value.roleId}`, roleData);
    } else {
      // 신규 등록 모드
      roleData.regUser = currentUserData.employeeId; // employeeId 사용
      roleData.regDate = regDate; // 등록일
      delete roleData.roleId; // 백엔드에서 자동 생성
      
      console.log('등록 모드 - 전송할 데이터:', {
        regUser: roleData.regUser,
        regDate: roleData.regDate
      });
      
      response = await axios.post(API_BASE_URL, roleData);
    }
    
    // 응답 처리
    console.log('서버 응답:', response.data);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(isUpdateMode ? 
        `권한이 성공적으로 수정되었습니다. (수정자: ${currentUserData.empName})` : 
        `권한이 성공적으로 등록되었습니다. (등록자: ${currentUserData.empName})`
      );
      
      // 폼 초기화
      if (standardInputRef.value && standardInputRef.value.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedRole.value = null;
      
      await loadRoles();
    } else {
      alert(`저장 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('권한 저장 실패:', error);
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
  if (!selectedRole.value || !selectedRole.value.roleId) {
    alert('삭제할 권한을 선택해주세요.');
    return;
  }

  // 삭제 확인
  const confirmDelete = confirm(
    `권한 "${selectedRole.value.roleName}"을(를) 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`
  );
  
  if (!confirmDelete) {
    return;
  }

  try {
    console.log('권한 삭제 시작:', selectedRole.value.roleId);
    
    const response = await axios.delete(`${API_BASE_URL}/${selectedRole.value.roleId}`);
    
    console.log('삭제 응답:', response.data);
    
    if (response.data.result_code === 'SUCCESS') {
      alert(`권한 "${selectedRole.value.roleName}"이(가) 성공적으로 삭제되었습니다.`);
      
      // 폼 초기화
      if (standardInputRef.value && standardInputRef.value.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedRole.value = null;
      
      await loadRoles();
    } else {
      alert(`삭제 실패: ${response.data.message || '삭제 중 오류가 발생했습니다.'}`);
    }
    
  } catch (error) {
    console.error('권한 삭제 실패:', error);
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
      errorMessage = '삭제할 권한을 찾을 수 없습니다.';
    } else if (error.response?.status === 409) {
      errorMessage = '해당 권한을 사용 중인 직원이 있어 삭제할 수 없습니다.';
    } else if (error.response?.data?.message) {
      errorMessage = response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    alert('삭제 실패: ' + errorMessage);
  }
};

// 권한 할당 모달 열기
const openPermissionModal = async () => {
  if (!selectedRole.value) {
    alert('권한을 선택해주세요.');
    return;
  }
  
  await loadAllPermissions();
  await loadRolePermissions(selectedRole.value.roleId);
  permissionModalVisible.value = true;
};

// 권한 할당 저장
const savePermissions = async () => {
  try {
    const response = await axios.post(`${API_BASE_URL}/${selectedRole.value.roleId}/permissions`, {
      permissionIds: selectedPermissions.value
    });
    
    if (response.data.result_code === 'SUCCESS') {
      alert('권한이 성공적으로 할당되었습니다.');
      permissionModalVisible.value = false;
      await loadRoles(); // 권한 수 업데이트를 위해 재조회
    } else {
      alert(`권한 할당 실패: ${response.data.message}`);
    }
  } catch (error) {
    console.error('권한 할당 실패:', error);
    alert('권한 할당 중 오류가 발생했습니다.');
  }
};

// 권한 선택/해제 처리
const togglePermission = (permId) => {
  const index = selectedPermissions.value.indexOf(permId);
  if (index > -1) {
    selectedPermissions.value.splice(index, 1);
  } else {
    selectedPermissions.value.push(permId);
  }
  
  // allPermissions의 selected 상태도 업데이트
  const permission = allPermissions.value.find(p => p.permId === permId);
  if (permission) {
    permission.selected = !permission.selected;
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
  
  await loadRoles();
  
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
    <!-- 삭제 및 권한 할당 버튼 추가 -->
    <template #btn>
      <Button 
        label="삭제" 
        severity="danger" 
        class="min-w-fit whitespace-nowrap mr-2" 
        outlined
        :disabled="!selectedRole"
        @click="deleteData"
      />
      <Button 
        label="권한할당" 
        severity="info" 
        class="min-w-fit whitespace-nowrap" 
        outlined
        :disabled="!selectedRole"
        @click="openPermissionModal"
      />
    </template>
  </StandardInput>

  <!-- 권한 할당 모달 -->
  <Dialog 
    v-model:visible="permissionModalVisible" 
    :modal="true" 
    :closable="true"
    :style="{ width: '60vw' }"
    header="권한 할당"
  >
    <div class="p-4">
      <div v-if="selectedRole" class="mb-4 p-3 bg-blue-50 rounded">
        <h4 class="text-lg font-semibold text-blue-800">
          선택된 권한: {{ selectedRole.roleName }}
        </h4>
        <p class="text-sm text-blue-600">{{ selectedRole.roleDesc }}</p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 max-h-96 overflow-y-auto">
        <div v-for="permission in allPermissions" :key="permission.permId" 
             class="flex items-center p-3 border rounded hover:bg-gray-50">
          <Checkbox 
            :id="`perm_${permission.permId}`"
            :value="permission.permId"
            v-model="selectedPermissions"
            @change="togglePermission(permission.permId)"
          />
          <label :for="`perm_${permission.permId}`" class="ml-3 cursor-pointer flex-1">
            <div class="font-medium">{{ permission.permName }}</div>
            <div class="text-sm text-gray-600">{{ permission.permDescription }}</div>
          </label>
        </div>
      </div>
      
      <div class="flex justify-end gap-2 mt-6 pt-4 border-t">
        <Button 
          label="취소" 
          severity="secondary" 
          outlined
          @click="permissionModalVisible = false"
        />
        <Button 
          label="저장" 
          severity="primary"
          @click="savePermissions"
        />
      </div>
    </div>
  </Dialog>
</template>