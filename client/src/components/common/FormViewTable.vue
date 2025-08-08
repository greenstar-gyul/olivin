<script setup>
import InputMaster from '@/components/inputForm/InputMaster.vue';
import BasicTable from '../table/BasicTable.vue';

const emit = defineEmits(['submit', 'redirect']);

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
    type: Array,
    default: () => ([])
  },
  columns: {
    type: Object,
    required: true
  },
  tableHeight: {
    type: String,
    default: '400px'
  }
});

const redirectToList = () => {
  emit('redirect');
};
</script>
<template>
  <Fluid>
    <InputMaster :title="props.title" :formData="props.defaultForm" :formSchema="props.formSchema">
      <template #btn>
        <slot name="btn" />
        <Button label="목록으로" class="min-w-fit whitespace-nowrap" severity="secondary" @click="redirectToList" outlined />
      </template>
    </InputMaster>
    <BasicTable :data="props.defaultTable" :header="props.columns" />
  </Fluid>
</template>