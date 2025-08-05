<script setup>
import StandardApproval from '@/components/common/StandardApproval.vue';
import { ref, onMounted, computed } from 'vue';
import axios from '@/service/axios';

const API_BASE_URL = '/api/products';

// 현재 로그인한 사용자 정보
const currentUser = ref({
  empId: '',
  empName: ''
});

// 사용자 정보 가져오기 함수 (간소화)
const getCurrentUser = async () => {
  try {
    const response = await axios.get('/api/auth/me');
    
    if (response.data.success && response.data.data) {
      const userData = response.data.data;
      
      let empId = 'olivin10001';
      let empName = '김홍인';
      
      // 사용자 데이터에서 ID와 이름 추출
      const possibleSources = [userData.user, userData, userData.employee, userData.userInfo, userData.loginUser];
      
      for (const source of possibleSources) {
        if (source && typeof source === 'object') {
          const foundEmpId = [source.empId, source.emp_id, source.EMPLOYEE_ID, source.employeeId, source.id, source.userId, source.user_id, source.USER_ID]
            .find(id => id && String(id).trim() !== '');
          
          const foundEmpName = [source.empName, source.emp_name, source.EMP_NAME, source.name, source.userName, source.user_name, source.USER_NAME, source.fullName, source.displayName]
            .find(name => name && String(name).trim() !== '');
          
          if (foundEmpId) empId = String(foundEmpId).trim();
          if (foundEmpName) empName = String(foundEmpName).trim();
          
          if (foundEmpId && foundEmpName) break;
        }
      }
      
      currentUser.value = { empId, empName };
      return currentUser.value;
    } else {
      throw new Error('사용자 정보를 찾을 수 없습니다');
    }
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error);
    currentUser.value = { empId: 'olivin10001', empName: '김홍인' };
    return currentUser.value;
  }
};

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

// 세부카테고리 옵션
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

// 검색 필터
const selectedCategoryMain = ref('');

// 조회용 세부카테고리 옵션 - computed로 반응형 처리
const filteredSearchCategorySubOptions = computed(() => {
  const categoryMainFilter = filters.value.find(f => f.name === 'categoryMain');
  const selectedMainCategory = categoryMainFilter?.value;
  
  console.log('승인페이지 - filteredSearchCategorySubOptions computed 실행됨:', selectedMainCategory);
  
  if (!selectedMainCategory) {
    return [];
  }
  
  const subOptions = categorySubOptions[selectedMainCategory] || [];
  console.log('승인페이지 - 조회용 세부카테고리 옵션:', subOptions);
  
  return subOptions;
});

const filters = ref([
  { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
  { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
  { type: 'select', label: '카테고리', value: '', placeholder: '카테고리를 선택하세요', name: 'categoryMain', options: categoryMainOptions },
  { type: 'select', label: '세부카테고리', value: '', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
  { type: 'number', label: '입수량', value: '', placeholder: '입수량을 입력하세요', name: 'packQty' },
  { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요', name: 'regUser' },
  { type: 'dateRange', label: '등록일 범위', value: ['', ''], placeholder: '등록일 범위를 선택하세요', name: 'regDateRange' }
]);

const items = ref([]);
const selectedProduct = ref(null);
const selectedProductId = ref(null);

// 테이블 헤더 - regUserName 사용 (백엔드 조인으로 가져온 직원 이름)
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
    regUserName: '등록자',  // 백엔드에서 조인된 직원 이름 사용
    regDate: '등록일자',
    status: '상태'
  },
  rightAligned: ['packQty', 'sellPrice']
});

// 코드 변환 함수들
const getCategorySubName = (code) => {
  const categorySubMap = {
    '121001': '스킨/토너', '121002': '에센스/세럼/앰플', '121003': '크림', '121004': '로션', '121005': '미스트/오일', '121006': '스킨케어 디바이스',
    '122001': '베이스 메이크업', '122002': '아이 메이크업', '122003': '치크&컨투어', '122004': '립 메이크업', '122005': '피니시&픽서', '122006': '네일 메이크업',
    '123001': '클렌징폼/젤', '123002': '오일/밤', '123003': '워터/밀크', '123004': '필링&스크럽', '123005': '티슈/패드', '123006': '립&아이리무버', '123007': '클렌징 디바이스',
    '124001': '샴푸/린스', '124002': '트리트먼트/팩', '124003': '두피앰플/토닉', '124004': '헤어에센스', '124005': '염색약/펌', '124006': '헤어기기/브러시', '124007': '스타일링',
    '125001': '칫솔', '125002': '치약', '125003': '애프터구강케어', '125004': '구강가전',
    '126001': '선크림', '126002': '선스틱', '126003': '선쿠션', '126004': '선스프레이/선패치', '126005': '태닝/애프터선',
    '127001': '메이크업소품', '127002': '아이소품', '127003': '스킨케어소품', '127004': '헤어소품', '127005': '네일/바디소품', '127006': '뷰티잡화',
    '128001': '비타민', '128002': '영양제', '128003': '유산균', '128004': '슬리밍/이너뷰티',
    '129001': '식단관리/이너뷰티', '129002': '과자/초콜릿/디저트', '129003': '생수/음료/커피', '129004': '간편식/요리', '129005': '베이비푸드'
  };
  return categorySubMap[code] || code;
};

const getUnitName = (code) => {
  const unitMap = {
    '130001': 'ml', '130002': 'g', '130003': 'ea', '130004': 'box', '130005': 'pack'
  };
  return unitMap[code] || code;
};

const getStatusName = (code) => {
  const statusMap = {
    '040001': '완료', '040002': '대기', '040003': '반려'
  };
  return statusMap[code] || code;
};

const getCategoryMainName = (code) => {
  const categoryMap = {
    '110001': '스킨케어', '110002': '메이크업', '110003': '클렌징', '110004': '헤어케어',
    '110005': '구강용품', '110006': '선케어', '110007': '뷰티소품', '110008': '건강/기능 식품', '110009': '푸드'
  };
  return categoryMap[code] || code;
};

// 날짜 포맷 함수 - 시간 표시 제거
const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}.${month}.${day}`;
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

// 날짜시간 포맷 함수 (필요시 사용)
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}.${month}.${day} ${hours}:${minutes}`;
  } catch (error) {
    console.error('날짜시간 포맷 오류:', error);
    return dateString;
  }
};

// 입력용 날짜 포맷 함수
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
  regUserName: '',  // 백엔드에서 조인으로 가져온 등록자 이름
  regDate: '',
  note: ''
});

// inputs computed - 백엔드 조인된 직원 이름 직접 사용
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
    { type: 'text', label: '등록자', value: formData.value.regUserName || formData.value.regUser, name: 'regUser', readonly: true }, // 백엔드 조인된 이름 우선 사용
    { type: 'date', label: '등록일자', value: formData.value.regDate, name: 'regDate', readonly: true },
    { type: 'textarea', label: '비고/처리사유', value: formData.value.note, name: 'note', readonly: false, placeholder: '승인 또는 반려 사유를 입력하세요' }
  ]
}));

// 제품 데이터 변환 함수 - 백엔드 조인된 데이터 그대로 사용
const filterProductData = (product) => {
  // 날짜 처리 - 확실하게 시간 제거
  let formattedRegDate = '';
  if (product.regDate) {
    try {
      const date = new Date(product.regDate);
      if (!isNaN(date.getTime())) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        formattedRegDate = `${year}.${month}.${day}`;
      }
    } catch (error) {
      console.error('날짜 변환 오류:', error);
      formattedRegDate = '';
    }
  }

  return {
    productId: product.productId,
    productName: product.productName,
    vendorName: product.vendorName,
    compId: product.compId,
    productSpec: product.productSpec,
    packQty: product.packQty,
    purchasePrice: product.purchasePrice,
    sellPrice: product.sellPrice,
    note: product.note,
    // 표시용 (변환된 값)
    categoryMain: getCategoryMainName(product.categoryMain),
    categorySub: getCategorySubName(product.categorySub),
    unit: getUnitName(product.unit),
    status: getStatusName(product.status),
    regUserName: product.regUserName || product.regUser, // 백엔드에서 조인된 이름 우선 사용
    regDate: formattedRegDate, // 확실하게 시간이 제거된 날짜
    // 원본 코드값 (폼 데이터용)
    categoryMainCode: product.categoryMain,
    categorySubCode: product.categorySub,
    unitCode: product.unit,
    statusCode: product.status,
    regUserCode: product.regUser
  };
};

// 승인 대기 제품 조회 - 기존 search API 사용 (백엔드 조인 포함)
const loadPendingProducts = async () => {
  try {
    console.log('승인 대기 제품 조회 시작...');
    
    const response = await axios.get(`${API_BASE_URL}/search`, {
      params: { status: '040002' }
    });
    
    console.log('API 응답 (백엔드 조인 포함):', response.data);
    
    if (response.data && Array.isArray(response.data)) {
      items.value = response.data.map(product => filterProductData(product));
      console.log('처리된 제품 목록:', items.value);
      
      // 백엔드 조인 확인
      if (items.value.length > 0) {
        console.log('✅ 백엔드 조인 성공 - 첫 번째 제품:');
        console.log('regUser (ID):', items.value[0].regUserCode);
        console.log('regUserName (이름):', items.value[0].regUserName);
      }
    }
  } catch (error) {
    console.error('승인 대기 제품 조회 실패:', error);
    alert('제품 목록을 불러오는데 실패했습니다.');
  }
};

// 검색 기능 - 카테고리 변경 처리 개선
const searchData = async (searchOptions) => {
  try {
    console.log('검색 옵션:', searchOptions);
    
    // 카테고리 변경 처리 개선
    if (searchOptions.categoryMain !== selectedCategoryMain.value) {
      selectedCategoryMain.value = searchOptions.categoryMain || '';
      
      const categorySubFilter = filters.value.find(f => f.name === 'categorySub');
      if (categorySubFilter) {
        categorySubFilter.options = searchOptions.categoryMain ? (categorySubOptions[searchOptions.categoryMain] || []) : [];
        if (!searchOptions.categoryMain) {
          categorySubFilter.value = '';
          searchOptions.categorySub = '';
        }
        console.log('세부카테고리 옵션 업데이트됨:', categorySubFilter.options);
      }
    }
    
    const params = { status: '040002' };
    
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
    console.log('검색 결과 (백엔드 조인 포함):', response.data);
    
    items.value = response.data
      .filter(product => product.status === '040002')
      .map(product => filterProductData(product));
    
    console.log('필터링된 결과:', items.value);
    
  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
  
  resetForm();
};

// 카테고리 변경 처리 함수 개선
const onCategoryMainChange = (selectedCategoryMain) => {
  console.log('승인 페이지 - 카테고리 변경됨:', selectedCategoryMain);
  
  const categorySubFilter = filters.value.find(f => f.name === 'categorySub');
  if (categorySubFilter) {
    // 세부카테고리 값 초기화
    categorySubFilter.value = '';
    
    console.log('승인 페이지 - 세부카테고리 값 초기화됨');
    console.log('승인 페이지 - 사용 가능한 세부카테고리 옵션:', categorySubOptions[selectedCategoryMain] || []);
  }
  
  // Vue의 반응성을 강제로 트리거
  filters.value = [...filters.value];
};

// 필터 변경 처리 함수 추가
const handleFilterChange = (filter) => {
  console.log('승인 페이지 - 필터 변경됨:', filter.name, filter.value);
  
  if (filter.name === 'categoryMain') {
    onCategoryMainChange(filter.value);
  }
};

// 제품 선택 처리
const onRowSelect = (row) => {
  if (!row) {
    resetForm();
    return;
  }
  
  console.log('선택된 제품 (백엔드 조인 데이터):', row);
  
  if (selectedProductId.value === row.productId) {
    return;
  }
  
  selectedProductId.value = row.productId;
  selectedProduct.value = { ...row };
  
  // formData 업데이트 - regUserName 포함
  Object.keys(formData.value).forEach(key => {
    if (key === 'categoryMain' && row.categoryMainCode) {
      formData.value[key] = String(row.categoryMainCode);
    } else if (key === 'categorySub' && row.categorySubCode) {
      formData.value[key] = String(row.categorySubCode);
    } else if (key === 'unit' && row.unitCode) {
      formData.value[key] = String(row.unitCode);
    } else if (key === 'status' && row.statusCode) {
      formData.value[key] = String(row.statusCode);
    } else if (key === 'regUser' && row.regUserCode) {
      formData.value[key] = String(row.regUserCode);
    } else if (key === 'regUserName' && row.regUserName) {
      formData.value[key] = String(row.regUserName); // 백엔드에서 조인된 이름
    } else if (key in row && !key.endsWith('Code')) {
      formData.value[key] = String(row[key] || '');
    }
  });
  
  formData.value.note = '';
  console.log('업데이트된 formData (백엔드 조인 이름 포함):', formData.value);
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
    const currentUserData = await getCurrentUser();
    
    const response = await axios.post(
      `${API_BASE_URL}/${targetProduct.productId}/approve`, 
      {
        approver: currentUserData.empId,
        reason: reason,
        status: '040001'
      },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    );
    
    if (response.status === 200 && response.data.success) {
      const approverName = response.data.approverName || currentUserData.empName;
      alert(`제품 "${targetProduct.productName}"이 승인되었습니다. (승인자: ${approverName})`);
      
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
    const currentUserData = await getCurrentUser();
    
    const response = await axios.post(
      `${API_BASE_URL}/${targetProduct.productId}/reject`, 
      {
        approver: currentUserData.empId,
        reason: reason,
        status: '040003'
      },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    );
    
    if (response.status === 200 && response.data.success) {
      const rejecterName = response.data.rejecterName || currentUserData.empName;
      alert(`제품 "${targetProduct.productName}"이 반려되었습니다. (반려자: ${rejecterName})`);
      
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

// 컴포넌트 마운트 - 세부카테고리 옵션 초기화 추가
onMounted(async () => {
  console.log('🚀 ProductApprovalPage 마운트 시작');
  
  try {
    // 사용자 정보 로드
    await getCurrentUser();
    console.log('✅ 사용자 정보:', currentUser.value);
    
    // 세부카테고리 필터 초기화
    const categorySubFilter = filters.value.find(f => f.name === 'categorySub');
    if (categorySubFilter) {
      categorySubFilter.options = [];
      console.log('✅ 세부카테고리 필터 초기화됨');
    }
    
    // 제품 목록 로드 (백엔드에서 직원 이름 조인 포함)
    await loadPendingProducts();
    
    console.log('🎉 모든 데이터 로드 완료');
    
  } catch (error) {
    console.error('❌ 초기 데이터 로드 실패:', error);
    
    // 실패 시에도 기본 동작
    await getCurrentUser().catch(() => {});
    await loadPendingProducts().catch(() => {});
  }
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