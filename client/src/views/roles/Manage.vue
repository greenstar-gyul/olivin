<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';

// API 기본 URL - 기존 구조 유지
const API_BASE_URL = '/api/employees';
const ROLES_API_URL = '/api/roles';
const PERMISSIONS_API_URL = '/api/permissions';

// 현재 로그인한 사용자 정보
const currentUser = ref({
  empId: '',
  employeeId: '',
  empName: ''
});

// 현재 사용자 정보 조회 (기존 함수 유지)
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    console.log('사용자 API 전체 응답:', JSON.stringify(response.data, null, 2));
    
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      console.log('userData 구조:', JSON.stringify(userData, null, 2));
      
      let employeeId = 'admin';
      let empName = '관리자';
      
      const possibleUserSources = [
        userData.user,
        userData,
        userData.employee,
        userData.userInfo,
        userData.loginUser
      ];
      
      for (const userSource of possibleUserSources) {
        if (userSource) {
          console.log('처리 중인 userSource:', typeof userSource, userSource);
          
          if (typeof userSource === 'object' && userSource !== null) {
            const possibleEmployeeIds = [
              userSource.employeeId,
              userSource.employee_id,
              userSource.EMPLOYEE_ID,
            ];
            
            const possibleEmpNames = [
              userSource.empName,
              userSource.emp_name,
              userSource.EMP_NAME,
            ];
            
            const foundEmployeeId = possibleEmployeeIds.find(id => id && id !== 'admin' && String(id).trim() !== '');
            const foundEmpName = possibleEmpNames.find(name => name && name !== '관리자' && String(name).trim() !== '');
            
            if (foundEmployeeId) {
              employeeId = String(foundEmployeeId).trim();
            }
            if (foundEmpName) {
              empName = String(foundEmpName).trim();
            }
            
            if (foundEmployeeId && foundEmpName) {
              break;
            }
          } else if (typeof userSource === 'string' && userSource.trim() !== '') {
            empName = userSource.trim();
            employeeId = userSource.trim();
            break;
          }
        }
      }
      
      currentUser.value = {
        empId: employeeId,
        employeeId: employeeId,
        empName: empName
      };
      
      console.log('최종 설정된 사용자 정보:', currentUser.value);
      return currentUser.value;
    }
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error);
    currentUser.value = {
      empId: 'admin',
      employeeId: 'admin',
      empName: '관리자'
    };
    return currentUser.value;
  }
};

// 검색 조건 (사원 기준으로 변경)
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '사원ID', value: '', placeholder: '사원ID를 입력하세요', name: 'employeeId' },
    { type: 'text', label: '사원명', value: '', placeholder: '사원명을 입력하세요', name: 'empName' },
    { type: 'text', label: '부서', value: '', placeholder: '부서를 입력하세요', name: 'departmentId' },
    { type: 'text', label: '직책', value: '', placeholder: '직책을 입력하세요', name: 'position' },
    { type: 'dateRange', label: '입사일 범위', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'hireDateRange' }
  ]
});

const items = ref([]);
const loading = ref(false);

// 테이블 헤더 (사원 기준으로 변경)
const header = ref({
  title: '권한 기준정보 관리',
  header: {
    empName: '사원명',
    departmentId: '부서',
    position: '직책',
    baseRole: '역할',
    permissionNames: '보유권한',
    permissionCount: '권한수',
    status: '상태',
    hireDate: '입사일'
  },
  rightAligned: ['permissionCount']
});

// 입력 폼 (사원 정보는 읽기전용, 역할만 수정 가능)
const inputs = ref({
  title: '사원 권한(역할) 변경',
  inputs: [
    { type: 'text', label: '사원ID', placeholder: '사원ID', name: 'employeeId', readonly: true },
    { type: 'text', label: '사원명', placeholder: '사원명', name: 'empName', readonly: true },
    { type: 'text', label: '부서', placeholder: '부서', name: 'departmentId', readonly: true },
    { type: 'text', label: '직책', placeholder: '직책', name: 'position', readonly: true },
    { type: 'text', label: '현재역할', placeholder: '현재 역할', name: 'currentRole', readonly: true },
    { type: 'dropdown', label: '변경할역할', placeholder: '변경할 역할을 선택하세요', name: 'newRoleId', required: true, options: [] },
    { type: 'text', label: '상태', placeholder: '상태', name: 'status', readonly: true }
  ]
});

// StandardInput 컴포넌트 ref
const standardInputRef = ref(null);

// 선택된 사원 정보
const selectedEmployee = ref(null);

// 역할 목록
const availableRoles = ref([]);

// 권한 상세 모달 관련
const permissionDetailModalVisible = ref(false);
const employeePermissions = ref([]);

// 날짜 포맷 함수
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

// 사원 목록 조회 (권한 정보 포함) - 수정된 API 사용
const loadEmployees = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('사원 목록 조회 시작...');
    
    // 기존 RolesController에 추가된 API 사용
    const response = await axios.get(`${API_BASE_URL}/permissions`, { params: searchParams });
    
    console.log('사원 API 원본 응답:', response.data);
    
    let employees = [];
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      employees = response.data.data;
    } else if (Array.isArray(response.data)) {
      employees = response.data;
    } else {
      console.error('예상하지 못한 응답 구조:', response.data);
      employees = [];
    }
    
    console.log('사원 데이터:', employees);
    
    // 데이터 가공 (Map 키를 camelCase로 변환)
    items.value = employees.map((item, index) => ({
      id: item.employeeId || item.EMPLOYEE_ID || `temp_emp_${Date.now()}_${index}`,
      employeeId: item.employeeId || item.EMPLOYEE_ID,
      empName: item.empName || item.EMP_NAME,
      departmentId: item.departmentId || item.DEPARTMENT_ID,
      position: item.position || item.POSITION,
      baseRole: item.baseRole || item.BASE_ROLE,
      status: item.status || item.STATUS,
      roleId: item.roleId || item.ROLE_ID,
      hireDate: item.hireDate ? formatDate(item.hireDate) : (item.HIRE_DATE ? formatDate(item.HIRE_DATE) : ''),
      permissionCount: item.permissionCount || item.PERMISSION_COUNT || 0,
      permissionNames: item.permissionNames || item.PERMISSION_NAMES || ''
    }));
    
    console.log('최종 사원 목록:', items.value);
    
  } catch (error) {
    console.error('사원 목록 조회 실패:', error);
    console.error('에러 응답:', error.response);
    alert('데이터 조회에 실패했습니다.');
    items.value = [];
  } finally {
    loading.value = false;
  }
};

// 역할 목록 조회 - 기존 ROLES API 사용
const loadRoles = async () => {
  try {
    const response = await axios.get(ROLES_API_URL);
    
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      availableRoles.value = response.data.data.map(role => ({
        label: role.roleName || role.ROLE_NAME,
        value: role.roleId || role.ROLE_ID
      }));
      
      console.log('역할 목록:', availableRoles.value);
    }
  } catch (error) {
    console.error('역할 목록 조회 실패:', error);
    alert('역할 목록 조회에 실패했습니다.');
  }
};

// 특정 사원의 권한 상세 조회
const loadEmployeePermissions = async (employeeId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/${employeeId}/permissions`);
    
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      employeePermissions.value = response.data.data;
    }
  } catch (error) {
    console.error('사원별 권한 조회 실패:', error);
  }
};

// 검색 실행
const searchData = async (searchOptions) => {
  console.log('사원 검색 조건:', searchOptions);
  
  const searchParams = {};
  
  if (searchOptions.employeeId && searchOptions.employeeId.trim() !== '') {
    searchParams.employeeId = searchOptions.employeeId.trim();
  }
  if (searchOptions.empName && searchOptions.empName.trim() !== '') {
    searchParams.empName = searchOptions.empName.trim();
  }
  if (searchOptions.departmentId && searchOptions.departmentId.trim() !== '') {
    searchParams.departmentId = searchOptions.departmentId.trim();
  }
  if (searchOptions.position && searchOptions.position.trim() !== '') {
    searchParams.position = searchOptions.position.trim();
  }
  if (searchOptions.hireDateRangeFrom && searchOptions.hireDateRangeTo) {
    searchParams.hireDateFrom = searchOptions.hireDateRangeFrom;
    searchParams.hireDateTo = searchOptions.hireDateRangeTo;
  }
  
  await loadEmployees(searchParams);
};

// 행 선택 처리 (역할 변경용 폼 데이터 설정)
const onRowSelect = (employee) => {
  console.log('선택된 사원:', employee);
  selectedEmployee.value = employee;
  
  if (standardInputRef.value && standardInputRef.value.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    
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
    
    // 폼 데이터 업데이트 (사원 정보는 읽기전용)
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      if (key === 'employeeId') {
        inputFormRef.inputDatas[key] = employee.employeeId || '';
      } else if (key === 'empName') {
        inputFormRef.inputDatas[key] = employee.empName || '';
      } else if (key === 'departmentId') {
        inputFormRef.inputDatas[key] = employee.departmentId || '';
      } else if (key === 'position') {
        inputFormRef.inputDatas[key] = employee.position || '';
      } else if (key === 'currentRole') {
        inputFormRef.inputDatas[key] = employee.baseRole || '';
      } else if (key === 'status') {
        inputFormRef.inputDatas[key] = employee.status || '';
      } else if (key === 'newRoleId') {
        // 변경할 역할은 비워둠 (사용자가 선택)
        inputFormRef.inputDatas[key] = '';
      }
    });
  }
  
  // 역할 옵션 업데이트
  updateRoleOptions();
};

// 역할 옵션 업데이트 (현재 역할 제외)
const updateRoleOptions = () => {
  if (selectedEmployee.value && standardInputRef.value && standardInputRef.value.inputFormRef) {
    const currentRoleId = selectedEmployee.value.roleId;
    
    // 현재 역할을 제외한 역할 목록
    const filteredRoles = availableRoles.value.filter(role => role.value !== currentRoleId);
    
    // StandardInput의 dropdown 옵션 업데이트
    const newRoleInput = inputs.value.inputs.find(input => input.name === 'newRoleId');
    if (newRoleInput) {
      newRoleInput.options = filteredRoles;
    }
  }
};

// 행 선택 해제 처리
const onRowUnselect = () => {
  selectedEmployee.value = null;
};

// 저장 처리 (사원의 역할 변경만 - 사원 등록 불가)
const saveData = async (inputData) => {
  try {
    console.log('역할 변경 데이터:', inputData);
    
    if (!selectedEmployee.value) {
      alert('역할을 변경할 사원을 선택해주세요.');
      return;
    }
    
    if (!inputData.newRoleId || inputData.newRoleId === '') {
      alert('변경할 역할을 선택해주세요.');
      return;
    }
    
    // 현재 역할과 같은지 확인
    if (parseInt(inputData.newRoleId) === selectedEmployee.value.roleId) {
      alert('현재 역할과 동일합니다. 다른 역할을 선택해주세요.');
      return;
    }
    
    const currentUserData = await getCurrentUser();
    
    const updateData = {
      roleId: parseInt(inputData.newRoleId),
      updateUser: currentUserData.employeeId
    };
    
    console.log('역할 변경 요청 데이터:', updateData);
    
    // 수정된 API 엔드포인트 사용
    const response = await axios.put(`${API_BASE_URL}/${selectedEmployee.value.employeeId}/role`, updateData);
    
    console.log('서버 응답:', response.data);
    
    if (response.data.result_code === 'SUCCESS') {
      const newRoleName = availableRoles.value.find(r => r.value === parseInt(inputData.newRoleId))?.label || '알 수 없음';
      alert(`사원 "${selectedEmployee.value.empName}"의 역할이 "${newRoleName}"으로 변경되었습니다.`);
      
      if (standardInputRef.value && standardInputRef.value.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedEmployee.value = null;
      
      await loadEmployees();
    } else {
      alert(`역할 변경 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('역할 변경 실패:', error);
    
    if (error.code === 'ERR_NETWORK') {
      alert('네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.');
    } else {
      alert('역할 변경 실패: ' + (error.response?.data?.message || error.message));
    }
  }
};

// 삭제 처리 (사원 삭제 X, 역할만 기본값으로 초기화)
const deleteData = async () => {
  if (!selectedEmployee.value || !selectedEmployee.value.employeeId) {
    alert('역할을 초기화할 사원을 선택해주세요.');
    return;
  }

  const confirmReset = confirm(
    `사원 "${selectedEmployee.value.empName}"의 역할을 기본 역할(일반 사용자)로 초기화하시겠습니까?\n\n※ 사원 정보는 삭제되지 않습니다.`
  );
  
  if (!confirmReset) {
    return;
  }

  try {
    console.log('역할 초기화 시작:', selectedEmployee.value.employeeId);
    
    const currentUserData = await getCurrentUser();
    
    // 기본 역할 ID 찾기 (일반 사용자 역할)
    const defaultRole = availableRoles.value.find(r => r.label.includes('사용자') || r.label.includes('USER'));
    const defaultRoleId = defaultRole ? defaultRole.value : 3; // 기본값 3
    
    const response = await axios.put(`${API_BASE_URL}/${selectedEmployee.value.employeeId}/role`, {
      roleId: defaultRoleId,
      updateUser: currentUserData.employeeId
    });
    
    console.log('초기화 응답:', response.data);
    
    if (response.data.result_code === 'SUCCESS') {
      const defaultRoleName = availableRoles.value.find(r => r.value === defaultRoleId)?.label || '일반 사용자';
      alert(`사원 "${selectedEmployee.value.empName}"의 역할이 "${defaultRoleName}"로 초기화되었습니다.`);
      
      if (standardInputRef.value && standardInputRef.value.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedEmployee.value = null;
      
      await loadEmployees();
    } else {
      alert(`초기화 실패: ${response.data.message || '초기화 중 오류가 발생했습니다.'}`);
    }
    
  } catch (error) {
    console.error('역할 초기화 실패:', error);
    alert('초기화 실패: ' + (error.response?.data?.message || error.message));
  }
};

// 권한 상세 모달 열기
const openPermissionDetailModal = async () => {
  if (!selectedEmployee.value) {
    alert('사원을 선택해주세요.');
    return;
  }
  
  await loadEmployeePermissions(selectedEmployee.value.employeeId);
  permissionDetailModalVisible.value = true;
};

// 모달 처리 함수
const openSearchModal = (inputName) => {
  console.log('모달 열기:', inputName);
};

// 초기화 함수
const initializeFormData = async () => {
  const user = await getCurrentUser();
  console.log('폼 초기화 시 사용자 정보:', user);
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  // 역할 목록 먼저 로드
  await loadRoles();
  
  // 사원 목록 로드
  await loadEmployees();
  
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
    <!-- 역할 관리 버튼 -->
    <template #btn>
      <Button 
        label="역할초기화" 
        severity="danger" 
        class="min-w-fit whitespace-nowrap mr-2" 
        outlined
        :disabled="!selectedEmployee"
        @click="deleteData"
      />
      <Button 
        label="권한상세보기" 
        severity="info" 
        class="min-w-fit whitespace-nowrap" 
        outlined
        :disabled="!selectedEmployee"
        @click="openPermissionDetailModal"
      />
    </template>
  </StandardInput>

  <!-- 권한 상세 모달 -->
  <Dialog 
    v-model:visible="permissionDetailModalVisible" 
    :modal="true" 
    :closable="true"
    :style="{ width: '60vw' }"
    header="사원 권한 상세"
  >
    <div class="p-4">
      <div v-if="selectedEmployee" class="mb-4 p-3 bg-blue-50 rounded">
        <h4 class="text-lg font-semibold text-blue-800">
          선택된 사원: {{ selectedEmployee.empName }} ({{ selectedEmployee.employeeId }})
        </h4>
        <p class="text-sm text-blue-600">{{ selectedEmployee.departmentId }} - {{ selectedEmployee.position }}</p>
        <p class="text-sm text-blue-600">현재역할: {{ selectedEmployee.baseRole }}</p>
      </div>
      
      <div class="grid grid-cols-1 gap-4 max-h-96 overflow-y-auto">
        <div v-if="employeePermissions && employeePermissions.length > 0">
          <h5 class="font-semibold mb-2">보유 권한 목록:</h5>
          <div v-for="permId in employeePermissions" :key="permId" 
               class="p-2 border rounded bg-gray-50">
            권한 ID: {{ permId }}
          </div>
        </div>
        <div v-else class="text-gray-500 text-center py-4">
          권한 정보가 없습니다.
        </div>
      </div>
      
      <div class="flex justify-end gap-2 mt-6 pt-4 border-t">
        <Button 
          label="닫기" 
          severity="secondary" 
          outlined
          @click="permissionDetailModalVisible = false"
        />
      </div>
    </div>
  </Dialog>
</template>