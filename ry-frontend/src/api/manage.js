// 定义各类请求方法
// get请求，post请求
import request from '@/utils/request'

// post
export const postAction = (url, parameters) => {
    return request({
        url: url,
        method: 'post',
        data: parameters
    })
}

export const getAction = (url, parameters) => {
    return request({
        url: url,
        method: 'get',
        params: parameters
    })
}


