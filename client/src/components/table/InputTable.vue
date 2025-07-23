<script setup>
import { ref, computed } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import DatePicker from 'primevue/datepicker';

const props = defineProps({
  data: {
    type: Array,
    required: true
  },
  columns: {
    type: Array,
    required: true
  },
  header: {
    type: Object,
    default: () => ({})
  },
  title: {
    type: String,
    default: '기본 테이블 폼'
  },
  selected: {
    type: Boolean,
    default: false
  },
  maxHeight: {
    type: String,
    default: '400px'
  },
  maxWidth: {
    type: String,
    default: '100%'
  }
});

const selectedProducts = ref([]);

const getSelection = () => {
  return selectedProducts.value;
};

const clearSelection = () => {
  selectedProducts.value = [];
};

defineExpose({
  getSelection,
  clearSelection
})
</script>

<template>
  <div class="card mt-6">
    <div class="grid grid-cols-1 gap-4 mb-4">
      <div class="flex justify-between">
        <div>
          <div class="font-semibold text-2xl">{{ title }}</div>
        </div>
        <div class="flex items-center gap-2 flex-nowrap">
          <slot name="btn"></slot>
        </div>
      </div>
    </div>
    <DataTable v-model:selection="selectedProducts" class="p-datatable-sm"
      :value="data" :dataKey="'id'"
      showGridlines scrollable :scrollHeight="maxHeight"
      :tableStyle="`min-width: 50rem; max-width: ${props.maxWidth};`"
    >
      <Column v-if="props.selected" selectionMode="multiple" headerStyle="width: 3rem"></Column>

      <Column v-for="col in props.columns" :key="col.id" :field="col.field" :header="col.header">
        <template #body="slotProps">
          <!-- Text Input -->
          <InputText v-if="col.inputType === 'text'" v-model="slotProps.data[col.field]" :placeholder="col.placeholder || 'Enter text...'" />

          <!-- Item Search -->
          <InputGroup v-else-if="col.inputType === 'item-search'">
            <InputText v-model="slotProps.data[col.field]" :placeholder="col.placeholder || 'Enter item name...'" />
            <Button icon="pi pi-search" class="p-button-outlined" />
          </InputGroup>

          <!-- DatePicker -->
          <DatePicker v-else-if="col.inputType === 'date'" v-model="slotProps.data[col.field]" :showIcon="true"
            :showButtonBar="true" dateFormat="yy-mm-dd" :placeholder="col.placeholder || 'Select date...'" />

          <!-- Select -->
          <Select v-else-if="col.inputType === 'select'" v-model="slotProps.data[col.field]" :options="col.options"
            optionLabel="name" :placeholder="col.placeholder || 'Select option...'" />

          <!-- Number Input -->
          <InputNumber v-else-if="col.inputType === 'number'" v-model="slotProps.data[col.field]" inputId="integeronly"
            :fluid="true" :placeholder="col.placeholder || 'Enter number...'" />

          <!-- Data -->
          <template v-else>
            <span v-if="col.type === 'text'" class="text-left">
              {{ slotProps.data[col.field] }}
            </span>
            <span v-else-if="col.type === 'number'" class="text-right">
              {{ Number(slotProps.data[col.field]).toLocaleString() }}
            </span>
          </template>
        </template>
      </Column>
    </DataTable>
  </div>
</template>