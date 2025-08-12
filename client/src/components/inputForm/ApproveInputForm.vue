<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import DatePickerFromTo from './DatePickerFromTo.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Textarea from 'primevue/textarea';
import DatePicker from 'primevue/datepicker';
import Select from 'primevue/select';

const props = defineProps({
    inputs: {
        type: Object,
        required: true
    }
});

const emit = defineEmits(['saveData', 'approve', 'reject']);

// ✅ 간소화된 데이터 관리
const inputDatas = ref({});

console.log('🏗️ ApproveInputForm 컴포넌트 초기화');

// ✅ 초기화 함수 개선
const initializeInputDatas = () => {
    console.log('🔄 ApproveInputForm - 데이터 초기화');

    const options = {};
    if (props.inputs && props.inputs.inputs) {
        props.inputs.inputs.forEach((element) => {
            if (element.type === 'dateRange') {
                options[element.name + 'From'] = '';
                options[element.name + 'To'] = '';
                return;
            }
            // props에서 전달된 value 값을 사용
            options[element.name] = element.value || '';
        });
    }

    inputDatas.value = options;
    console.log('✅ ApproveInputForm - 초기화 완료:', inputDatas.value);
};

// ✅ props.inputs가 변경될 때만 업데이트 (무한루프 방지)
watch(
    () => props.inputs,
    (newInputs) => {
        if (newInputs && newInputs.inputs) {
            console.log('📥 ApproveInputForm - props.inputs 변경됨:', newInputs);

            const options = {};
            newInputs.inputs.forEach((element) => {
                if (element.type === 'dateRange') {
                    options[element.name + 'From'] = '';
                    options[element.name + 'To'] = '';
                    return;
                }
                options[element.name] = element.value || '';
            });

            inputDatas.value = options;
            console.log('🔄 ApproveInputForm - inputDatas 업데이트됨:', inputDatas.value);
        }
    },
    { deep: true, immediate: true }
);

// ✅ inputDatas 변경 시 부모에게 알림 (디바운스 적용)
let saveTimeout = null;
watch(
    () => inputDatas.value,
    (newData) => {
        console.log('📤 ApproveInputForm - inputDatas 변경됨:', newData);

        // 디바운스로 불필요한 호출 방지
        if (saveTimeout) {
            clearTimeout(saveTimeout);
        }

        saveTimeout = setTimeout(() => {
            emit('saveData', { ...newData });
            console.log('💾 ApproveInputForm - saveData 이벤트 발송');
        }, 100);
    },
    { deep: true }
);

const resetInputDatas = () => {
    console.log('🔄 ApproveInputForm - 수동 초기화');
    initializeInputDatas();
};

const confirm = () => {
    console.log('✅ ApproveInputForm - 확인 버튼 클릭');
    emit('saveData', { ...inputDatas.value });
};

// ✅ 승인/반려 버튼 이벤트 추가
const handleApprove = () => {
    console.log('✅ ApproveInputForm - 승인 버튼 클릭');
    emit('approve', { ...inputDatas.value });
};

const handleReject = () => {
    console.log('❌ ApproveInputForm - 반려 버튼 클릭');

    // 반려 시 사유 필수 체크
    if (!inputDatas.value.note || inputDatas.value.note.trim() === '') {
        alert('반려 사유를 입력해주세요.');
        return;
    }

    emit('reject', { ...inputDatas.value });
};
</script>

<template>
    <div class="card flex flex-col gap-4 mt-6 h-full">
        <!-- 테이블 상단 (타이틀만) -->
        <div class="grid grid-cols-1 gap-4 mb-4">
            <div>
                <div class="font-semibold text-2xl">{{ inputs.title || '제품 정보' }}</div>
            </div>
        </div>

        <!-- 입력 필드들 (2개씩 자동 배치) -->
        <div class="grid grid-cols-2 gap-4" v-if="inputs && inputs.inputs && inputs.inputs.length > 0">
            <div v-for="(input, index) in inputs.inputs" :key="input.name || index" class="grid grid-cols-12 gap-2" :class="input.type === 'dateRange' || input.type === 'textarea' ? 'col-span-2' : 'col-span-1'">
                <label :for="input.label" class="flex items-center col-span-12 mb-2 md:col-span-3 md:mb-0">
                    {{ input.label }}
                </label>

                <div v-if="input.type !== 'textarea'" class="col-span-12 md:col-span-9 flex">
                    <!-- Text Input -->
                    <InputText v-if="input.type === 'text'" :id="'input-' + index" type="text" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" :readonly="input.readonly" class="flex-1" />

                    <!-- Date Picker -->
                    <DatePicker
                        v-else-if="input.type === 'date'"
                        :id="'input-' + index"
                        v-model="inputDatas[input.name]"
                        :placeholder="input.placeholder || 'Select date...'"
                        dateFormat="yy-mm-dd"
                        class="flex-1"
                        :show-icon="true"
                        :show-button-bar="true"
                        :readonly="input.readonly"
                    />

                    <!-- Date Picker From To -->
                    <DatePickerFromTo
                        v-else-if="input.type === 'dateRange'"
                        v-model:fromValue="inputDatas[input.name + 'From']"
                        v-model:toValue="inputDatas[input.name + 'To']"
                        :fromPlaceholder="input.fromPlaceholder"
                        :toPlaceholder="input.toPlaceholder"
                        class="flex-1"
                    />

                    <!-- Number Input -->
                    <InputNumber v-else-if="input.type === 'number'" :id="'input-' + index" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter number...'" :readonly="input.readonly" class="flex-1" />

                    <!-- Select Input -->
                    <Select
                        v-else-if="input.type === 'select'"
                        :id="'input-' + index"
                        v-model="inputDatas[input.name]"
                        :options="input.options"
                        optionLabel="name"
                        optionValue="value"
                        :placeholder="input.placeholder || 'Select option...'"
                        :disabled="input.readonly"
                        class="flex-1"
                    />

                    <!-- Default fallback to text input -->
                    <InputText v-else :id="'input-' + index" type="text" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" :readonly="input.readonly" class="flex-1" />
                </div>

                <div v-else class="col-span-12 md:col-span-12 flex">
                    <!-- Textarea Input -->
                    <Textarea v-if="input.type === 'textarea'" :id="'input-' + index" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" :readonly="input.readonly" class="flex-1" rows="3" />
                </div>
            </div>
        </div>

        <!-- ✅ inputs가 없을 때를 위한 대체 메시지 -->
        <div v-else class="text-center text-gray-500 py-8">제품을 선택하면 정보가 표시됩니다.</div>

        <!-- ✅ 승인/반려 버튼 - 항상 표시 -->
        <div class="mt-6 flex gap-2 border-t pt-4">
            <Button label="승인" severity="success" @click="handleApprove" class="flex-1" :disabled="!inputDatas.productId" />
            <Button label="반려" severity="danger" @click="handleReject" class="flex-1" :disabled="!inputDatas.productId" />
        </div>
    </div>
</template>
