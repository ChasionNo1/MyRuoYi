import { getAction, postAction} from '@/api/manage'

// 监控数据
export const getMonitorData = () => getAction('/sys/monitor/getSystemInfo')

