<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

const API_BASE_URL = '/api/products';
const toast = useToast();
const fileUploadRef = ref();

// window.location.origin을 computed로 처리
const baseUrl = computed(() => {
  return typeof window !== 'undefined' ? window.location.origin : '';
});

// ✅ 카테고리 옵션 (명세서 기준)
const categoryMainOptions = [
  { name: '스킨케어', value: '0001', code: '11' },
  { name: '메이크업', value: '0002', code: '11' },
  { name: '클렌징', value: '0003', code: '11' },
  { name: '헤어케어', value: '0004', code: '11' },
  { name: '구강용품', value: '0005', code: '11' },
  { name: '선케어', value: '0006', code: '11' },
  { name: '뷰티소품', value: '0007', code: '11' },
  { name: '건강/기능 식품', value: '0008', code: '11' },
  { name: '푸드', value: '0009', code: '11' }
];

// ✅ 세부카테고리 옵션 (명세서 기준)
const categorySubOptions = {
  '0001': [ // 스킨케어
    { name: '스킨/토너', value: '1001' },
    { name: '에센스/세럼/앰플', value: '1002' },
    { name: '크림', value: '1003' },
    { name: '로션', value: '1004' },
    { name: '미스트/오일', value: '1005' },
    { name: '스킨케어 디바이스', value: '1006' }
  ],
  '0002': [ // 메이크업
    { name: '베이스 메이크업', value: '2001' },
    { name: '아이 메이크업', value: '2002' },
    { name: '치크&컨투어', value: '2003' },
    { name: '립 메이크업', value: '2004' },
    { name: '피니시&픽서', value: '2005' },
    { name: '네일 메이크업', value: '2006' }
  ],
  '0003': [ // 클렌징
    { name: '클렌징폼/젤', value: '3001' },
    { name: '오일/밤', value: '3002' },
    { name: '워터/밀크', value: '3003' },
    { name: '필링&스크럽', value: '3004' },
    { name: '티슈/패드', value: '3005' },
    { name: '립&아이리무버', value: '3006' },
    { name: '클렌징 디바이스', value: '3007' }
  ],
  '0004': [ // 헤어케어
    { name: '샴푸/린스', value: '4001' },
    { name: '트리트먼트/팩', value: '4002' },
    { name: '두피앰플/토닉', value: '4003' },
    { name: '헤어에센스', value: '4004' },
    { name: '염색약/펌', value: '4005' },
    { name: '헤어기기/브러시', value: '4006' },
    { name: '스타일링', value: '4007' }
  ],
  '0005': [ // 구강용품
    { name: '칫솔', value: '5001' },
    { name: '치약', value: '5002' },
    { name: '애프터구강케어', value: '5003' },
    { name: '구강가전', value: '5004' }
  ],
  '0006': [ // 선케어
    { name: '선크림', value: '6001' },
    { name: '선스틱', value: '6002' },
    { name: '선쿠션', value: '6003' },
    { name: '선스프레이/선패치', value: '6004' },
    { name: '태닝/애프터선', value: '6005' }
  ],
  '0007': [ // 뷰티소품
    { name: '메이크업소품', value: '7001' },
    { name: '아이소품', value: '7002' },
    { name: '스킨케어소품', value: '7003' },
    { name: '헤어소품', value: '7004' },
    { name: '네일/바디소품', value: '7005' },
    { name: '뷰티잡화', value: '7006' }
  ],
  '0008': [ // 건강/기능 식품
    { name: '비타민', value: '8001' },
    { name: '영양제', value: '8002' },
    { name: '유산균', value: '8003' },
    { name: '슬리밍/이너뷰티', value: '8004' }
  ],
  '0009': [ // 푸드
    { name: '식단관리/이너뷰티', value: '9001' },
    { name: '과자/초콜릿/디저트', value: '9002' },
    { name: '생수/음료/커피', value: '9003' },
    { name: '간편식/요리', value: '9004' },
    { name: '베이비푸드', value: '9005' }
  ]
};

// ✅ 단위 옵션 (명세서 기준)
const unitOptions = [
  { name: 'ml', value: '0001' },
  { name: 'g', value: '0002' },
  { name: '개', value: '0003' },
  { name: '박스', value: '0004' },
  { name: '팩', value: '0005' }
];

// ✅ 제품 ID 생성 함수
const generateProductId = (categoryMain) => {
  const categoryMap = {
    '0001': '1', // 스킨케어: 100001~199999
    '0002': '2', // 메이크업: 200001~299999
    '0003': '3', // 클렌징: 300001~399999
    '0004': '4', // 헤어케어: 400001~499999
    '0005': '5', // 구강용품: 500001~599999
    '0006': '6', // 선케어: 600001~699999
    '0007': '7', // 뷰티소품: 700001~799999
    '0008': '8', // 건강/기능 식품: 800001~899999
    '0009': '9'  // 푸드: 900001~999999
  };
  
  const prefix = categoryMap[categoryMain];
  if (!prefix) return '';
  
  // 임시로 000001부터 시작 (실제로는 DB에서 마지막 번호 조회 후 +1)
  const sequence = '00001';
  return `PRD${prefix}${sequence}`;
};

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
    { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
    { type: 'select', label: '카테고리', value: '', placeholder: '카테고리를 선택하세요', name: 'categoryMain', options: categoryMainOptions },
    { type: 'select', label: '세부카테고리', value: '', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
    { type: 'text', label: '회사코드', value: '', placeholder: 'OY001, OY002 등', name: 'compId' },
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
    compId: '회사코드', 
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
  note: ''
});

const inputs = ref({
  title: '제품 등록/수정',
  inputs: [
    { type: 'text', label: '제품ID', placeholder: '카테고리 선택 시 자동생성', name: 'productId', readonly: true },
    { type: 'text', label: '회사코드', placeholder: 'OY001, OY002 등', name: 'compId', required: true },
    { type: 'text', label: '제품명', placeholder: '제품명을 입력하세요', name: 'productName', required: true },
    { type: 'select', label: '카테고리', placeholder: '카테고리를 선택하세요', name: 'categoryMain', required: true, options: categoryMainOptions },
    { type: 'select', label: '세부카테고리', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
    { type: 'text', label: '브랜드', placeholder: '브랜드명을 입력하세요', name: 'vendorName', required: true },
    { type: 'text', label: '용량/규격', placeholder: '50ml, 30포, 7.5g 등', name: 'productSpec' },
    { type: 'select', label: '단위', placeholder: '단위를 선택하세요', name: 'unit', required: true, options: unitOptions },
    { type: 'number', label: '입수량', placeholder: '박스당 개수', name: 'packQty' },
    { type: 'number', label: '안전재고', placeholder: '최소 재고량', name: 'safetyStock' },
    { type: 'number', label: '구매가격', placeholder: '원가 (원)', name: 'purchasePrice' },
    { type: 'number', label: '판매가격', placeholder: '소비자가격 (원)', name: 'sellPrice' },
    { type: 'text', label: '등록자', placeholder: '등록자 ID', name: 'regUser' },
    { type: 'datetime-local', label: '등록일시', placeholder: '등록일시', name: 'regDate' },
    { type: 'textarea', label: '비고', placeholder: '제품 설명, 특징, 주의사항 등을 상세히 입력하세요', name: 'note' }
  ]
});

const selectedImageFile = ref(null);
const selectedImageFiles = ref([]);
const uploadedImageUrl = ref('');

// ✅ 선택된 대분류에 따른 세부카테고리 옵션
const filteredCategorySubOptions = computed(() => {
  return categorySubOptions[formData.value.categoryMain] || [];
});

// ✅ 조회 조건의 카테고리에 따른 세부카테고리 옵션
const filteredSearchCategorySubOptions = computed(() => {
  const categoryMainFilter = filters.value.filters.find(f => f.name === 'categoryMain');
  return categorySubOptions[categoryMainFilter?.value] || [];
});

// ✅ 카테고리 변경 시 제품 ID 자동생성 및 세부카테고리 초기화
const onCategoryMainChange = () => {
  // 세부카테고리 초기화
  formData.value.categorySub = '';
  
  // 제품 ID 자동생성
  if (formData.value.categoryMain) {
    formData.value.productId = generateProductId(formData.value.categoryMain);
  } else {
    formData.value.productId = '';
  }
};

// ✅ 조회 조건의 카테고리 변경 시 세부카테고리 초기화
const onSearchCategoryMainChange = () => {
  const categorySubFilter = filters.value.filters.find(f => f.name === 'categorySub');
  if (categorySubFilter) {
    categorySubFilter.value = '';
  }
};

// ✅ 코드를 이름으로 변환하는 함수들
const getCategoryMainName = (code) => {
  const category = categoryMainOptions.find(opt => opt.value === code);
  return category ? category.name : code;
};

const getCategorySubName = (code) => {
  for (const mainCode in categorySubOptions) {
    const subCategory = categorySubOptions[mainCode].find(opt => opt.value === code);
    if (subCategory) return subCategory.name;
  }
  return code;
};

const getUnitName = (code) => {
  const unit = unitOptions.find(opt => opt.value === code);
  return unit ? unit.name : code;
};

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
      { field: 'compId', label: '회사코드' },
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
      status: '040002' // 등록 대기 상태
    };
    
    // 제품 ID가 있으면 수정 모드 (updateUser, updateDate는 백엔드에서 처리)
    if (formData.value.productId && formData.value.productId.trim() !== '') {
      // 수정 시에는 제품 ID를 유지
    } else {
      // 신규 등록 시에는 제품 ID 제거 (백엔드에서 생성)
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
          
          <!-- 텍스트/숫자 입력 -->
          <input
            v-if="filter.type === 'text' || filter.type === 'number'"
            v-model="filter.value"
            :type="filter.type"
            :placeholder="filter.placeholder"
            class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          
          <!-- 셀렉트 박스 -->
          <select
            v-else-if="filter.type === 'select'"
            v-model="filter.value"
            @change="filter.name === 'categoryMain' ? onSearchCategoryMainChange() : null"
            class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">{{ filter.placeholder }}</option>
            <option 
              v-for="option in filter.name === 'categorySub' ? filteredSearchCategorySubOptions : filter.options" 
              :key="option.value" 
              :value="option.value"
            >
              {{ option.name }}
            </option>
          </select>
          
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
                    <span v-else-if="key === 'categoryMain'">
                      {{ getCategoryMainName(item[key]) }}
                    </span>
                    <span v-else-if="key === 'categorySub'">
                      {{ getCategorySubName(item[key]) }}
                    </span>
                    <span v-else-if="key === 'unit'">
                      {{ getUnitName(item[key]) }}
                    </span>
                    <span v-else-if="key === 'purchasePrice' || key === 'sellPrice'">
                      {{ item[key] ? item[key].toLocaleString() : '' }}원
                    </span>
                    <span v-else-if="key === 'regDate'">
                      {{ item[key] ? formatDateTime(item[key]) : '-' }}
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
        <!-- 제목과 버튼을 같은 라인에 배치 -->
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
              label="등록" 
              @click="saveData"
              severity="success"
              icon="pi pi-save"
              size="small"
            />
          </div>
        </div>
        
        <!-- 기본 정보 입력 필드들 -->
        <div class="space-y-4 mb-6">
          <!-- 일반 필드들 (2열 그리드) -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-for="input in inputs.inputs.filter(i => i.type !== 'textarea')" :key="input.name" class="flex flex-col">
              <label class="block text-sm font-medium mb-2">
                {{ input.label }}
                <span v-if="input.required" class="text-red-500">*</span>
              </label>
              
              <!-- ✅ select 필드들 처리 -->
              <select
                v-if="input.type === 'select'"
                v-model="formData[input.name]"
                @change="input.name === 'categoryMain' ? onCategoryMainChange() : null"
                class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">{{ input.placeholder }}</option>
                <!-- 세부카테고리는 필터링된 옵션 사용 -->
                <option 
                  v-for="option in input.name === 'categorySub' ? filteredCategorySubOptions : input.options" 
                  :key="option.value" 
                  :value="option.value"
                >
                  {{ option.name }}
                </option>
              </select>
              
              <!-- 일반 입력 필드들 -->
              <input
                v-else-if="input.type === 'text' || input.type === 'number' || input.type === 'datetime-local'"
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
      </div>
    </div>
  </div>
</template>