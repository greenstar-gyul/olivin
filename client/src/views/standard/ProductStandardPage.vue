<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';

// 현재 로그인한 사용자 정보
const currentUser = ref({
  empId: '',
  employeeId: '',
  empName: ''
});

const API_BASE_URL = '/api/products';
const toast = useToast();

// window.location.origin을 computed로 처리
const baseUrl = computed(() => {
  return typeof window !== 'undefined' ? window.location.origin : '';
});

// 사용자 정보 가져오기 함수
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      
      let employeeId = 'olivin10001';
      let empName = '김홍인';
      
      const possibleUserSources = [
        userData.user,
        userData,
        userData.employee,
        userData.userInfo,
        userData.loginUser
      ];
      
      for (const userSource of possibleUserSources) {
        if (userSource && typeof userSource === 'object') {
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
          
          const foundEmployeeId = possibleEmployeeIds.find(id => id && String(id).trim() !== '');
          const foundEmpName = possibleEmpNames.find(name => name && String(name).trim() !== '');
          
          if (foundEmployeeId) employeeId = String(foundEmployeeId).trim();
          if (foundEmpName) empName = String(foundEmpName).trim();
          
          if (foundEmployeeId && foundEmpName) break;
        }
      }
      
      currentUser.value = {
        empId: employeeId,
        employeeId: employeeId,
        empName: empName
      };
      
      return currentUser.value;
    }
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error);
    currentUser.value = {
      empId: 'olivin10001',
      employeeId: 'olivin10001',
      empName: '김홍인'
    };
    return currentUser.value;
  }
};

// 회사 모달 관련
const companyModalVisible = ref(false);
const companyModalHeaders = ref([
  { field: 'compId', header: '업체ID' },
  { field: 'compName', header: '업체명' },
  { field: 'bizNumber', header: '사업자번호' },
  { field: 'ceoName', header: 'CEO명' },
  { field: 'phoneNumber', header: '전화번호' },
  { field: 'address', header: '주소' },
]);
const companyModalItems = ref([]);

// 카테고리 옵션
const categoryMainOptions = [
  { name: '스킨케어', value: '110001' },
  { name: '메이크업', value: '110002' },
  { name: '클렌징', value: '110003' },
  { name: '헤어케어', value: '110004' },
  { name: '구강용품', value: '110005' },
  { name: '선케어', value: '110006' },
  { name: '뷰티소품', value: '110007' },
  { name: '건강/기능 식품', value: '110008' },
  { name: '푸드', value: '110009' }
];

const categorySubOptions = {
  '110001': [
    { name: '스킨/토너', value: '121001' },
    { name: '에센스/세럼/앰플', value: '121002' },
    { name: '크림', value: '121003' },
    { name: '로션', value: '121004' },
    { name: '미스트/오일', value: '121005' },
    { name: '스킨케어 디바이스', value: '121006' }
  ],
  '110002': [
    { name: '베이스 메이크업', value: '122001' },
    { name: '아이 메이크업', value: '122002' },
    { name: '치크&컨투어', value: '122003' },
    { name: '립 메이크업', value: '122004' },
    { name: '피니시&픽서', value: '122005' },
    { name: '네일 메이크업', value: '122006' }
  ],
  '110003': [
    { name: '클렌징폼/젤', value: '123001' },
    { name: '오일/밤', value: '123002' },
    { name: '워터/밀크', value: '123003' },
    { name: '필링&스크럽', value: '123004' },
    { name: '티슈/패드', value: '123005' },
    { name: '립&아이리무버', value: '123006' },
    { name: '클렌징 디바이스', value: '123007' }
  ],
  '110004': [
    { name: '샴푸/린스', value: '124001' },
    { name: '트리트먼트/팩', value: '124002' },
    { name: '두피앰플/토닉', value: '124003' },
    { name: '헤어에센스', value: '124004' },
    { name: '염색약/펌', value: '124005' },
    { name: '헤어기기/브러시', value: '124006' },
    { name: '스타일링', value: '124007' }
  ],
  '110005': [
    { name: '칫솔', value: '125001' },
    { name: '치약', value: '125002' },
    { name: '애프터구강케어', value: '125003' },
    { name: '구강가전', value: '125004' }
  ],
  '110006': [
    { name: '선크림', value: '126001' },
    { name: '선스틱', value: '126002' },
    { name: '선쿠션', value: '126003' },
    { name: '선스프레이/선패치', value: '126004' },
    { name: '태닝/애프터선', value: '126005' }
  ],
  '110007': [
    { name: '메이크업소품', value: '127001' },
    { name: '아이소품', value: '127002' },
    { name: '스킨케어소품', value: '127003' },
    { name: '헤어소품', value: '127004' },
    { name: '네일/바디소품', value: '127005' },
    { name: '뷰티잡화', value: '127006' }
  ],
  '110008': [
    { name: '비타민', value: '128001' },
    { name: '영양제', value: '128002' },
    { name: '유산균', value: '128003' },
    { name: '슬리밍/이너뷰티', value: '128004' }
  ],
  '110009': [
    { name: '식단관리/이너뷰티', value: '129001' },
    { name: '과자/초콜릿/디저트', value: '129002' },
    { name: '생수/음료/커피', value: '129003' },
    { name: '간편식/요리', value: '129004' },
    { name: '베이비푸드', value: '129005' }
  ]
};

const unitOptions = [
  { name: 'ml', value: '130001' },
  { name: 'g', value: '130002' },
  { name: 'ea', value: '130003' },
  { name: 'box', value: '130004' },
  { name: 'pack', value: '130005' }
];

// ✅ 검색 조건 (원래 구조 복원)
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
    { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
    { type: 'select', label: '카테고리', value: '', placeholder: '카테고리를 선택하세요', name: 'categoryMain', options: categoryMainOptions },
    { type: 'select', label: '세부카테고리', value: '', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
    { type: 'number', label: '입수량', value: '', placeholder: '입수량을 입력하세요', name: 'packQty' },
    { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요', name: 'regUser' },
    { type: 'dateRange', label: '등록일', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'regDateRange' }
  ]
});

const items = ref([]);
const selectedProduct = ref(null);

// 테이블 헤더
const header = ref({
  title: '제품 기준정보 관리',
  header: {
    productId: '제품ID',
    productName: '제품명', 
    vendorName: '브랜드',
    categoryMain: '카테고리', 
    categorySub: '세부카테고리',
    productSpec: '용량',
    unit: '단위',
    packQty: '입수량',
    safetyStock: '안전재고',
    purchasePrice: '구매가격',
    sellPrice: '판매가격',
  },
  rightAligned: ['packQty', 'safetyStock', 'purchasePrice', 'sellPrice']
});

// 입력 폼
const inputs = ref({
  title: '제품 등록/수정',
  inputs: [
    { type: 'text', label: '제품ID', placeholder: '등록 시 자동생성됩니다', name: 'productId', readonly: true },
    { type: 'text', label: '제품명', placeholder: '제품명을 입력하세요', name: 'productName', required: true },
    { type: 'text', label: '회사코드', placeholder: '회사선택 필수', name: 'compId', required: true, readonly: true },
    { type: 'item-search', label: '브랜드', placeholder: '회사 선택시 자동 입력', name: 'vendorName', required: true },
    { type: 'select', label: '카테고리', placeholder: '카테고리를 선택하세요', name: 'categoryMain', required: true, options: categoryMainOptions },
    { type: 'select', label: '세부카테고리', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
    { type: 'text', label: '용량', placeholder: '50ml, 30포, 7.5g 등', name: 'productSpec' },
    { type: 'select', label: '단위', placeholder: '단위를 선택하세요', name: 'unit', required: true, options: unitOptions },
    { type: 'number', label: '입수량', placeholder: '박스당 개수', name: 'packQty' },
    { type: 'number', label: '안전재고', placeholder: '최소 재고량', name: 'safetyStock' },
    { type: 'number', label: '구매가격', placeholder: '원가 (원)', name: 'purchasePrice' },
    { type: 'number', label: '판매가격', placeholder: '소비자가격 (원)', name: 'sellPrice' },
    { type: 'text', label: '등록자', placeholder: '현재 로그인 사용자 자동 설정', name: 'regUser', readonly: true },
    { type: 'text', label: '등록일', placeholder: '2024-01-01 형식으로 입력하세요', name: 'regDate' },
    { type: 'textarea', label: '비고', placeholder: '제품 설명, 특징, 주의사항 등을 상세히 입력하세요', name: 'note' },
    { type: 'file', label: '제품 이미지', placeholder: '이미지를 선택하세요', name: 'productImage', accept: 'image/*', maxFileSize: 10000000, multiple: false }
  ]
});

// StandardInput 컴포넌트 ref
const standardInputRef = ref(null);

// ✅ 카테고리 변경 처리 함수 개선
const handleCategoryMainChange = (categoryMainValue) => {
  console.log('입력 폼 카테고리 변경됨:', categoryMainValue);
  
  // 세부카테고리 옵션 업데이트
  const categorySubInput = inputs.value.inputs.find(input => input.name === 'categorySub');
  if (categorySubInput) {
    categorySubInput.options = categorySubOptions[categoryMainValue] || [];
    console.log('세부카테고리 옵션 업데이트됨:', categorySubInput.options);
  }
  
  // ✅ 현재 선택된 세부카테고리 초기화 (기존 값과 충돌 방지)
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    inputFormRef.inputDatas.categorySub = '';
  }
  
  // ✅ Vue 반응성 강제 업데이트
  inputs.value = { ...inputs.value };
};

// 코드 변환 함수들
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

const getStatusName = (code) => {
  const statusMap = {
    '040001': '완료',
    '040002': '대기', 
    '040003': '반려'
  };
  return statusMap[code] || code;
};

// 날짜 포맷팅
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

const formatDateTimeForInput = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  } catch (error) {
    return dateString;
  }
};

// 제품 데이터 변환 (고유키 추가)
const filterProductData = (product, index = 0) => {
  return {
    id: product.productId || `temp_product_${Date.now()}_${index}`, // 고유 ID 추가
    productId: product.productId,
    productName: product.productName,
    vendorName: product.vendorName,
    compId: product.compId,
    productSpec: product.productSpec,
    packQty: product.packQty,
    safetyStock: product.safetyStock,
    purchasePrice: product.purchasePrice,
    sellPrice: product.sellPrice,
    note: product.note,
    productImage: product.productImage,
    categoryMain: getCategoryMainName(product.categoryMain),
    categorySub: getCategorySubName(product.categorySub),
    unit: getUnitName(product.unit),
    status: getStatusName(product.status),
    regUserName: product.regUserName || product.regUser,
    regDate: product.regDate ? formatDate(product.regDate) : '',
    categoryMainCode: product.categoryMain,
    categorySubCode: product.categorySub,
    unitCode: product.unit,
    statusCode: product.status,
    regUserCode: product.regUser
  };
};

// 제품 목록 로드
const loadProducts = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}`);
    
    if (response.data && Array.isArray(response.data)) {
      items.value = response.data.map((product, index) => filterProductData(product, index));
    }
  } catch (error) {
    console.error('제품 목록 조회 실패:', error);
    toast.add({ 
      severity: 'error', 
      summary: '오류', 
      detail: '제품 목록을 불러오는데 실패했습니다.', 
      life: 3000 
    });
  }
};

// 기존 searchData 함수에서 날짜 처리 부분만 수정하세요

const searchData = async (searchOptions) => {
  try {
    // 기존 파라미터 처리 코드는 그대로 두고...
    const params = {};
    
    if (searchOptions.productName?.trim()) params.productName = searchOptions.productName.trim();
    if (searchOptions.vendorName?.trim()) params.vendorName = searchOptions.vendorName.trim();
    if (searchOptions.categoryMain?.trim()) params.categoryMain = searchOptions.categoryMain.trim();
    if (searchOptions.categorySub?.trim()) params.categorySub = searchOptions.categorySub.trim();
    if (searchOptions.compId?.trim()) params.compId = searchOptions.compId.trim();
    if (searchOptions.packQty) params.packQty = searchOptions.packQty;
    if (searchOptions.regUser?.trim()) params.regUser = searchOptions.regUser.trim();
    
    // ✅ 날짜 범위 파라미터 처리만 수정 (Oracle 호환)
    if (searchOptions.regDateRangeFrom && searchOptions.regDateRangeTo) {
      try {
        // 날짜 객체로 변환하여 검증
        const fromDate = new Date(searchOptions.regDateRangeFrom);
        const toDate = new Date(searchOptions.regDateRangeTo);
        
        // 유효한 날짜인지 확인
        if (!isNaN(fromDate.getTime()) && !isNaN(toDate.getTime())) {
          // YYYY-MM-DD 형식으로 변환 (Oracle에서 인식 가능한 형식)
          params.regDateFrom = fromDate.toISOString().split('T')[0];
          params.regDateTo = toDate.toISOString().split('T')[0];
          
          console.log('날짜 범위 설정:', {
            original: { from: searchOptions.regDateRangeFrom, to: searchOptions.regDateRangeTo },
            converted: { from: params.regDateFrom, to: params.regDateTo }
          });
        } else {
          console.warn('유효하지 않은 날짜:', {
            from: searchOptions.regDateRangeFrom,
            to: searchOptions.regDateRangeTo
          });
        }
      } catch (dateError) {
        console.error('날짜 변환 오류:', dateError);
        // 에러가 발생해도 검색은 계속 진행 (날짜 조건 제외)
      }
    }
    
    // 승인 페이지인 경우 상태 제한
    if (typeof window !== 'undefined' && window.location.pathname.includes('approval')) {
      params.status = '040002'; // 승인 대기
    }
    
    console.log('최종 검색 파라미터:', params);
    
    // 기존 API 호출 코드는 그대로...
    const response = await axios.get(`${API_BASE_URL}/search`, { params });
    
    if (response.data && Array.isArray(response.data)) {
      items.value = response.data.map((product, index) => filterProductData(product, index));
      console.log(`검색 완료: ${items.value.length}개의 제품이 검색되었습니다.`);
    } else {
      console.warn('검색 응답이 배열이 아님:', response.data);
      items.value = [];
    }
    
  } catch (error) {
    console.error('검색 실패:', error);
    
    // 에러 메시지 개선
    let errorMessage = '검색 중 오류가 발생했습니다.';
    if (error.response?.status === 500) {
      errorMessage = '서버 내부 오류가 발생했습니다. 검색 조건을 확인해주세요.';
    } else if (error.response?.status === 400) {
      errorMessage = '검색 조건이 올바르지 않습니다.';
    }
    
    // Toast 또는 alert 사용
    if (typeof toast !== 'undefined' && toast.add) {
      toast.add({ 
        severity: 'error', 
        summary: '검색 실패', 
        detail: errorMessage, 
        life: 3000 
      });
    } else {
      alert(errorMessage);
    }
    
    items.value = [];
  }
  
  selectedProduct.value = null;
};

// 행 선택 처리
const onRowSelect = async (product) => {
  console.log('🎯 제품 선택됨:', product);
  selectedProduct.value = product;
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    
    // ✅ 방법 1: 모든 데이터를 먼저 초기화
    console.log('🔄 폼 데이터 초기화...');
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      inputFormRef.inputDatas[key] = '';
    });
    
    // ✅ 방법 2: 카테고리 옵션 업데이트 (세부카테고리 초기화 포함)
    if (product.categoryMainCode) {
      console.log('🔧 카테고리 옵션 업데이트:', product.categoryMainCode);
      handleCategoryMainChange(product.categoryMainCode);
    }
    
    // ✅ 방법 3: 충분한 대기 시간
    await nextTick();
    await new Promise(resolve => setTimeout(resolve, 150));
    
    // ✅ 방법 4: 카테고리부터 순차적으로 설정
    console.log('📝 카테고리 데이터 설정...');
    if (product.categoryMainCode) {
      inputFormRef.inputDatas.categoryMain = String(product.categoryMainCode);
      console.log('✅ 메인 카테고리 설정:', product.categoryMainCode);
    }
    
    // ✅ 방법 5: 세부카테고리 설정 (추가 대기 후)
    await nextTick();
    if (product.categorySubCode) {
      inputFormRef.inputDatas.categorySub = String(product.categorySubCode);
      console.log('✅ 세부 카테고리 설정:', product.categorySubCode);
    }
    
    // ✅ 방법 6: 나머지 데이터 설정
    console.log('📝 나머지 폼 데이터 설정...');
    Object.keys(inputFormRef.inputDatas).forEach(key => {
      if (key === 'categoryMain' || key === 'categorySub') {
        // 이미 설정됨
        return;
      } else if (key === 'unit' && product.unitCode) {
        inputFormRef.inputDatas[key] = String(product.unitCode);
      } else if (key === 'regUser' && product.regUserCode) {
        inputFormRef.inputDatas[key] = String(product.regUserCode);
      } else if (key === 'productImage_preview' && product.productImage) {
        inputFormRef.inputDatas[key] = product.productImage.startsWith('http') ? product.productImage : `${baseUrl.value}${product.productImage}`;
      } else if (key in product && !key.endsWith('Code') && !key.endsWith('_preview') && key !== 'id') {
        let value = product[key] || '';
        if ((key === 'regDate' || key === 'updateDate') && value) {
          value = formatDateTimeForInput(value);
        }
        inputFormRef.inputDatas[key] = String(value);
      }
    });
    
    // ✅ 방법 7: 최종 확인 및 재설정 (필요시)
    await nextTick();
    if (product.categorySubCode && !inputFormRef.inputDatas.categorySub) {
      console.log('🔧 세부카테고리 재설정 시도:', product.categorySubCode);
      inputFormRef.inputDatas.categorySub = String(product.categorySubCode);
    }
    
    console.log('✅ 최종 폼 데이터:', {
      categoryMain: inputFormRef.inputDatas.categoryMain,
      categorySub: inputFormRef.inputDatas.categorySub,
      productName: inputFormRef.inputDatas.productName,
      세부카테고리옵션수: inputs.value.inputs.find(input => input.name === 'categorySub')?.options?.length || 0,
      현재세부카테고리옵션: inputs.value.inputs.find(input => input.name === 'categorySub')?.options?.map(opt => `${opt.name}(${opt.value})`) || []
    });
  }
};

const onRowUnselect = () => {
  selectedProduct.value = null;
};

// 이미지 업로드 처리
const uploadProductImage = async (file) => {
  if (!file) return null;
  
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await axios.post(`${API_BASE_URL}/upload-image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    if (response.data.success && response.data.imageUrl) {
      return response.data.imageUrl;
    } else {
      throw new Error(response.data.message || '이미지 업로드 실패');
    }
  } catch (error) {
    if (error.response?.status === 404) {
      throw new Error('이미지 업로드 API를 찾을 수 없습니다.');
    } else if (error.response?.status === 413) {
      throw new Error('파일 크기가 너무 큽니다. 10MB 이하의 파일을 선택해주세요.');
    } else {
      throw new Error('이미지 업로드 중 오류가 발생했습니다.');
    }
  }
};

// 저장 처리
const saveData = async (inputData) => {
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
      if (!inputData[req.field]?.trim()) {
        toast.add({ 
          severity: 'error', 
          summary: '검증 오류', 
          detail: `${req.label}은(는) 필수입력 항목입니다.`, 
          life: 3000 
        });
        return;
      }
    }
    
    let imageUrl = null;
    
    // 이미지 업로드 처리
    if (inputData.productImage && inputData.productImage instanceof File) {
      try {
        imageUrl = await uploadProductImage(inputData.productImage);
      } catch (error) {
        const continueWithoutImage = confirm(`이미지 업로드에 실패했습니다.\n오류: ${error.message}\n\n이미지 없이 제품을 등록하시겠습니까?`);
        if (!continueWithoutImage) return;
        imageUrl = null;
      }
    } else if (inputData.productImage_preview) {
      // 기존 이미지가 있는 경우
      imageUrl = selectedProduct.value?.productImage || null;
    }
    
    const currentUserData = await getCurrentUser();
    const isUpdateMode = selectedProduct.value?.productId?.trim();
    
    let response;
    
    if (isUpdateMode) {
      // 수정 모드
      let regDate = null;
      if (inputData.regDate?.trim()) {
        try {
          regDate = new Date(inputData.regDate.trim() + 'T00:00:00');
          if (isNaN(regDate.getTime())) throw new Error('유효하지 않은 날짜');
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
      
      const updateData = {
        ...inputData,
        productId: selectedProduct.value.productId,
        updateUser: currentUserData.employeeId,
        updateDate: new Date(),
        regDate: regDate,
        productImage: imageUrl,
        regUser: selectedProduct.value.regUserCode || inputData.regUser
      };
      
      response = await axios.put(`${API_BASE_URL}/${selectedProduct.value.productId}`, updateData);
    } else {
      // 신규 등록 모드
      let regDate = inputData.regDate?.trim() ? 
        new Date(inputData.regDate.trim() + 'T00:00:00') : 
        new Date();
      
      const newProductData = {
        compId: inputData.compId,
        productName: inputData.productName,
        categoryMain: inputData.categoryMain,
        categorySub: inputData.categorySub || null,
        vendorName: inputData.vendorName,
        productSpec: inputData.productSpec || null,
        unit: inputData.unit,
        packQty: inputData.packQty ? parseInt(inputData.packQty) : null,
        safetyStock: inputData.safetyStock ? parseInt(inputData.safetyStock) : null,
        purchasePrice: inputData.purchasePrice ? parseFloat(inputData.purchasePrice) : null,
        sellPrice: inputData.sellPrice ? parseFloat(inputData.sellPrice) : null,
        regUser: currentUserData.employeeId,
        regDate: regDate,
        status: '040002',
        productImage: imageUrl,
        note: inputData.note || null
      };
      
      response = await axios.post(API_BASE_URL, newProductData);
    }
    
    if (response.data.success) {
      const successMessage = isUpdateMode ? 
        `제품이 성공적으로 수정되었습니다. (수정자: ${currentUserData.empName})` : 
        `제품이 성공적으로 등록되었습니다. (등록자: ${currentUserData.empName})`;
      
      toast.add({ 
        severity: 'success', 
        summary: '성공', 
        detail: successMessage, 
        life: 3000 
      });
      
      // 폼 초기화
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedProduct.value = null;
      
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
    console.error('저장 오류:', error);
    
    let errorMessage = '저장 중 오류가 발생했습니다.';
    if (error.code === 'ERR_NETWORK') {
      errorMessage = '네트워크 오류: 서버 연결을 확인해주세요.';
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    
    toast.add({ 
      severity: 'error', 
      summary: '저장 실패', 
      detail: errorMessage, 
      life: 5000 
    });
  }
};

// 삭제 처리
const deleteData = async () => {
  if (!selectedProduct.value?.productId) {
    toast.add({ 
      severity: 'warn', 
      summary: '선택 필요', 
      detail: '삭제할 제품을 선택해주세요.', 
      life: 3000 
    });
    return;
  }

  const confirmDelete = confirm(
    `제품 "${selectedProduct.value.productName}"을(를) 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`
  );
  
  if (!confirmDelete) return;

  try {
    const response = await axios.delete(`${API_BASE_URL}/${selectedProduct.value.productId}`);
    
    if (response.data.success) {
      toast.add({ 
        severity: 'success', 
        summary: '삭제 완료', 
        detail: `제품 "${selectedProduct.value.productName}"이(가) 성공적으로 삭제되었습니다.`, 
        life: 3000 
      });
      
      if (standardInputRef.value?.inputFormRef) {
        standardInputRef.value.inputFormRef.resetInputDatas();
      }
      selectedProduct.value = null;
      
      await loadProducts();
    } else {
      toast.add({ 
        severity: 'error', 
        summary: '삭제 실패', 
        detail: response.data.message || '삭제 중 오류가 발생했습니다.', 
        life: 5000 
      });
    }
  } catch (error) {
    console.error('제품 삭제 실패:', error);
    toast.add({ 
      severity: 'error', 
      summary: '삭제 실패', 
      detail: '삭제 중 오류가 발생했습니다.', 
      life: 5000 
    });
  }
};

// 회사 데이터 로드
const loadCompanyData = async () => {
  try {
    const result = await axios.get('/api/companies');
    const companies = result.data.data || result.data;
    const suppliers = companies.filter(item => item.compType === '100003');
    
    companyModalItems.value = suppliers.map((item) => ({
      ...item,
      compId: item.compId || item.comp_id || item.COMP_ID,
      compName: item.compName || item.comp_name || item.COMP_NAME,
      phoneNumber: item.phoneNumber || item.phone_number || item.phone || '',
      address: item.address || item.addr || '',
      bizNumber: item.bizNumber || item.biz_number || '',
      ceoName: item.ceoName || item.ceo_name || '',
    }));
  } catch (e) {
    toast.add({ 
      severity: 'error', 
      summary: '오류', 
      detail: '공급업체 데이터를 불러오는데 실패했습니다.', 
      life: 3000 
    });
  }
};

// 모달 함수들
const closeCompanyModal = () => {
  companyModalVisible.value = false;
};

const confirmCompanyModal = async (selectedItems) => {
  if (selectedItems?.compId && standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    inputFormRef.inputDatas.compId = selectedItems.compId;
    inputFormRef.inputDatas.vendorName = selectedItems.compName;
    
    toast.add({ 
      severity: 'success', 
      summary: '성공', 
      detail: `회사 "${selectedItems.compName}" 선택 완료`, 
      life: 3000 
    });
  }
  companyModalVisible.value = false;
};

const loadCompanyOnClick = () => {
  companyModalVisible.value = true;
};

const searchModal = (searchValue) => {
  // 검색 로직 구현
};

const openSearchModal = (inputName) => {
  if (inputName === 'vendorName') {
    loadCompanyOnClick();
  }
};

// 파일 관련 이벤트 처리 (InputForm에서 emit되는 이벤트들)
const handleFileSelected = (event) => {
  console.log('파일 선택됨:', event);
};

const handleFileUploaded = async (event) => {
  console.log('파일 업로드 요청:', event);
  if (event.file) {
    try {
      const imageUrl = await uploadProductImage(event.file);
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

const handleFileRemoved = (event) => {
  console.log('파일 제거됨:', event);
};

// 초기화 함수
const initializeFormData = async () => {
  const user = await getCurrentUser();
  
  if (standardInputRef.value?.inputFormRef) {
    const inputFormRef = standardInputRef.value.inputFormRef;
    inputFormRef.inputDatas.regUser = user.employeeId;
    
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    inputFormRef.inputDatas.regDate = `${year}-${month}-${day}`;
  }
};

// ✅ 검색 조건 카테고리 변경 처리 함수 추가
const handleSearchCategoryMainChange = (categoryMainValue) => {
  console.log('검색 조건 카테고리 변경됨:', categoryMainValue);
  
  // 검색 조건의 세부카테고리 옵션 업데이트
  const categorySubFilter = filters.value.filters.find(f => f.name === 'categorySub');
  if (categorySubFilter) {
    categorySubFilter.options = categorySubOptions[categoryMainValue] || [];
    console.log('검색 조건 세부카테고리 옵션 업데이트됨:', categorySubFilter.options);
  }
  
  // 검색 조건의 현재 선택된 세부카테고리 초기화
  if (standardInputRef.value?.searchFormRef) {
    const searchFormRef = standardInputRef.value.searchFormRef;
    if (searchFormRef.searchOptions) {
      searchFormRef.searchOptions.categorySub = '';
    }
  }
};

// ✅ 컴포넌트 마운트 (검색 조건 watch도 추가)
onMounted(async () => {
  try {
    await Promise.all([loadProducts(), loadCompanyData()]);
    await getCurrentUser();
    
    setTimeout(async () => {
      await initializeFormData();
      
      // ✅ 입력 폼의 categoryMain 값 변화 감지
      if (standardInputRef.value?.inputFormRef) {
        console.log('입력 폼 watch 설정 중...');
        
        // categoryMain 값 변화 감지
        watch(
          () => standardInputRef.value.inputFormRef.inputDatas.categoryMain,
          (newValue, oldValue) => {
            console.log('입력 폼 categoryMain 변경 감지:', { oldValue, newValue });
            if (newValue && newValue !== oldValue) {
              handleCategoryMainChange(newValue);
            }
          },
          { immediate: false } // 초기값은 무시, 변경 시에만 실행
        );
        
        console.log('입력 폼 watch 설정 완료');
      }
      
      // ✅ 검색 조건의 categoryMain 값 변화 감지
      if (standardInputRef.value?.searchFormRef) {
        console.log('검색 조건 watch 설정 중...');
        
        // 검색 조건 categoryMain 값 변화 감지
        watch(
          () => {
            const searchFormRef = standardInputRef.value.searchFormRef;
            return searchFormRef?.searchOptions?.categoryMain;
          },
          (newValue, oldValue) => {
            console.log('검색 조건 categoryMain 변경 감지:', { oldValue, newValue });
            if (newValue && newValue !== oldValue) {
              handleSearchCategoryMainChange(newValue);
            }
          },
          { immediate: false }
        );
        
        console.log('검색 조건 watch 설정 완료');
      }
    }, 200); // StandardInput이 완전히 마운트될 때까지 대기
    
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
  <Toast />
  
  <div class="product-page-container">
    <!-- 메인 제품 관리 영역 -->
    <StandardInput
      ref="standardInputRef"
      :filters="filters"
      :items="items"
      :header="header"
      :inputs="inputs"
      :scrollHeight="'600px'"
      @searchData="searchData"
      @saveData="saveData"
      @openSearchModal="openSearchModal"
      @rowSelect="onRowSelect"
      @rowUnselect="onRowUnselect"
      @fileSelected="handleFileSelected"
      @fileUploaded="handleFileUploaded"
      @fileRemoved="handleFileRemoved"
    >
      <!-- 삭제 버튼 -->
      <template #btn>
        <Button 
          label="삭제" 
          severity="danger" 
          class="min-w-fit whitespace-nowrap" 
          outlined
          :disabled="!selectedProduct"
          @click="deleteData"
        />
      </template>
    </StandardInput>
  </div>

  <!-- 회사 선택 모달 -->
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
</template>

<style scoped>
.product-page-container {
  position: relative;
}
</style>