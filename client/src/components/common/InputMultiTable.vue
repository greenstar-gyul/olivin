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
  tableHeight: {
    type: String,
    default: '400px'
  }, // InputTable
});

const inputTableRef = ref(null);
const selectRow = ref(null);



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
}

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
    <!-- :checked="true"
    :checkType="'multiple'" -->
    <BasicTable 
      :data="props.tableData"
      :header="props.tableHeader"
      @rowSelect="onRowSelect"
      @rowUnselect="onRowUnselect"    
    >
      <template #btn>
        <slot name="basicBtn" />
      </template>
    </BasicTable>
  </Fluid>
</template>