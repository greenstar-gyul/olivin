<script setup>
import InputText from 'primevue/inputtext';
import { ref } from 'vue';
import InputTable from '@/components/table/InputTable.vue';
import StandardInput from '@/components/common/StandardInput.vue';
import SearchTable from '@/components/common/SearchTable.vue';

const inputTableRef = ref(null);

const products = ref([
  { name: "기본", price: null, date: null, text: 'hello'},
  { name: null, price: null, date: null, text: '1000.234' }
]);

const columns = [
  { field: 'name', header: '상품명', inputType: 'item-search' },
  { field: 'date', header: '날짜', inputType: 'date' },
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
    <div class="card flex flex-col gap-4">
      <div class="flex justify-between items-center">
        <div class="font-semibold text-xl">발주서 정보</div>
        <div class="flex gap-2">
          <Button label="등록"></Button>
          <Button label="취소" severity="secondary" />
        </div>
      </div>
      <div class="grid grid-cols-2 gap-6">
        <!-- 왼쪽 컬럼 -->
        <div class="flex flex-col gap-4">
          <div class="grid grid-cols-12 gap-2">
            <label for="name1" class="flex items-center justify-center col-span-12 mb-2 sm:col-span-3 sm:mb-0 bg-green-200">Name</label>
            <div class="col-span-12 sm:col-span-9">
              <InputText id="name1" type="text" />
            </div>
          </div>
          <div class="grid grid-cols-12 gap-2">
            <label for="email1" class="flex items-center justify-center col-span-12 mb-2 sm:col-span-3 sm:mb-0 bg-green-200">Email</label>
            <div class="col-span-12 sm:col-span-9">
              <InputText id="email1" type="text" />
            </div>
          </div>
        </div>
          
          <!-- 오른쪽 컬럼 -->
          <div class="flex flex-col gap-4">
            <div class="grid grid-cols-12 gap-2">
              <label for="name2" class="flex items-center justify-center col-span-12 mb-2 sm:col-span-3 sm:mb-0 bg-green-200">Name</label>
              <div class="col-span-12 sm:col-span-9">
                <InputText id="name2" type="text" />
              </div>
            </div>
            <div class="grid grid-cols-12 gap-2">
              <label for="email2" class="flex items-center justify-center col-span-12 mb-2 sm:col-span-3 sm:mb-0 bg-green-200">Email</label>
              <div class="col-span-12 sm:col-span-9">
                <InputText id="email2" type="text" />
              </div>
            </div>
          </div>
      </div>
    </div>

    <SearchTable>

    </SearchTable>
    <InputTable 
      :ref="'inputTableRef'"
      title="테이블 인풋"
      :data="products"
      :columns="columns"
      :selected="true"
      maxHeight="800px"
    >
      <template #btn>
        <Button label="추가" @click="addProductHandler" />
        <Button label="삭제" severity="danger" @click="removeProductHandler" />
      </template>
    </InputTable>
  </Fluid>
</template>