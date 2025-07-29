<script setup>
import { ref } from 'vue';
import InputForm from '../inputForm/InputForm.vue';
import InputMaster from '../inputForm/InputMaster.vue';
import SearchForm from '../inputForm/SearchForm.vue';
import BasicTable from '../table/BasicTable.vue';

const emit = defineEmits(['searchData', 'saveData', 'openSearchModal']);
const props = defineProps({
  filters: {
    type: Array,
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
  }
});

const selectedItems = ref(null);

const searchFormRef = ref(null);
const inputFormRef = ref(null);

const searchData = (searchOptions) => {
  emit('searchData', searchOptions);
};

const saveData = (inputData) => {
  emit('saveData', inputData);
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
}

const onRowUnselect = (data) => {
  if (props.checkType !== 'single') {
    selectedItems.value = selectedItems.value.filter(item => item !== data);
  } else {
    selectedItems.value = null;
  }
};

const openSearchModal = (inputName) => {
  emit('openSearchModal', inputName);
};

defineExpose({
  searchFormRef,
  inputFormRef
})

</script>
<template>
  <SearchForm ref="searchFormRef" :filters="props.filters" @searchData="searchData" @openSearchModal="openSearchModal" />
  <div class="grid grid-cols-7 gap-4 mb-4 items-stretch">
    <BasicTable :data="props.items" :header="props.header" :checked="true" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect" class="col-span-4">
      <template #btn>
        <Button label="삭제" severity="danger" class="min-w-fit whitespace-nowrap" outlined></Button>
      </template>
    </BasicTable>
    <InputForm ref="inputFormRef" :inputs="props.inputs" @saveData="saveData" @openSearchModal="openSearchModal" class="col-span-3"></InputForm>
  </div>
</template>
<style scoped>
</style>