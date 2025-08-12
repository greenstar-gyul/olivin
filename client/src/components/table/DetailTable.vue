<script setup>
import { ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const emit = defineEmits(['rowSelect', 'rowUnselect', 'actionHandler']);
const props = defineProps({
    data: {
        type: Array,
        required: true
    },
    dataKey: {
        type: String,
        default: 'id'
    },
    header: {
        type: Object,
        required: true
    },
    columns: {
        type: Array,
        default: []
    },
    checked: {
        type: Boolean,
        default: false
    },
    checkType: {
        type: String,
        default: 'single'
    },
    isAction: {
        type: Boolean,
        default: false
    },
    actionName: {
        type: String,
        default: '버튼'
    }
});

const items = ref([]);

// checkType에 따라 selectedItems 초기값 설정
const selectedItems = ref(props.checkType === 'single' ? null : []);

// checkType이 변경되면 selectedItems 초기화
watch(
    () => props.checkType,
    (newType) => {
        selectedItems.value = newType === 'single' ? null : [];
    }
);

// 타입 검증과 값 존재 검증을 해서 값이 있을 때 데이터 추가..
// 문제 있으면 바로 빈배열..
watch(
    () => props.data,
    (newVal) => {
        if (props.columns.length > 0) return; // columns가 있을 경우 watch 종료하고 존재하는 컬럼 사용..

        // header에 정의된 필드들만 사용
        if (props.header && props.header.header) {
            items.value = Object.keys(props.header.header);
        } else if (Array.isArray(newVal) && newVal.length > 0) {
            items.value = Object.keys(newVal[0]);
        } else {
            items.value = [];
        }
    },
    { immediate: true }
);

// 컬럼이 바뀌면 해당 컬럼 목록으로 바꾸기..?
watch(
    () => props.columns,
    (newVal) => {
        if (newVal.length > 0) {
            items.value = newVal;
        } else if (props.header && props.header.header) {
            // header에 정의된 필드들만 사용
            items.value = Object.keys(props.header.header);
        } else if (Array.isArray(props.data) && props.data.length > 0) {
            items.value = Object.keys(props.data[0]);
        } else {
            items.value = [];
        }
    },
    { immediate: true }
);

// header가 변경될 때도 컬럼 업데이트
watch(
    () => props.header,
    (newVal) => {
        if (props.columns.length > 0) return; // columns가 있을 경우 무시

        if (newVal && newVal.header) {
            items.value = Object.keys(newVal.header);
        } else {
            items.value = [];
        }
    },
    { immediate: true }
);

const onRowSelect = (event) => {
    // 선택된 행이 있을 때만 처리
    if (props.checkType === 'single') {
        selectedItems.value = event.data;
    } else {
        // 다중 선택 모드에서는 선택된 아이템을 배열로 유지
        if (!selectedItems.value.includes(event.data)) {
            selectedItems.value.push(event.data);
        }
    }
    emit('rowSelect', event.data);
};

const onRowUnselect = (event) => {
    // 다중 선택 모드에서 행이 선택 해제되면 해당 아이템 제거
    if (props.checkType !== 'single') {
        selectedItems.value = selectedItems.value.filter((item) => item !== event.data);
    }
    emit('rowUnselect', event.data);
};

const actionHandler = (data) => {
    emit('actionHandler', data);
};
</script>
<template>
    <!-- 📋 검색 조회 테이블 영역 -->
    <div class="card flex flex-col gap-4 mt-6 h-full">
        <!-- 테이블 상단 (타이틀 + 엑셀 다운로드 버튼) -->
        <div class="grid grid-cols-1 gap-4 mb-4">
            <div class="flex justify-between">
                <div>
                    <div class="font-semibold text-2xl">{{ header.title }}</div>
                </div>
                <div class="flex items-center gap-2 flex-nowrap">
                    <slot name="btn"></slot>
                    <!-- <Button label="엑셀 다운로드" severity="success" class="min-w-fit whitespace-nowrap" outlined /> -->
                </div>
            </div>
        </div>

        <!-- DataTable (PrimeVue) -->
        <DataTable
            v-model:selection="selectedItems"
            :value="props.data"
            :dataKey="props.dataKey"
            showGridlines
            scrollable
            scrollHeight="400px"
            tableStyle="min-width: 50rem"
            @rowSelect="onRowSelect"
            @rowUnselect="onRowUnselect"
            :selectionMode="props.checked ? props.checkType : null"
        >
            <Column v-if="props.checked" :selectionMode="props.checkType" headerStyle="width: 3rem"></Column>

            <!-- 동적 컬럼 생성 -->
            <Column v-for="item in items" :key="item" :field="item" :header="header.header[item] ?? item">
                <!-- 날짜포맷변경을 위해 추가한 파트 -->
                <template #body="slotProps">
                    <!-- 숫자형 데이터는 오른쪽 정렬하고 3자리 콤마 추가 -->
                    <!-- <span v-if="header.rightAligned && header.rightAligned.includes(item)" class="text-right block">
            {{ slotProps.data[item].toLocaleString() }} -->
                    <!-- </span> -->
                    <!-- 일반 텍스트 데이터는 기본 정렬 -->
                    <!-- <span v-else>
            {{ slotProps.data[item] }}
          </span> -->
                    <span>
                        {{ slotProps.data[item] }}
                    </span>
                </template>
            </Column>

            <Column class="w-24 !text-end">
                <template #body="{ data }">
                    <Button @click="actionHandler(data)" severity="success" rounded>{{ actionName }}</Button>
                </template>
            </Column>
        </DataTable>
    </div>
</template>
<style scoped>
/* 필요시 커스텀 스타일 여기에 추가 */
</style>
