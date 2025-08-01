<script setup>
import StandardApproval from '@/components/common/StandardApproval.vue';
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';

const API_BASE_URL = '/api/products';

// 필터 옵션에도 6자리 코드 기준 셀렉트 박스 추가
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

const filters = ref([
  { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
  { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
  { type: 'select', label: '카테고리', value: '', placeholder: '카테고리를 선택하세요', name: 'categoryMain', options: categoryMainOptions },
  { type: 'text', label: '세부카테고리', value: '', placeholder: '세부카테고리를 입력하세요', name: 'categorySub' },
  { type: 'numberRange', label: '입수량', value: '', placeholder: '입수량 범위를 입력하세요', name: 'packQtyRange' },
  { type: 'dateRange', label: '등록일 범위', value: '', placeholder: '등록일 범위를 선택하세요', name: 'regDateRange' }
]);

const items = ref([]);
const selectedProduct = ref(null);
const selectedProductId = ref(null);

const header = ref({
  title: '제품 승인 요청 목록',
  header: {
    productId: '제품ID',
    productName: '제품명',
    vendorName: '브랜드',
    categoryMain: '카테고리',
    categorySub: '세부카테고리',
    compId: '매장코드',
    productSpec: '용량/규격',
    unit: '단위',
    packQty: '입수량',
    purchasePrice: '구매단가',
    sellPrice: '판매단가',
    regUser: '등록자',
    regDate: '등록일자',
    status: '상태'
  },
  rightAligned: ['packQty', 'sellPrice']
});

// 코드를 이름으로 변환하는 함수들 (6자리 코드 기준)
const getCategoryMainName = (code) => {
  const categoryMap = {
    '110001': '스킨케어',
    '110002': '메이크업', 
    '110003': '클렌징',
    '110004': '헤어케어',
    '110005': '구강용품',
    '110006': '선케어',
    '110007': '뷰티소품',
    '110008': '건강/기능 식품',
    '110009': '푸드'
  };
  return categoryMap[code] || code;
};

const getCategorySubName = (code) => {
  const categorySubMap = {
    // 스킨케어 (121xxx)
    '121001': '스킨/토너',
    '121002': '에센스/세럼/앰플',
    '121003': '크림',
    '121004': '로션',
    '121005': '미스트/오일',
    '121006': '스킨케어 디바이스',
    // 메이크업 (122xxx)
    '122001': '베이스 메이크업',
    '122002': '아이 메이크업',
    '122003': '치크&컨투어',
    '122004': '립 메이크업',
    '122005': '피니시&픽서',
    '122006': '네일 메이크업',
    // 클렌징 (123xxx)
    '123001': '클렌징폼/젤',
    '123002': '오일/밤',
    '123003': '워터/밀크',
    '123004': '필링&스크럽',
    '123005': '티슈/패드',
    '123006': '립&아이리무버',
    '123007': '클렌징 디바이스',
    // 헤어케어 (124xxx)
    '124001': '샴푸/린스',
    '124002': '트리트먼트/팩',
    '124003': '두피앰플/토닉',
    '124004': '헤어에센스',
    '124005': '염색약/펌',
    '124006': '헤어기기/브러시',
    '124007': '스타일링',
    // 구강용품 (125xxx)
    '125001': '칫솔',
    '125002': '치약',
    '125003': '애프터구강케어',
    '125004': '구강가전',
    // 선케어 (126xxx)
    '126001': '선크림',
    '126002': '선스틱',
    '126003': '선쿠션',
    '126004': '선스프레이/선패치',
    '126005': '태닝/애프터선',
    // 뷰티소품 (127xxx)
    '127001': '메이크업소품',
    '127002': '아이소품',
    '127003': '스킨케어소품',
    '127004': '헤어소품',
    '127005': '네일/바디소품',
    '127006': '뷰티잡화',
    // 건강/기능 식품 (128xxx)
    '128001': '비타민',
    '128002': '영양제',
    '128003': '유산균',
    '128004': '슬리밍/이너뷰티',
    // 푸드 (129xxx)
    '129001': '식단관리/이너뷰티',
    '129002': '과자/초콜릿/디저트',
    '129003': '생수/음료/커피',
    '129004': '간편식/요리',
    '129005': '베이비푸드'
  };
  return categorySubMap[code] || code;
};

const getUnitName = (code) => {
  const unitMap = {
    '130001': 'ml',
    '130002': 'g',
    '130003': 'ea',
    '130004': 'box',
    '130005': 'pack'
  };
  return unitMap[code] || code;
};

const getStatusName = (code) => {
  const statusMap = {
    '040001': '완료',
    '040002': '대기',
    '040003': '반려'
  };
  return statusMap[code] || code;
};

// 폼 데이터 관리
const formData = ref({
  productId: '',
  productName: '',
  vendorName: '',
  categoryMain: '',
  categorySub: '',
  compId: '',
  productSpec: '',
  unit: '',
  packQty: '',
  purchasePrice: '',
  sellPrice: '',
  regUser: '',
  regDate: '',
  note: ''
});

// inputs를 computed로 만들어서 formData와 동기화
const inputs = computed(() => ({
  title: '제품 정보 및 승인 처리',
  inputs: [
    { type: 'text', label: '제품ID', value: formData.value.productId, name: 'productId', readonly: true },
    { type: 'text', label: '제품명', value: formData.value.productName, name: 'productName', readonly: true },
    { type: 'text', label: '브랜드', value: formData.value.vendorName, name: 'vendorName', readonly: true },
    { type: 'text', label: '카테고리', value: getCategoryMainName(formData.value.categoryMain), name: 'categoryMain', readonly: true },
    { type: 'text', label: '세부카테고리', value: getCategorySubName(formData.value.categorySub), name: 'categorySub', readonly: true },
    { type: 'text', label: '매장코드', value: formData.value.compId, name: 'compId', readonly: true },
    { type: 'text', label: '용량/규격', value: formData.value.productSpec, name: 'productSpec', readonly: true },
    { type: 'text', label: '단위', value: getUnitName(formData.value.unit), name: 'unit', readonly: true },
    { type: 'number', label: '입수량', value: formData.value.packQty, name: 'packQty', readonly: true },
    { type: 'number', label: '구매가격', value: formData.value.purchasePrice, name: 'purchasePrice', readonly: true },
    { type: 'number', label: '판매가격', value: formData.value.sellPrice, name: 'sellPrice', readonly: true },
    { type: 'text', label: '등록자', value: formData.value.regUser, name: 'regUser', readonly: true },
    { type: 'date', label: '등록일자', value: formData.value.regDate, name: 'regDate', readonly: true },
    { type: 'textarea', label: '비고/처리사유', value: formData.value.note, name: 'note', readonly: false, placeholder: '승인 또는 반려 사유를 입력하세요' }
  ]
}));

// 테이블에 표시할 데이터 변환 함수 (6자리 코드를 이름으로 변환)
const transformProductForDisplay = (product) => {
  return {
    ...product,
    categoryMain: getCategoryMainName(product.categoryMain), // 원본 필드명으로 변환
    categorySub: getCategorySubName(product.categorySub),     // 원본 필드명으로 변환
    unit: getUnitName(product.unit),                          // 원본 필드명으로 변환
    status: getStatusName(product.status),                    // 원본 필드명으로 변환
    regDate: product.regDate ? formatDate(product.regDate) : ''
  };
};

// 필요한 필드만 추출하는 함수 (원본 코드값은 별도 저장)
const filterProductData = (product) => {
  const {
    productId, productName, vendorName, categoryMain, categorySub,
    compId, productSpec, unit, packQty, purchasePrice, sellPrice,
    regUser, regDate, status, note
  } = product;
  
  // 원본 코드값들을 별도 필드로 저장
  const processedProduct = {
    productId, productName, vendorName, compId, productSpec, packQty, 
    purchasePrice, sellPrice, regUser, note,
    // 표시용 (변환된 값)
    categoryMain: getCategoryMainName(categoryMain),
    categorySub: getCategorySubName(categorySub),
    unit: getUnitName(unit),
    status: getStatusName(status),
    regDate: regDate ? formatDate(regDate) : '',
    // 원본 코드값 (폼 데이터용)
    categoryMainCode: categoryMain,
    categorySubCode: categorySub,
    unitCode: unit,
    statusCode: status
  };
  
  return processedProduct;
};

// 승인 대기 제품 조회 (6자리 상태 코드 사용)
const loadPendingProducts = async () => {
  try {
    console.log('승인 대기 제품 조회 시작...');
    const response = await axios.get(`${API_BASE_URL}/search`, {
      params: { status: '040002' } // 대기 상태 (6자리 코드)
    });
    
    console.log('API 응답:', response.data);
    
    if (response.data && Array.isArray(response.data)) {
      items.value = response.data
        .filter(product => product.status === '040002') // 대기 상태만 필터링
        .map(product => filterProductData(product));
      console.log('처리된 제품 목록:', items.value);
    }
  } catch (error) {
    console.error('승인 대기 제품 조회 실패:', error);
    alert('제품 목록을 불러오는데 실패했습니다.');
  }
};

// 검색 기능 (6자리 상태 코드 사용)
const searchData = async (searchOptions) => {
  try {
    console.log('검색 옵션:', searchOptions);
    const params = { status: '040002' }; // 대기 상태만 조회
    
    // 검색 조건 추가
    if (searchOptions.productName?.trim()) params.productName = searchOptions.productName.trim();
    if (searchOptions.vendorName?.trim()) params.vendorName = searchOptions.vendorName.trim();
    if (searchOptions.categoryMain?.trim()) params.categoryMain = searchOptions.categoryMain.trim();
    if (searchOptions.categorySub?.trim()) params.categorySub = searchOptions.categorySub.trim();
    
    // 날짜 범위 처리
    if (searchOptions.regDateRange && searchOptions.regDateRange.length === 2) {
      params.regDateFrom = searchOptions.regDateRange[0];
      params.regDateTo = searchOptions.regDateRange[1];
    }
    
    console.log('검색 파라미터:', params);
    const response = await axios.get(`${API_BASE_URL}/search`, { params });
    console.log('검색 결과:', response.data);
    
    items.value = response.data
      .filter(product => product.status === '040002') // 대기 상태만 필터링
      .map(product => filterProductData(product));
    
    console.log('필터링된 결과:', items.value);
    
  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
  
  resetForm();
};

// 제품 선택 처리
const onRowSelect = (row) => {
  if (!row) {
    resetForm();
    return;
  }
  
  console.log('선택된 제품:', row);
  
  // 중복 선택 방지
  if (selectedProductId.value === row.productId) {
    return;
  }
  
  selectedProductId.value = row.productId;
  selectedProduct.value = { ...row };
  
  // formData 업데이트 (원본 코드값으로)
  Object.keys(formData.value).forEach(key => {
    // 원본 코드값들을 사용
    if (key === 'categoryMain' && row.categoryMainCode) {
      formData.value[key] = String(row.categoryMainCode);
    } else if (key === 'categorySub' && row.categorySubCode) {
      formData.value[key] = String(row.categorySubCode);
    } else if (key === 'unit' && row.unitCode) {
      formData.value[key] = String(row.unitCode);
    } else if (key === 'status' && row.statusCode) {
      formData.value[key] = String(row.statusCode);
    } else if (key in row && !key.endsWith('Code')) {
      formData.value[key] = String(row[key] || '');
    }
  });
  
  formData.value.note = '';
  console.log('업데이트된 formData:', formData.value);
};

// 승인 처리
const handleApprove = async (approvalData) => {
  const targetProduct = approvalData.selectedItem || selectedProduct.value;
  
  if (!targetProduct) {
    alert('승인할 제품을 선택해주세요.');
    return;
  }
  
  const reason = approvalData?.note || '승인 완료';
  
  if (!confirm(`제품 "${targetProduct.productName}"을(를) 승인하시겠습니까?`)) {
    return;
  }
  
  try {
    console.log('승인 처리 시작:', targetProduct.productId);
    const response = await axios.post(
      `${API_BASE_URL}/${targetProduct.productId}/approve`, 
      {
        approver: 'ADMIN',
        reason: reason,
        status: '040001' // 완료 상태 (6자리 코드)
      },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    );
    
    console.log('승인 응답:', response);
    
    if (response.status === 200 && response.data.success) {
      alert(`제품 "${targetProduct.productName}"이 승인되었습니다.`);
      
      // 로컬에서 제거 후 새로고침
      items.value = items.value.filter(item => item.productId !== targetProduct.productId);
      resetForm();
      await loadPendingProducts();
    } else {
      alert('승인에 실패했습니다: ' + (response.data.message || '알 수 없는 오류'));
    }
    
  } catch (error) {
    console.error('승인 처리 실패:', error);
    const errorMessage = error.response?.data?.message || error.message || '알 수 없는 오류';
    alert(`승인 처리 실패: ${errorMessage}`);
  }
};

// 반려 처리
const handleReject = async (rejectionData) => {
  const targetProduct = rejectionData.selectedItem || selectedProduct.value;
  
  if (!targetProduct) {
    alert('반려할 제품을 선택해주세요.');
    return;
  }
  
  const reason = rejectionData?.note;
  
  if (!reason || reason.trim() === '') {
    alert('반려 사유를 입력해주세요.');
    return;
  }
  
  if (!confirm(`제품 "${targetProduct.productName}"을(를) 반려하시겠습니까?`)) {
    return;
  }
  
  try {
    console.log('반려 처리 시작:', targetProduct.productId);
    const response = await axios.post(
      `${API_BASE_URL}/${targetProduct.productId}/reject`, 
      {
        approver: 'ADMIN',
        reason: reason,
        status: '040003' // 반려 상태 (6자리 코드)
      },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    );
    
    console.log('반려 응답:', response);
    
    if (response.status === 200 && response.data.success) {
      alert(`제품 "${targetProduct.productName}"이 반려되었습니다.`);
      
      // 로컬에서 제거 후 새로고침
      items.value = items.value.filter(item => item.productId !== targetProduct.productId);
      resetForm();
      await loadPendingProducts();
    } else {
      alert('반려에 실패했습니다: ' + (response.data.message || '알 수 없는 오류'));
    }
    
  } catch (error) {
    console.error('반려 처리 실패:', error);
    const errorMessage = error.response?.data?.message || error.message || '알 수 없는 오류';
    alert(`반려 처리 실패: ${errorMessage}`);
  }
};

// 폼 초기화
const resetForm = () => {
  selectedProduct.value = null;
  selectedProductId.value = null;
  
  Object.keys(formData.value).forEach(key => {
    formData.value[key] = '';
  });
};

// InputForm에서 받은 데이터로 formData 업데이트
const saveData = (inputData) => {
  Object.keys(inputData).forEach(key => {
    if (formData.value.hasOwnProperty(key)) {
      formData.value[key] = inputData[key];
    }
  });
};

// 날짜 포맷 함수
const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

onMounted(() => {
  console.log('컴포넌트 마운트됨');
  loadPendingProducts();
});
</script>

<template>
  <StandardApproval
    :filters="filters"
    :items="items"
    :header="header"
    :inputs="inputs"
    @searchData="searchData"
    @saveData="saveData"
    @rowSelect="onRowSelect"
    @approve="handleApprove"
    @reject="handleReject"
  />
</template>