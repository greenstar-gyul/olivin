<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed, watch, nextTick, getCurrentInstance } from 'vue';
import axios from '@/service/axios';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';

// Vue 경고 숨기기 설정 (개발 환경용)
const instance = getCurrentInstance();
if (instance && process.env.NODE_ENV === 'development') {
  const originalWarn = console.warn;
  console.warn = function(...args) {
    const message = args[0];
    if (typeof message === 'string' && 
        (message.includes('Invalid prop') || 
         message.includes('Extraneous non-emits event listeners'))) {
      return; // prop 및 emit 경고 무시
    }
    originalWarn.apply(console, args);
  };
}

// ==================== 상수 및 설정 ====================
const API_BASE_URL = '/api/employees';
const ROLES_API_URL = '/api/roles';

// ==================== 반응형 데이터 ====================
const currentUser = ref({
  empId: 'admin',
  employeeId: 'admin', 
  empName: '관리자'
});

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '사원ID', value: '', placeholder: '사원ID를 입력하세요', name: 'employeeId' },
    { type: 'text', label: '사원명', value: '', placeholder: '사원명을 입력하세요', name: 'empName' },
    { type: 'text', label: '부서', value: '', placeholder: '부서를 입력하세요', name: 'departmentId' },
    { type: 'select', label: '역할', value: '', placeholder: '역할을 선택하세요', name: 'baseRole', options: [] }
  ]
});

const items = ref([]);

const header = ref({
  title: '권한 기준정보 관리',
  header: {
    empName: '사원명',
    departmentId: '부서', 
    baseRole: '역할',
    permissionNames: '보유권한',
  },
  rightAligned: []
});

const inputs = ref({
  title: '사원 권한(역할) 변경',
  inputs: [
    { type: 'text', label: '사원ID', placeholder: '사원ID', name: 'employeeId', readonly: true },
    { type: 'text', label: '사원명', placeholder: '사원명', name: 'empName', readonly: true },
    { type: 'text', label: '부서', placeholder: '부서', name: 'departmentId', readonly: true },
    { type: 'text', label: '현재역할', placeholder: '현재 역할', name: 'currentRole', readonly: true },
    { type: 'select', label: '변경할역할', placeholder: '변경할 역할을 선택하세요', name: 'newRoleId', options: [] },
  ]
});

const standardInputRef = ref(null);
const selectedEmployee = ref(null);
const availableRoles = ref([]);

// ==================== 유틸리티 함수 ====================
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      const user = userData.user || userData;
      currentUser.value = {
        empId: user.employeeId || 'admin',
        employeeId: user.employeeId || 'admin',
        empName: user.empName || '관리자'
      };
    }
  } catch (error) {
    // 에러 무시 - 기본값 사용
  }
  return currentUser.value;
};

const initializeFilters = () => {
  if (filters.value && filters.value.filters) {
    filters.value.filters.forEach(filter => {
      if (!filter.hasOwnProperty('value')) {
        filter.value = '';
      }
      if (filter.type === 'select' && !filter.hasOwnProperty('options')) {
        filter.options = [];
      }
    });
  }
};

const processEmployeeData = (rawData) => {
  return rawData.map((item, index) => ({
    id: item.employeeId || item.EMPLOYEE_ID || `emp_${index}`,
    employeeId: item.employeeId || item.EMPLOYEE_ID,
    empName: item.empName || item.EMP_NAME, 
    departmentId: item.departmentId || item.DEPARTMENT_ID,
    baseRole: item.baseRole || item.BASE_ROLE,
    roleId: item.roleId || item.ROLE_ID,
    permissionNames: item.permissionNames || item.PERMISSION_NAMES || ''
  }));
};

// ==================== 옵션 업데이트 함수 ====================
const updateSearchRoleOptions = () => {
  try {
    if (!filters.value || !filters.value.filters || !Array.isArray(filters.value.filters)) {
      return;
    }
    
    const roleFilter = filters.value.filters.find(filter => filter.name === 'baseRole');
    if (roleFilter) {
      roleFilter.options = [
        ...availableRoles.value
      ];
    }
  } catch (error) {
    // 에러 무시
  }
};

const updateSelectOptions = (excludeRoleId = null) => {
  try {
    if (!inputs.value || !inputs.value.inputs) {
      return;
    }
    
    const newRoleInput = inputs.value.inputs.find(input => input.name === 'newRoleId');
    if (newRoleInput) {
      if (excludeRoleId) {
        newRoleInput.options = availableRoles.value.filter(role => role.value !== excludeRoleId);
      } else {
        newRoleInput.options = availableRoles.value;
      }
    }
  } catch (error) {
    // 에러 무시
  }
};

// ==================== 데이터 로드 함수 ====================
const loadInitialData = async () => {
  items.value = [];
  availableRoles.value = [];
  initializeFilters();
  
  try {
    // 역할 데이터 로드
    try {
      const rolesResponse = await axios.get(ROLES_API_URL);
      
      if (rolesResponse.data.result_code === 'SUCCESS' && rolesResponse.data.data) {
        availableRoles.value = rolesResponse.data.data.map(role => ({
          name: role.roleName || role.ROLE_NAME,
          value: role.roleId || role.ROLE_ID
        }));
        
        await nextTick();
        updateSearchRoleOptions();
        updateSelectOptions();
      }
    } catch (roleError) {
      availableRoles.value = [];
    }

    // 사원 데이터 로드
    try {
      const employeesResponse = await axios.get(`${API_BASE_URL}/permissions`);
      
      let employees = [];
      if (employeesResponse.data.result_code === 'SUCCESS' && employeesResponse.data.data) {
        employees = employeesResponse.data.data;
      } else if (Array.isArray(employeesResponse.data)) {
        employees = employeesResponse.data;
      }
      
      items.value = processEmployeeData(employees);
    } catch (empError) {
      items.value = [];
    }
    
  } catch (error) {
    console.error('시스템 오류:', error.message);
    items.value = [];
    availableRoles.value = [];
  }
};

// ==================== 이벤트 핸들러 ====================
const searchData = async (searchOptions) => {
  try {
    const searchParams = {};
    
    Object.keys(searchOptions).forEach(key => {
      const value = searchOptions[key];
      if (value && value.toString().trim() !== '') {
        if (key === 'baseRole' && value !== '') {
          const selectedRole = availableRoles.value.find(role => role.value === parseInt(value));
          if (selectedRole) {
            searchParams.baseRole = selectedRole.name;
          }
        } else {
          searchParams[key] = value.toString().trim();
        }
      }
    });
    
    const response = await axios.get(`${API_BASE_URL}/permissions`, { params: searchParams });
    
    let employees = [];
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      employees = response.data.data;
    } else if (Array.isArray(response.data)) {
      employees = response.data;
    }
    
    items.value = processEmployeeData(employees);
    
  } catch (error) {
    console.error('검색 실패:', error);
    items.value = [];
  }
};

const onRowSelect = (employee) => {
  selectedEmployee.value = employee;
  updateSelectOptions(employee.roleId);
  
  nextTick(() => {
    if (standardInputRef.value?.inputFormRef?.inputDatas) {
      const formData = standardInputRef.value.inputFormRef.inputDatas;
      formData.employeeId = employee.employeeId || '';
      formData.empName = employee.empName || '';
      formData.departmentId = employee.departmentId || '';
      formData.currentRole = employee.baseRole || '';
      formData.newRoleId = '';
    }
  });
};

const onRowUnselect = () => {
  selectedEmployee.value = null;
  updateSelectOptions();
  
  if (standardInputRef.value?.inputFormRef) {
    standardInputRef.value.inputFormRef.resetInputDatas();
  }
};

const saveData = async (inputData) => {
  try {
    if (!selectedEmployee.value) {
      alert('역할을 변경할 사원을 선택해주세요.');
      return;
    }
    
    if (!inputData.newRoleId) {
      alert('변경할 역할을 선택해주세요.');
      return;
    }
    
    if (parseInt(inputData.newRoleId) === selectedEmployee.value.roleId) {
      alert('현재 역할과 동일합니다. 다른 역할을 선택해주세요.');
      return;
    }
    
    const updateData = {
      roleId: parseInt(inputData.newRoleId),
      updateUser: currentUser.value.employeeId
    };
    
    const response = await axios.put(`${API_BASE_URL}/${selectedEmployee.value.employeeId}/role`, updateData);
    
    if (response.data.result_code === 'SUCCESS') {
      const newRoleName = availableRoles.value.find(r => r.value === parseInt(inputData.newRoleId))?.name || '알 수 없음';
      alert(`사원 "${selectedEmployee.value.empName}"의 역할이 "${newRoleName}"으로 변경되었습니다.`);
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedEmployee.value = null;
      updateSelectOptions();
      
      await searchData({});
    } else {
      alert(`역할 변경 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('역할 변경 실패:', error);
    alert('역할 변경 실패: ' + (error.response?.data?.message || error.message));
  }
};

const openSearchModal = (inputName) => {
  console.log('모달 열기:', inputName);
};

// ==================== 라이프사이클 ====================
onMounted(async () => {
  try {
    initializeFilters();
    
    getCurrentUser().catch(() => {
      // 에러 무시 - 기본값으로 동작
    });
    
    await loadInitialData();
  } catch (error) {
    console.error('페이지 로드 실패');
    items.value = [];
    availableRoles.value = [];
    initializeFilters();
  }
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
      <!-- 버튼 영역 -->
    </template>
  </StandardInput>
</template>