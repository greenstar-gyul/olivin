<script setup>
import StandardInput from '@/components/common/StandardInput.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import axios from '@/service/axios';

const API_BASE_URL = '/api/products';

const currentUser = ref({
    empId: '',
    employeeId: '',
    empName: ''
});

const baseUrl = computed(() => {
    return typeof window !== 'undefined' ? window.location.origin : '';
});

const formatDateForOracle = (dateInput) => {
    if (!dateInput) return null;

    try {
        let date;
        if (dateInput instanceof Date) {
            date = dateInput;
        } else if (typeof dateInput === 'string') {
            if (/^\d{4}-\d{2}-\d{2}$/.test(dateInput.trim())) {
                return dateInput.trim();
            }
            date = new Date(dateInput.trim());
        } else {
            date = new Date(dateInput);
        }

        if (isNaN(date.getTime())) {
            throw new Error('유효하지 않은 날짜');
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    } catch (error) {
        return null;
    }
};

const getCurrentUser = async () => {
    try {
        const response = await axios.get('/api/auth/me');

        if (response.data.success && response.data.data) {
            const userData = response.data.data;

            let employeeId = 'admin';
            let empName = '관리자';

            const possibleUserSources = [userData.user, userData, userData.employee, userData.userInfo, userData.loginUser];

            for (const userSource of possibleUserSources) {
                if (userSource && typeof userSource === 'object') {
                    const possibleEmployeeIds = [userSource.employeeId, userSource.employee_id, userSource.EMPLOYEE_ID];
                    const possibleEmpNames = [userSource.empName, userSource.emp_name, userSource.EMP_NAME];

                    const foundEmployeeId = possibleEmployeeIds.find((id) => id && id !== 'admin' && String(id).trim() !== '');
                    const foundEmpName = possibleEmpNames.find((name) => name && name !== '관리자' && String(name).trim() !== '');

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
        } else {
            throw new Error('API 응답에 사용자 데이터가 없습니다');
        }
    } catch (error) {
        currentUser.value = { empId: 'admin', employeeId: 'admin', empName: '관리자' };
        return currentUser.value;
    }
};

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
    110001: [
        { name: '스킨/토너', value: '121001' },
        { name: '에센스/세럼/앰플', value: '121002' },
        { name: '크림', value: '121003' },
        { name: '로션', value: '121004' },
        { name: '미스트/오일', value: '121005' },
        { name: '스킨케어 디바이스', value: '121006' }
    ],
    110002: [
        { name: '베이스 메이크업', value: '122001' },
        { name: '아이 메이크업', value: '122002' },
        { name: '치크&컨투어', value: '122003' },
        { name: '립 메이크업', value: '122004' },
        { name: '피니시&픽서', value: '122005' },
        { name: '네일 메이크업', value: '122006' }
    ],
    110003: [
        { name: '클렌징폼/젤', value: '123001' },
        { name: '오일/밤', value: '123002' },
        { name: '워터/밀크', value: '123003' },
        { name: '필링&스크럽', value: '123004' },
        { name: '티슈/패드', value: '123005' },
        { name: '립&아이리무버', value: '123006' },
        { name: '클렌징 디바이스', value: '123007' }
    ],
    110004: [
        { name: '샴푸/린스', value: '124001' },
        { name: '트리트먼트/팩', value: '124002' },
        { name: '두피앰플/토닉', value: '124003' },
        { name: '헤어에센스', value: '124004' },
        { name: '염색약/펌', value: '124005' },
        { name: '헤어기기/브러시', value: '124006' },
        { name: '스타일링', value: '124007' }
    ],
    110005: [
        { name: '칫솔', value: '125001' },
        { name: '치약', value: '125002' },
        { name: '애프터구강케어', value: '125003' },
        { name: '구강가전', value: '125004' }
    ],
    110006: [
        { name: '선크림', value: '126001' },
        { name: '선스틱', value: '126002' },
        { name: '선쿠션', value: '126003' },
        { name: '선스프레이/선패치', value: '126004' },
        { name: '태닝/애프터선', value: '126005' }
    ],
    110007: [
        { name: '메이크업소품', value: '127001' },
        { name: '아이소품', value: '127002' },
        { name: '스킨케어소품', value: '127003' },
        { name: '헤어소품', value: '127004' },
        { name: '네일/바디소품', value: '127005' },
        { name: '뷰티잡화', value: '127006' }
    ],
    110008: [
        { name: '비타민', value: '128001' },
        { name: '영양제', value: '128002' },
        { name: '유산균', value: '128003' },
        { name: '슬리밍/이너뷰티', value: '128004' }
    ],
    110009: [
        { name: '식단관리/이너뷰티', value: '129001' },
        { name: '과자/초콜릿/디저트', value: '129002' },
        { name: '상수/음료/커피', value: '129003' },
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

const items = ref([]);
const selectedProduct = ref(null);
const loading = ref(false);
const standardInputRef = ref(null);

const filtersData = {
    title: '조회 조건',
    filters: [
        { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
        { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
        { type: 'select', label: '카테고리', value: '', placeholder: '카테고리를 선택하세요', name: 'categoryMain', options: [...categoryMainOptions] },
        { type: 'select', label: '세부카테고리', value: '', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
        { type: 'number', label: '입수량', value: '', placeholder: '입수량을 입력하세요', name: 'packQty' },
        { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요', name: 'regUser' },
        { type: 'dateRange', label: '등록일', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'regDateRange' }
    ]
};

const filters = ref(filtersData);

const headerData = {
    title: '제품 기준정보 관리 (승인완료)',
    header: {
        productName: '제품명',
        vendorName: '브랜드',
        categoryMain: '카테고리',
        categorySub: '세부카테고리',
        productSpec: '용량',
        unit: '단위',
        packQty: '입수량',
        safetyStock: '안전재고',
        regUserName: '등록자',
        regDate: '등록일'
    },
    rightAligned: ['packQty', 'safetyStock', 'purchasePrice', 'sellPrice']
};

const header = ref(headerData);

const inputsData = {
    title: '제품 등록/수정',
    inputs: [
        { type: 'text', label: '제품ID', placeholder: '등록 시 자동생성됩니다', name: 'productId', readonly: true },
        { type: 'text', label: '제품명', placeholder: '제품명을 입력하세요', name: 'productName', required: true },
        { type: 'text', label: '회사코드', placeholder: '회사선택 필수', name: 'compId', required: true, readonly: true },
        { type: 'item-search', label: '브랜드', placeholder: '회사 선택시 자동 입력', name: 'vendorName', required: true },
        { type: 'select', label: '카테고리', placeholder: '카테고리를 선택하세요', name: 'categoryMain', required: true, options: [...categoryMainOptions] },
        { type: 'select', label: '세부카테고리', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
        { type: 'text', label: '용량', placeholder: '50ml, 30포, 7.5g 등', name: 'productSpec' },
        { type: 'select', label: '단위', placeholder: '단위를 선택하세요', name: 'unit', required: true, options: [...unitOptions] },
        { type: 'number', label: '입수량', placeholder: '박스당 개수', name: 'packQty' },
        { type: 'number', label: '안전재고', placeholder: '최소 재고량', name: 'safetyStock' },
        { type: 'number', label: '구매가격', placeholder: '원가 (원)', name: 'purchasePrice' },
        { type: 'number', label: '판매가격', placeholder: '소비자가격 (원)', name: 'sellPrice' },
        { type: 'text', label: '등록자', placeholder: '현재 로그인 사용자 자동 설정', name: 'regUser', readonly: true },
        { type: 'text', label: '등록일', placeholder: '2024-01-01 형식으로 입력하세요', name: 'regDate' },
        { type: 'textarea', label: '비고', placeholder: '제품 설명, 특징, 주의사항 등을 상세히 입력하세요', name: 'note' },
        { type: 'file', label: '제품 이미지', placeholder: '이미지를 선택하세요', name: 'productImage', accept: 'image/*', maxFileSize: 10000000, multiple: false }
    ]
};

const inputs = ref(inputsData);

const companyModalVisible = ref(false);
const companyModalHeaders = ref([
    { field: 'compId', header: '업체ID' },
    { field: 'compName', header: '업체명' },
    { field: 'bizNumber', header: '사업자번호' },
    { field: 'ceoName', header: 'CEO명' },
    { field: 'phoneNumber', header: '전화번호' },
    { field: 'address', header: '주소' }
]);
const companyModalItems = ref([]);

const generateProductId = async (categoryMain) => {
    try {
        const categoryIdRangeMap = {
            '110001': { prefix: 'PRD', start: 100001, end: 199999 },
            '110002': { prefix: 'PRD', start: 200001, end: 299999 },
            '110003': { prefix: 'PRD', start: 300001, end: 399999 },
            '110004': { prefix: 'PRD', start: 400001, end: 499999 },
            '110005': { prefix: 'PRD', start: 500001, end: 599999 },
            '110006': { prefix: 'PRD', start: 600001, end: 699999 },
            '110007': { prefix: 'PRD', start: 700001, end: 799999 },
            '110008': { prefix: 'PRD', start: 800001, end: 899999 },
            '110009': { prefix: 'PRD', start: 900001, end: 999999 }
        };

        const range = categoryIdRangeMap[categoryMain];
        if (!range) {
            throw new Error(`유효하지 않은 카테고리: ${categoryMain}`);
        }

        try {
            const response = await axios.get(`${API_BASE_URL}/next-id/${categoryMain}`);
            if (response.data.result_code === 'SUCCESS' && response.data.data?.nextProductId) {
                return response.data.data.nextProductId;
            }
        } catch (apiError) {
            // API 실패 시 로컬에서 생성
        }

        const startId = range.start;
        return `${range.prefix}${startId}`;

    } catch (error) {
        return `PRD${Date.now()}`;
    }
};

const getCategoryMainName = (code) => {
    const category = categoryMainOptions.find((opt) => opt.value === code);
    return category ? category.name : code;
};

const getCategorySubName = (code) => {
    for (const mainCode in categorySubOptions) {
        const subCategory = categorySubOptions[mainCode].find((opt) => opt.value === code);
        if (subCategory) return subCategory.name;
    }
    return code;
};

const getUnitName = (code) => {
    const unit = unitOptions.find((opt) => opt.value === code);
    return unit ? unit.name : code;
};

const getStatusName = (code) => {
    const statusMap = {
        '040001': '승인완료',
        '040002': '승인대기',
        '040003': '승인반려',
        '040004': '판매중단'
    };
    return statusMap[code] || code;
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

const formatDateForInput = (dateString) => {
    if (!dateString) return '';
    try {
        let dateOnly = dateString;
        if (dateString.includes('T')) {
            dateOnly = dateString.split('T')[0];
        }
        return dateOnly;
    } catch (error) {
        return dateString;
    }
};

const filterProductData = (product, index = 0) => {
    const filtered = {
        id: product.productId || `temp_product_${Date.now()}_${index}`,
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
    
    return filtered;
};

// ✅ 수정된 승인완료 제품 로드 함수 - 디버깅 강화
const loadProducts = async (searchParams = {}) => {
    try {
        loading.value = true;
        console.log('🔍 승인완료 제품 조회 시작:', searchParams);

        const defaultProducts = [
            {
                productId: 'PRD100001',
                productName: '골드 원 오일',
                vendorName: '네이처하이',
                compId: 'COM50001',
                categoryMain: '110001',
                categorySub: '121001',
                productSpec: '150ml',
                unit: '130001',
                packQty: 12,
                safetyStock: 10,
                purchasePrice: 15000,
                sellPrice: 25000,
                status: '040001',
                regUser: 'admin',
                regUserName: '관리자',
                regDate: new Date(),
                note: '프리미엄 골든 오일'
            },
            {
                productId: 'PRD200001', 
                productName: '립스틱',
                vendorName: '뷰티브랜드',
                compId: 'COM50001',
                categoryMain: '110002',
                categorySub: '122004',
                productSpec: '3.5g',
                unit: '130002',
                packQty: 24,
                safetyStock: 5,
                purchasePrice: 8000,
                sellPrice: 15000,
                status: '040001',
                regUser: 'admin',
                regUserName: '관리자',
                regDate: new Date(),
                note: '롱래스팅 립스틱'
            }
        ];

        let products = defaultProducts;
        
        try {
            // ✅ 검색 조건 유무에 따른 API 엔드포인트 결정
            let apiUrl;
            let apiParams = {};
            
            // 검색 조건이 있으면 검색 API 사용
            if (Object.keys(searchParams).length > 0) {
                apiUrl = `${API_BASE_URL}/search`;
                apiParams = { 
                    ...searchParams, 
                    status: '040001' // 승인완료 상태 강제 추가
                };
                console.log('🔍 검색 API 호출:', { url: apiUrl, params: apiParams });
            } else {
                // 검색 조건이 없으면 승인완료 제품 전용 API 사용
                apiUrl = `${API_BASE_URL}/approved`;
                console.log('📋 승인완료 제품 전체 조회 API 호출:', apiUrl);
            }
            
            const response = await axios.get(apiUrl, { params: apiParams });
            console.log('📥 API 응답:', response.data);
            
            // ✅ 응답 데이터 처리 개선
            let apiProducts = [];
            
            if (response.data?.result_code === 'SUCCESS' && response.data.data) {
                apiProducts = response.data.data;
            } else if (response.data?.success && response.data.data) {
                apiProducts = response.data.data;
            } else if (Array.isArray(response.data)) {
                apiProducts = response.data;
            } else if (response.data?.products && Array.isArray(response.data.products)) {
                apiProducts = response.data.products;
            } else {
                console.warn('⚠️ 예상하지 못한 응답 구조:', response.data);
                apiProducts = [];
            }

            // ✅ 승인완료 상태 필터링 (안전장치)
            if (apiProducts.length > 0) {
                products = apiProducts.filter(product => 
                    product.status === '040001' || product.statusCode === '040001'
                );
                console.log(`✅ API에서 ${apiProducts.length}개 조회, 승인완료 ${products.length}개 필터링됨`);
            } else {
                console.log('📭 API에서 조회된 제품이 없음, 기본 데이터 사용');
            }
                
        } catch (apiError) {
            console.log('🔄 API 실패, 대체 방법 시도:', apiError.message);
            
            // ✅ 대체 방법: 전체 제품 API + 클라이언트 필터링
            try {
                const fallbackResponse = await axios.get(API_BASE_URL, { 
                    params: { status: '040001', ...searchParams } 
                });
                
                if (fallbackResponse.data?.result_code === 'SUCCESS' && fallbackResponse.data.data) {
                    products = fallbackResponse.data.data.filter(product => 
                        product.status === '040001' || product.statusCode === '040001'
                    );
                    console.log(`🔄 대체 방법으로 승인완료 제품 ${products.length}개 로드됨`);
                }
            } catch (fallbackError) {
                console.error('❌ 대체 API도 실패:', fallbackError);
            }
        }

        // ✅ 최종 상태 체크 로그
        console.log('📊 최종 제품 목록 상태 분석:');
        if (products.length > 0) {
            products.forEach((product, index) => {
                if (index < 3) {
                    console.log(`  ${index + 1}. ${product.productName}: ${getStatusName(product.status || product.statusCode)}`);
                }
            });
            if (products.length > 3) {
                console.log(`  ... 및 ${products.length - 3}개 더`);
            }
        } else {
            console.log('  조회된 승인완료 제품이 없습니다.');
        }

        items.value = products.map((product, index) => filterProductData(product, index));
        console.log(`🎯 최종 제품 목록: ${items.value.length}개 (모두 승인완료 상태)`);
        
    } catch (error) {
        console.error('❌ 제품 목록 조회 실패:', error);
        alert('데이터 조회에 실패했습니다.');
        items.value = [];
    } finally {
        loading.value = false;
    }
};

// ✅ 수정된 검색 함수 - 디버깅 강화 및 로직 개선
const searchData = async (searchOptions) => {
    console.log('🔍 승인완료 제품 검색 조건:', searchOptions);

    // 모든 검색 조건이 비어있는지 확인 (초기화 버튼을 눌렀을 때)
    const hasSearchCondition = Object.values(searchOptions).some((value) => {
        if (typeof value === 'string') {
            return value.trim() !== '';
        }
        return value !== null && value !== undefined && value !== '';
    });

    // 검색 조건이 없으면 입력 폼도 함께 초기화
    if (!hasSearchCondition) {
        console.log('🔄 검색 조건이 없어서 입력 폼도 초기화합니다.');

        await loadProducts();
        selectedProduct.value = null;

        if (standardInputRef.value?.inputFormRef) {
            standardInputRef.value.inputFormRef.resetInputDatas();

            setTimeout(async () => {
                await initializeFormData();
            }, 100);
        }

        return;
    }

    // ✅ 검색 파라미터 구성 개선
    const searchParams = {};

    if (searchOptions.productName?.trim()) {
        searchParams.productName = searchOptions.productName.trim();
    }
    if (searchOptions.vendorName?.trim()) {
        searchParams.vendorName = searchOptions.vendorName.trim();
    }
    if (searchOptions.categoryMain?.trim()) {
        searchParams.categoryMain = searchOptions.categoryMain.trim();
    }
    if (searchOptions.categorySub?.trim()) {
        searchParams.categorySub = searchOptions.categorySub.trim();
    }
    if (searchOptions.packQty) {
        searchParams.packQty = searchOptions.packQty;
    }
    if (searchOptions.regUser?.trim()) {
        searchParams.regUser = searchOptions.regUser.trim();
    }

    // ✅ 날짜 범위 파라미터 처리 개선
    if (searchOptions.regDateRangeFrom && searchOptions.regDateRangeTo) {
        try {
            const fromDate = new Date(searchOptions.regDateRangeFrom);
            const toDate = new Date(searchOptions.regDateRangeTo);

            if (!isNaN(fromDate.getTime()) && !isNaN(toDate.getTime())) {
                searchParams.regDateFrom = fromDate.toISOString().split('T')[0];
                searchParams.regDateTo = toDate.toISOString().split('T')[0];

                console.log('📅 날짜 범위 설정:', {
                    original: { from: searchOptions.regDateRangeFrom, to: searchOptions.regDateRangeTo },
                    converted: { from: searchParams.regDateFrom, to: searchParams.regDateTo }
                });
            }
        } catch (dateError) {
            console.error('⚠️ 날짜 변환 오류:', dateError);
        }
    }

    console.log('📤 최종 검색 파라미터 (승인완료 제품 대상):', searchParams);
    await loadProducts(searchParams);
};

const onRowSelect = (product) => {
    selectedProduct.value = product;

    if (standardInputRef.value?.inputFormRef) {
        const inputFormRef = standardInputRef.value.inputFormRef;

        Object.keys(inputFormRef.inputDatas).forEach((key) => {
            if (key !== 'id' && key in product) {
                let value = product[key] || '';

                if (key === 'categoryMain' && product.categoryMainCode) {
                    value = product.categoryMainCode;
                } else if (key === 'categorySub' && product.categorySubCode) {
                    value = product.categorySubCode;
                } else if (key === 'unit' && product.unitCode) {
                    value = product.unitCode;
                } else if ((key === 'regDate' || key === 'updateDate') && value) {
                    value = formatDateForInput(value);
                }

                inputFormRef.inputDatas[key] = String(value);
            }
        });
        
        if (product.categoryMainCode) {
            handleCategoryMainChange(product.categoryMainCode);
            
            setTimeout(() => {
                if (product.categorySubCode && standardInputRef.value?.inputFormRef) {
                    standardInputRef.value.inputFormRef.inputDatas.categorySub = product.categorySubCode;
                }
            }, 100);
        }
    }
};

const onRowUnselect = () => {
    selectedProduct.value = null;
};

const saveData = async (inputData) => {
    try {
        console.log('저장할 제품 데이터:', inputData);

        const requiredFields = [
            { field: 'productName', label: '제품명' },
            { field: 'compId', label: '회사코드' },
            { field: 'vendorName', label: '브랜드' },
            { field: 'categoryMain', label: '카테고리' },
            { field: 'unit', label: '단위' }
        ];

        for (const req of requiredFields) {
            if (!inputData[req.field]?.trim()) {
                alert(`${req.label}은(는) 필수입력 항목입니다.`);
                return;
            }
        }

        const currentUserData = await getCurrentUser();
        const isUpdateMode = selectedProduct.value?.productId;

        const productData = {
            ...inputData
        };

        let response;

        if (isUpdateMode) {
            let regDate = null;
            if (inputData.regDate?.trim()) {
                regDate = formatDateForOracle(inputData.regDate);
                if (!regDate) {
                    alert('등록일 형식이 올바르지 않습니다. (예: 2024-01-01)');
                    return;
                }
            }

            productData.productId = selectedProduct.value.productId;
            productData.updateUser = currentUserData.employeeId;
            productData.updateDate = formatDateForOracle(new Date());
            productData.regDate = regDate;
            productData.status = selectedProduct.value.statusCode || '040001';

            response = await axios.put(`${API_BASE_URL}/${selectedProduct.value.productId}`, productData);
        } else {
            let regDate = inputData.regDate?.trim() ? formatDateForOracle(inputData.regDate) : formatDateForOracle(new Date());

            if (!regDate) {
                alert('등록일 형식이 올바르지 않습니다. (예: 2024-01-01)');
                return;
            }

            const newProductId = await generateProductId(inputData.categoryMain);
            productData.productId = newProductId;

            productData.regUser = currentUserData.employeeId;
            productData.regDate = regDate;
            productData.status = '040002'; // 승인대기 상태 강제 설정

            response = await axios.post(API_BASE_URL, productData);
        }

        if (response.data.result_code === 'SUCCESS') {
            const statusMessage = isUpdateMode ? 
                `제품이 성공적으로 수정되었습니다. (수정자: ${currentUserData.empName})` : 
                `제품이 성공적으로 등록되었습니다.\n제품 ID: ${productData.productId}\n상태: 승인 대기 (등록자: ${currentUserData.empName})`;
            
            alert(statusMessage);

            if (standardInputRef.value?.inputFormRef) {
                standardInputRef.value.inputFormRef.resetInputDatas();
            }
            selectedProduct.value = null;

            await loadProducts();
        } else {
            alert(`저장 실패: ${response.data.message || '알 수 없는 오류'}`);
        }
    } catch (error) {
        console.error('제품 저장 실패:', error);

        if (error.code === 'ERR_NETWORK') {
            alert('네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.');
        } else {
            alert('저장 실패: ' + (error.response?.data?.message || error.message));
        }
    }
};

const deleteData = async () => {
    if (!selectedProduct.value?.productId) {
        alert('중단할 제품을 선택해주세요.');
        return;
    }

    const confirmStop = confirm(`제품 "${selectedProduct.value.productName}"을(를) 판매 중단하시겠습니까?\n\n중단된 제품은 더 이상 판매되지 않습니다.`);

    if (!confirmStop) return;

    try {
        const currentUserData = await getCurrentUser();

        const requestData = {
            updateUser: currentUserData.employeeId
        };

        const response = await axios.put(`${API_BASE_URL}/${selectedProduct.value.productId}/stop`, requestData);

        if (response.data.result_code === 'SUCCESS') {
            alert(`제품 "${selectedProduct.value.productName}"이(가) 판매 중단되었습니다. (처리자: ${currentUserData.empName})`);

            if (standardInputRef.value?.inputFormRef) {
                standardInputRef.value.inputFormRef.resetInputDatas();
            }
            selectedProduct.value = null;

            await loadProducts();
        } else {
            alert(`중단 실패: ${response.data.message || '중단 처리 중 오류가 발생했습니다.'}`);
        }
    } catch (error) {
        console.error('제품 중단 실패:', error);

        let errorMessage = '중단 처리 중 오류가 발생했습니다.';

        if (error.code === 'ERR_NETWORK') {
            errorMessage = '네트워크 오류: 백엔드 서버가 실행되고 있는지 확인해주세요.';
        } else if (error.response?.status === 404) {
            errorMessage = '중단할 제품을 찾을 수 없습니다.';
        } else if (error.response?.status === 409) {
            errorMessage = '이미 중단된 제품이거나 중단할 수 없는 상태입니다.';
        } else if (error.response?.data?.message) {
            errorMessage = error.response.data.message;
        }

        alert('중단 실패: ' + errorMessage);
    }
};

const handleCategoryMainChange = (categoryMainValue) => {
    try {
        const newSubOptions = categorySubOptions[categoryMainValue] || [];

        const currentInputs = { ...inputs.value };
        const categorySubInput = currentInputs.inputs.find(input => input.name === 'categorySub');
        
        if (categorySubInput) {
            categorySubInput.options = newSubOptions.map(option => ({ ...option }));
        }

        inputs.value = currentInputs;

        nextTick(() => {
            if (standardInputRef.value?.inputFormRef?.inputDatas) {
                standardInputRef.value.inputFormRef.inputDatas.categorySub = '';
            }
        });

    } catch (error) {
        console.error('카테고리 변경 처리 중 오류:', error);
    }
};

const handleSearchCategoryMainChange = (categoryMainValue) => {
    try {
        const newSubOptions = categorySubOptions[categoryMainValue] || [];

        const currentFilters = { ...filters.value };
        const categorySubFilter = currentFilters.filters.find(f => f.name === 'categorySub');
        
        if (categorySubFilter) {
            categorySubFilter.options = newSubOptions.map(option => ({ ...option }));
        }

        filters.value = currentFilters;
        
    } catch (error) {
        console.error('검색 카테고리 변경 처리 중 오류:', error);
    }
};

const openSearchModal = (inputName) => {
    if (inputName === 'vendorName') {
        companyModalVisible.value = true;
        loadCompanyData();
    }
};

const closeCompanyModal = () => {
    companyModalVisible.value = false;
};

const confirmCompanyModal = (selectedItems) => {
    if (selectedItems?.compId && standardInputRef.value?.inputFormRef) {
        const inputFormRef = standardInputRef.value.inputFormRef;
        if (inputFormRef.inputDatas) {
            inputFormRef.inputDatas.compId = selectedItems.compId;
            inputFormRef.inputDatas.vendorName = selectedItems.compName;
        }
        alert(`회사 "${selectedItems.compName}" 선택 완료`);
    }
    companyModalVisible.value = false;
};

const loadCompanyData = async () => {
    try {
        const response = await axios.get('/api/companies/active/type/100003');
        
        if (response.data.result_code === 'SUCCESS' && response.data.data) {
            companyModalItems.value = response.data.data.map(company => ({
                compId: company.compId,
                compName: company.compName,
                bizNumber: company.bizNumber,
                ceoName: company.ceoName,
                phoneNumber: company.phone,
                address: company.address
            }));
        } else {
            throw new Error('API 응답 형식 오류');
        }
    } catch (error) {
        companyModalItems.value = [
            {
                compId: 'COM50001',
                compName: '네이처하이',
                bizNumber: '123-45-67890',
                ceoName: '김대표',
                phoneNumber: '02-1234-5678',
                address: '서울시 강남구'
            },
            {
                compId: 'COM50002',
                compName: '뷰티브랜드',
                bizNumber: '987-65-43210',
                ceoName: '이대표',
                phoneNumber: '02-8765-4321',
                address: '서울시 서초구'
            }
        ];
    }
};

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
        
        if (!selectedProduct.value) {
            inputFormRef.inputDatas.status = '040002';
        }
    }
};

const setupWatchers = () => {
    try {
        setTimeout(() => {
            if (standardInputRef.value?.inputFormRef) {
                watch(
                    () => {
                        const formRef = standardInputRef.value?.inputFormRef;
                        const categoryMain = formRef?.inputDatas?.categoryMain;
                        return categoryMain || '';
                    },
                    (newValue, oldValue) => {
                        if (newValue && newValue !== oldValue && newValue !== '' && categorySubOptions[newValue]) {
                            handleCategoryMainChange(newValue);
                        }
                    },
                    { immediate: false }
                );
            }

            if (standardInputRef.value?.searchFormRef) {
                watch(
                    () => {
                        const searchRef = standardInputRef.value?.searchFormRef;
                        const categoryMain = searchRef?.searchOptions?.categoryMain;
                        return categoryMain || '';
                    },
                    (newValue, oldValue) => {
                        if (newValue && newValue !== oldValue && newValue !== '' && categorySubOptions[newValue]) {
                            handleSearchCategoryMainChange(newValue);
                        }
                    },
                    { immediate: false }
                );
            }
            
        }, 1000);

    } catch (error) {
        console.error('watch 설정 중 오류:', error);
    }
};

onMounted(async () => {
    try {
        console.log('=== ProductStandardPage 마운트 시작 ===');
        
        await getCurrentUser();
        await nextTick();
        await loadProducts();
        
        setTimeout(async () => {
            await initializeFormData();
            setupWatchers();
            
        }, 500);
        
    } catch (error) {
        console.error('마운트 실패:', error);
    }
});
</script>

<template>
    <div class="product-page-container">
        <StandardInput
            ref="standardInputRef"
            :filters="filters"
            :items="items"
            :header="header"
            :inputs="inputs"
            :loading="loading"
            :scrollHeight="'600px'"
            @searchData="searchData"
            @saveData="saveData"
            @openSearchModal="openSearchModal"
            @rowSelect="onRowSelect"
            @rowUnselect="onRowUnselect"
            @categoryMainChange="handleCategoryMainChange"
            @searchCategoryMainChange="handleSearchCategoryMainChange"
        >
            <template #btn>
                <Button 
                    label="판매중단" 
                    severity="danger" 
                    class="min-w-fit whitespace-nowrap" 
                    outlined 
                    :disabled="!selectedProduct" 
                    @click="deleteData" 
                />
            </template>
        </StandardInput>
    </div>

    <DialogModal 
        title="공급업체 검색" 
        :display="companyModalVisible" 
        :headers="companyModalHeaders" 
        :items="companyModalItems" 
        :selectionMode="'single'" 
        @close="closeCompanyModal" 
        @confirm="confirmCompanyModal" 
    />
</template>

<style scoped>
.product-page-container {
    position: relative;
}
</style>