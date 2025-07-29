<script setup>
import { InputGroup, InputText } from 'primevue';
import { onMounted, ref, watch } from 'vue';

const emit = defineEmits(['confirm', 'close', 'searchModal']);
const props = defineProps({
  display: {
    type: Boolean,
    default: false
  },
  selectionMode: {
    type: String,
    default: 'single'
  },
  items: {
    type: Array,
    default: () => []
  },
  headers: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: 'Dialog Modal'
  },
  placeholder: {
    type: String,
    default: 'Search...'
  }
});

const selectedItems = ref(null);
const searchValue = ref('');

const initializeSelectedProduct = () => {
  if (props.selectionMode === 'single') {
    selectedItems.value = null;
  } else {
    selectedItems.value = [];
  }
  // 검색 텍스트박스도 초기화
  searchValue.value = '';
};

const close = () => {
  emit('close');
  initializeSelectedProduct();
}

const confirm = () => {
  emit('confirm', selectedItems.value);
  initializeSelectedProduct();
}

const onProductSelect = (event) => {
  if (props.selectionMode === 'single') {
    selectedItems.value = event.data;
  } else {
    if (!selectedItems.value) {
      selectedItems.value = [];
    }
    if (!selectedItems.value.includes(event.data)) {
      selectedItems.value.push(event.data);
    }
  }
};

const searchModal = (searchValue) => {
  emit('searchModal', searchValue);
};

onMounted(() => {
  initializeSelectedProduct();
});

// 모달이 열릴 때마다 초기화
watch(() => props.display, (newValue) => {
  if (newValue) {
    initializeSelectedProduct();
  }
});

</script>
<template>
  <Dialog :header="props.title" v-model:visible="props.display" :breakpoints="{ '960px': '75vw' }"
    :style="{ width: '45vw' }" :modal="true" :closable="false">
    <div class="flex flex-col gap-4">
        <!-- Item Search -->
        <InputGroup class="flex-1">
          <InputText :id="'filter-' + rowIndex + '-' + filterIndex" v-model="searchValue" :placeholder="placeholder || 'Enter item name...'" />
          <Button icon="pi pi-search" class="p-button-outlined" @click="searchModal(searchValue)" />
        </InputGroup>

      <DataTable v-model:selection="selectedItems" :value="props.items" :selectionMode="props.selectionMode"
        :paginator="false" :rows="5" @row-select="onProductSelect" scrollable="true" scrollHeight="400px">
        <Column v-for="col in props.headers" :key="col.field" :field="col.field" :header="col.header">
          <template #body="slotProps">
            {{ slotProps.data[col.field] }}
          </template>
        </Column>
      </DataTable>

      <div class="flex justify-center gap-2 mt-4">
        <Button label="취소" severity="secondary" @click="close" />
        <Button label="확인" @click="confirm" />
      </div>
    </div>
  </Dialog>
</template>
<style></style>