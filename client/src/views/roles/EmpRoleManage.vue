// EmpRoleManage.vue - 조회조건 필드명을 데이터베이스 스키마에 맞춰 통일

<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed, watch, nextTick, getCurrentInstance } from 'vue';
import axios from '@/service/axios';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';

// Vue 경고 숨기기 설정 (개발환경용)
const instance = getCurrentInstance();
if (instance && process.env.NODE_ENV === 'development') {
    const originalWarn = console.warn;
    console.warn = function (...args) {
        const message = args[0];
        if (typeof message === 'string' && (message.includes('Invalid prop') || message.includes('Extraneous non-emits event listeners'))) {
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

// 🔥 검색 조건 - 데이터베이스 필드명으로 통일
const filters = ref({
    title: '조회 조건',
    filters: [
        { type: 'text', label: '사원명', value: '', placeholder: '사원명을 입력하세요', name: 'EMP_NAME' },
        { type: 'text', label: '부서명', value: '', placeholder: '부서명을 입력하세요', name: 'DEPT_NAME' },
        { type: 'select', label: '역할명', value: '', placeholder: '역할을 선택하세요', name: 'ROLE_DESC', options: [] }
    ]
});

const items = ref([]);

// 테이블 헤더 - 부서명과 역할설명이 표시되도록 수정
const header = ref({
    title: '권한 기준정보 관리',
    header: {
        empName: '사원명',
        deptName: '부서명',      // 부서 코드 대신 부서명 표시
        roleDesc: '역할설명',    // 역할명 대신 역할설명 표시
        permissionNames: '보유권한'
    },
    rightAligned: []
});

// 입력 폼 - 부서명과 현재 역할설명도 함께 표시
const inputs = ref({
    title: '사원 권한(역할) 변경',
    inputs: [
        { type: 'text', label: '사원ID', placeholder: '사원ID', name: 'employeeId', readonly: true },
        { type: 'text', label: '사원명', placeholder: '사원명', name: 'empName', readonly: true },
        { type: 'text', label: '부서명', placeholder: '부서명', name: 'deptName', readonly: true },
        { type: 'text', label: '현재역할', placeholder: '현재 역할설명', name: 'currentRoleDesc', readonly: true },
        { type: 'select', label: '변경할역할', placeholder: '변경할 역할을 선택하세요', name: 'newRoleId', options: [] }
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
        filters.value.filters.forEach((filter) => {
            if (!filter.hasOwnProperty('value')) {
                filter.value = '';
            }
            if (filter.type === 'select' && !filter.hasOwnProperty('options')) {
                filter.options = [];
            }
        });
    }
};

// 사원 데이터 처리 - 부서명과 역할설명 포함
const processEmployeeData = (rawData) => {
    return rawData.map((item, index) => ({
        id: item.employeeId || item.EMPLOYEE_ID || `emp_${index}`,
        employeeId: item.employeeId || item.EMPLOYEE_ID,
        empName: item.empName || item.EMP_NAME,
        departmentId: item.departmentId || item.DEPARTMENT_ID,
        deptName: item.deptName || item.DEPT_NAME,          // 부서명 추가
        baseRole: item.baseRole || item.BASE_ROLE,          // 역할명 (검색용)
        roleDesc: item.roleDesc || item.ROLE_DESC,          // 역할설명 추가
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

        // 🔥 필드명 수정: 'BASE_ROLE' → 'ROLE_DESC'
        const roleFilter = filters.value.filters.find((filter) => filter.name === 'ROLE_DESC');
        if (roleFilter) {
            // 역할 옵션을 역할설명으로 표시하되, 값은 역할ID 사용
            roleFilter.options = availableRoles.value.map(role => ({
                name: role.desc,  // 역할설명만 표시
                value: role.value  // 역할ID 값
            }));
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

        const newRoleInput = inputs.value.inputs.find((input) => input.name === 'newRoleId');
        if (newRoleInput) {
            let roleOptions = availableRoles.value;
            
            if (excludeRoleId) {
                roleOptions = availableRoles.value.filter((role) => role.value !== excludeRoleId);
            }
            
            // 변경할 역할 선택 시에도 역할설명 표시
            newRoleInput.options = roleOptions.map(role => ({
                name: `${role.name} (${role.desc})`,  // 역할명 + 역할설명 표시
                value: role.value
            }));
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
        // 역할 데이터 로드 - 역할설명도 함께 로드
        try {
            const rolesResponse = await axios.get(ROLES_API_URL);

            if (rolesResponse.data.result_code === 'SUCCESS' && rolesResponse.data.data) {
                availableRoles.value = rolesResponse.data.data.map((role) => ({
                    name: role.roleName || role.ROLE_NAME,
                    desc: role.roleDesc || role.ROLE_DESC,    // 역할설명 추가
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
// 🔥 검색 데이터 처리 - 데이터베이스 필드명으로 매핑
const searchData = async (searchOptions) => {
    try {
        const searchParams = {};

        // 🔥 검색 파라미터 매핑 (대문자 필드명 → 백엔드 호환)
        Object.keys(searchOptions).forEach((key) => {
            const value = searchOptions[key];
            if (value && value.toString().trim() !== '') {
                // 역할설명으로 검색 처리
                if (key === 'ROLE_DESC' && value !== '') {
                    const selectedRole = availableRoles.value.find((role) => role.value === parseInt(value));
                    if (selectedRole) {
                        // 백엔드에서 기대하는 필드명으로 매핑 (역할명으로 검색)
                        searchParams.baseRole = selectedRole.name;  // 역할명으로 검색
                    }
                } else {
                    // 다른 필드들은 카멜케이스로 변환
                    const camelCaseKey = convertToCamelCase(key);
                    searchParams[camelCaseKey] = value.toString().trim();
                }
            }
        });

        console.log('🔍 검색 파라미터:', searchParams);

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

// 🔥 대문자 스네이크케이스를 카멜케이스로 변환하는 유틸리티 함수
const convertToCamelCase = (str) => {
    const conversions = {
        'EMPLOYEE_ID': 'employeeId',
        'EMP_NAME': 'empName',
        'DEPARTMENT_ID': 'departmentId',
        'DEPT_NAME': 'deptName',
        'BASE_ROLE': 'baseRole'
    };
    
    return conversions[str] || str.toLowerCase().replace(/_([a-z])/g, (match, letter) => letter.toUpperCase());
};

const onRowSelect = (employee) => {
    selectedEmployee.value = employee;
    updateSelectOptions(employee.roleId);

    // 현재 역할의 설명 찾기
    const currentRole = availableRoles.value.find(role => role.value === employee.roleId);
    const currentRoleDesc = currentRole ? `${currentRole.name} (${currentRole.desc})` : employee.baseRole;

    nextTick(() => {
        if (standardInputRef.value?.inputFormRef?.inputDatas) {
            const formData = standardInputRef.value.inputFormRef.inputDatas;
            formData.employeeId = employee.employeeId || '';
            formData.empName = employee.empName || '';
            formData.deptName = employee.deptName || '';           // 부서명 표시
            formData.currentRoleDesc = currentRoleDesc;            // 역할설명 표시
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
            const newRole = availableRoles.value.find((r) => r.value === parseInt(inputData.newRoleId));
            const newRoleName = newRole ? `${newRole.name} (${newRole.desc})` : '알 수 없음';
            
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
    <StandardInput ref="standardInputRef" :filters="filters" :items="items" :header="header" :inputs="inputs" @searchData="searchData" @saveData="saveData" @openSearchModal="openSearchModal" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect">
        <template #btn>
            <!-- 버튼 영역 -->
        </template>
    </StandardInput>
</template>