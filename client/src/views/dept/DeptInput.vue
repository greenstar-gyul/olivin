<script>
import { ref } from 'vue';
import SearchForm from '@/components/inputForm/SearchForm.vue';
import BasicTable from '@/components/table/BasicTable.vue';
import DeptEmpInputForm from '@/components/inputForm/DeptEmpInputForm.vue';
import Button from 'primevue/button';


export default {
  name: 'DeptInput',

  components: {
    SearchForm,
    BasicTable,
    DeptEmpInputForm,
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

    const searchData = (searchOptions) => {
      emit('searchData', searchOptions);
    };

    const saveData = (inputData) => {
      emit('saveData', inputData);
    };

    const onSaveData = (data, mode) => {
      emit('saveData', data, mode);  // 그대로 상위로 전달
    };

    const openSearchModal = (inputName) => {
      emit('openSearchModal', inputName);
    };

    const onRowSelect = (data) => {
      if (props.checkType === 'single') {
        selectedItems.value = data;
      } else {
        if (!selectedItems.value) {
          selectedItems.value = [];
        }
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

    const deleteSelected = () => {
      if (selectedItems.value) {
        emit('saveData', selectedItems.value, 'delete');
      }
    };

    expose({
      searchFormRef,
      inputFormRef
    });

    // setup 함수에서 선언한 변수와 함수들을 반환해야 template에서 사용할 수 있다.
    // props는 template에서 바로 접근 가능하므로 반환할 필요가 없음.
    return {
      selectedItems,
      searchFormRef,
      inputFormRef,
      deleteSelected,
      searchData,
      saveData,
      onSaveData,
      openSearchModal,
      onRowSelect,
      onRowUnselect,
      filters // 외부에 선언된 설정 객체도 template에서 사용하려면 반환해야 함
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
      dataKey="departmentId"
      @searchData="searchData"
      @rowSelect="onRowSelect"
      @rowUnselect="onRowUnselect"
      class="col-span-4"
    >
      <template #btn>
        <Button label="삭제" severity="danger" class="min-w-fit whitespace-nowrap" outlined />
      </template>
    </BasicTable>

    <DeptEmpInputForm
      ref="inputFormRef"
      :inputs="inputs"
      :init="selectedItems"
      @saveData="saveData"
      @openSearchModal="openSearchModal"
      class="col-span-3"
    />
  </div>
</template>