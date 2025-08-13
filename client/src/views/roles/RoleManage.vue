<script setup>
import { ref, onMounted } from 'vue';
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

// ✅ 선택된 역할 정보
const selectedRole = ref(null);

// ✅ 모든 권한 목록
const allPermissions = ref([]);

// ✅ 선택된 역할의 권한 목록
const rolePermissions = ref([]);

// ✅ 선택된 권한들 (체크박스용)
const selectedPermissions = ref([]);

// ✅ 역할 입력 폼 데이터
const roleFormData = ref({
    roleId: '',
    roleName: '',
    roleDesc: ''
});

// ✅ 편집 모드 (신규/수정 구분)
const isEditMode = ref(false);

// ✅ 폼 유효성 검사
const formErrors = ref({
    roleName: '',
    roleDesc: ''
});

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

// ✅ 폼 유효성 검사
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

// ✅ 새 역할 등록
const createRole = async () => {
    try {
        if (!validateForm()) {
            return;
        }

        const roleData = {
            roleName: roleFormData.value.roleName.trim(),
            roleDesc: roleFormData.value.roleDesc.trim(),
            permissionIds: selectedPermissions.value // 권한도 함께 등록
        };

        console.log('등록할 역할 데이터:', roleData);

        const response = await axios.post(ROLES_API_URL, roleData);

        if (response.data.result_code === 'SUCCESS') {
            const user = await getCurrentUser();
            alert(`역할 "${roleData.roleName}"이(가) 성공적으로 등록되었습니다. (등록자: ${user.empName})`);

            // 폼 초기화 및 목록 새로고침
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

// ✅ 역할 정보 수정
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
            permissionIds: selectedPermissions.value // 권한도 함께 수정
        };

        console.log('수정할 역할 데이터:', roleData);

        const response = await axios.put(`${ROLES_API_URL}/${selectedRole.value.roleId}`, roleData);

        if (response.data.result_code === 'SUCCESS') {
            const user = await getCurrentUser();
            alert(`역할 "${roleData.roleName}"이(가) 성공적으로 수정되었습니다. (수정자: ${user.empName})`);

            // 권한도 별도로 업데이트
            await savePermissions();

            // 폼 초기화 및 목록 새로고침
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

// ✅ 역할 삭제
const deleteRole = async () => {
    try {
        if (!selectedRole.value || !selectedRole.value.roleId) {
            alert('삭제할 역할을 선택해주세요.');
            return;
        }

        // 사원이 사용 중인지 확인
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

            // 폼 초기화 및 목록 새로고침
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

// ✅ 폼 초기화
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
    
    // 역할의 권한 목록 조회
    await loadRolePermissions(role.roleId);
    
    // 현재 역할의 권한을 체크박스에 반영
    selectedPermissions.value = [...rolePermissions.value];

    // ✅ 수정 모드로 자동 전환하고 폼에 데이터 로드
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

            // 데이터 새로고침
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

// 검색 조건 초기화 함수
const handleResetSearchOptions = async () => {
    console.log('검색 조건 초기화');
    
    // 검색 조건 초기화
    filters.value.filters.forEach(filter => {
        filter.value = '';
    });
    
    // 폼 초기화
    resetForm();
    
    // 전체 목록 조회
    await loadRoles();
    
    console.log('검색 조건 초기화 및 전체 목록 조회 완료');
};

// 초기화 함수
const initializeFormData = async () => {
    const user = await getCurrentUser();
    console.log('폼 초기화 시 사용자 정보:', user);
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
    console.log('역할 권한 관리 컴포넌트 마운트 시작...');

    // 모든 권한 목록 먼저 로드
    await loadAllPermissions();

    // 역할 목록 로드
    await loadRoles();

    setTimeout(async () => {
        await initializeFormData();
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
            <!-- ✅ 삭제 버튼만 좌측에 유지 (다른 페이지와 동일한 패턴) -->
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
          <!-- ✅ 역할 기본 정보 입력/표시 -->
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
          
          <!-- ✅ 권한 설정 영역 -->
          <div class="border-t pt-4 mt-4">
            <h5 class="font-semibold mb-3">권한 목록 (체크박스로 선택/해제)</h5>
            <div class="grid grid-cols-1 gap-3 max-h-96 overflow-y-auto border rounded p-3">
              <div v-for="permission in allPermissions" :key="permission.id" 
                   class="flex items-center space-x-2 p-2 border rounded hover:bg-gray-50">
                <Checkbox 
                  v-model="selectedPermissions" 
                  :value="permission.id" 
                  :inputId="`perm-${permission.id}`"
                />
                <label :for="`perm-${permission.id}`" class="flex-1 cursor-pointer">
                  <div class="font-medium text-sm">{{ permission.name }}</div>
                  <div class="text-xs text-gray-500">{{ permission.description }}</div>
                  <div v-if="permission.icon || permission.parentTo" class="text-xs text-gray-400 mt-1">
                    <span v-if="permission.icon">아이콘: {{ permission.icon }}</span>
                    <span v-if="permission.parentTo"> | 상위: {{ permission.parentTo }}</span>
                  </div>
                </label>
              </div>
            </div>
          
            <!-- ✅ 권한 관련 정보 표시 -->
            <div class="flex justify-between items-center pt-4 border-t">
              <div class="text-sm text-gray-600">
                선택된 권한: {{ selectedPermissions.length }}개 / 전체: {{ allPermissions.length }}개
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