import { format, formatDistanceToNow, parseISO, isToday, isYesterday } from 'date-fns'
import { ko } from 'date-fns/locale'

/**
 * 날짜를 한국어 형식으로 포맷
 */
export const formatDate = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = typeof dateString === 'string' ? parseISO(dateString) : dateString
    return format(date, 'yyyy년 MM월 dd일 HH:mm', { locale: ko })
  } catch (error) {
    console.warn('날짜 포맷 오류:', error)
    return dateString
  }
}

/**
 * 상대적 시간 표시 (예: 3시간 전)
 */
export const formatRelativeTime = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = typeof dateString === 'string' ? parseISO(dateString) : dateString
    
    if (isToday(date)) {
      return formatDistanceToNow(date, { addSuffix: true, locale: ko })
    } else if (isYesterday(date)) {
      return '어제 ' + format(date, 'HH:mm', { locale: ko })
    } else {
      return format(date, 'MM월 dd일', { locale: ko })
    }
  } catch (error) {
    console.warn('상대 시간 포맷 오류:', error)
    return dateString
  }
}

/**
 * 오늘 날짜를 한국어로 표시
 */
export const getTodayString = () => {
  return format(new Date(), 'yyyy년 MM월 dd일 (eeee)', { locale: ko })
}

/**
 * 날짜 유효성 검사
 */
export const isValidDate = (dateString) => {
  if (!dateString) return false
  
  try {
    const date = typeof dateString === 'string' ? parseISO(dateString) : dateString
    return date instanceof Date && !isNaN(date)
  } catch {
    return false
  }
}
