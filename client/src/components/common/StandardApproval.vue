<script setup>
import { ref } from 'vue';
import InputForm from '../inputForm/ApproveInputForm.vue';
import InputMaster from '../inputForm/InputMaster.vue';
import SearchForm from '../inputForm/SearchForm.vue';
import BasicTable from '../table/BasicTable.vue';

const emit = defineEmits(['searchData', 'approve', 'reject']); 
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

const searchData = (searchOptions) => {
  emit('searchData', searchOptions);
};

const saveData = (inputData) => {
  emit('saveData', inputData);
};

// ✅ 승인/반려 함수 추가
const approve = () => {
  emit('approve', selectedItems.value);
};

const reject = () => {
  emit('reject', selectedItems.value);
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
</script>

<template>
  <SearchForm :filters="props.filters" @searchData="searchData" />
  <div class="grid grid-cols-7 gap-4 mb-4 items-stretch">
    <BasicTable :data="props.items" :header="props.header" :checked="true" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect" class="col-span-4">
      <template #btn>
        <Button label="일괄승인" severity="success" class="min-w-fit whitespace-nowrap" outlined ></Button>
        <Button label="일괄반려" severity="danger" class="min-w-fit whitespace-nowrap" outlined ></Button>
      </template>
    </BasicTable>
    <InputForm :inputs="props.inputs" @saveData="saveData" class="col-span-3">
      <template #btn>
        <Button label="승인" severity="success" class="min-w-fit whitespace-nowrap" outlined ></Button>
        <Button label="반려" severity="danger" class="min-w-fit whitespace-nowrap" outlined ></Button>
      </template>
    </InputForm>
  </div>
</template>

<style scoped>
</style>