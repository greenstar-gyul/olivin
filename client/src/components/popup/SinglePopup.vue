<script setup>
import { ref, watch } from 'vue';

// Props & Emits
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
        type: Object,
        default: () => ({})
    },
    title: {
        type: String,
        default: ''
    },
    placeholder: {
        type: String,
        default: ''
    },
    columns: {
        type: Array,
        default: []
    },
    selectedHeader: {
        type: Array,
        default: []
    }
});

const emit = defineEmits(['update:visible', 'confirm', 'search']);

// 내부 상태
const selectedItem = ref(null);
const searchKeyword = ref('');
const visibleFields = ref([]);

// 테이블 컬럼 자동 추출
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

// 팝업 닫힐 때 선택 초기화
watch(
    () => props.visible,
    (val) => {
        if (!val) selectedItem.value = null;
    }
);

// 버튼 핸들러
const cancel = () => {
    emit('update:visible', false);
};

const confirm = () => {
    if (selectedItem.value) {
        emit('confirm', selectedItem.value);
        emit('update:visible', false);
    }
};

const searchOrders = () => {
    // 검색 로직은 emit 후 부모에서 처리 가능
    emit('search', searchKeyword.value);
};

const rowClass = (data) => {
    return data.disabled ? 'p-disabled-row' : '';
};

const handleRowSelect = (event) => {
    if (event.data.disabled) {
        selectedItem.value = null;
        return;
    }

    selectedItem.value = event.data;
    confirm(); // ✅ 선택 즉시 confirm 호출
};
</script>

<template>
    <Dialog :visible="visible" modal :header="title" :style="{ width: '70vw' }" :closable="false">
        <!-- 검색창 -->
        <div class="flex items-center gap-2 mb-4">
            <InputText v-model="searchKeyword" :placeholder="props.placeholder" class="flex-1" />
            <Button label="검색" severity="info" @click="searchOrders" />
        </div>

        <!-- 데이터 테이블 -->
        <DataTable :value="items" v-model:selection="selectedItem" selectionMode="single" :dataKey="dataKey" showGridlines scrollable scrollHeight="300px" :rowClass="rowClass" @rowSelect="handleRowSelect">
            <Column selectionMode="single" headerStyle="width: 3rem" />

            <Column v-for="field in visibleFields" :key="field" :field="field" :header="mapper[field] ?? field" />
        </DataTable>

        <!-- 버튼 영역 -->
        <div class="flex justify-center gap-3 mt-4">
            <Button label="취소" severity="contrast" @click="cancel" />
            <Button label="확인" severity="warning" :disabled="!selectedItem" @click="confirm" />
        </div>
    </Dialog>
</template>
<style scoped>
.p-disabled-row {
    pointer-events: none;
    opacity: 0.5;
}
</style>
