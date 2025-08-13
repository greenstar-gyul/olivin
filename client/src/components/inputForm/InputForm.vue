<script setup>
import { ref, watch } from 'vue';
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

// ✅ resetClick 이벤트 추가
const emit = defineEmits(['saveData', 'openSearchModal', 'fileSelected', 'fileUploaded', 'fileRemoved', 'resetClick']);

// 검색 조건을 담을 객체
const inputDatas = ref({});

// Input 데이터를 바인딩.
watch(
    () => props.init,
    (newVal) => {
        if (newVal) {
            inputDatas.value = { ...newVal };
        } else {
            inputDatas.value = {}; // 선택 해제 시 초기화
        }
    },
    { immediate: true }
);

// inputs 기반으로 기본값 초기화, 각 필터의 name을 키로 사용.
// 단, dateRange 타입의 필터는 fromValue와 toValue로 분리하여 처리
const initializeInputDatas = () => {
    const options = {};
    props.inputs.inputs.forEach((element) => {
        if (element.type === 'dateRange') {
            options[element.name + 'From'] = '';
            options[element.name + 'To'] = '';
            return;
        }
        if (element.type === 'file') {
            options[element.name] = null;
            options[element.name + '_preview'] = '';
            return;
        }
        options[element.name] = '';
    });
    inputDatas.value = options;
};

// 파일 처리 함수들
const handleFileSelect = (event) => {
    const inputName = event.target?.name || 'productImage'; // 기본값으로 productImage 사용
    if (event.files?.length > 0) {
        const file = event.files[0];
        inputDatas.value[inputName] = file;

        // 이미지 미리보기 생성
        if (file.type.startsWith('image/')) {
            const reader = new FileReader();
            reader.onload = (e) => {
                inputDatas.value[inputName + '_preview'] = e.target.result;
            };
            reader.readAsDataURL(file);
        }

        emit('fileSelected', { inputName: inputName, file: file });
    }
};

const handleFileUpload = (event) => {
    const inputName = event.target?.name || 'productImage';
    emit('fileUploaded', { inputName: inputName, file: inputDatas.value[inputName] });
};

const handleFileRemove = (event) => {
    const inputName = event.target?.name || 'productImage';
    inputDatas.value[inputName] = null;
    inputDatas.value[inputName + '_preview'] = '';
    emit('fileRemoved', { inputName: inputName });
};

const handleFileClear = (event) => {
    const inputName = event.target?.name || 'productImage';
    inputDatas.value[inputName] = null;
    inputDatas.value[inputName + '_preview'] = '';
    emit('fileRemoved', { inputName: inputName });
};

const clearFilePreview = (inputName) => {
    inputDatas.value[inputName] = null;
    inputDatas.value[inputName + '_preview'] = '';
    emit('fileRemoved', { inputName: inputName });
};

// 초기화
initializeInputDatas();

const resetInputDatas = () => {
    initializeInputDatas();
};

// ✅ 새로운 초기화 핸들러 - 부모에게 이벤트 전송
const handleReset = () => {
    console.log('InputForm - 초기화 버튼 클릭');
    
    // 1. 기존 초기화 실행
    resetInputDatas();
    
    // 2. 부모(StandardInput)에게 알림
    emit('resetClick');
};

const confirm = () => {
    emit('saveData', inputDatas.value);
};

const openSearchModal = (inputName) => {
    emit('openSearchModal', inputName);
};

defineExpose({
    inputDatas,
    resetInputDatas
});
</script>
<template>
    <div class="card flex flex-col gap-4 mt-6 h-full">
        <!-- 테이블 상단 (타이틀 + 엑셀 다운로드 버튼) -->
        <div class="grid grid-cols-1 gap-4 mb-4">
            <div class="flex justify-between">
                <div>
                    <div class="font-semibold text-2xl">{{ inputs.title }}</div>
                </div>
                <div class="flex items-center gap-2 flex-nowrap">
                    <!-- ✅ 초기화 버튼에 새로운 핸들러 연결 -->
                    <Button label="초기화" severity="secondary" @click="handleReset" outlined />
                    <Button label="등록" @click="confirm" outlined />
                    <!-- <Button label="엑셀 다운로드" severity="success" class="min-w-fit whitespace-nowrap" outlined /> -->
                </div>
            </div>
        </div>

        <!-- 입력 필드들 (2개씩 자동 배치) -->
        <div class="grid grid-cols-2 gap-4">
            <div v-for="(input, index) in inputs.inputs" :key="input.name || index" class="grid grid-cols-12 gap-2" :class="input.type === 'dateRange' || input.type === 'textarea' || input.type === 'file' ? 'col-span-2' : 'col-span-1'">
                <label :for="input.label" class="flex items-center col-span-12 mb-2 md:col-span-3 md:mb-0 text-lg font-medium">{{ input.label }}</label>
                <div v-if="input.type !== 'textarea' && input.type !== 'file'" class="col-span-12 md:col-span-9 flex">
                    <!-- Text Input -->
                    <InputText v-if="input.type === 'text'" :id="'input-' + index" type="text" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1" :readonly="input.readonly" />

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
                    <InputNumber v-else-if="input.type === 'number'" :id="'input-' + index" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter number...'" class="flex-1" />

                    <!-- Select Input -->
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

                    <!-- Default fallback to text input -->
                    <InputText v-else :id="'input-' + index" type="text" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1" />
                </div>
                <div v-else-if="input.type === 'textarea'" class="col-span-12 md:col-span-12 flex">
                    <!-- Textarea Input -->
                    <Textarea v-if="input.type === 'textarea'" :id="'input-' + index" v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1" rows="3" />
                </div>
                <div v-else-if="input.type === 'file'" class="col-span-12 md:col-span-12">
                    <!-- File Upload -->
                    <div class="flex-1">
                        <FileUpload
                            :id="'input-' + index"
                            :name="input.name"
                            :accept="input.accept || 'image/*'"
                            :maxFileSize="input.maxFileSize || 10000000"
                            :multiple="input.multiple || false"
                            customUpload
                            @select="handleFileSelect"
                            @uploader="handleFileUpload"
                            @remove="handleFileRemove"
                            @clear="handleFileClear"
                            chooseLabel="선택"
                            uploadLabel="업로드"
                            cancelLabel="취소"
                        >
                            <template #empty>
                                <div class="text-center py-2">
                                    <i class="pi pi-cloud-upload text-sm text-gray-400"></i>
                                    <p class="text-xs text-gray-500 mt-1">{{ input.placeholder || '파일 선택' }}</p>
                                </div>
                            </template>
                        </FileUpload>

                        <!-- 이미지 미리보기 -->
                        <div v-if="inputDatas[input.name + '_preview']" class="mt-2 text-center">
                            <img :src="inputDatas[input.name + '_preview']" alt="미리보기" class="w-full h-16 object-cover rounded border" />
                            <button @click="clearFilePreview(input.name)" class="mt-1 px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 w-full">제거</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>