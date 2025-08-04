<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import Button from 'primevue/button';
import { ref, onMounted, nextTick } from 'vue';
import axios from '@/service/axios';

const API_BASE_URL = '/api/companies';

// 회사 유형 코드 상수 
const COMPANY_TYPES = {
  HEADQUARTERS: '100001', // 본사
  BRANCH: '100002',       // 지점  
  SUPPLIER: '100003'      // 공급업체
};

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요', name: 'compName' },
    { type: 'text', label: '사업자번호', value: '', placeholder: '000-00-00000', name: 'bizNumber' },
    { type: 'text', label: 'CEO명', value: '', placeholder: 'CEO명을 입력하세요', name: 'ceoName' },
    { type: 'text', label: '전화번호', value: '', placeholder: '02-0000-0000', name: 'phone' },
    { type: 'dateRange', label: '등록일 범위', value: ['', ''], placeholder: '등록일 범위를 선택하세요', name: 'dateRange' }
  ]
});

const items = ref([]);
const loading = ref(false);
const selectedBranch = ref(null);
const selectedBranchId = ref('');

const header = ref({
  title: '지점 기준정보 관리',
  header: {
    compId: '지점ID', 
    compName: '지점명', 
    bizNumber: '사업자번호', 
    ceoName: 'CEO명', 
    phone: '전화번호', 
    address: '주소',
    addressDetail: '상세주소',
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
    { type: 'text', label: '등록자', placeholder: '등록자 ID', name: 'regUser' },
    { type: 'datetime-local', label: '등록일시', placeholder: '등록일시', name: 'regDate' },
    { type: 'textarea', label: '비고', placeholder: '특이사항을 입력하세요', name: 'note' }
  ]
});

// 폼 데이터를 반응형으로 관리
const formData = ref({
  compId: '',
  compName: '',
  bizNumber: '',
  ceoName: '',
  phone: '',
  zipcode: '',
  address: '',
  addressDetail: '',
  settleCycle: '',
  settleMgr: '',
  regUser: 'admin',
  regDate: '',
  note: ''
});

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await loadBranches();
  initializeFormData();
});

// 폼 데이터 초기화
const initializeFormData = () => {
  formData.value.regUser = 'admin';
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  formData.value.regDate = `${year}-${month}-${day}T${hours}:${minutes}`;
};

// 지점 목록 조회 - 제품 방식과 동일하게 처리
const loadBranches = async (searchParams = {}) => {
  try {
    loading.value = true;
    console.log('지점 목록 조회 시작...');
    
    // 모든 회사 조회 후 지점만 필터링 (제품 방식과 동일)
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
    
    // 데이터 가공 (제품 방식과 동일)
    items.value = branches.map(item => ({
      ...item,
      address: formatAddress(item.address, item.addressDetail),
      regDate: item.regDate ? formatDateTime(item.regDate) : '',
      updateDate: item.updateDate ? formatDateTime(item.updateDate) : null
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

// 지점 선택 시 폼 데이터 업데이트 (제품 방식과 동일)
const onBranchSelect = async (branch) => {
  if (!branch) return;
  
  selectedBranch.value = branch;
  selectedBranchId.value = branch.compId;
  
  await updateFormData(branch);
};

// 폼 데이터 업데이트 함수 (제품 방식과 동일)
const updateFormData = async (branchData) => {
  try {
    Object.keys(formData.value).forEach(key => {
      if (key in branchData) {
        let value = branchData[key] || '';
        
        // datetime-local 타입을 위한 포맷팅
        if ((key === 'regDate' || key === 'updateDate') && value) {
          value = formatDateTimeForInput(value);
        }
        
        formData.value[key] = String(value);
      }
    });
    
    await nextTick();
    
  } catch (error) {
    console.error('폼 업데이트 중 오류:', error);
  }
};

// 라디오 버튼 변경 시 처리 (제품 방식과 동일)
const onRadioChange = (compId) => {
  const branch = items.value.find(item => item.compId === compId);
  if (branch) {
    onBranchSelect(branch);
  }
};

// 테이블 행 클릭 시 처리 (제품 방식과 동일)
const onRowClick = (branch) => {
  selectedBranchId.value = branch.compId;
  onBranchSelect(branch);
};

// 주소 포맷 함수
const formatAddress = (address, addressDetail) => {
  if (!address) return '';
  return addressDetail ? `${address} ${addressDetail}` : address;
};

// 날짜 포맷 함수들 (제품 방식과 동일)
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return dateString;
  }
};

const formatDateTimeForInput = (dateString) => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    
    if (isNaN(date.getTime())) {
      return dateString;
    }
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

// 폼 초기화 (제품 방식과 동일)
const clearForm = () => {
  selectedBranch.value = null;
  selectedBranchId.value = '';
  
  // formData 초기화
  Object.keys(formData.value).forEach(key => {
    if (key === 'regUser') {
      formData.value[key] = 'admin';
    } else if (key === 'regDate') {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      formData.value[key] = `${year}-${month}-${day}T${hours}:${minutes}`;
    } else {
      formData.value[key] = '';
    }
  });
};

// 검색 실행 (제품 방식과 동일)
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
  if (searchOptions.dateRange && searchOptions.dateRange.length === 2) {
    searchParams.regDateFrom = searchOptions.dateRange[0];
    searchParams.regDateTo = searchOptions.dateRange[1];
  }
  
  await loadBranches(searchParams);
};

// 지점 등록/수정 (제품 방식과 동일)
const saveData = async () => {
  try {
    console.log('저장할 지점 데이터:', formData.value);
    
    // 필수 필드 검증
    const requiredFields = [
      { field: 'compName', label: '지점명' },
      { field: 'bizNumber', label: '사업자번호' },
      { field: 'ceoName', label: 'CEO명' }
    ];
    
    for (const req of requiredFields) {
      if (!formData.value[req.field] || formData.value[req.field].trim() === '') {
        alert(`${req.label}은(는) 필수입력 항목입니다.`);
        return;
      }
    }
    
    // API 호출을 위한 데이터 구성
    const branchData = {
      ...formData.value,
      compType: COMPANY_TYPES.BRANCH // 지점 유형 고정
    };
    
    let response;
    if (formData.value.compId && formData.value.compId.trim() !== '') {
      // 수정 모드
      response = await axios.put(`${API_BASE_URL}/${formData.value.compId}`, branchData);
    } else {
      // 신규 등록 모드
      delete branchData.compId; // 백엔드에서 자동 생성
      response = await axios.post(API_BASE_URL, branchData);
    }
    
    // 응답 처리 (제품 방식과 동일한 구조)
    if (response.data.result_code === 'SUCCESS') {
      alert(formData.value.compId ? '지점이 성공적으로 수정되었습니다.' : '지점이 성공적으로 등록되었습니다.');
      clearForm();
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

</script>

<template>
  <div class="space-y-6">
    <!-- 상단: 조회 조건 -->
    <div class="card p-6">
      <div class="font-semibold text-xl mb-4">{{ filters.title }}</div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 mb-4">
        <div v-for="filter in filters.filters" :key="filter.name" class="flex flex-col">
          <label class="block text-sm font-medium mb-2">{{ filter.label }}</label>
          
          <!-- 텍스트 입력 -->
          <input
            v-if="filter.type === 'text'"
            v-model="filter.value"
            :type="filter.type"
            :placeholder="filter.placeholder"
            class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          
          <!-- 날짜 범위 -->
          <div v-else-if="filter.type === 'dateRange'" class="flex gap-2">
            <input
              v-model="filter.value[0]"
              type="date"
              class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1"
            />
            <span class="self-center">~</span>
            <input
              v-model="filter.value[1]"
              type="date"
              class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1"
            />
          </div>
        </div>
      </div>
      
      <div class="flex justify-center gap-3">
        <Button 
          label="초기화" 
          @click="() => {
            filters.filters.forEach(filter => {
              if (filter.type === 'dateRange') {
                filter.value = ['', ''];
              } else {
                filter.value = '';
              }
            });
            loadBranches();
          }"
          severity="secondary"
        />
        <Button 
          label="조회" 
          @click="() => {
            const searchOptions = {};
            filters.filters.forEach(filter => {
              if (filter.type === 'dateRange') {
                searchOptions[filter.name] = filter.value;
              } else {
                searchOptions[filter.name] = filter.value;
              }
            });
            searchData(searchOptions);
          }"
          severity="success"
        />
      </div>
    </div>
    
    <!-- 하단: 좌우 분할 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <!-- 좌측: 지점 목록 -->
      <div class="card p-6">
        <div class="font-semibold text-xl mb-4">{{ header.title }}</div>
        <div v-if="loading" class="text-center py-4">
          데이터를 불러오는 중...
        </div>
        <div v-else class="overflow-x-auto">
          <div class="min-w-max">
            <table class="w-full border-collapse border border-gray-300">
              <thead>
                <tr class="bg-gray-100">
                  <th class="border border-gray-300 p-2 text-center sticky left-0 bg-gray-100 z-10 min-w-[60px]">선택</th>
                  <th v-for="(headerText, key) in header.header" :key="key" 
                      class="border border-gray-300 p-2 text-center whitespace-nowrap min-w-[100px]">
                    {{ headerText }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in items" :key="item.compId" class="hover:bg-gray-50 cursor-pointer" 
                    @click="onRowClick(item)">
                  <td class="border border-gray-300 p-2 text-center sticky left-0 bg-white z-10">
                    <input 
                      type="radio" 
                      :name="'branch-select'" 
                      :value="item.compId" 
                      v-model="selectedBranchId"
                      @change="onRadioChange(item.compId)"
                    />
                  </td>
                  <td v-for="(headerText, key) in header.header" :key="key" 
                      class="border border-gray-300 p-2 whitespace-nowrap"
                      :class="header.rightAligned?.includes(key) ? 'text-right' : 'text-left'">
                    <span v-if="key === 'note' && item[key]" 
                          class="inline-block max-w-[200px] truncate" 
                          :title="item[key]">
                      {{ item[key] }}
                    </span>
                    <span v-else>
                      {{ item[key] || '' }}
                    </span>
                  </td>
                </tr>
                <tr v-if="items.length === 0">
                  <td :colspan="Object.keys(header.header).length + 1" class="border border-gray-300 p-4 text-center text-gray-500">
                    등록된 지점이 없습니다.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
      <!-- 우측: 지점 등록/수정 폼 -->
      <div class="card p-6">
        <div class="flex justify-between items-center mb-4">
          <div class="font-semibold text-xl">{{ inputs.title }}</div>
          <div class="flex gap-3">
            <Button 
              label="초기화" 
              @click="clearForm" 
              severity="secondary"
              icon="pi pi-refresh"
              size="small"
            />
            <Button 
              label="저장" 
              @click="saveData"
              severity="success"
              icon="pi pi-save"
              size="small"
            />
          </div>
        </div>
        
        <!-- 입력 필드들 -->
        <div class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-for="input in inputs.inputs.filter(i => i.type !== 'textarea')" :key="input.name" class="flex flex-col">
              <label class="block text-sm font-medium mb-2">
                {{ input.label }}
                <span v-if="input.required" class="text-red-500">*</span>
              </label>
              
              <!-- select 필드들 처리 -->
              <select
                v-if="input.type === 'select'"
                v-model="formData[input.name]"
                class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">{{ input.placeholder }}</option>
                <option 
                  v-for="option in input.options" 
                  :key="option.value" 
                  :value="option.value"
                >
                  {{ option.name }}
                </option>
              </select>
              
              <!-- 일반 입력 필드들 -->
              <input
                v-else-if="input.type === 'text' || input.type === 'datetime-local'"
                v-model="formData[input.name]"
                :type="input.type"
                :placeholder="input.placeholder"
                :readonly="input.readonly"
                class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                :class="{ 'bg-gray-100': input.readonly }"
              />
            </div>
          </div>
          
          <!-- 비고 필드 (전체 폭) -->
          <div v-for="input in inputs.inputs.filter(i => i.type === 'textarea')" :key="input.name" class="flex flex-col">
            <label class="block text-sm font-medium mb-2">
              {{ input.label }}
              <span v-if="input.required" class="text-red-500">*</span>
            </label>
            
            <textarea
              v-model="formData[input.name]"
              :placeholder="input.placeholder"
              rows="4"
              class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none w-full"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.space-y-6 > * + * {
  margin-top: 1.5rem;
}

.space-y-4 > * + * {
  margin-top: 1rem;
}

.gap-6 {
  gap: 1.5rem;
}

.gap-4 {
  gap: 1rem;
}

.gap-3 {
  gap: 0.75rem;
}

.gap-2 {
  gap: 0.5rem;
}
</style>