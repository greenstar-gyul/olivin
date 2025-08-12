<script setup>
import { ref, watch } from 'vue';

// props 및 emits
const props = defineProps({
    visible: Boolean,
    items: {
        type: Array,
        required: true
    },
    dataKey: {
        type: String,
        default: 'id'
    },
    mapper: {
        type: Array,
        required: true
    },
    title: {
        type: String,
        default: ''
    },
    placeholder: {
        type: String,
        default: ''
    },
    selectedHeader: {
        type: Array,
        default: []
    }
});
const emit = defineEmits(['update:visible', 'confirm', 'search']);

const selectedItems = ref([]);
const searchKeyword = ref('');

// const multiplePopupItems = ref([]);
const visibleFields = ref([]);

// visible 상태 양방향 바인딩
watch(
    () => props.visible,
    (val) => {
        if (!val) selectedItems.value = null;
    }
);

// 데이터가 바뀔 때마다 열 추출
watch(
    () => props.items,
    (newVal) => {
        if (props.selectedHeader.length > 0) return; // selectedHeader가 있을 경우 watch 종료.

        if (Array.isArray(newVal) && newVal.length > 0) {
            visibleFields.value = Object.keys(newVal[0]);
        } else {
            visibleFields.value = [];
        }
    },
    { immediate: true }
);

watch(
    () => props.selectedHeader,
    (newVal) => {
        if (newVal.length > 0) {
            visibleFields.value = newVal;
        } else if (Array.isArray(props.items) && props.items.length > 0) {
            visibleFields.value = Object.keys(props.items[0]);
        } else {
            visibleFields.value = [];
        }
    },
    { immediate: true }
);

const cancel = () => {
    emit('update:visible', false);
};

const confirm = () => {
    emit('confirm', selectedItems.value);
    emit('update:visible', false);
};

const searchOrders = () => {
    // console.log('검색 실행:', searchKeyword.value);
    // 실제 검색 로직은 부모에서 props로 넘겨도 되고, emit 해도 됨
    emit('search', searchKeyword.value);
};
</script>

<template>
    <Dialog :visible="visible" modal :header="title" :style="{ width: '70vw' }" :closable="false">
        <!-- 검색창 -->
        <div class="flex items-center gap-2 mb-4">
            <InputText v-model="searchKeyword" :placeholder="props.placeholder" class="flex-1" />
            <Button label="검색" severity="info" @click="searchOrders" />
        </div>

        <!-- 주문 테이블 -->
        <DataTable :value="items" v-model:selection="selectedItems" :dataKey="dataKey" showGridlines scrollable scrollHeight="300px">
            <Column selectionMode="multiple" headerStyle="width: 3rem" />

            <!-- 동적 컬럼 생성 -->
            <Column v-for="item in visibleFields" :key="item" :field="item" :header="mapper[item] ?? item" />

            <!-- <Column field="ord_code" header="주문번호" />
      <Column field="ord_date" header="주문일자" />
      <Column field="ord_name" header="주문명" />
      <Column field="client" header="거래처" />
      <Column field="delivery_date" header="납기일" />
      <Column field="priority" header="우선순위" /> -->
        </DataTable>

        <!-- 버튼 영역 -->
        <div class="flex justify-center gap-3 mt-4">
            <Button label="취소" severity="contrast" @click="cancel" />
            <Button label="확인" severity="warning" @click="confirm" />
        </div>
    </Dialog>
</template>
