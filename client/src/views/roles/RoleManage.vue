<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import axios from '@/service/axios';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Checkbox from 'primevue/checkbox';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

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
      
      const possibleUserSources = [
        userData.user,
        userData,
        userData.employee,
        userData.userInfo,
        userData.loginUser
      ];
      
      for (const userSource of possibleUserSources) {
        if (userSource) {
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

// 입력 폼 (권한 체크박스를 포함하도록 수정)
const inputs = ref({
  title: '역할 권한 설정',
  inputs: [
    { type: 'text', label: '역할ID', placeholder: '역할ID', name: 'roleId', readonly: true },
    { type: 'text', label: '역할명', placeholder: '역할명', name: 'roleName', readonly: true },
    { type: 'text', label: '역할설명', placeholder: '역할설명', name: 'roleDesc', readonly: true },
    { type: 'text', label: '현재권한수', placeholder: '현재 권한 수', name: 'currentPermissionCount', readonly: true }
  ]
});

// StandardInput 컴포넌트 ref
const standardInputRef = ref(null);

// 선택된 역할 정보
const selectedRole = ref(null);

// 모든 권한 목록
const allPermissions = ref([]);

// 선택된 역할의 권한 목록
const rolePermissions = ref([]);

// 선택된 권한들 (체크박스용)
const selectedPermissions = ref([]);

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
    
    console.log('역할 데이터:', roles);
    
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
    console.error('에러 응답:', error.response);
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
        id: perm.PERM_ID || perm.permId,
        name: perm.PERM_NAME || perm.permName,
        description: perm.PERM_DESCRIPTION || perm.permDescription
      }));
      
      console.log('모든 권한 목록:', allPermissions.value);
    }
  } catch (error) {
    console.error('권한 목록 조회 실패:', error);
    alert('권한 목록 조회에 실패했습니다.');
  }
};

// 특정 역할의 권한 목록 조회
const loadRolePermissions = async (roleId) => {
  try {
    const response = await axios.get(`${ROLES_API_URL}/${roleId}/permissions`);
    
    if (response.data.result_code === 'SUCCESS' && response.data.data) {
      rolePermissions.value = response.data.data;
      console.log('역할별 권한 목록:', rolePermissions.value);
    }
  } catch (error) {
    console.error('역할별 권한 조회 실패:', error);
  }
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

// 행 선택 처리 (PrimeVue DataTable 이벤트 형식에 맞게 수정)
const onRowSelect = async (event) => {
  const role = event.data;
  console.log('선택된 역할:', role);
  selectedRole.value = role;
  
  // 역할의 권한 목록 조회
  await loadRolePermissions(role.roleId);
  
  // 현재 역할의 권한을 체크박스에 반영
  selectedPermissions.value = [...rolePermissions.value];
};

// 행 선택 해제 처리 (PrimeVue DataTable 이벤트 형식에 맞게 수정)
const onRowUnselect = (event) => {
  selectedRole.value = null;
  rolePermissions.value = [];
  selectedPermissions.value = []; // 권한 체크박스도 초기화
};

// 권한 설정 저장 (모달 없이 직접 저장)
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
      
      // 폼 업데이트
      if (standardInputRef.value && standardInputRef.value.inputFormRef) {
        const inputFormRef = standardInputRef.value.inputFormRef;
        inputFormRef.inputDatas.currentPermissionCount = selectedPermissions.value.length;
      }
    } else {
      alert(`권한 설정 실패: ${response.data.message || '알 수 없는 오류'}`);
    }
    
  } catch (error) {
    console.error('권한 설정 실패:', error);
    alert('권한 설정 실패: ' + (error.response?.data?.message || error.message));
  }
};

// ✅ 수정된 검색 조건 초기화 함수 - 초기화 후 전체 목록 자동 조회
const handleResetSearchOptions = async () => {
  console.log('검색 조건 초기화');
  
  // 검색 조건 초기화
  filters.value.filters.forEach(filter => {
    filter.value = '';
  });
  
  // 선택된 역할 및 권한 초기화
  selectedRole.value = null;
  rolePermissions.value = [];
  selectedPermissions.value = [];
  
  // 우측 폼 초기화
  if (standardInputRef.value && standardInputRef.value.inputFormRef) {
    standardInputRef.value.inputFormRef.resetInputDatas();
  }
  
  // ✅ 초기화 후 전체 목록 자동 조회
  await loadRoles(); // 빈 객체 전달로 전체 목록 조회
  
  console.log('검색 조건 초기화 및 전체 목록 조회 완료');
};

// 모달 처리 함수
const openSearchModal = (inputName) => {
  console.log('모달 열기:', inputName);
};

// 저장 처리 (권한 설정 직접 저장)
const saveData = async (inputData) => {
  await savePermissions();
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
  <!-- StandardInput 대신 커스텀 레이아웃 사용 -->
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
        <!-- ✅ 조회 버튼 색상을 다른 페이지와 일치시킴 (severity 제거 또는 primary로 변경) -->
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
        <div class="font-semibold text-xl mb-4">{{ header.title }}</div>
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
        <div class="font-semibold text-xl mb-4">역할 권한 설정</div>
        
        <div v-if="!selectedRole" class="text-center text-gray-500 py-8">
          역할을 선택하면 정보가 표시됩니다.
        </div>
        
        <div v-else class="space-y-6">
          <!-- 역할 기본 정보 -->
          <div class="grid grid-cols-2 gap-4">
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">역할ID</label>
              <div class="col-span-9">
                <input type="text" :value="selectedRole.roleId" readonly 
                       class="w-full p-2 border border-gray-300 rounded bg-gray-50" />
              </div>
            </div>
            
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">역할명</label>
              <div class="col-span-9">
                <input type="text" :value="selectedRole.roleName" readonly 
                       class="w-full p-2 border border-gray-300 rounded bg-gray-50" />
              </div>
            </div>
            
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">역할설명</label>
              <div class="col-span-9">
                <input type="text" :value="selectedRole.roleDesc" readonly 
                       class="w-full p-2 border border-gray-300 rounded bg-gray-50" />
              </div>
            </div>
            
            <div class="grid grid-cols-12 gap-2">
              <label class="flex items-center col-span-3">현재권한수</label>
              <div class="col-span-9">
                <input type="text" :value="selectedPermissions.length" readonly 
                       class="w-full p-2 border border-gray-300 rounded bg-gray-50" />
              </div>
            </div>
          </div>
          
          <!-- 권한 설정 영역 -->
          <div class="border-t pt-4">
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
                </label>
              </div>
            </div>
          </div>
          
          <!-- 저장 버튼 -->
          <div class="flex justify-between items-center pt-4 border-t">
            <div class="text-sm text-gray-600">
              선택된 권한: {{ selectedPermissions.length }}개 / 전체: {{ allPermissions.length }}개
            </div>
            <div class="flex gap-2">
              <Button 
                label="초기화" 
                severity="secondary" 
                outlined
                @click="onRowUnselect"
              />
              <Button 
                label="저장" 
                severity="success" 
                @click="savePermissions"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>