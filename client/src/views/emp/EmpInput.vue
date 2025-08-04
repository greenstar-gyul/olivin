<script>
import { ref } from 'vue';
import SearchForm from '@/components/inputForm/SearchForm.vue';
import BasicTable from '@/components/table/BasicTable.vue';
import InputForm from '@/components/inputForm/InputForm.vue';
import Button from 'primevue/button';
import { Password } from 'primevue';

// URL
const API_BASE_URL = '/api/depts';

export default {
  name: 'StandardInput',

  components: {
    SearchForm,
    BasicTable,
    InputForm,
    Button
  },

  props: {
    filters: {
      type: Object,
      required: true
    },
    items: {
      type: Array,
      default: () => []
    },
    header: {
      type: Object,
      required: true
    },
    inputs: {
      type: Object,
      required: true
    },
    checkType: {
      type: String,
      default: 'single'
    }
  },
  
  emits: ['searchData', 'saveData', 'openSearchModal'],
  
  setup(props, { emit, expose }) {
// 폼 데이터를 반응형으로 관리
  const formData = ref({
      compId: '',
      compName: '',
      departmentId: '',
      deptName: '',
      employeeId: '',
      empName: '',
      empType: '',
      email: '',
      password: '',
      phone: '',
      position: '',
    });

    const selectedDept = ref(null);
    const searchFormRef = ref(null);
    const inputFormRef = ref(null);
    const selectedItems = ref(null); // BasicTable에서 선택된 항목
  
    // 필터 정의
    const filters = ref({         
      title: '조회 조건',
      filters: [
        { type: 'text', label: '회사코드', value: '', name: 'compId' },
        { type: 'text', label: '회사명', value: '', name: 'compName' },
        { type: 'text', label: '부서코드', value: '', name: 'departmentId' },
        { type: 'text', label: '부서명', value: '', name: 'deptName' }
      ]
    });

    // 입력필드
    const inputs = ref({
      title: '입력 폼',
      inputs: [
        { type: 'text', label: '회사코드', value: '', name: 'compId' },
        { type: 'text', label: '회사명', value: '', name: 'compName' },
        { type: 'text', label: '사원번호', value: '', name: 'employeeId' },
        {
          type: 'select',
          label: '고용형태',
          value: '',
          name: 'empType',
          options: [
            { name: '정규직', value: '정규직' },
            { name: '비정규직', value: '비정규직' }
          ]
        },
        { type: 'text', label: '이메일', value: '', name: 'email' },
        { type: 'text', label: '비밀번호', value: '', name: 'password' },
        { type: 'text', label: '연락처', value: '', name: 'phone' },
        {
          type: 'select',
          label: '부서명',
          value: '',
          name: 'deptName',
          options: [
            { name: '경영지원팀', value: '경영지원팀' },
            { name: '영업팀', value: '영업팀' },
            { name: '물류팀', value: '물류팀' },
            { name: '구매팀', value: '구매팀' },
            { name: '회계팀', value: '회계팀' },
            { name: '마케팅팀', value: '마케팅팀' },
            { name: '품질관리팀', value: '품질관리팀' },
            { name: '연구개발팀', value: '연구개발팀' },
            { name: '고객서비스팀', value: '고객서비스팀' },
            { name: 'IT팀', value: 'IT팀' },
            { name: '전략기획팀', value: '전략기획팀' },
            { name: '인사팀', value: '인사팀' },
            { name: '법무팀', value: '법무팀' },
            { name: '감사팀', value: '감사팀' },
            { name: '해외사업팀', value: '해외사업팀' },
            { name: '디지털마케팅팀', value: '디지털마케팅팀' },
            { name: '브랜드팀', value: '브랜드팀' },
            { name: '상품기획팀', value: '상품기획팀' },
            { name: '교육팀', value: '교육팀' },
            { name: '안전관리팀', value: '안전관리팀' },
          ]},
          { type: 'text', label: '직책', value: '', name: 'position' },
        ]
      });

    const searchData = (searchOptions) => {
      emit('searchData', searchOptions);
    };

    const saveData = (inputData) => {
      emit('saveData', inputData);
    };

    const openSearchModal = (inputName) => {
      emit('openSearchModal', inputName);
    };

    const onRowSelect = (data) => {
      if (props.checkType === 'single') {
        selectedItems.value = data;
      } else {
        if (!selectedItems.value) selectedItems.value = [];
        selectedItems.value.push(data);
      }
    };

    const onRowUnselect = (data) => {
      if (props.checkType !== 'single') {
        selectedItems.value = selectedItems.value.filter(item => item !== data);
      } else {
        selectedItems.value = null;
      }
    };

    expose({
      searchFormRef,
      inputFormRef
    });

    // setup 함수에서 선언한 변수와 함수들을 반환해야 template에서 사용할 수 있다.
    // props는 template에서 바로 접근 가능하므로 반환할 필요가 없음.
    return {
      formData,
      selectedDept,
      searchFormRef,
      inputFormRef,
      searchData,
      saveData,
      openSearchModal,
      onRowSelect,
      onRowUnselect,
      filters, // 외부에 선언된 설정 객체도 template에서 사용하려면 반환해야 함
      inputs
    };
  }
};
</script>
<template>
  <SearchForm
    ref="searchFormRef"
    :filters="filters"
    @searchData="searchData"
    @openSearchModal="openSearchModal"
  />

  <div class="grid grid-cols-7 gap-4 mb-4 items-stretch">
    <BasicTable
      :data="items"
      :header="header"
      :checked="true"
      @rowSelect="onRowSelect"
      @rowUnselect="onRowUnselect"
      class="col-span-4"
    >
      <template #btn>
        <Button label="삭제" severity="danger" class="min-w-fit whitespace-nowrap" outlined />
      </template>
    </BasicTable>

    <InputForm
      ref="inputFormRef"
      :inputs="inputs"
      @saveData="saveData"
      @openSearchModal="openSearchModal"
      class="col-span-3"
    />
  </div>
</template>