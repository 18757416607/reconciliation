/**
 * easyui  格式化列
 * @param value
 * @param row
 * @param index
 * @returns {string}
 */
function diyFormatter(value,row,index) {
    return "<span title='"+value+"'>"+value+"</span>";
}