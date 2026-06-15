/**
 * @file format.js
 * @description 格式化工具函数集合
 * @author SmartHealth Team
 * @date 2024
 */

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param {Date|string|number} date - 日期对象、日期字符串或时间戳
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm:ss 格式
 * @param {Date|string|number} date - 日期对象、日期字符串或时间戳
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化数字，保留指定小数位数
 * @param {number} num - 待格式化的数字
 * @param {number} [decimals=2] - 小数位数，默认为2
 * @returns {string} 格式化后的数字字符串
 */
export const formatNumber = (num, decimals = 2) => {
  if (num === null || num === undefined) return '--'
  return Number(num).toFixed(decimals)
}

/**
 * 格式化身高（保留1位小数）
 * @param {number} height - 身高值（单位：cm）
 * @returns {string} 格式化后的身高字符串
 */
export const formatHeight = (height) => {
  return formatNumber(height, 1)
}

/**
 * 格式化体重（保留1位小数）
 * @param {number} weight - 体重值（单位：kg）
 * @returns {string} 格式化后的体重字符串
 */
export const formatWeight = (weight) => {
  return formatNumber(weight, 1)
}

/**
 * 格式化BMI值（保留2位小数）
 * @param {number} bmi - BMI值
 * @returns {string} 格式化后的BMI字符串
 */
export const formatBmi = (bmi) => {
  return formatNumber(bmi, 2)
}

/**
 * 格式化卡路里（保留2位小数）
 * @param {number} calories - 卡路里值
 * @returns {string} 格式化后的卡路里字符串
 */
export const formatCalories = (calories) => {
  return formatNumber(calories, 2)
}