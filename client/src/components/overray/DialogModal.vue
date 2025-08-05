<script setup>
import { InputGroup, InputText } from 'primevue';
import { onMounted, ref, watch, computed } from 'vue';

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

// 취소 버튼 누를 경우 선택 초기화
const close = () => {
  emit('close');
  initializeSelectedProduct();
}

// 확인 버튼 눌렀을 때 선택한 값 넘겨주는 부분
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

// Dialog 크기를 동적으로 계산
const dialogWidth = computed(() => {
  const headerCount = props.headers.length;
  const itemCount = props.items.length;
  
  // 컬럼 수와 데이터 수에 따라 크기 조절
  let width = 30; // 최소 30vw
  
  // 컬럼이 많을수록 크기 증가
  if (headerCount >= 6) {
    width = 60; // 최대 60vw
  } else if (headerCount >= 3) {
    width = 45; // 중간 크기
  }
  
  return `${width}vw`;
});

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
    :style="{ width: dialogWidth }" :modal="true" :closable="false">
    <div class="flex flex-col gap-4">
        <!-- Item Search -->
        <InputGroup class="flex-1">
          <InputText :id="'filter'" v-model="searchValue" :placeholder="placeholder || 'Enter item name...'" />
          <Button icon="pi pi-search" class="p-button-outlined" @click="searchModal(searchValue)" />
        </InputGroup>

      <DataTable v-model:selection="selectedItems" :value="props.items" :selectionMode="props.selectionMode"
        :paginator="false" :rows="5" @row-select="onProductSelect" :scrollable="true" scrollHeight="400px">
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