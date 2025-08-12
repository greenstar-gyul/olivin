<script setup>
import { ref, watch, nextTick } from 'vue';
import DatePickerFromTo from './DatePickerFromTo.vue';

const props = defineProps({
    inputs: {
        type: Object,
        required: true
    },
    init: {
        type: Object
    }
});

const emit = defineEmits(['saveData', 'openSearchModal']);

// 입력 데이터 상태
const inputDatas = ref({});

// 기본값 초기화
const initializeInputDatas = () => {
    const options = {};
    props.inputs.inputs.forEach((element) => {
        if (element.type === 'dateRange') {
            options[element.name + 'From'] = '';
            options[element.name + 'To'] = '';
            return;
        }
        options[element.name] = '';
    });
    inputDatas.value = options;
};

// 초기화 버튼
const resetInputDatas = () => {
    initializeInputDatas();
    focusFirstInput();
};

// 첫 번째 입력창 포커스
const focusFirstInput = async () => {
    await nextTick();
    const firstInput = document.querySelector('input, select, textarea');
    if (firstInput) firstInput.focus({ preventScroll: true });
};

// init 값 변경 감지
watch(
    () => props.init,
    (newVal) => {
        if (newVal && newVal.employeeId) {
            inputDatas.value = { ...newVal };
        } else {
            initializeInputDatas();
        }
        focusFirstInput();
    },
    { immediate: true }
);

// 등록 버튼 클릭 시 insert/update 판단
const onSave = () => {
    const mode = inputDatas.value.employeeId ? 'update' : 'insert';
    emit('saveData', inputDatas.value, mode);
};

function openSearchModal(inputName) {
    emit('openSearchModal', inputName);
}

defineExpose({
    inputDatas
});
</script>

<template>
    <div class="card flex flex-col gap-4 mt-6 h-full">
        <!-- 상단 -->
        <div class="grid grid-cols-1 gap-4 mb-4">
            <div class="flex justify-between">
                <div>
                    <div class="font-semibold text-2xl">{{ inputs.title }}</div>
                </div>
                <div class="flex items-center gap-2 flex-nowrap">
                    <Button label="초기화" severity="secondary" @click="resetInputDatas" outlined />
                    <Button label="등록" severity="primary" @click="onSave" outlined />
                </div>
            </div>
        </div>

        <!-- 입력 필드 -->
        <div class="grid grid-cols-2 gap-4">
            <div v-for="(input, index) in inputs.inputs" :key="input.name || index" class="grid grid-cols-12 gap-2" :class="input.type === 'dateRange' || input.type === 'textarea' ? 'col-span-2' : 'col-span-1'">
                <label :for="'input-' + index" class="flex items-center col-span-12 mb-2 md:col-span-3 md:mb-0">
                    {{ input.label }}
                </label>

                <div v-if="input.type !== 'textarea'" class="col-span-12 md:col-span-9 flex">
                    <!-- Text -->
                    <InputText v-if="input.type === 'text'" :id="'input-' + index" type="text" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1" />

                    <!-- Date -->
                    <DatePicker
                        v-else-if="input.type === 'date'"
                        :id="'input-' + index"
                        v-model="inputDatas[input.name]"
                        :placeholder="input.placeholder || 'Select date...'"
                        dateFormat="yy-mm-dd"
                        class="flex-1"
                        :show-icon="true"
                        :show-button-bar="true"
                    />

                    <!-- Date Range -->
                    <DatePickerFromTo
                        v-else-if="input.type === 'dateRange'"
                        v-model:fromValue="inputDatas[input.name + 'From']"
                        v-model:toValue="inputDatas[input.name + 'To']"
                        :fromPlaceholder="input.fromPlaceholder"
                        :toPlaceholder="input.toPlaceholder"
                        class="flex-1"
                    />

                    <!-- Number -->
                    <InputNumber v-else-if="input.type === 'number'" :id="'input-' + index" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter number...'" class="flex-1" />

                    <!-- Select -->
                    <Select
                        v-else-if="input.type === 'select'"
                        :id="'input-' + index"
                        v-model="inputDatas[input.name]"
                        :options="input.options"
                        optionLabel="name"
                        optionValue="value"
                        :placeholder="input.placeholder || 'Select option...'"
                        class="flex-1"
                    />

                    <!-- Item Search -->
                    <InputGroup v-else-if="input.type === 'item-search'" class="flex-1">
                        <InputText :id="'input-' + index" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter item name...'" />
                        <Button icon="pi pi-search" class="p-button-outlined" @click="openSearchModal(input.name)" />
                    </InputGroup>

                    <!-- Default -->
                    <InputText v-else :id="'input-' + index" type="text" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1" />
                </div>

                <!-- Textarea -->
                <div v-else class="col-span-12 md:col-span-12 flex">
                    <Textarea :id="'input-' + index" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1" rows="3" />
                </div>
            </div>
        </div>
    </div>
</template>
