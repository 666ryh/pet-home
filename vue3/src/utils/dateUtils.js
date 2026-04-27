/**
 * 日期格式化工具类
 */

/**
 * 格式化日期
 * @param {Date|string|number} date 日期对象/日期字符串/时间戳
 * @param {string} format 格式化模式 支持：
 * YYYY: 年
 * MM: 月
 * DD: 日
 * HH: 时
 * mm: 分
 * ss: 秒
 * @returns {string} 格式化后的日期字符串
 */
function formatDate(date, format = 'YYYY-MM-DD') {
  // 如果日期是Java LocalDateTime格式的字符串，先进行处理
  if (typeof date === 'string' && date.includes('LocalDateTime')) {
    date = parseLocalDateTime(date);
  }
  
  // 转换输入日期为Date对象
  const dateObj = date instanceof Date ? date : new Date(date);
  
  // 如果日期无效则返回空字符串
  if (isNaN(dateObj.getTime())) {
    return '';
  }

  // 获取日期各个部分
  const year = dateObj.getFullYear();
  const month = dateObj.getMonth() + 1;
  const day = dateObj.getDate();
  const hours = dateObj.getHours();
  const minutes = dateObj.getMinutes();
  const seconds = dateObj.getSeconds();

  // 补零函数
  const padZero = (num) => num.toString().padStart(2, '0');

  // 替换格式字符串
  return format
    .replace('YYYY', year)
    .replace('MM', padZero(month))
    .replace('DD', padZero(day))
    .replace('HH', padZero(hours))
    .replace('mm', padZero(minutes))
    .replace('ss', padZero(seconds));
}

/**
 * 解析Java LocalDateTime格式的字符串
 * @param {string} dateStr LocalDateTime字符串 
 * @returns {string} 标准日期时间字符串，可被new Date()解析
 */
function parseLocalDateTime(dateStr) {
  try {
    // 尝试匹配如 "2023-04-01T10:30:00" 格式的字符串
    const dateMatch = dateStr.match(/(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})/);
    if (dateMatch) {
      return `${dateMatch[1]}-${dateMatch[2]}-${dateMatch[3]} ${dateMatch[4]}:${dateMatch[5]}:${dateMatch[6]}`;
    }
    return dateStr;
  } catch (e) {
    console.error('解析LocalDateTime失败:', e, dateStr);
    return dateStr;
  }
}

/**
 * 格式化为年月 YYYY-MM
 * @param {Date|string|number} date 日期
 * @returns {string}
 */
function formatYearMonth(date) {
  return formatDate(date, 'YYYY-MM');
}

/**
 * 格式化为年月日 YYYY-MM-DD
 * @param {Date|string|number} date 日期
 * @returns {string}
 */
function format(date) {
  return formatDate(date, 'YYYY-MM-DD');
}

/**
 * 格式化为完整日期时间 YYYY-MM-DD HH:mm:ss
 * @param {Date|string|number} date 日期
 * @returns {string}
 */
function formatDateTime(date) {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss');
}

// 导出所有函数
export {
  formatDate,
  format,
  formatYearMonth,
  formatDateTime,
  parseLocalDateTime
};

// 兼容原来的使用方式，保留原有的DateUtils类
export default class DateUtils {
  static format(date, format = 'YYYY-MM-DD') {
    return formatDate(date, format);
  }
  
  static parseLocalDateTime(dateStr) {
    return parseLocalDateTime(dateStr);
  }

  static formatYearMonth(date) {
    return formatYearMonth(date);
  }

  static formatDate(date) {
    return format(date);
  }

  static formatDateTime(date) {
    return formatDateTime(date);
  }
} 