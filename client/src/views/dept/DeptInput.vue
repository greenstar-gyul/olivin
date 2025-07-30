<script>
import { ref } from 'vue';
import SearchForm from '@/components/inputForm/SearchForm.vue';
import BasicTable from '@/components/table/BasicTable.vue';
import InputForm from '@/components/inputForm/InputForm.vue';
import Button from 'primevue/button';

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
    const selectedItems = ref(null);
    const searchFormRef = ref(null);
    const inputFormRef = ref(null);

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

    return {
      searchFormRef,
      inputFormRef,
      searchData,
      saveData,
      openSearchModal,
      onRowSelect,
      onRowUnselect,
      filters: props.filters,
      items: props.items,
      header: props.header,
      inputs: props.inputs
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