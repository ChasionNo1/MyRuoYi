// src/utils/echarts.js
import * as echarts from 'echarts/core'; // 引入核心模块

// 按需引入图表类型
import { BarChart, LineChart, PieChart, GaugeChart } from 'echarts/charts';

// 按需引入组件
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent
} from 'echarts/components';

// 引入 Canvas 渲染器
import { CanvasRenderer } from 'echarts/renderers';

// 注册必须的组件
echarts.use([
  BarChart,
  LineChart,
  PieChart,
  GaugeChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent,
  CanvasRenderer
]);

export default echarts;