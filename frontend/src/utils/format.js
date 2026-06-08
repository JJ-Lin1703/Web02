export const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

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

export const formatNumber = (num, decimals = 2) => {
  if (num === null || num === undefined) return '--'
  return Number(num).toFixed(decimals)
}

export const formatHeight = (height) => {
  return formatNumber(height, 1)
}

export const formatWeight = (weight) => {
  return formatNumber(weight, 1)
}

export const formatBmi = (bmi) => {
  return formatNumber(bmi, 2)
}

export const formatCalories = (calories) => {
  return formatNumber(calories, 2)
}