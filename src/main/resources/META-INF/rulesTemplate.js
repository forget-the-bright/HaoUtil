//相加
function add(a,b){
    return a+b;
}
//相减
function sub(a,b){
    return a-b;
}
//打印
function print(obj){
    System.out.println(obj);
}

// 1. 向上取整
function ceil(a) {
    return java.lang.Math.ceil(a); // 使用 java.lang.Math.ceil() 来向上取整
}

// 2. 向下取整
function floor(a) {
    return java.lang.Math.floor(a); // 使用 java.lang.Math.floor() 来向下取整
}

// 3. 四舍五入
function round(a) {
    return java.lang.Math.round(a); // 使用 java.lang.Math.round() 进行四舍五入
}

// 4. 获取最大值
function max(a,b) {
    return java.lang.Math.max(a, b); // 使用 java.lang.Math.max() 获取最大值
}

// 5. 获取最小值
function min(a,b) {
    return java.lang.Math.min(a, b); // 使用 java.lang.Math.min() 获取最小值
}

// 6. 求幂 (base^exp)
function power(base,exp) {
    return java.lang.Math.pow(base, exp); // 使用 java.lang.Math.pow() 计算 base 的 exp 次幂
}

// 7. 求平方根
function sqrt(a) {
    return java.lang.Math.sqrt(a); // 使用 java.lang.Math.sqrt() 来计算平方根
}

// 8. 获取随机数
function random() {
    return java.lang.Math.random(); // 使用 java.lang.Math.random() 生成随机数
}

// 9. 计算正弦
function sin(a) {
    return java.lang.Math.sin(a); // 使用 java.lang.Math.sin() 计算正弦（弧度制）
}

// 10. 计算余弦
function cos(a) {
    return java.lang.Math.cos(a); // 使用 java.lang.Math.cos() 计算余弦（弧度制）
}

// 11. 计算正切
function tan(a) {
    return java.lang.Math.tan(a); // 使用 java.lang.Math.tan() 计算正切（弧度制）
}

// 12. 求自然对数
function log(a) {
    return java.lang.Math.log(a); // 使用 java.lang.Math.log() 计算自然对数
}

// 13. 求以10为底的对数
function log10(a) {
    return java.lang.Math.log10(a); // 使用 java.lang.Math.log10() 计算以 10 为底的对数
}

// 14. 求指数 (e^x)
function exp(a) {
    return java.lang.Math.exp(a); // 使用 java.lang.Math.exp() 计算 e^x
}

// 15. 求反正弦
function asin(a) {
    return java.lang.Math.asin(a); // 使用 java.lang.Math.asin() 计算反正弦（弧度制）
}

// 16. 求反余弦
function acos(a) {
    return java.lang.Math.acos(a); // 使用 java.lang.Math.acos() 计算反余弦（弧度制）
}

// 17. 求反正切
function atan(a) {
    return java.lang.Math.atan(a); // 使用 java.lang.Math.atan() 计算反正切（弧度制）
}

// 18. 求坐标点 (x, y) 和 x 轴的夹角
function atan2(y,x) {
    return java.lang.Math.atan2(y, x); // 使用 java.lang.Math.atan2() 计算坐标 (x, y) 和 x 轴的夹角（弧度制）
}

// 19. 去掉小数部分
function trunc(a) {
    return java.lang.Math.floor(a); // 使用 java.lang.Math.floor() 去掉小数部分
}

function now(){
    return new java.util.Date();
}

function toDate(date){
    if (date == null){
        return new cn.hutool.core.date.DateTime();
    }
    return new cn.hutool.core.date.DateTime(date);
}
function dateTime(date){
    if (date == null){
        return cn.hutool.core.date.DateUtil.formatDateTime(now());
    }
    return cn.hutool.core.date.DateUtil.formatDateTime(date);
}
function offsetWeek(date, offset){
    if (date == null || offset == null){
        return null;
    }
    return cn.hutool.core.date.DateUtil.offsetWeek(date,offset);
}
function offsetMonth(date, offset){
    if (date == null || offset == null){
        return null;
    }
    return cn.hutool.core.date.DateUtil.offsetMonth(date,offset);
}
function offsetYear(date, offset){
    if (date == null || offset == null){
        return null;
    }
    return cn.hutool.core.date.DateUtil.offset(date, cn.hutool.core.date.DateField.YEAR,offset);
}
function offsetDay(date, offset){
    if (date == null || offset == null){
        return null;
    }
    return cn.hutool.core.date.DateUtil.offsetDay(date,offset);
}
function offsetHour(date, offset){
    if (date == null || offset == null){
        return null;
    }
    return cn.hutool.core.date.DateUtil.offsetHour(date,offset);
}
function offsetMinute(date, offset){
    if (date == null || offset == null){
        return null;
    }
    return cn.hutool.core.date.DateUtil.offsetMinute(date,offset);
}
