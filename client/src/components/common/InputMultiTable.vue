<script setup>
import { ref, watch } from 'vue';
import InputMaster from '@/components/inputForm/InputMaster.vue';
import InputTable from '@/components/table/InputTable.vue';
import BasicTable from '../table/BasicTable.vue';

const emit = defineEmits(['onRowSelect']);

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
  tableHeader: {
    type: Object,
    required: true
  },
  tableData: {
    type: Array,
    default: () => []
  },
  detailData: {
    type: Object,
    default: () => ({})
  },
  detailColumns: {
    type: Array,
    required: true
  },
  detailCRUD: {
    type: Boolean,
    default: false
  },
  tableHeight: {
    type: String,
    default: '400px'
  }
});

const inputTableRef = ref(null);
const selectRow = ref(null);

const tableData = ref([]);

watch(
  () => props.detailData,
  (newVal) => {
    if (newVal.length > 0) {
      tableData.value = newVal;
    } else {
      tableData.value = [];
    }
  },
  { immediate: true }
);

//수정요함
const resetFormHandler = () => {
  tableData.value = [];
};

const onRowSelect = (select) => {
  selectRow.value = select;
  emit('onRowSelect', select);
};

const onRowUnselect = () => {
  selectRow.value = null;
  tableData.value = [];
}

const addProductHandler = () => {
  tableData.value.push({ ...props.detailColumns.map(column => ({
      [column.field]: props.detailData[column.field] || '',
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
    <InputMaster :title="props.title" :formData="props.defaultForm" :formSchema="props.formSchema">
      <template #btn>
        <Button label="초기화" class="min-w-fit whitespace-nowrap" severity="secondary" @click="resetFormHandler" />
        <slot name="btn" />
      </template>
    </InputMaster>

    <!-- middle table -->
    <BasicTable 
      :checked="true"
      :data="props.tableData"
      :header="props.tableHeader"
      @rowSelect="onRowSelect"
      @rowUnselect="onRowUnselect"
    />

    <!-- detail table -->
    <InputTable 
      :ref="'inputTableRef'"
      title="제품 상세 목록"
      :data="tableData"
      :columns="props.detailColumns"
      :selected="true"
      :maxHeight="props.tableHeight"
    >
      <template #btn>
        <slot name="detailBtn" />
        <template v-if="props.detailCRUD">
          <Button label="추가" @click="addProductHandler" :disabled="!selectRow" />
          <Button label="삭제" severity="danger" @click="removeProductHandler" :disabled="!selectRow" />
        </template>
      </template>
    </InputTable>
  </Fluid>
</template>