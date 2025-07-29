<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from 'axios';

const API_BASE_URL = '/api/products';
const toast = useToast();
const fileUploadRef = ref();

// window.location.origin을 computed로 처리
const baseUrl = computed(() => {
  return typeof window !== 'undefined' ? window.location.origin : '';
});

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
    { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
    { type: 'text', label: '카테고리', value: '', placeholder: '카테고리를 입력하세요', name: 'categoryMain' },
    { type: 'text', label: '세부카테고리', value: '', placeholder: '세부카테고리를 입력하세요', name: 'categorySub' },
    { type: 'text', label: '매장코드', value: '', placeholder: 'OY001, OY002 등', name: 'compId' },
    { type: 'number', label: '입수량', value: '', placeholder: '입수량을 입력하세요', name: 'packQty' },
    { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요', name: 'regUser' },
    { type: 'dateRange', label: '등록일', value: ['', ''], placeholder: '등록일 범위를 선택하세요', name: 'regDateRange' }
  ]
});

const items = ref([]);
const selectedProduct = ref(null);
const selectedProductId = ref('');

const header = ref({
  title: '제품 기준정보 관리',
  header: {
    productId: '제품ID',
    compId: '매장코드', 
    productName: '제품명', 
    categoryMain: '카테고리', 
    categorySub: '세부카테고리',
    vendorName: '브랜드',
    productSpec: '용량/규격',
    unit: '단위',
    packQty: '입수량',
    safetyStock: '안전재고',
    purchasePrice: '구매가격',
    sellPrice: '판매가격',
    status: '상태',
    regUser: '등록자',
    regDate: '등록일',
    updateUser: '수정자',
    updateDate: '수정일',
    productImage: '제품이미지',
    note: '비고'
  },
  rightAligned: ['packQty', 'safetyStock', 'purchasePrice', 'sellPrice']
});

// 폼 데이터를 반응형으로 관리
const formData = ref({
  productId: '',
  compId: '',
  productName: '',
  categoryMain: '',
  categorySub: '',
  vendorName: '',
  productSpec: '',
  unit: '',
  packQty: '',
  safetyStock: '',
  purchasePrice: '',
  sellPrice: '',
  regUser: 'admin',
  regDate: '',
  updateUser: '',
  updateDate: '',
  note: ''
});

// ✅ 단위 옵션 추가
const unitOptions = [
  { name: '개', value: 'ea' },
  { name: '박스', value: 'box' },
  { name: '팩', value: 'pack' },
  { name: 'g', value: 'g' },
  { name: 'ml', value: 'ml' },
];

const inputs = ref({
  title: '제품 등록/수정',
  inputs: [
    { type: 'text', label: '제품ID', placeholder: '자동생성 또는 입력', name: 'productId' },
    { type: 'text', label: '매장코드', placeholder: 'OY001, OY002 등', name: 'compId', required: true },
    { type: 'text', label: '제품명', placeholder: '제품명을 입력하세요', name: 'productName', required: true },
    { type: 'text', label: '카테고리', placeholder: '화장품, 건강식품, 의류 등', name: 'categoryMain', required: true },
    { type: 'text', label: '세부카테고리', placeholder: '스킨케어, 메이크업, 클렌징 등', name: 'categorySub' },
    { type: 'text', label: '브랜드', placeholder: '브랜드명을 입력하세요', name: 'vendorName', required: true },
    { type: 'text', label: '용량/규격', placeholder: '50ml, 30포, 7.5g 등', name: 'productSpec' },
    { type: 'select', label: '단위', placeholder: '단위를 선택하세요', name: 'unit', required: true, options: unitOptions },
    { type: 'number', label: '입수량', placeholder: '박스당 개수', name: 'packQty' },
    { type: 'number', label: '안전재고', placeholder: '최소 재고량', name: 'safetyStock' },
    { type: 'number', label: '구매가격', placeholder: '원가 (원)', name: 'purchasePrice' },
    { type: 'number', label: '판매가격', placeholder: '소비자가격 (원)', name: 'sellPrice' },
    { type: 'text', label: '등록자', placeholder: '등록자 ID', name: 'regUser' },
    { type: 'datetime-local', label: '등록일시', placeholder: '등록일시', name: 'regDate' },
    { type: 'text', label: '수정자', placeholder: '수정자 ID (수정시에만)', name: 'updateUser' },
    { type: 'datetime-local', label: '수정일시', placeholder: '수정일시 (수정시에만)', name: 'updateDate' },
    { type: 'textarea', label: '비고', placeholder: '제품 설명, 특징, 주의사항 등을 상세히 입력하세요', name: 'note' }
  ]
});

const selectedImageFile = ref(null);
const selectedImageFiles = ref([]);
const uploadedImageUrl = ref('');

const onProductSelect = async (product) => {
  if (!product) return;
  
  selectedProduct.value = product;
  selectedProductId.value = product.productId;
  
  await updateFormData(product);
  
  if (product.productImage) {
    uploadedImageUrl.value = product.productImage;
  } else {
    uploadedImageUrl.value = '';
  }
};

// 폼 데이터 업데이트 함수 개선
const updateFormData = async (productData) => {
  try {
    // formData 객체를 직접 업데이트
    Object.keys(formData.value).forEach(key => {
      if (key in productData) {
        let value = productData[key] || '';
        
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

const onRadioChange = (productId) => {
  const product = items.value.find(item => item.productId === productId);
  if (product) {
    onProductSelect(product);
  }
};

const onRowClick = (product) => {
  selectedProductId.value = product.productId;
  onProductSelect(product);
};

const loadProducts = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}`);
    items.value = response.data.map(product => ({
      ...product,
      regDate: product.regDate ? product.regDate : '',
      updateDate: product.updateDate ? product.updateDate : null
    }));
  } catch (error) {
    console.error('제품 목록 조회 실패:', error);
    alert('제품 목록을 불러오는데 실패했습니다.');
    items.value = [];
  }
};

const uploadProductImage = async (file) => {
  if (!file) return null;
  
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await axios.post(`${API_BASE_URL}/upload-image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    if (response.data.success) {
      return response.data.imageUrl;
    } else {
      throw new Error(response.data.message);
    }
  } catch (error) {
    console.error('이미지 업로드 실패:', error);
    throw error;
  }
};

const onFileSelect = (event) => {
  selectedImageFiles.value = event.files;
  if (event.files && event.files.length > 0) {
    selectedImageFile.value = event.files[0];
  }
};

const onFileUpload = async () => {
  if (selectedImageFile.value) {
    try {
      const imageUrl = await uploadProductImage(selectedImageFile.value);
      uploadedImageUrl.value = imageUrl;
      toast.add({ 
        severity: 'success', 
        summary: '성공', 
        detail: '이미지가 성공적으로 업로드되었습니다.', 
        life: 3000 
      });
    } catch (error) {
      toast.add({ 
        severity: 'error', 
        summary: '오류', 
        detail: '이미지 업로드에 실패했습니다.', 
        life: 3000 
      });
    }
  }
};

const manualUpload = () => {
  if (fileUploadRef.value) {
    fileUploadRef.value.upload();
  }
};

const onFileRemove = () => {
  selectedImageFile.value = null;
  selectedImageFiles.value = [];
  uploadedImageUrl.value = '';
};

const onFileClear = () => {
  selectedImageFile.value = null;
  selectedImageFiles.value = [];
  uploadedImageUrl.value = '';
};

const clearForm = () => {
  selectedProduct.value = null;
  selectedProductId.value = '';
  
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
  
  selectedImageFile.value = null;
  selectedImageFiles.value = [];
  uploadedImageUrl.value = '';
  if (fileUploadRef.value) {
    fileUploadRef.value.clear();
  }
};

const searchData = async (searchOptions) => {
  try {
    const params = {};
    
    if (searchOptions.productName && searchOptions.productName.trim() !== '') {
      params.productName = searchOptions.productName.trim();
    }
    
    if (searchOptions.vendorName && searchOptions.vendorName.trim() !== '') {
      params.vendorName = searchOptions.vendorName.trim();
    }
    
    if (searchOptions.categoryMain && searchOptions.categoryMain.trim() !== '') {
      params.categoryMain = searchOptions.categoryMain.trim();
    }
    
    if (searchOptions.categorySub && searchOptions.categorySub.trim() !== '') {
      params.categorySub = searchOptions.categorySub.trim();
    }

    if (searchOptions.compId && searchOptions.compId.trim() !== '') {
      params.compId = searchOptions.compId.trim();
    }
    
    if (searchOptions.packQty && searchOptions.packQty !== '') {
      params.packQty = searchOptions.packQty;
    }
    
    if (searchOptions.regUser && searchOptions.regUser.trim() !== '') {
      params.regUser = searchOptions.regUser.trim();
    }
    
    if (searchOptions.regDateRange && searchOptions.regDateRange.length === 2) {
      params.regDateFrom = searchOptions.regDateRange[0];
      params.regDateTo = searchOptions.regDateRange[1];
    }
    
    const response = await axios.get(`${API_BASE_URL}/search`, { params });
    
    items.value = response.data.map(product => ({
      ...product,
      regDate: product.regDate ? product.regDate : '',
      updateDate: product.updateDate ? product.updateDate : null
    }));
    
  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
};

const saveData = async () => {
  try {
    const requiredFields = [
      { field: 'productName', label: '제품명' },
      { field: 'compId', label: '매장코드' },
      { field: 'categoryMain', label: '카테고리' },
      { field: 'vendorName', label: '브랜드' },
      { field: 'unit', label: '단위' }
    ];
    
    for (const req of requiredFields) {
      if (!formData.value[req.field] || formData.value[req.field].trim() === '') {
        alert(`${req.label}은(는) 필수입력 항목입니다.`);
        return;
      }
    }
    
    let imageUrl = uploadedImageUrl.value;
    
    if (selectedImageFile.value && !uploadedImageUrl.value) {
      try {
        imageUrl = await uploadProductImage(selectedImageFile.value);
        uploadedImageUrl.value = imageUrl;
      } catch (error) {
        const continueWithoutImage = confirm('이미지 업로드에 실패했습니다. 이미지 없이 등록하시겠습니까?');
        if (!continueWithoutImage) {
          return;
        }
        imageUrl = null;
      }
    }
    
    const productData = {
      ...formData.value,
      productImage: imageUrl,
      status: '040002'
    };
    
    if (formData.value.productId && formData.value.productId.trim() !== '') {
      productData.updateUser = 'admin';
      
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      productData.updateDate = `${year}-${month}-${day}T${hours}:${minutes}`;
      
      formData.value.updateUser = 'admin';
      formData.value.updateDate = productData.updateDate;
    }
    
    if (productData.productId) {
      delete productData.productId;
    }
    
    const response = await axios.post(API_BASE_URL, productData);
    
    if (response.data.success) {
      alert('제품이 승인 대기 상태로 등록되었습니다.\n승인 페이지에서 확인해주세요.');
      clearForm();
      await loadProducts();
    } else {
      alert('등록 실패: ' + (response.data.message || '알 수 없는 오류'));
    }
    
  } catch (error) {
    console.error('저장 실패:', error);
    
    if (error.code === 'ERR_NETWORK') {
      alert('네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.');
    } else {
      alert('등록 실패: ' + (error.response?.data?.message || error.message));
    }
  }
};

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

onMounted(() => {
  loadProducts();
  
  // 초기 폼 데이터 설정
  formData.value.regUser = 'admin';
  
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  formData.value.regDate = `${year}-${month}-${day}T${hours}:${minutes}`;
});

</script>

<template>
  <div class="space-y-6">
    <!-- 상단: 조회 조건 -->
    <div class="card p-6">
      <div class="font-semibold text-xl mb-4">{{ filters.title }}</div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 mb-4">
        <div v-for="filter in filters.filters" :key="filter.name" class="flex flex-col">
          <label class="block text-sm font-medium mb-2">{{ filter.label }}</label>
          
          <input
            v-if="filter.type === 'text' || filter.type === 'number'"
            v-model="filter.value"
            :type="filter.type"
            :placeholder="filter.placeholder"
            class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          
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
            loadProducts();
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
      <!-- 좌측: 제품 목록 -->
      <div class="card p-6">
        <div class="font-semibold text-xl mb-4">{{ header.title }}</div>
        <div class="overflow-x-auto">
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
                <tr v-for="item in items" :key="item.productId" class="hover:bg-gray-50 cursor-pointer" 
                    @click="onRowClick(item)">
                  <td class="border border-gray-300 p-2 text-center sticky left-0 bg-white z-10">
                    <input 
                      type="radio" 
                      :name="'product-select'" 
                      :value="item.productId" 
                      v-model="selectedProductId"
                      @change="onRadioChange(item.productId)"
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
                    <span v-else-if="key === 'purchasePrice' || key === 'sellPrice'">
                      {{ item[key] ? item[key].toLocaleString() : '' }}원
                    </span>
                    <span v-else-if="key === 'regDate' || key === 'updateDate'">
                      {{ item[key] ? formatDateTime(item[key]) : '-' }}
                    </span>
                    <span v-else-if="key === 'updateUser'">
                      {{ item[key] || '-' }}
                    </span>
                    <span v-else>
                      {{ item[key] || '' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
      <!-- 우측: 제품 등록/수정 폼 -->
      <div class="card p-6">
        <div class="font-semibold text-xl mb-4">{{ inputs.title }}</div>
        
        <!-- 기본 정보 입력 필드들 -->
        <div class="space-y-4 mb-6">
          <!-- 일반 필드들 (2열 그리드) -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-for="input in inputs.inputs.filter(i => i.type !== 'textarea')" :key="input.name" class="flex flex-col">
              <label class="block text-sm font-medium mb-2">
                {{ input.label }}
                <span v-if="input.required" class="text-red-500">*</span>
              </label>
              
              <!-- ✅ 단위 필드는 select로 렌더링 -->
              <select
                v-if="input.type === 'select'"
                v-model="formData[input.name]"
                class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">{{ input.placeholder }}</option>
                <option v-for="option in input.options" :key="option.value" :value="option.value">
                  {{ option.name }}
                </option>
              </select>
              
              <!-- 일반 입력 필드들 -->
              <input
                v-else-if="input.type === 'text' || input.type === 'number' || input.type === 'datetime-local'"
                v-model="formData[input.name]"
                :type="input.type"
                :placeholder="input.placeholder"
                class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
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
        
        <!-- 제품 이미지 업로드 섹션 -->
        <div class="border-t pt-6">
          <div class="font-medium text-lg mb-4">제품 이미지</div>
          
          <!-- 파일 업로드 영역 -->
          <div class="mb-4">
            <label class="block text-sm font-medium mb-2">이미지 파일</label>
            <Toast />
            <FileUpload 
              ref="fileUploadRef"
              name="productImage" 
              accept="image/*" 
              :maxFileSize="10000000"
              :multiple="false"
              customUpload
              @select="onFileSelect"
              @uploader="onFileUpload"
              @remove="onFileRemove"
              @clear="onFileClear"
              chooseLabel="파일 선택"
              uploadLabel="업로드"
              cancelLabel="취소"
            >
              <template #empty>
                <div class="text-center">
                  <i class="pi pi-cloud-upload text-4xl text-gray-400"></i>
                  <p class="mt-2 text-gray-500">드래그 앤 드롭하거나 클릭해서 이미지를 선택하세요</p>
                </div>
              </template>
            </FileUpload>
            
            <!-- 수동 업로드 버튼 -->
            <div v-if="selectedImageFile && !uploadedImageUrl" class="mt-3">
              <Button 
                label="이미지 업로드" 
                @click="manualUpload" 
                severity="secondary"
                icon="pi pi-upload"
              />
            </div>
          </div>
          
          <!-- 업로드된 이미지 미리보기 -->
          <div v-if="uploadedImageUrl" class="flex flex-col items-center">
            <label class="block text-sm font-medium mb-2">미리보기</label>
            <div class="border rounded-lg p-4 bg-gray-50">
              <img 
                :src="uploadedImageUrl.startsWith('http') ? uploadedImageUrl : `${baseUrl}${uploadedImageUrl}`" 
                alt="제품 이미지 미리보기"
                class="max-w-full max-h-48 object-contain rounded"
              />
              <p class="text-sm text-gray-600 mt-2 text-center">업로드 완료</p>
              <button 
                @click="onFileClear"
                class="mt-2 px-3 py-1 bg-red-500 text-white rounded text-sm hover:bg-red-600 w-full"
              >
                이미지 제거
              </button>
            </div>
          </div>
        </div>
        
        <!-- 저장 및 초기화 버튼 -->
        <div class="flex gap-3 mt-6 pt-4 border-t">
          <Button 
            label="초기화" 
            @click="clearForm" 
            severity="secondary"
            icon="pi pi-refresh"
            class="flex-1"
          />
          <Button 
            label="저장" 
            @click="saveData"
            severity="success"
            icon="pi pi-save"
            class="flex-1"
          />
        </div>
      </div>
    </div>
  </div>
</template>