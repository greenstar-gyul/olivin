<script setup>
import ViewDataTable from '@/components/common/FormViewTable.vue';

/* View Data */

// 폼 기본값
const defaultForm = {
    itemName: '사구',
    description: '사구 설명',
    date: '2023-10-01',
    price: 1234.06,
    select: 'I0231',
    data: 0.11
};

// 폼 스키마
const formSchema = [
    { label: '상품명', id: 'itemName' },
    { label: '상품설명', id: 'description' },
    { label: '날짜', id: 'date', data: 'date' },
    { label: '가격', id: 'price', data: 'number' },
    { label: '콤보박스', id: 'select' },
    { label: '데이터', id: 'data', data: 'number' }
];

/* View Table */

// 테이블 기본값
const defaultTable = [
    {
        id: '1',
        name: '사구',
        date: '2023-10-01',
        select: 'I0231',
        price: 100,
        text: 'hello'
    },
    {
        id: '2',
        name: '사구2',
        date: '2023-10-02',
        select: 'I0232',
        price: 200,
        text: 'world'
    },
    {
        id: '3',
        name: '사구3',
        date: '2023-10-03',
        select: 'I0233',
        price: 300,
        text: 'vue'
    }
];

// 테이블 컬럼 정의
const columns = {
    title: '상품 목록',
    header: {
        id: 'ID',
        name: '제품명',
        date: '날짜',
        select: '선택',
        price: '가격',
        text: '텍스트'
    },
    rightAligned: ['price']
};

/* redirect */
const redirectToList = () => {
    console.log('목록으로 이동');
};
</script>
<template>
    <ViewDataTable title="발주서정보" :defaultForm="defaultForm" :formSchema="formSchema" :defaultTable="defaultTable" :columns="columns" @redirect="redirectToList" />
</template>
