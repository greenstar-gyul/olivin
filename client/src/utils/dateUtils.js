// 문자열을 날짜 형식(yyyy-MM-dd)으로 변환하는 함수
export const convertDate = (date) => {
    if (!date) return '';

    const d = new Date(date);
    const year = String(d.getFullYear());
    const month = String(d.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
    const day = String(d.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
};

// 문자열을 날짜 및 시간 형식(yyyy-MM-dd HH:mm:ss)으로 변환하는 함수
export const convertDateTime = (date) => {
    if (!date) return '';

    const d = new Date(date);
    const year = String(d.getFullYear());
    const month = String(d.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

export default { convertDate, convertDateTime };
