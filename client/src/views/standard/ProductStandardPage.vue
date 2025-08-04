<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

// 현재 로그인한 사용자 정보
const currentUser = ref({
  empId: '',
  empName: ''
});

const API_BASE_URL = '/api/products';
const toast = useToast();
const fileUploadRef = ref();

// 현재 로그인한 사용자 정보 가져오기
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      
      currentUser.value = {
        empId: userData.employeeId || userData.empId || userData.emp_id || 'olivin10001',
        empName: userData.empName || userData.emp_name || userData.name || userData.userName || '김홍인'
      };
      
      return currentUser.value;
    } else {
      throw new Error('API 응답에 사용자 데이터가 없습니다');
    }
  } catch (error) {
    // API 실패 시 기본값 사용
    currentUser.value = {
      empId: 'olivin10001',
      empName: '김홍인'
    };
    return currentUser.value;
  }
};

// window.location.origin을 computed로 처리
const baseUrl = computed(() => {
  return typeof window !== 'undefined' ? window.location.origin : '';
});

/* Modal functions */

// 회사코드 모달의 visible 상태를 관리하는 ref 변수
const companyModalVisible = ref(false);

// 회사코드 모달창의 테이블 헤더 정보 (공급업체 기준정보와 동일하게)
const companyModalHeaders = ref([
  { field: 'compId', header: '업체ID' },
  { field: 'compName', header: '업체명' },
  { field: 'bizNumber', header: '사업자번호' },
  { field: 'ceoName', header: 'CEO명' },
  { field: 'phoneNumber', header: '전화번호' },
  { field: 'address', header: '주소' },
]);

// 모달창의 데이터 아이템
const companyModalItems = ref([]);

// 모달창 닫기 함수
const closeCompanyModal = () => {
  companyModalVisible.value = false;
};

// 회사코드 모달창 확인 버튼 클릭 시 호출되는 함수
const confirmCompanyModal = async (selectedItems) => {
  // 선택된 회사 정보를 폼 데이터에 반영
  if (selectedItems && selectedItems.compId) {
    // COMP_ID 설정
    formData.value.compId = selectedItems.compId;
    
    // VENDOR_NAME 설정 (COMP_NAME을 브랜드명으로 사용)
    formData.value.vendorName = selectedItems.compName;
    
    toast.add({ 
      severity: 'success', 
      summary: '성공', 
      detail: `회사 "${selectedItems.compName}" 선택 완료`, 
      life: 3000 
    });
  }
  
  companyModalVisible.value = false;
};

// 회사코드 모달을 열 때 호출되는 함수
const loadCompanyOnClick = () => {
  companyModalVisible.value = true;
};

const searchModal = (searchValue) => {
  // 검색 로직 구현
};

/* end of Modal functions */

// 카테고리 옵션 (6자리 데이터베이스 코드 기준)
const categoryMainOptions = [
  { name: '스킨케어', value: '110001', code: '11' },
  { name: '메이크업', value: '110002', code: '11' },
  { name: '클렌징', value: '110003', code: '11' },
  { name: '헤어케어', value: '110004', code: '11' },
  { name: '구강용품', value: '110005', code: '11' },
  { name: '선케어', value: '110006', code: '11' },
  { name: '뷰티소품', value: '110007', code: '11' },
  { name: '건강/기능 식품', value: '110008', code: '11' },
  { name: '푸드', value: '110009', code: '11' }
];

// 세부카테고리 옵션 (6자리 데이터베이스 코드 기준)
const categorySubOptions = {
  '110001': [ // 스킨케어
    { name: '스킨/토너', value: '121001' },
    { name: '에센스/세럼/앰플', value: '121002' },
    { name: '크림', value: '121003' },
    { name: '로션', value: '121004' },
    { name: '미스트/오일', value: '121005' },
    { name: '스킨케어 디바이스', value: '121006' }
  ],
  '110002': [ // 메이크업
    { name: '베이스 메이크업', value: '122001' },
    { name: '아이 메이크업', value: '122002' },
    { name: '치크&컨투어', value: '122003' },
    { name: '립 메이크업', value: '122004' },
    { name: '피니시&픽서', value: '122005' },
    { name: '네일 메이크업', value: '122006' }
  ],
  '110003': [ // 클렌징
    { name: '클렌징폼/젤', value: '123001' },
    { name: '오일/밤', value: '123002' },
    { name: '워터/밀크', value: '123003' },
    { name: '필링&스크럽', value: '123004' },
    { name: '티슈/패드', value: '123005' },
    { name: '립&아이리무버', value: '123006' },
    { name: '클렌징 디바이스', value: '123007' }
  ],
  '110004': [ // 헤어케어
    { name: '샴푸/린스', value: '124001' },
    { name: '트리트먼트/팩', value: '124002' },
    { name: '두피앰플/토닉', value: '124003' },
    { name: '헤어에센스', value: '124004' },
    { name: '염색약/펌', value: '124005' },
    { name: '헤어기기/브러시', value: '124006' },
    { name: '스타일링', value: '124007' }
  ],
  '110005': [ // 구강용품
    { name: '칫솔', value: '125001' },
    { name: '치약', value: '125002' },
    { name: '애프터구강케어', value: '125003' },
    { name: '구강가전', value: '125004' }
  ],
  '110006': [ // 선케어
    { name: '선크림', value: '126001' },
    { name: '선스틱', value: '126002' },
    { name: '선쿠션', value: '126003' },
    { name: '선스프레이/선패치', value: '126004' },
    { name: '태닝/애프터선', value: '126005' }
  ],
  '110007': [ // 뷰티소품
    { name: '메이크업소품', value: '127001' },
    { name: '아이소품', value: '127002' },
    { name: '스킨케어소품', value: '127003' },
    { name: '헤어소품', value: '127004' },
    { name: '네일/바디소품', value: '127005' },
    { name: '뷰티잡화', value: '127006' }
  ],
  '110008': [ // 건강/기능 식품
    { name: '비타민', value: '128001' },
    { name: '영양제', value: '128002' },
    { name: '유산균', value: '128003' },
    { name: '슬리밍/이너뷰티', value: '128004' }
  ],
  '110009': [ // 푸드
    { name: '식단관리/이너뷰티', value: '129001' },
    { name: '과자/초콜릿/디저트', value: '129002' },
    { name: '생수/음료/커피', value: '129003' },
    { name: '간편식/요리', value: '129004' },
    { name: '베이비푸드', value: '129005' }
  ]
};

// 단위 옵션 (6자리 데이터베이스 코드 기준)
const unitOptions = [
  { name: 'ml', value: '130001' },
  { name: 'g', value: '130002' },
  { name: 'ea', value: '130003' },
  { name: 'box', value: '130004' },
  { name: 'pack', value: '130005' }
];

const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
    { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
    { type: 'select', label: '카테고리', value: '', placeholder: '카테고리를 선택하세요', name: 'categoryMain', options: categoryMainOptions },
    { type: 'select', label: '세부카테고리', value: '', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
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
    productName: '제품명', 
    compId: '회사코드', 
    vendorName: '브랜드',
    categoryMain: '카테고리', 
    categorySub: '세부카테고리',
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
  regUser: '', // 현재 로그인한 사용자의 empId로 자동 설정
  regDate: '',
  note: ''
});

const inputs = ref({
  title: '제품 등록/수정',
  inputs: [
    { type: 'text', label: '제품ID', placeholder: '등록 시 자동생성됩니다', name: 'productId', readonly: true },
    { type: 'text', label: '제품명', placeholder: '제품명을 입력하세요', name: 'productName', required: true },
    { type: 'text', label: '회사코드', placeholder: '회사선택 필수', name: 'compId', required: true, readonly: true },
    { type: 'text-with-button', label: '브랜드', placeholder: '회사 선택시 자동 입력', name: 'vendorName', required: true, readonly: true, buttonLabel: '회사선택', buttonAction: 'loadCompany' },
    { type: 'select', label: '카테고리', placeholder: '카테고리를 선택하세요', name: 'categoryMain', required: true, options: categoryMainOptions },
    { type: 'select', label: '세부카테고리', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
    { type: 'text', label: '용량/규격', placeholder: '50ml, 30포, 7.5g 등', name: 'productSpec' },
    { type: 'select', label: '단위', placeholder: '단위를 선택하세요', name: 'unit', required: true, options: unitOptions },
    { type: 'number', label: '입수량', placeholder: '박스당 개수', name: 'packQty' },
    { type: 'number', label: '안전재고', placeholder: '최소 재고량', name: 'safetyStock' },
    { type: 'number', label: '구매가격', placeholder: '원가 (원)', name: 'purchasePrice' },
    { type: 'number', label: '판매가격', placeholder: '소비자가격 (원)', name: 'sellPrice' },
    { type: 'text', label: '등록자', placeholder: '현재 로그인 사용자 자동 설정', name: 'regUser', readonly: true },
    { type: 'text', label: '등록일', placeholder: '2024-01-01 형식으로 입력하세요', name: 'regDate' },
    { type: 'textarea', label: '비고', placeholder: '제품 설명, 특징, 주의사항 등을 상세히 입력하세요', name: 'note' }
  ]
});

const selectedImageFile = ref(null);
const selectedImageFiles = ref([]);
const uploadedImageUrl = ref('');

// 선택된 대분류에 따른 세부카테고리 옵션
const filteredCategorySubOptions = computed(() => {
  return categorySubOptions[formData.value.categoryMain] || [];
});

// 조회 조건의 카테고리에 따른 세부카테고리 옵션
const filteredSearchCategorySubOptions = computed(() => {
  const categoryMainFilter = filters.value.filters.find(f => f.name === 'categoryMain');
  return categorySubOptions[categoryMainFilter?.value] || [];
});

// 카테고리 변경 시 세부카테고리 초기화만 (제품 ID는 등록 시에만 생성)
const onCategoryMainChange = () => {
  // 세부카테고리 초기화
  formData.value.categorySub = '';
  
  // 제품 ID는 등록 버튼 클릭 시에만 생성되므로 여기서는 처리하지 않음
  formData.value.productId = '';
};

// 조회 조건의 카테고리 변경 시 세부카테고리 초기화
const onSearchCategoryMainChange = () => {
  const categorySubFilter = filters.value.filters.find(f => f.name === 'categorySub');
  if (categorySubFilter) {
    categorySubFilter.value = '';
  }
};

// 버튼 액션 핸들러
const handleButtonAction = (action) => {
  switch(action) {
    case 'loadCompany':
      loadCompanyOnClick();
      break;
    default:
      break;
  }
};

// 코드를 이름으로 변환하는 함수들 (6자리 코드 기준)
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

// 상태 코드를 이름으로 변환하는 함수 (6자리 코드 기준)
const getStatusName = (code) => {
  const statusMap = {
    '040001': '완료',
    '040002': '대기', 
    '040003': '반려'
  };
  return statusMap[code] || code;
};

// 제품 선택 시 폼 데이터 업데이트
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

// 폼 데이터 업데이트 함수
const updateFormData = async (productData) => {
  try {
    // formData 객체를 직접 업데이트
    Object.keys(formData.value).forEach(key => {
      if (key in productData) {
        let value = productData[key] || '';
        
        // regDate는 문자열로 처리 (입력형)
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

// 라디오 버튼 변경 시 처리
const onRadioChange = (productId) => {
  const product = items.value.find(item => item.productId === productId);
  if (product) {
    onProductSelect(product);
  }
};

// 테이블 행 클릭 시 처리
const onRowClick = (product) => {
  selectedProductId.value = product.productId;
  onProductSelect(product);
};

// 제품 목록 로드
const loadProducts = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}`);
    items.value = response.data.map(product => ({
      ...product,
      regDate: product.regDate ? product.regDate : '',
      updateDate: product.updateDate ? product.updateDate : null
    }));
  } catch (error) {
    toast.add({ 
      severity: 'error', 
      summary: '오류', 
      detail: '제품 목록을 불러오는데 실패했습니다.', 
      life: 3000 
    });
    items.value = [];
  }
};

// 회사 목록 로드
const loadCompanyData = async () => {
  try {
    const result = await axios.get('/api/companies');
    
    // API 응답에서 data 필드 추출
    const companies = result.data.data || result.data;
    
    // 공급업체만 필터링 (compType이 '100003'인 것만)
    const suppliers = companies.filter(item => item.compType === '100003');
    
    companyModalItems.value = suppliers.map((item) => {
      return {
        ...item,
        // DB 컬럼명에 맞춰 매핑 (COMP_ID, COMP_NAME)
        compId: item.compId || item.comp_id || item.COMP_ID,
        compName: item.compName || item.comp_name || item.COMP_NAME,
        phoneNumber: item.phoneNumber || item.phone_number || item.phone || item.telNumber || '',
        address: item.address || item.addr || item.compAddress || '',
        compType: item.compType || item.comp_type || item.type || '',
        bizNumber: item.bizNumber || item.biz_number || '',
        ceoName: item.ceoName || item.ceo_name || '',
      }
    });
  } catch (e) {
    toast.add({ 
      severity: 'error', 
      summary: '오류', 
      detail: '공급업체 데이터를 불러오는데 실패했습니다.', 
      life: 3000 
    });
  }
};

// 제품 이미지 업로드
const uploadProductImage = async (file) => {
  if (!file) return null;
  
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    // 백엔드 API 호출
    const response = await axios.post(`${API_BASE_URL}/upload-image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    // ProductController의 응답 구조에 맞춰 처리
    if (response.data.success && response.data.imageUrl) {
      return response.data.imageUrl;
    } else {
      throw new Error(response.data.message || '이미지 업로드 실패');
    }
  } catch (error) {
    // 구체적인 에러 메시지
    if (error.response?.status === 404) {
      throw new Error('이미지 업로드 API를 찾을 수 없습니다.');
    } else if (error.response?.status === 413) {
      throw new Error('파일 크기가 너무 큽니다. 10MB 이하의 파일을 선택해주세요.');
    } else if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    } else {
      throw new Error('이미지 업로드 중 오류가 발생했습니다.');
    }
  }
};

// 파일 선택 시 처리
const onFileSelect = (event) => {
  selectedImageFiles.value = event.files;
  if (event.files && event.files.length > 0) {
    selectedImageFile.value = event.files[0];
  }
};

// 파일 업로드 처리
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
        detail: '이미지 업로드에 실패했습니다: ' + error.message, 
        life: 3000 
      });
    }
  }
};

// 수동 업로드
const manualUpload = () => {
  if (fileUploadRef.value) {
    fileUploadRef.value.upload();
  }
};

// 파일 제거
const onFileRemove = () => {
  selectedImageFile.value = null;
  selectedImageFiles.value = [];
  uploadedImageUrl.value = '';
};

// 파일 클리어
const onFileClear = () => {
  selectedImageFile.value = null;
  selectedImageFiles.value = [];
  uploadedImageUrl.value = '';
};

// 폼 초기화
const clearForm = async () => {
  selectedProduct.value = null;
  selectedProductId.value = '';
  
  // 현재 사용자 정보 가져오기
  const user = await getCurrentUser();
  
  // formData 초기화
  Object.keys(formData.value).forEach(key => {
    if (key === 'regUser') {
      formData.value[key] = user.empId; // empId를 저장하되 화면에는 empName 표시
    } else if (key === 'regDate') {
      // 현재 날짜를 YYYY-MM-DD 형식으로 설정
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      formData.value[key] = `${year}-${month}-${day}`;
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

// 검색 처리
const searchData = async (searchOptions) => {
  try {
    const params = {};
    
    // 검색 파라미터 설정
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
    toast.add({ 
      severity: 'error', 
      summary: '오류', 
      detail: '검색에 실패했습니다.', 
      life: 3000 
    });
  }
};

// 데이터 저장 (등록 버튼 클릭 시 제품 ID 자동 생성)
const saveData = async () => {
  try {
    // 필수 필드 검증
    const requiredFields = [
      { field: 'productName', label: '제품명' },
      { field: 'compId', label: '회사코드' },
      { field: 'categoryMain', label: '카테고리' },
      { field: 'vendorName', label: '브랜드' },
      { field: 'unit', label: '단위' }
    ];
    
    for (const req of requiredFields) {
      if (!formData.value[req.field] || formData.value[req.field].trim() === '') {
        toast.add({ 
          severity: 'error', 
          summary: '검증 오류', 
          detail: `${req.label}은(는) 필수입력 항목입니다.`, 
          life: 3000 
        });
        return;
      }
    }
    
    let imageUrl = uploadedImageUrl.value;
    
    // 이미지 업로드 처리 (선택사항)
    if (selectedImageFile.value && !uploadedImageUrl.value) {
      try {
        imageUrl = await uploadProductImage(selectedImageFile.value);
        uploadedImageUrl.value = imageUrl;
      } catch (error) {
        // 사용자에게 선택권 제공
        const continueWithoutImage = confirm(`이미지 업로드에 실패했습니다.\n오류: ${error.message}\n\n이미지 없이 제품을 등록하시겠습니까?`);
        if (!continueWithoutImage) {
          return;
        }
        imageUrl = null;
      }
    }
    
    // 등록일 처리 (날짜 문자열을 Date 객체로 변환)
    let regDate = null;
    if (formData.value.regDate && formData.value.regDate.trim() !== '') {
      try {
        // "2024-01-01" 형식을 Date 객체로 변환
        const dateStr = formData.value.regDate.trim();
        regDate = new Date(dateStr + 'T00:00:00'); // 시간은 00:00:00으로 설정
        
        // 유효한 날짜인지 확인
        if (isNaN(regDate.getTime())) {
          throw new Error('유효하지 않은 날짜 형식');
        }
      } catch (error) {
        toast.add({ 
          severity: 'error', 
          summary: '검증 오류', 
          detail: '등록일 형식이 올바르지 않습니다. (예: 2024-01-01)', 
          life: 3000 
        });
        return;
      }
    }
    
    // 선택된 제품이 있으면 수정 모드, 없으면 신규 등록
    const isUpdateMode = selectedProduct.value && selectedProductId.value;
    
    // 기본 제품 데이터 구성
    const productData = {
      ...formData.value,
      productImage: imageUrl || null,
      status: '040002' // 등록 대기 상태 (6자리 코드)
    };
    
    let response;
    
    if (isUpdateMode) {
      // 수정 모드 - update_user와 update_date 추가
      const currentUserData = await getCurrentUser();
      const now = new Date();
      
      productData.productId = selectedProductId.value;
      productData.updateUser = currentUserData.empId; // 수정자 ID
      productData.updateDate = now; // 수정일시
      productData.regDate = regDate; // 등록일은 기존 값 유지
      
      response = await axios.put(`${API_BASE_URL}/${selectedProductId.value}`, productData);
    } else {
      // 신규 등록 모드 - 서버에서 제품 ID 자동 생성
      productData.regDate = regDate; // 등록일
      delete productData.productId; // 백엔드에서 자동 생성
      
      response = await axios.post(API_BASE_URL, productData);
    }
    
    // ProductController의 응답 구조에 맞춰 처리
    if (response.data.success) {
      toast.add({ 
        severity: 'success', 
        summary: '성공', 
        detail: isUpdateMode ? '제품이 성공적으로 수정되었습니다.' : `제품이 성공적으로 등록되었습니다. (제품ID: ${response.data.productId})`, 
        life: 3000 
      });
      clearForm();
      await loadProducts();
    } else {
      toast.add({ 
        severity: 'error', 
        summary: '저장 실패', 
        detail: response.data.message || '알 수 없는 오류가 발생했습니다.', 
        life: 5000 
      });
    }
    
  } catch (error) {
    let errorMessage = '저장 중 오류가 발생했습니다.';
    
    if (error.code === 'ERR_NETWORK') {
      errorMessage = '네트워크 오류: 서버 연결을 확인해주세요.';
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    toast.add({ 
      severity: 'error', 
      summary: '저장 실패', 
      detail: errorMessage, 
      life: 5000 
    });
  }
};

// 날짜 포맷팅 함수들
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
    
    return `${year}-${month}-${day}`;
  } catch (error) {
    return dateString;
  }
};

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  try {
    // 병렬로 초기 데이터 로드
    await Promise.all([
      loadProducts(),
      loadCompanyData(),
      getCurrentUser()
    ]);
    
    // 현재 로그인한 사용자 정보 설정 (empId를 저장)
    formData.value.regUser = currentUser.value.empId;
    
    // 현재 날짜를 YYYY-MM-DD 형식으로 설정
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    formData.value.regDate = `${year}-${month}-${day}`;
    
  } catch (error) {
    toast.add({ 
      severity: 'error', 
      summary: '초기화 오류', 
      detail: '페이지 초기화 중 오류가 발생했습니다.', 
      life: 3000 
    });
  }
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
        <div class="overflow-auto max-h-[1000px] border border-gray-200 rounded">
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
                    <span v-else-if="key === 'status'">
                      {{ getStatusName(item[key]) }}
                    </span>
                    <span v-else-if="key === 'purchasePrice' || key === 'sellPrice'">
                      {{ item[key] ? item[key].toLocaleString() : '' }}원
                    </span>
                    <span v-else-if="key === 'regDate'">
                      {{ item[key] ? formatDate(item[key]) : '-' }}
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
              label="저장" 
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
              
              <!-- select 필드들 처리 -->
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
              
              <!-- 버튼이 있는 텍스트 입력 필드 -->
              <div v-else-if="input.type === 'text-with-button'" class="flex gap-2">
                <input
                  v-model="formData[input.name]"
                  type="text"
                  :placeholder="input.placeholder"
                  :readonly="input.readonly"
                  class="flex-1 p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                  :class="{ 'bg-gray-100': input.readonly }"
                />
                <Button 
                  :label="input.buttonLabel" 
                  @click="handleButtonAction(input.buttonAction)"
                  severity="info"
                  size="small"
                  class="min-w-fit whitespace-nowrap"
                  outlined
                />
              </div>
              
              <!-- 등록자 필드 - 특별 처리 (사용자명만 표시) -->
              <input
                v-else-if="input.name === 'regUser'"
                :value="currentUser.empName || '사용자'"
                type="text"
                :placeholder="input.placeholder"
                readonly
                class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 bg-gray-100"
              />
              
              <!-- 일반 입력 필드들 -->
              <input
                v-else
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

    <!-- 회사코드 선택 모달 -->
    <DialogModal 
      title="공급업체 검색" 
      :display="companyModalVisible" 
      :headers="companyModalHeaders" 
      :items="companyModalItems" 
      :selectionMode="'single'" 
      @close="closeCompanyModal" 
      @confirm="confirmCompanyModal" 
      @search-modal="searchModal"
    />
  </div>
</template>