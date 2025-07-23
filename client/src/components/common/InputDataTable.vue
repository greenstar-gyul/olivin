<script setup>
import { ref } from 'vue';
import InputMaster from '@/components/inputForm/InputMaster.vue';
import InputTable from '@/components/table/InputTable.vue';

const emit = defineEmits(['submit']);

const props = defineProps({
  title: { 
    type: String,
    default: '폼 기본정보'
  },
  defaultForm: {
    type: Object,
    default: () => ({})
  },
  formSchema: {
    type: Array,
    required: true
  },
  defaultTable: {
    type: Object,
    default: () => ({})
  },
  columns: {
    type: Array,
    required: true
  },
  tableHeight: {
    type: String,
    default: '400px'
  }
});

const inputTableRef = ref(null);


function defaultFormData() {
  return props.formSchema.map(element => ({
    [element.id]: props.defaultForm[element.id] || ''
  })).reduce((acc, curr) => ({
    ...acc, ...curr
  }), {});
}

function defaultTableData() {
  return props.columns.map(column => ({
    [column.field]: props.defaultTable[column.field] || ''
  })).reduce((acc, curr) => ({
    ...acc, ...curr
  }), {});
}

function defaultTable() {
  return [ defaultTableData() ];
}

const formData = ref(defaultFormData());
const tableData = ref(defaultTable());

const resetFormHandler = () => {
  formData.value = defaultFormData();
  tableData.value = defaultTable();
};

const saveFormHandler = () => {
  emit('submit', formData.value, tableData.value.map(({id, ...rest}) => rest));
};

const addProductHandler = () => {
  tableData.value.push({ ...props.columns.map(column => ({
      [column.field]: props.defaultTable[column.field] || ''
    })).reduce((acc, curr) => ({
      ...acc, ...curr
    }), {}), id: tableData.value.reduce(
    (max, current) => (current.id > max ? current.id : max)
    , 0) + 1 
  });
};

const removeProductHandler = () => {
  const selected = inputTableRef.value.getSelection();
  const selectedIds = selected.map(item => item.id);
  
  tableData.value = tableData.value.filter((p, idx) => !selectedIds.includes(p.id));
  inputTableRef.value.clearSelection();
};
</script>
<template>
  <Fluid>
    <InputMaster title="발주서정보" :formData="formData" :formSchema="props.formSchema">
      <template #btn>
        <Button label="초기화" class="min-w-fit whitespace-nowrap" severity="secondary" @click="resetFormHandler" />
        <Button label="저장" @click="saveFormHandler" />
      </template>
    </InputMaster>

    <InputTable 
      :ref="'inputTableRef'"
      title="상품 목록"
      :data="tableData"
      :columns="props.columns"
      :selected="true"
      :maxHeight="props.tableHeight"
    >
      <template #btn>
        <Button label="추가" @click="addProductHandler" />
        <Button label="삭제" severity="danger" @click="removeProductHandler" />
      </template>
    </InputTable>
  </Fluid>
</template>