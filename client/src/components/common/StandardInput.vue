<script setup>
import { ref, nextTick } from 'vue';
import InputForm from '../inputForm/InputForm.vue';
import InputMaster from '../inputForm/InputMaster.vue';
import SearchForm from '../inputForm/SearchForm.vue';
import BasicTable from '../table/BasicTable.vue';

// ✅ formReset 이벤트 추가
const emit = defineEmits(['searchData', 'saveData', 'openSearchModal', 'rowSelect', 'rowUnselect', 'formReset']);

// ✅ loading prop 추가
const props = defineProps({
    filters: {
        type: Object, // ✅ Array에서 Object로 변경
        required: true
    },
    items: {
        type: Array,
        default: () => []
    },
    header: {
        type: Object,
        required: true
    },
    inputs: {
        type: Object,
        required: true
    },
    loading: {
        type: Boolean,
        default: false
    },
    scrollHeight: {
        type: String,
        default: '400px'
    },
    checkType: {
        type: String,
        default: 'single'
    }
});

const selectedItems = ref(null);

const searchFormRef = ref(null);
const inputFormRef = ref(null);
const basicTableRef = ref(null);

const searchData = (searchOptions) => {
    emit('searchData', searchOptions);
};

const saveData = (inputData) => {
    emit('saveData', inputData);
};

const onRowSelect = (data) => {
    if (props.checkType === 'single') {
        selectedItems.value = data;
    } else {
        if (!selectedItems.value) {
            selectedItems.value = [];
        }
        selectedItems.value.push(data);
    }

    emit('rowSelect', data);
};

const onRowUnselect = (data) => {
    if (props.checkType !== 'single') {
        selectedItems.value = selectedItems.value.filter((item) => item !== data);
    } else {
        selectedItems.value = null;
    }

    emit('rowUnselect', data);
};

const openSearchModal = (inputName) => {
    emit('openSearchModal', inputName);
};

// ✅ InputForm에서 초기화 버튼 클릭 시 호출되는 핸들러
const handleResetClick = () => {
    console.log('StandardInput - 초기화 버튼 클릭 감지');
    
    // 1. 테이블 선택 상태 초기화 (Vue 상태)
    selectedItems.value = null;
    
    // 2. BasicTable 컴포넌트 초기화
    if (basicTableRef.value && basicTableRef.value.clearSelection) {
        basicTableRef.value.clearSelection();
    }
    
    // 3. DOM 직접 조작으로 확실하게 해제
    nextTick(() => {
        clearAllSelections();
    });
    
    // 4. 부모 컴포넌트에 초기화 완료 알림
    emit('formReset');
    
    console.log('StandardInput - 전체 초기화 완료');
};

// ✅ DOM에서 모든 선택 상태 직접 초기화
const clearAllSelections = () => {
    console.log('🔧 DOM에서 모든 선택 상태 해제 시작');
    
    try {
        // 1. 모든 라디오 버튼 해제
        const radioInputs = document.querySelectorAll('.p-datatable-tbody .p-radiobutton input');
        radioInputs.forEach(input => {
            if (input.checked) {
                input.checked = false;
                console.log('라디오 버튼 해제됨');
            }
        });
        
        // 2. 모든 라디오 버튼 스타일 해제
        const radioButtons = document.querySelectorAll('.p-datatable-tbody .p-radiobutton');
        radioButtons.forEach(button => {
            if (button.classList.contains('p-radiobutton-checked')) {
                button.classList.remove('p-radiobutton-checked');
                console.log('라디오 버튼 스타일 해제됨');
            }
        });
        
        // 3. 모든 체크박스 해제 (다중 선택인 경우)
        const checkboxInputs = document.querySelectorAll('.p-datatable-tbody .p-checkbox input');
        checkboxInputs.forEach(input => {
            if (input.checked) {
                input.checked = false;
                console.log('체크박스 해제됨');
            }
        });
        
        // 4. 모든 체크박스 스타일 해제
        const checkboxes = document.querySelectorAll('.p-datatable-tbody .p-checkbox');
        checkboxes.forEach(checkbox => {
            if (checkbox.classList.contains('p-checkbox-checked')) {
                checkbox.classList.remove('p-checkbox-checked');
                console.log('체크박스 스타일 해제됨');
            }
        });
        
        // 5. 모든 행 선택 상태 해제
        const selectedRows = document.querySelectorAll('.p-datatable-tbody tr.p-datatable-row-selected');
        selectedRows.forEach(row => {
            row.classList.remove('p-datatable-row-selected');
            row.setAttribute('aria-selected', 'false');
            console.log('행 선택 상태 해제됨');
        });
        
        // 6. 헤더 체크박스도 해제 (전체 선택인 경우)
        const headerCheckbox = document.querySelector('.p-datatable-thead .p-checkbox input');
        const headerCheckboxBox = document.querySelector('.p-datatable-thead .p-checkbox');
        
        if (headerCheckbox && headerCheckbox.checked) {
            headerCheckbox.checked = false;
            console.log('헤더 체크박스 해제됨');
        }
        
        if (headerCheckboxBox && headerCheckboxBox.classList.contains('p-checkbox-checked')) {
            headerCheckboxBox.classList.remove('p-checkbox-checked');
            console.log('헤더 체크박스 스타일 해제됨');
        }
        
        console.log('✅ DOM 선택 상태 모두 해제 완료');
        
    } catch (error) {
        console.error('❌ DOM 조작 중 오류 발생:', error);
    }
};

// ✅ 외부에서 직접 호출할 수 있는 초기화 메서드
const resetAll = () => {
    console.log('StandardInput - 외부에서 전체 초기화 호출됨');
    
    // 1. InputForm 초기화
    if (inputFormRef.value && inputFormRef.value.resetInputDatas) {
        inputFormRef.value.resetInputDatas();
    }
    
    // 2. 테이블 초기화
    handleResetClick();
};

defineExpose({
    searchFormRef,
    inputFormRef,
    basicTableRef,
    handleResetClick,
    clearAllSelections,
    resetAll
});
</script>

<template>
    <SearchForm 
        ref="searchFormRef" 
        :filters="props.filters" 
        @searchData="searchData" 
        @openSearchModal="openSearchModal" 
    />
    
    <div class="grid grid-cols-7 gap-4 mb-4 items-stretch">
        <BasicTable 
            ref="basicTableRef"
            :data="props.items" 
            :header="props.header" 
            :checked="true" 
            :loading="props.loading"
            :scrollHeight="props.scrollHeight" 
            v-model:selection="selectedItems"
            @rowSelect="onRowSelect" 
            @rowUnselect="onRowUnselect" 
            class="col-span-4"
        >
            <template #btn>
                <slot name="btn"></slot>
            </template>
        </BasicTable>
        
        <InputForm 
            ref="inputFormRef" 
            :inputs="props.inputs" 
            :loading="props.loading"
            @saveData="saveData" 
            @openSearchModal="openSearchModal" 
            @resetClick="handleResetClick"
            class="col-span-3"
        />
    </div>
</template>

<style scoped></style>