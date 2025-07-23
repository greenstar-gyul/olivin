<script setup>
import InputText from 'primevue/inputtext';
import { ref } from 'vue';
import InputTable from '@/components/table/InputTable.vue';
import InputMaster from '@/components/inputForm/InputMaster.vue';

const inputTableRef = ref(null);

const products = ref([
  { name: "기본", price: null, date: null, text: 'hello'},
  { name: null, price: null, date: null, text: '1000.234' }
]);

const columns = [
  { field: 'name', header: '상품명', inputType: 'item-search' },
  { field: 'date', header: '날짜', inputType: 'date' },
  { field: 'price', header: '가격', inputType: 'number' },
  { field: 'price', header: '가격', inputType: 'number' },
  { field: 'price', header: '가격', inputType: 'number' },
  { field: 'select', header: '콤보박스', inputType: 'select', options: [{ name: '옵션1', value: 1 }, { name: '옵션2', value: 2 }] },
  { field: 'text', header: '텍스트', type: 'number' }
];

const addProductHandler = () => {
  products.value.push({ name: null, price: null, date: null, text: '' });
};

const removeProductHandler = () => {
  const selected = inputTableRef.value.getSelection();
  const selectedIds = selected.map(item => item.id);
  products.value = products.value.filter((p, idx) => !selectedIds.includes(idx + 1));
  inputTableRef.value.clearSelection();
};
</script>
<template>
  <Fluid>
    <InputMaster 
    :masterInputs="{
      title: '마스터 - 디테일 입력 테스트',
      inputs: [
        { name: 'name', label: '이름', type: 'text', placeholder: '이름을 입력하세요' },
        { name: 'email', label: '이메일', type: 'text', placeholder: '이메일을 입력하세요' },
        { name: 'date', label: '날짜', type: 'date', placeholder: '날짜를 선택하세요' },
        { name: 'number', label: '숫자', type: 'number', placeholder: '숫자를 입력하세요' }
      ]
    }"></InputMaster>

    <InputTable :ref="'inputTableRef'" title="테이블 인풋" :data="products" :columns="columns" :selected="true"
      maxHeight="800px">
      <template #btn>
        <Button label="추가" @click="addProductHandler" />
        <Button label="삭제" severity="danger" @click="removeProductHandler" />
      </template>
    </InputTable>
  </Fluid>
</template>
<style scoped>
</style>