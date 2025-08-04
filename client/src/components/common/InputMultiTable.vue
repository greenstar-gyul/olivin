<script setup>
import { ref, watch } from 'vue';
import InputMaster from '@/components/inputForm/InputMaster.vue';
import InputTable from '@/components/table/InputTable.vue';
import BasicTable from '../table/BasicTable.vue';

const emit = defineEmits(['onRowSelect']);

/*
props
required: true인 값은 필수로 적어줘야함 
*/
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
  }, // InputMaster 
  tableHeader: {
    type: Object,
    required: true
  }, // BasicTable
  tableData: {
    type: Array,
    default: () => []
  }, // BasicTable
  detailData: {
    type: Object,
    default: () => ({})
  }, // 초기값
  detailColumns: {
    type: Array,
    required: true
  }, // InputTable
  detailCRUD: {
    type: Boolean,
    default: false
  }, // 추가/삭제 버튼 활성화 
  tableHeight: {
    type: String,
    default: '400px'
  }, // InputTable
});

const inputTableRef = ref(null);
const selectRow = ref(null);

const detailTableData = ref([]);

watch(
  () => props.detailData,
  (newVal) => {
    if (newVal.length > 0) {
      detailTableData.value = newVal;
    } else {
      detailTableData.value = [];
    }
  },
  { immediate: true }
);

//수정요함
const resetFormHandler = () => {
  detailTableData.value = [];
};

const onRowSelect = (select) => {
  selectRow.value = select;
  emit('onRowSelect', select);
};

const onRowUnselect = () => {
  selectRow.value = null;
  detailTableData.value = [];
}

const addProductHandler = () => {
  detailTableData.value.push({ ...props.detailColumns.map(column => ({
      [column.field]: props.detailData[column.field] || '',
    })).reduce((acc, curr) => ({
      ...acc, ...curr
    }), {}), id: detailTableData.value.reduce(
    (max, current) => (current.id > max ? current.id : max)
    , 0) + 1 
  });
};

const removeProductHandler = () => {
  const selected = inputTableRef.value.getSelection();
  const selectedIds = selected.map(item => item.id);
  
  detailTableData.value = detailTableData.value.filter((p, idx) => !selectedIds.includes(p.id));
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
      :checkType="'single'"
      :data="props.tableData"
      :header="props.tableHeader"
      @rowSelect="onRowSelect"
      @rowUnselect="onRowUnselect"
    />

    <!-- detail table -->
    <InputTable 
      :ref="'inputTableRef'"
      title="제품 상세 목록"
      :data="detailTableData"
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