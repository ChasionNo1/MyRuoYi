import { createRouter, createWebHistory } from "vue-router";
import Layout from "@/layout";
import AuthLayout from "@/layout/components/auth/AuthLayout.vue";

// 公共路由
export const constantRoutes = [
  {
    path: "/redirect",
    component: Layout,
    hidden: true,
    children: [
      {
        //  /redirect/:path(.*) 是一个 动态路由，用于匹配以 /redirect/ 开头的任意路径
        path: "/redirect/:path(.*)",
        component: () => import("@/views/redirect/index.vue"),
      },
    ],
  },
  {
    path: "/auth",
    component: AuthLayout,
    redirect: "/auth/login",
    hidden: true,
    children: [
      {
        path: "login",
        name: 'login',
        component: () => import("@/views/login"),
        hidden: true,
      },
      {
        path: 'register',
        name: 'register',
        component: () => import("@/views/register")
      }
    ],
  },

  // {
  //   path: "/:pathMatch(.*)*",
  //   component: () => import("@/views/error/404"),
  //   hidden: true,
  // },
  // {
  //   path: "/401",
  //   component: () => import("@/views/error/401"),
  //   hidden: true,
  // },
  {
    path: "",
    component: Layout,
    redirect: "/index",
    children: [
      {
        path: "/index",
        component: () => import("@/views/index"),
        name: "Index",
        meta: { title: "首页", icon: "dashboard", affix: true },
      },
    ],
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  }
];
 export const dynamicRoutes = []
// 动态路由，基于用户权限动态去加载
// export const dynamicRoutes = [
//   {
//     path: '/system/user-auth',
//     component: Layout,
//     hidden: true,
//     permissions: ['system:user:edit'],
//     children: [
//       {
//         path: 'role/:userId(\\d+)',
//         component: () => import('@/views/system/user/authRole'),
//         name: 'AuthRole',
//         meta: { title: '分配角色', activeMenu: '/system/user' }
//       }
//     ]
//   },
//   {
//     path: '/system/role-auth',
//     component: Layout,
//     hidden: true,
//     permissions: ['system:role:edit'],
//     children: [
//       {
//         path: 'user/:roleId(\\d+)',
//         component: () => import('@/views/system/role/authUser'),
//         name: 'AuthUser',
//         meta: { title: '分配用户', activeMenu: '/system/role' }
//       }
//     ]
//   },
//   {
//     path: '/system/dict-data',
//     component: Layout,
//     hidden: true,
//     permissions: ['system:dict:list'],
//     children: [
//       {
//         path: 'index/:dictId(\\d+)',
//         component: () => import('@/views/system/dict/data'),
//         name: 'Data',
//         meta: { title: '字典数据', activeMenu: '/system/dict' }
//       }
//     ]
//   },
//   {
//     path: '/monitor/job-log',
//     component: Layout,
//     hidden: true,
//     permissions: ['monitor:job:list'],
//     children: [
//       {
//         path: 'index/:jobId(\\d+)',
//         component: () => import('@/views/monitor/job/log'),
//         name: 'JobLog',
//         meta: { title: '调度日志', activeMenu: '/monitor/job' }
//       }
//     ]
//   },
//   {
//     path: '/tool/gen-edit',
//     component: Layout,
//     hidden: true,
//     permissions: ['tool:gen:edit'],
//     children: [
//       {
//         path: 'index/:tableId(\\d+)',
//         component: () => import('@/views/tool/gen/editTable'),
//         name: 'GenEdit',
//         meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
//       }
//     ]
//   }
// ]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  },
});

export default router;
