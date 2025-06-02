import { defineStore } from "pinia";
import { ref } from 'vue'

export const useTagsViewStore = defineStore('tags-view', () => {
  const visitedViews = ref([]); // 已访问的标签页列表
  const cachedViews = ref([]); // 缓存的标签页名称列表（通常用于组件缓存）
  const iframeViews = ref([]); // 嵌入的 iframe 标签页列表

  // 添加标签页（同时处理访问记录和缓存）
  const addView = (view) => {
    addVisitedView(view);
    addCachedView(view);
  };

  // 添加 iframe 标签页（避免重复）
  const addIframeView = (view) => {
    if (iframeViews.value.some(v => v.path === view.path)) return;
    iframeViews.value.push(
      Object.assign({}, view, {
        title: view.meta.title || 'no-name' // 确保标题存在
      })
    );
  };

  // 添加已访问标签页（避免重复）
  const addVisitedView = (view) => {
    if (visitedViews.value.some(v => v.path === view.path)) return;
    visitedViews.value.push(
      Object.assign({}, view, {
        title: view.meta.title || 'no-name' // 补全标题
      })
    );
  };

  // 添加缓存标签页（根据路由元信息判断是否缓存）
  const addCachedView = (view) => {
    if (cachedViews.value.includes(view.name)) return; // 避免重复缓存
    if (!view.meta.noCache) { // 若路由元信息允许缓存
      cachedViews.value.push(view.name); // 缓存组件名称
    }
  };

  // 删除单个标签页（返回 Promise 方便异步操作）
  const delView = (view) => {
    return new Promise(resolve => {
      delVisitedView(view);
      delCachedView(view);
      resolve({
        visitedViews: [...visitedViews.value], // 返回更新后的数组（避免引用共享）
        cachedViews: [...cachedViews.value]
      });
    });
  };

  // 删除已访问标签页
  const delVisitedView = (view) => {
    return new Promise(resolve => {
      // 遍历查找并删除对应路径的标签页
      for (const [i, v] of visitedViews.value.entries()) {
        if (v.path === view.path) {
          visitedViews.value.splice(i, 1); // 移除标签页
          break;
        }
      }
      // 同时移除对应的 iframe 标签页
      iframeViews.value = iframeViews.value.filter(item => item.path !== view.path);
      resolve([...visitedViews.value]); // 返回更新后的已访问列表
    });
  };

  // 删除 iframe 标签页
  const delIframeView = (view) => {
    return new Promise(resolve => {
      iframeViews.value = iframeViews.value.filter(item => item.path !== view.path); // 过滤指定路径
      resolve([...iframeViews.value]); // 返回更新后的 iframe 列表
    });
  };

  // 删除缓存标签页
  const delCachedView = (view) => {
    return new Promise(resolve => {
      const index = cachedViews.value.indexOf(view.name); // 根据组件名称查找缓存索引
      index > -1 && cachedViews.value.splice(index, 1); // 存在则移除
      resolve([...cachedViews.value]); // 返回更新后的缓存列表
    });
  };

  // 删除其他所有标签页（保留当前页和固定标签页）
  const delOthersViews = (view) => {
    return new Promise(resolve => {
      delOthersVisitedViews(view); // 删除其他已访问标签页
      delOthersCachedViews(view); // 删除其他缓存标签页
      resolve({
        visitedViews: [...visitedViews.value],
        cachedViews: [...cachedViews.value]
      });
    });
  };

  // 删除其他已访问标签页（保留当前页和固定标签页）
  const delOthersVisitedViews = (view) => {
    return new Promise(resolve => {
      // 保留固定标签页（meta.affix为true）和当前页
      visitedViews.value = visitedViews.value.filter(v => {
        return v.meta.affix || v.path === view.path;
      });
      // 仅保留当前页的 iframe 标签页
      iframeViews.value = iframeViews.value.filter(item => item.path === view.path);
      resolve([...visitedViews.value]);
    });
  };

  // 删除其他缓存标签页（保留当前页的缓存）
  const delOthersCachedViews = (view) => {
    return new Promise(resolve => {
      const index = cachedViews.value.indexOf(view.name); // 查找当前页的缓存索引
      if (index > -1) {
        // 仅保留当前页的缓存
        cachedViews.value = cachedViews.value.slice(index, index + 1);
      } else {
        // 若无当前页缓存，则清空所有缓存
        cachedViews.value = [];
      }
      resolve([...cachedViews.value]);
    });
  };

  // 删除所有标签页（保留固定标签页）
  const delAllViews = (view) => {
    return new Promise(resolve => {
      delAllVisitedViews(view); // 删除所有非固定标签页
      delAllCachedViews(view); // 清空所有缓存
      resolve({
        visitedViews: [...visitedViews.value],
        cachedViews: [...cachedViews.value]
      });
    });
  };

  // 删除所有已访问标签页（仅保留固定标签页）
  const delAllVisitedViews = (view) => {
    return new Promise(resolve => {
      // 过滤出固定标签页
      const affixTags = visitedViews.value.filter(tag => tag.meta.affix);
      visitedViews.value = affixTags;
      iframeViews.value = []; // 清空所有 iframe 标签页
      resolve([...visitedViews.value]);
    });
  };

  // 清空所有缓存标签页
  const delAllCachedViews = (view) => {
    return new Promise(resolve => {
      cachedViews.value = []; // 直接清空缓存列表
      resolve([...cachedViews.value]);
    });
  };

  // 更新已访问标签页信息（如标题变化）
  const updateVisitedView = (view) => {
    for (let v of visitedViews.value) {
      if (v.path === view.path) {
        Object.assign(v, view); // 合并更新后的路由信息
        break;
      }
    }
  };

  // 删除右侧所有标签页（从当前页右侧开始删除）
  const delRightTags = (view) => {
    return new Promise(resolve => {
      const index = visitedViews.value.findIndex(v => v.path === view.path); // 查找当前页索引
      if (index === -1) return; // 若不存在，直接返回

      visitedViews.value = visitedViews.value.filter((item, idx) => {
        // 保留当前页、左侧标签页和固定标签页
        if (idx <= index || (item.meta && item.meta.affix)) {
          return true;
        }
        // 删除右侧标签页的缓存和 iframe（如果有）
        const i = cachedViews.value.indexOf(item.name);
        i > -1 && cachedViews.value.splice(i, 1);
        if (item.meta.link) {
          const fi = iframeViews.value.findIndex(v => v.path === item.path);
          fi > -1 && iframeViews.value.splice(fi, 1);
        }
        return false;
      });
      resolve([...visitedViews.value]);
    });
  };

  // 删除左侧所有标签页（从当前页左侧开始删除）
  const delLeftTags = (view) => {
    return new Promise(resolve => {
      const index = visitedViews.value.findIndex(v => v.path === view.path); // 查找当前页索引
      if (index === -1) return; // 若不存在，直接返回

      visitedViews.value = visitedViews.value.filter((item, idx) => {
        // 保留当前页、右侧标签页和固定标签页
        if (idx >= index || (item.meta && item.meta.affix)) {
          return true;
        }
        // 删除左侧标签页的缓存和 iframe（如果有）
        const i = cachedViews.value.indexOf(item.name);
        i > -1 && cachedViews.value.splice(i, 1);
        if (item.meta.link) {
          const fi = iframeViews.value.findIndex(v => v.path === item.path); // 修正为 item.path
          fi > -1 && iframeViews.value.splice(fi, 1);
        }
        return false;
      });
      resolve([...visitedViews.value]);
    });
  };

  // 导出所有状态和方法
  return {
    visitedViews, 
    cachedViews, 
    iframeViews, 
    addView, 
    addIframeView, 
    addVisitedView, 
    addCachedView, 
    delView, 
    delVisitedView, 
    delIframeView, 
    delCachedView, 
    delOthersViews, 
    delOthersVisitedViews, 
    delOthersCachedViews, 
    delAllViews, 
    delAllVisitedViews, 
    delAllCachedViews, 
    updateVisitedView, 
    delRightTags, 
    delLeftTags
  };
}, {
  persist: true // 启用持久化存储（默认 localStorage）
});

export default useTagsViewStore;