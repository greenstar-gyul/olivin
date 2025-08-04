<script>
import { ref } from 'vue';
import SearchForm from '@/components/inputForm/SearchForm.vue';
import BasicTable from '@/components/table/BasicTable.vue';
import InputForm from '@/components/inputForm/InputForm.vue';
import Button from 'primevue/button';

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
      deptName: ''
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

//   const header = ref({
//     title: '조회 결과',
//     header: {
//     compId: '회사명',
//     compName: '직급',
//     departmentId: '부서코드',
//     deptName: '부서명'
//   },
//   rightAligned: ['quantity', 'safe']
// });

    // 테이블 아이템 예시데이터
    // const items = ref([
    //   { id: 1, name: '제품 A', category: '카테고리 1', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
    //   { id: 2, name: '제품 B', category: '카테고리 2', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
    //   { id: 3, name: '제품 C', category: '카테고리 3', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 }
    // ]);

    // 입력필드
    const inputs = ref({
      title: '입력 폼',
      inputs: [
        { type: 'text', label: '회사ID', value: '', name: 'compId' },
        { type: 'text', label: '회사명', value: '', name: 'compName' },
        {
          type: 'select',
          label: '부서명',
          value: '',
          name: 'select1',
          options: [
            { name: '부서 1', value: '부서 1' },
            { name: '부서 2', value: '부서 2' },
            { name: '부서 3', value: '부서 3' }
          ]
        },
        {
          type: 'select',
          label: '직급',
          value: '',
          name: 'select2',
          options: [
            { name: '직급 1', value: '직급 1' },
            { name: '직급 2', value: '직급 2' },
            { name: '직급 3', value: '직급 3' }
          ]
        },
        { type: 'text', label: '등록사원', value: '', name: 'empName' },
        { type: 'text', label: '연락처', value: '', name: 'phone' },
        { type: 'text', label: '기타', value: '', name: 'password' },
        { type: 'textarea', label: '비고', value: '', name: 'note' }
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
      items: props.items, // props.items는 template에서 직접 사용 가능하지만 명시적으로 반환
      //header,
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
      @searchData="searchData"
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