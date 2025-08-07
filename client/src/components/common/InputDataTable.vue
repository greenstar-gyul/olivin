<script setup>
import { ref, watch } from 'vue';
import InputMaster from '@/components/inputForm/InputMaster.vue';
import InputTable from '@/components/table/InputTable.vue';

const emit = defineEmits(['submit', 'formSearch', 'tableSearch']);

const props = defineProps({
  title: { 
    type: String,
    default: '폼 기본정보'
  },
  tableTitle: {
    type: String,
    default: '테이블 기본정보'
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
  tableData: {
    type: Array,
    default: () => []
  },
  tableHeight: {
    type: String,
    default: '400px'
  }
});

const inputTableRef = ref(null);


function defaultFormData() {
  const formSchemaIds = (props.formSchema || []).map(e => e.id);

  const schemaBased = formSchemaIds.reduce((acc, id) => {
    acc[id] = props.defaultForm?.[id] || '';
    return acc;
  }, {});

  const fullData = { ...schemaBased };

  // defaultForm에만 있는 값 추가
  Object.keys(props.defaultForm || {}).forEach(key => {
    if (!formSchemaIds.includes(key)) {
      fullData[key] = props.defaultForm[key];
    }
  });

  return fullData;
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

watch(
  () => props.defaultForm,
  (newVal) => {
    formData.value = defaultFormData();
  },
  { deep: true }
);

const resetFormHandler = () => {
  formData.value = defaultFormData();
};

const resetTableHandler = () => {
  tableData.value = defaultTable();
}

const resetInputFormHandler = () => {
  resetFormHandler();
  resetTableHandler();
}

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

// 모달관련

const formSearch = (item, fieldName) => {
  emit('formSearch', item, fieldName);
};

const tableSearch = (item, fieldName) => {
  emit('tableSearch', item, fieldName, tableData);
}

const getFormData = () => {
  return formData;
}

const getTableData = () => {
  return tableData;
}

defineExpose({
  resetFormHandler,
  resetTableHandler,
  getFormData,
  getTableData,
})
</script>
<template>
  <Fluid>
    <InputMaster :title="props.title" :formData="formData" :formSchema="props.formSchema" @formSearch="formSearch">
      <template #btn>
        <Button label="초기화" class="min-w-fit whitespace-nowrap" severity="secondary" @click="resetInputFormHandler" />
        <Button label="저장" @click="saveFormHandler" />
      </template>
    </InputMaster>

    <InputTable 
      :ref="'inputTableRef'"
      :title="props.tableTitle"
      :data="tableData"
      :columns="props.columns"
      :selected="true"
      :maxHeight="props.tableHeight"
      @tableSearch="tableSearch"
    >
      <template #btn>
        <Button label="추가" @click="addProductHandler" />
        <Button label="삭제" severity="danger" @click="removeProductHandler" />
      </template>
    </InputTable>
  </Fluid>
</template>