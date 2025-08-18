// RoleManage.vue - 부모권한 자동선택 기능 및 스마트한 해제 로직 추가

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from '@/service/axios';
import Button from 'primevue/button';
import Checkbox from 'primevue/checkbox';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';

// API 기본 URL
const ROLES_API_URL = '/api/roles';
const PERMISSIONS_API_URL = '/api/permissions';

// 현재 로그인한 사용자 정보
const currentUser = ref({
    empId: '',
    employeeId: '',
    empName: ''
});

// 현재 사용자 정보 조회
const getCurrentUser = async () => {
    try {
        const response = await axios.get('/api/auth/me');
        console.log('사용자 API 전체 응답:', JSON.stringify(response.data, null, 2));

        if (response.data.success && response.data.data) {
            const userData = response.data.data;

            let employeeId = 'admin';
            let empName = '관리자';

            const possibleUserSources = [userData.user, userData, userData.employee, userData.userInfo, userData.loginUser];

            for (const userSource of possibleUserSources) {
                if (userSource) {
                    if (typeof userSource === 'object' && userSource !== null) {
                        const possibleEmployeeIds = [userSource.employeeId, userSource.employee_id, userSource.EMPLOYEE_ID];
                        const possibleEmpNames = [userSource.empName, userSource.emp_name, userSource.EMP_NAME];

                        const foundEmployeeId = possibleEmployeeIds.find((id) => id && id !== 'admin' && String(id).trim() !== '');
                        const foundEmpName = possibleEmpNames.find((name) => name && name !== '관리자' && String(name).trim() !== '');

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

// 검색 조건
const filters = ref({
    title: '조회 조건',
    filters: [
        { type: 'text', label: '역할ID', value: '', placeholder: '역할ID를 입력하세요', name: 'roleId' },
        { type: 'text', label: '역할명', value: '', placeholder: '역할명을 입력하세요', name: 'roleName' },
        { type: 'text', label: '역할설명', value: '', placeholder: '역할설명을 입력하세요', name: 'roleDesc' }
    ]
});

const items = ref([]);
const loading = ref(false);

// 테이블 헤더
const header = ref({
    title: '역할별 권한 관리',
    header: {
        roleName: '역할명',
        roleDesc: '역할설명',
        employeeCount: '사원수',
        permissionCount: '권한수',
        permissionNames: '할당된권한'
    },
    rightAligned: ['employeeCount', 'permissionCount']
});

// 선택된 역할 정보
const selectedRole = ref(null);

// 모든 권한 목록
const allPermissions = ref([]);

// 선택된 역할의 권한 목록
const rolePermissions = ref([]);

// 선택된 권한들 (체크박스용)
const selectedPermissions = ref([]);

// 역할 입력 폼 데이터
const roleFormData = ref({
    roleId: '',
    roleName: '',
    roleDesc: ''
});

// 편집 모드 (신규/수정 구분)
const isEditMode = ref(false);

// 폼 유효성 검사
const formErrors = ref({
    roleName: '',
    roleDesc: ''
});

// ✅ 권한 계층 구조 관리를 위한 computed 속성들
const permissionMap = computed(() => {
    const map = new Map();
    allPermissions.value.forEach(perm => {
        map.set(perm.id, perm);
    });
    return map;
});

// ✅ 부모-자식 관계 매핑
const parentChildMap = computed(() => {
    const map = new Map();
    allPermissions.value.forEach(perm => {
        if (perm.parentTo) {
            if (!map.has(perm.parentTo)) {
                map.set(perm.parentTo, []);
            }
            map.get(perm.parentTo).push(perm.id);
        }
    });
    return map;
});

// ✅ 자식-부모 관계 매핑
const childParentMap = computed(() => {
    const map = new Map();
    allPermissions.value.forEach(perm => {
        if (perm.parentTo) {
            map.set(perm.id, perm.parentTo);
        }
    });
    return map;
});

// ✅ 루트 권한들 (부모가 없는 권한)
const rootPermissions = computed(() => {
    return allPermissions.value.filter(perm => !perm.parentTo);
});

// ✅ 계층구조로 정렬된 권한 목록
const hierarchicalPermissions = computed(() => {
    const result = [];
    
    // 루트 권한들을 먼저 추가
    rootPermissions.value.forEach(rootPerm => {
        result.push({
            ...rootPerm,
            level: 0,
            isParent: parentChildMap.value.has(rootPerm.id)
        });
        
        // 자식 권한들 재귀적으로 추가
        addChildPermissions(rootPerm.id, result, 1);
    });
    
    return result;
});

// ✅ 자식 권한들을 재귀적으로 추가하는 함수
const addChildPermissions = (parentId, result, level) => {
    const children = parentChildMap.value.get(parentId) || [];
    children.forEach(childId => {
        const childPerm = permissionMap.value.get(childId);
        if (childPerm) {
            result.push({
                ...childPerm,
                level: level,
                isParent: parentChildMap.value.has(childId)
            });
            
            // 더 깊은 자식들도 추가
            addChildPermissions(childId, result, level + 1);
        }
    });
};

// ✅ 특정 권한의 모든 부모 권한들을 찾는 함수
const getAllParents = (permissionId) => {
    const parents = [];
    let currentId = permissionId;
    
    while (childParentMap.value.has(currentId)) {
        const parentId = childParentMap.value.get(currentId);
        parents.push(parentId);
        currentId = parentId;
    }
    
    return parents;
};

// ✅ 특정 권한의 모든 자식 권한들을 찾는 함수 (재귀적)
const getAllChildren = (permissionId) => {
    const children = [];
    const directChildren = parentChildMap.value.get(permissionId) || [];
    
    directChildren.forEach(childId => {
        children.push(childId);
        // 재귀적으로 손자, 증손자... 권한들도 추가
        const grandChildren = getAllChildren(childId);
        children.push(...grandChildren);
    });
    
    return children;
};

// ✅ 특정 권한의 모든 형제 권한들을 찾는 함수 (같은 부모를 가진 권한들)
const getSiblingPermissions = (permissionId) => {
    const parentId = childParentMap.value.get(permissionId);
    if (!parentId) return []; // 부모가 없으면 형제도 없음
    
    const siblings = parentChildMap.value.get(parentId) || [];
    return siblings.filter(id => id !== permissionId); // 자기 자신은 제외
};

// ✅ 권한 선택/해제 토글 처리 (개선된 방식)
const handlePermissionToggle = (permissionId, isChecked) => {
    console.log(`권한 ${permissionId} ${isChecked ? '선택' : '해제'}됨`);
    
    // 현재 선택된 권한들을 복사
    let newSelectedPermissions = [...selectedPermissions.value];
    
    if (isChecked) {
        // ✅ 권한이 선택된 경우: 해당 권한과 모든 부모 권한들 자동 선택
        if (!newSelectedPermissions.includes(permissionId)) {
            newSelectedPermissions.push(permissionId);
        }
        
        // 모든 부모 권한들도 자동 선택
        const parents = getAllParents(permissionId);
        parents.forEach(parentId => {
            if (!newSelectedPermissions.includes(parentId)) {
                newSelectedPermissions.push(parentId);
                console.log(`부모 권한 ${parentId} 자동 선택됨`);
            }
        });
        
    } else {
        // ✅ 권한이 해제된 경우: 해당 권한과 모든 자식 권한들 자동 해제
        const indexToRemove = newSelectedPermissions.indexOf(permissionId);
        if (indexToRemove > -1) {
            newSelectedPermissions.splice(indexToRemove, 1);
        }
        
        // 모든 자식 권한들도 자동 해제
        const children = getAllChildren(permissionId);
        children.forEach(childId => {
            const childIndex = newSelectedPermissions.indexOf(childId);
            if (childIndex > -1) {
                newSelectedPermissions.splice(childIndex, 1);
                console.log(`자식 권한 ${childId} 자동 해제됨`);
            }
        });
        
        // ✅ 부모 권한 해제 여부 검사 - 다른 형제 권한이 선택되어 있지 않은 경우에만 해제
        const parentId = childParentMap.value.get(permissionId);
        if (parentId && newSelectedPermissions.includes(parentId)) {
            // 현재 해제되는 권한의 형제 권한들을 찾음
            const siblings = getSiblingPermissions(permissionId);
            
            // 형제 권한 중 선택된 것이 있는지 확인 (이미 해제된 자식들은 제외)
            const selectedSiblings = siblings.filter(siblingId => 
                newSelectedPermissions.includes(siblingId)
            );
            
            console.log(`권한 ${permissionId}의 형제 권한들:`, siblings);
            console.log(`선택된 형제 권한들:`, selectedSiblings);
            
            // 선택된 형제 권한이 없다면 부모 권한도 해제
            if (selectedSiblings.length === 0) {
                const parentIndex = newSelectedPermissions.indexOf(parentId);
                if (parentIndex > -1) {
                    newSelectedPermissions.splice(parentIndex, 1);
                    console.log(`부모 권한 ${parentId} 자동 해제됨 (다른 자식 권한이 없음)`);
                    
                    // 부모의 부모도 재귀적으로 검사 (조부모, 증조부모...)
                    checkAndRemoveAncestors(parentId, newSelectedPermissions);
                }
            } else {
                console.log(`부모 권한 ${parentId} 유지됨 (다른 자식 권한 ${selectedSiblings.length}개 존재)`);
            }
        }
    }
    
    // 상태 업데이트
    selectedPermissions.value = newSelectedPermissions;
};

// ✅ 조상 권한들을 재귀적으로 검사하여 필요시 해제하는 함수
const checkAndRemoveAncestors = (permissionId, selectedPermissions) => {
    const parentId = childParentMap.value.get(permissionId);
    if (!parentId || !selectedPermissions.includes(parentId)) {
        return; // 부모가 없거나 이미 해제된 경우 종료
    }
    
    // 현재 권한의 형제 권한들 중 선택된 것이 있는지 확인
    const siblings = getSiblingPermissions(permissionId);
    const selectedSiblings = siblings.filter(siblingId => 
        selectedPermissions.includes(siblingId)
    );
    
    // 선택된 형제 권한이 없다면 부모 권한도 해제하고 재귀 호출
    if (selectedSiblings.length === 0) {
        const parentIndex = selectedPermissions.indexOf(parentId);
        if (parentIndex > -1) {
            selectedPermissions.splice(parentIndex, 1);
            console.log(`조상 권한 ${parentId} 자동 해제됨 (다른 자식 권한이 없음)`);
            
            // 부모의 부모도 재귀적으로 검사
            checkAndRemoveAncestors(parentId, selectedPermissions);
        }
    }
};

// ✅ 권한 체크박스 상태 확인 함수
const isPermissionChecked = (permissionId) => {
    return selectedPermissions.value.includes(permissionId);
};

// ✅ 권한이 비활성화되어야 하는지 확인 - 모든 권한 활성화 (부모 선택 여부와 무관)
const isPermissionDisabled = (permission) => {
    // 모든 권한을 선택 가능하도록 변경 (부모 권한 선택 여부와 무관)
    return false;
};

// 역할 목록 조회
const loadRoles = async (searchParams = {}) => {
    try {
        loading.value = true;
        console.log('역할 목록 조회 시작...');

        const response = await axios.get(ROLES_API_URL, { params: searchParams });
        console.log('역할 API 원본 응답:', response.data);

        let roles = [];
        if (response.data.result_code === 'SUCCESS' && response.data.data) {
            roles = response.data.data;
        } else if (Array.isArray(response.data)) {
            roles = response.data;
        } else {
            console.error('예상하지 못한 응답 구조:', response.data);
            roles = [];
        }

        // 데이터 가공
        items.value = roles.map((item, index) => ({
            id: item.roleId || item.ROLE_ID || `temp_role_${Date.now()}_${index}`,
            roleId: item.roleId || item.ROLE_ID,
            roleName: item.roleName || item.ROLE_NAME,
            roleDesc: item.roleDesc || item.ROLE_DESC,
            employeeCount: item.employeeCount || item.EMPLOYEE_COUNT || 0,
            permissionCount: item.permissionCount || item.PERMISSION_COUNT || 0,
            permissionNames: item.permissionNames || item.PERMISSION_NAMES || ''
        }));

        console.log('최종 역할 목록:', items.value);
    } catch (error) {
        console.error('역할 목록 조회 실패:', error);
        alert('데이터 조회에 실패했습니다.');
        items.value = [];
    } finally {
        loading.value = false;
    }
};

// 모든 권한 목록 조회
const loadAllPermissions = async () => {
    try {
        const response = await axios.get(PERMISSIONS_API_URL);
        
        if (response.data.result_code === 'SUCCESS' && response.data.data) {
            allPermissions.value = response.data.data.map(perm => ({
                id: String(perm.PERM_ID || perm.permId),
                name: perm.PERM_NAME || perm.permName,
                description: perm.PERM_DESCRIPTION || perm.permDescription,
                icon: perm.ICON || perm.icon,
                parentTo: perm.PARENT_TO || perm.parentTo
            }));
            
            console.log('모든 권한 목록:', allPermissions.value);
            console.log('부모-자식 관계:', parentChildMap.value);
            console.log('자식-부모 관계:', childParentMap.value);
        }
    } catch (error) {
        console.error('권한 목록 조회 실패:', error);
        allPermissions.value = [];
    }
};

// 특정 역할의 권한 목록 조회
const loadRolePermissions = async (roleId) => {
    try {
        const response = await axios.get(`${ROLES_API_URL}/${roleId}/permissions`);
        
        if (response.data.result_code === 'SUCCESS' && response.data.data) {
            rolePermissions.value = response.data.data.map(permId => String(permId));
            console.log('역할별 권한 목록:', rolePermissions.value);
        }
    } catch (error) {
        console.error('역할별 권한 조회 실패:', error);
        rolePermissions.value = [];
    }
};

// 폼 유효성 검사
const validateForm = () => {
    formErrors.value = {
        roleName: '',
        roleDesc: ''
    };

    let isValid = true;

    if (!roleFormData.value.roleName || roleFormData.value.roleName.trim() === '') {
        formErrors.value.roleName = '역할명은 필수입니다.';
        isValid = false;
    } else if (roleFormData.value.roleName.length > 50) {
        formErrors.value.roleName = '역할명은 50자 이내로 입력하세요.';
        isValid = false;
    }

    if (!roleFormData.value.roleDesc || roleFormData.value.roleDesc.trim() === '') {
        formErrors.value.roleDesc = '역할설명은 필수입니다.';
        isValid = false;
    } else if (roleFormData.value.roleDesc.length > 200) {
        formErrors.value.roleDesc = '역할설명은 200자 이내로 입력하세요.';
        isValid = false;
    }

    return isValid;
};

// 새 역할 등록
const createRole = async () => {
    try {
        if (!validateForm()) {
            return;
        }

        const roleData = {
            roleName: roleFormData.value.roleName.trim(),
            roleDesc: roleFormData.value.roleDesc.trim(),
            permissionIds: selectedPermissions.value
        };

        console.log('등록할 역할 데이터:', roleData);

        const response = await axios.post(ROLES_API_URL, roleData);

        if (response.data.result_code === 'SUCCESS') {
            const user = await getCurrentUser();
            alert(`역할 "${roleData.roleName}"이(가) 성공적으로 등록되었습니다. (등록자: ${user.empName})`);

            resetForm();
            await loadRoles();
        } else {
            alert(`역할 등록 실패: ${response.data.message || '알 수 없는 오류'}`);
        }
    } catch (error) {
        console.error('역할 등록 실패:', error);
        alert('역할 등록 실패: ' + (error.response?.data?.message || error.message));
    }
};

// 역할 정보 수정
const updateRole = async () => {
    try {
        if (!selectedRole.value || !selectedRole.value.roleId) {
            alert('수정할 역할을 선택해주세요.');
            return;
        }

        if (!validateForm()) {
            return;
        }

        const roleData = {
            roleName: roleFormData.value.roleName.trim(),
            roleDesc: roleFormData.value.roleDesc.trim(),
            permissionIds: selectedPermissions.value
        };

        console.log('수정할 역할 데이터:', roleData);

        const response = await axios.put(`${ROLES_API_URL}/${selectedRole.value.roleId}`, roleData);

        if (response.data.result_code === 'SUCCESS') {
            const user = await getCurrentUser();
            alert(`역할 "${roleData.roleName}"이(가) 성공적으로 수정되었습니다. (수정자: ${user.empName})`);

            await savePermissions();
            resetForm();
            await loadRoles();
        } else {
            alert(`역할 수정 실패: ${response.data.message || '알 수 없는 오류'}`);
        }
    } catch (error) {
        console.error('역할 수정 실패:', error);
        alert('역할 수정 실패: ' + (error.response?.data?.message || error.message));
    }
};

// 역할 삭제
const deleteRole = async () => {
    try {
        if (!selectedRole.value || !selectedRole.value.roleId) {
            alert('삭제할 역할을 선택해주세요.');
            return;
        }

        if (selectedRole.value.employeeCount > 0) {
            alert(`해당 역할을 사용 중인 사원이 ${selectedRole.value.employeeCount}명 있어 삭제할 수 없습니다.`);
            return;
        }

        const confirmDelete = confirm(
            `역할 "${selectedRole.value.roleName}"을(를) 삭제하시겠습니까?\n\n` +
            `- 역할ID: ${selectedRole.value.roleId}\n` +
            `- 할당된 권한: ${selectedRole.value.permissionCount}개\n\n` +
            `삭제된 역할은 복구할 수 없습니다.`
        );

        if (!confirmDelete) return;

        const response = await axios.delete(`${ROLES_API_URL}/${selectedRole.value.roleId}`);

        if (response.data.result_code === 'SUCCESS') {
            const user = await getCurrentUser();
            alert(`역할 "${selectedRole.value.roleName}"이(가) 성공적으로 삭제되었습니다. (삭제자: ${user.empName})`);

            resetForm();
            await loadRoles();
        } else {
            alert(`역할 삭제 실패: ${response.data.message || '알 수 없는 오류'}`);
        }
    } catch (error) {
        console.error('역할 삭제 실패:', error);
        alert('역할 삭제 실패: ' + (error.response?.data?.message || error.message));
    }
};

// 폼 초기화
const resetForm = () => {
    selectedRole.value = null;
    roleFormData.value = {
        roleId: '',
        roleName: '',
        roleDesc: ''
    };
    selectedPermissions.value = [];
    rolePermissions.value = [];
    isEditMode.value = false;
    formErrors.value = {
        roleName: '',
        roleDesc: ''
    };
};

// 검색 실행
const searchData = async (searchOptions) => {
    console.log('역할 검색 조건:', searchOptions);

    const searchParams = {};

    if (searchOptions.roleId && searchOptions.roleId.trim() !== '') {
        searchParams.roleId = searchOptions.roleId.trim();
    }
    if (searchOptions.roleName && searchOptions.roleName.trim() !== '') {
        searchParams.roleName = searchOptions.roleName.trim();
    }
    if (searchOptions.roleDesc && searchOptions.roleDesc.trim() !== '') {
        searchParams.roleDesc = searchOptions.roleDesc.trim();
    }

    await loadRoles(searchParams);
};

// 행 선택 처리
const onRowSelect = async (event) => {
    const role = event.data;
    console.log('선택된 역할:', role);
    selectedRole.value = role;
    
    await loadRolePermissions(role.roleId);
    selectedPermissions.value = [...rolePermissions.value];

    isEditMode.value = true;
    roleFormData.value = {
        roleId: role.roleId,
        roleName: role.roleName,
        roleDesc: role.roleDesc
    };
};

// 행 선택 해제 처리
const onRowUnselect = (event) => {
    resetForm();
};

// 권한 설정 저장
const savePermissions = async () => {
    try {
        if (!selectedRole.value) {
            alert('역할을 선택해주세요.');
            return;
        }

        console.log('권한 설정 저장:', selectedPermissions.value);

        const response = await axios.post(`${ROLES_API_URL}/${selectedRole.value.roleId}/permissions`, {
            permissionIds: selectedPermissions.value
        });

        if (response.data.result_code === 'SUCCESS') {
            alert(`역할 "${selectedRole.value.roleName}"의 권한이 성공적으로 설정되었습니다.`);

            await loadRolePermissions(selectedRole.value.roleId);
            await loadRoles();
        } else {
            alert(`권한 설정 실패: ${response.data.message || '알 수 없는 오류'}`);
        }
    } catch (error) {
        console.error('권한 설정 실패:', error);
        alert('권한 설정 실패: ' + (error.response?.data?.message || error.message));
    }
};

// 검색 조건 초기화
const handleResetSearchOptions = async () => {
    filters.value.filters.forEach(filter => {
        filter.value = '';
    });
    
    resetForm();
    await loadRoles();
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
    console.log('역할 권한 관리 컴포넌트 마운트 시작...');
    
    await loadAllPermissions();
    await loadRoles();
    
    setTimeout(async () => {
        await getCurrentUser();
    }, 100);
});
</script>

<template>
  <div class="space-y-6">
    <!-- 상단: 검색 조건 -->
    <div class="card p-6">
      <div class="font-semibold text-xl mb-4">조회 조건</div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
        <div v-for="filter in filters.filters" :key="filter.name" class="flex flex-col">
          <label class="block text-sm font-medium mb-2">{{ filter.label }}</label>
          <input
            v-model="filter.value"
            :type="filter.type"
            :placeholder="filter.placeholder"
            class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>
      
      <div class="flex justify-center gap-3">
        <Button 
          label="초기화" 
          @click="handleResetSearchOptions"
          severity="secondary"
        />
        <Button 
          label="조회" 
          @click="() => {
            const searchOptions = {};
            filters.filters.forEach(filter => {
              searchOptions[filter.name] = filter.value;
            });
            searchData(searchOptions);
          }"
        />
      </div>
    </div>
    
    <!-- 하단: 좌우 분할 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <!-- 좌측: 역할 목록 -->
      <div class="card p-6">
        <div class="font-semibold text-xl mb-4 flex justify-between">
          <div>{{ header.title }}</div>
          <div class="flex items-center gap-2 flex-nowrap">
            <Button 
              label="삭제" 
              severity="danger" 
              class="min-w-fit whitespace-nowrap" 
              outlined 
              :disabled="!selectedRole || selectedRole.employeeCount > 0"
              @click="deleteRole"
            />
          </div>
        </div>
        <DataTable 
          v-model:selection="selectedRole" 
          :value="items" 
          dataKey="roleId" 
          showGridlines 
          scrollable
          scrollHeight="400px" 
          tableStyle="min-width: 50rem"
          @rowSelect="onRowSelect" 
          @rowUnselect="onRowUnselect"
          selectionMode="single"
        >
          <Column selectionMode="single" headerStyle="width: 3rem"></Column>
          
          <Column v-for="(headerText, key) in header.header" :key="key" :field="key" :header="headerText">
            <template #body="slotProps">
              <span v-if="key === 'permissionNames' && slotProps.data[key]" 
                    class="inline-block max-w-[200px] truncate" 
                    :title="slotProps.data[key]">
                {{ slotProps.data[key] }}
              </span>
              <span v-else-if="header.rightAligned?.includes(key)">
                {{ slotProps.data[key] ? slotProps.data[key].toLocaleString() : '0' }}
              </span>
              <span v-else>
                {{ slotProps.data[key] || '' }}
              </span>
            </template>
          </Column>
        </DataTable>
      </div>
      
      <!-- 우측: 역할 정보 및 권한 설정 -->
      <div class="card p-6">
        <div class="font-semibold text-xl mb-4 flex justify-between">
          <div>{{ isEditMode ? '역할 수정' : '역할 등록/권한 설정' }}</div>
          <div class="flex items-center gap-2 flex-nowrap">
            <Button 
              label="초기화" 
              severity="secondary" 
              class="min-w-fit whitespace-nowrap" 
              outlined
              @click="resetForm"
            />
            <Button 
              v-if="isEditMode"
              label="수정" 
              class="min-w-fit whitespace-nowrap"
              @click="updateRole"
            />
            <Button 
              v-else
              label="등록" 
              class="min-w-fit whitespace-nowrap"
              @click="createRole"
            />
          </div>
        </div>
        
        <div class="space-y-6">
          <!-- 역할 기본 정보 입력/표시 -->
          <div class="grid grid-cols-1 gap-4">
            <!-- 역할ID (읽기 전용) -->
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">역할ID</label>
              <div class="col-span-9">
                <InputText 
                  v-model="roleFormData.roleId" 
                  readonly 
                  placeholder="등록 시 자동생성"
                  class="w-full bg-gray-50" 
                />
              </div>
            </div>
            
            <!-- 역할명 (입력 가능) -->
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">
                역할명 <span class="text-red-500">*</span>
              </label>
              <div class="col-span-9">
                <InputText 
                  v-model="roleFormData.roleName" 
                  placeholder="역할명을 입력하세요"
                  class="w-full" 
                />
                <small v-if="formErrors.roleName" class="text-red-500">{{ formErrors.roleName }}</small>
              </div>
            </div>
            
            <!-- 역할설명 (입력 가능) -->
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">
                역할설명 <span class="text-red-500">*</span>
              </label>
              <div class="col-span-9">
                <Textarea 
                  v-model="roleFormData.roleDesc" 
                  placeholder="역할설명을 입력하세요"
                  class="w-full" 
                  rows="2"
                />
                <small v-if="formErrors.roleDesc" class="text-red-500">{{ formErrors.roleDesc }}</small>
              </div>
            </div>
            
            <!-- 현재권한수 (표시용) -->
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">현재권한수</label>
              <div class="col-span-9">
                <InputText 
                  :value="selectedPermissions.length" 
                  readonly 
                  class="w-full bg-gray-50" 
                />
              </div>
            </div>
          </div>
          
          <!-- ✅ 개선된 권한 설정 영역 - 계층구조 표시 -->
          <div class="border-t pt-4 mt-4">
            <h5 class="font-semibold mb-3">
              권한 목록 (계층구조) 
              <small class="text-gray-500 font-normal">- 하위권한 선택 시 부모권한 자동선택</small>
            </h5>
            
            <!-- ✅ 계층구조로 표시되는 권한 목록 -->
            <div class="grid grid-cols-1 gap-2 max-h-96 overflow-y-auto border rounded p-3">
              <div v-for="permission in hierarchicalPermissions" :key="permission.id" 
                   class="flex items-start space-x-2 p-2 border rounded hover:bg-gray-50"
                   :class="{
                     'bg-blue-50 border-blue-200': permission.level === 0,
                     'bg-green-50 border-green-200': permission.level === 1,
                     'bg-yellow-50 border-yellow-200': permission.level === 2,
                     'opacity-60': isPermissionDisabled(permission)
                   }"
                   :style="{ marginLeft: `${permission.level * 20}px` }">
                
                <!-- ✅ 계층 구조 표시를 위한 인덴트 및 아이콘 -->
                <div class="flex items-center min-w-0">
                  <!-- 계층 레벨 표시 -->
                  <span v-if="permission.level > 0" class="text-gray-400 text-xs mr-1">
                    {{ '└'.repeat(permission.level) }}
                  </span>
                  
                  <!-- 부모/자식 아이콘 -->
                  <i v-if="permission.isParent" class="pi pi-folder text-blue-500 text-sm mr-1" title="부모 권한"></i>
                  <i v-else class="pi pi-file text-gray-400 text-sm mr-1" title="자식 권한"></i>
                </div>
                
                <!-- ✅ 더 간단한 체크박스 방식 -->
                <input 
                  type="checkbox"
                  :id="`perm-${permission.id}`"
                  :value="permission.id"
                  :checked="selectedPermissions.includes(permission.id)"
                  :disabled="isPermissionDisabled(permission)"
                  @change="(event) => handlePermissionToggle(permission.id, event.target.checked)"
                  class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 focus:ring-2"
                  :class="{
                    'opacity-50 cursor-not-allowed': isPermissionDisabled(permission)
                  }"
                />
                
                <!-- 권한 정보 -->
                <label :for="`perm-${permission.id}`" class="flex-1 cursor-pointer min-w-0">
                  <div class="flex items-center space-x-2">
                    <!-- 권한명 -->
                    <div class="font-medium text-sm" 
                         :class="{
                           'text-blue-700': permission.level === 0,
                           'text-green-700': permission.level === 1,
                           'text-orange-700': permission.level === 2,
                           'text-gray-500': isPermissionDisabled(permission)
                         }">
                      {{ permission.name }}
                    </div>
                    
                    <!-- 레벨 뱃지 -->
                    <span class="text-xs px-2 py-1 rounded-full" 
                          :class="{
                            'bg-blue-100 text-blue-600': permission.level === 0,
                            'bg-green-100 text-green-600': permission.level === 1,
                            'bg-yellow-100 text-yellow-600': permission.level === 2,
                            'bg-gray-100 text-gray-500': permission.level > 2
                          }">
                      L{{ permission.level }}{{ permission.isParent ? ' (부모)' : '' }}
                    </span>
                  </div>
                  
                  <!-- 권한 설명 -->
                  <div class="text-xs text-gray-500 mt-1">{{ permission.description }}</div>
                  
                  <!-- 권한 상세정보 -->
                  <div v-if="permission.icon || permission.parentTo" class="text-xs text-gray-400 mt-1">
                    <span v-if="permission.icon">아이콘: {{ permission.icon }}</span>
                    <span v-if="permission.parentTo"> | 부모: {{ permission.parentTo }}</span>
                  </div>
                </label>
              </div>
            </div>
          
            <!-- ✅ 권한 관련 정보 및 제어 버튼 -->
            <div class="flex justify-between items-center pt-4 border-t">
              <div class="text-sm text-gray-600">
                <div>선택된 권한: {{ selectedPermissions.length }}개 / 전체: {{ allPermissions.length }}개</div>
                <div class="text-xs text-gray-500 mt-1">
                  부모권한: {{ selectedPermissions.filter(id => parentChildMap.has(id)).length }}개 | 
                  자식권한: {{ selectedPermissions.filter(id => childParentMap.has(id)).length }}개
                </div>
              </div>
              <div class="flex gap-2">
                <Button 
                  label="전체선택" 
                  severity="secondary" 
                  outlined 
                  size="small"
                  @click="selectedPermissions = allPermissions.map(p => p.id)" 
                />
                <Button 
                  label="루트만선택" 
                  severity="secondary" 
                  outlined 
                  size="small"
                  @click="selectedPermissions = rootPermissions.map(p => p.id)" 
                />
                <Button 
                  label="권한초기화" 
                  severity="secondary" 
                  outlined 
                  size="small"
                  @click="selectedPermissions = []" 
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>