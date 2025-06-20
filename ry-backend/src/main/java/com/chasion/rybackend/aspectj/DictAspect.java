package com.chasion.rybackend.aspectj;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO
 * @date 2025/6/20 17:37
 */
package org.jeecg.common.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.File;
import org.jeecg.common.base.StringPool;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.BizFileVO;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: 字典aop类
 * @Author: dangzhenghui
 * @Date: 2019-3-17 21:50
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class DictAspect {
    @Autowired
    public RedisTemplate redisTemplate;
    @Lazy
    @Autowired
    private CommonAPI commonApi;

    /**
     * 定义切点Pointcut  分页翻译
     */
    @Pointcut("execution(public * org.jeecg.modules..*.*Controller.*(..))")
    public void excudeService() {
    }

    /**
     * 定义切点Pointcut   单个对象翻译 切点是使用AutoDictAndFile这个注解
     */
    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.AutoDictAndFile)")
    public void translateAutoDict() {

    }

    /**
     * 定义切点Pointcut   列表翻译
     */
    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.AutoDictList)")
    public void translateAutoDictList() {

    }
    /** 环绕增强 */
    @Around("translateAutoDict()")
    public Object after(ProceedingJoinPoint point) throws Throwable {
        // 执行被切面的方法（即目标方法），并获取其返回值。在此上下文中，这通常是某个Controller方法的执行。
        Object result = point.proceed();
        // 对controller返回的结果进行翻译处理
        this.translateObj(result);
        return result;
    }

    @Around("translateAutoDictList()")
    public Object afterAutoDictList(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();
        this.translateList(result);
        return result;
    }

    /**
     * 翻译集合数据
     *
     * @param result
     */
    private void translateList(Object result) {
        if (result instanceof Result) {
            Collection data = (Collection) ((Result) result).getResult();
            List<JSONObject> items = new ArrayList<>();
            //step.1 筛选出加了 Dict 注解的字段列表
            List<Field> dictFieldList = new ArrayList<>();
            // 字典数据列表， key = 字典code，value=数据列表
            Map<String, List<String>> dataListMap = new HashMap<>(5);
            for (Object record : data) {
                if (Objects.isNull(record)) {
                    continue;
                }
                ObjectMapper mapper = new ObjectMapper();
                String json = "{}";
                try {
                    //解决@JsonFormat注解解析不了的问题详见SysAnnouncement类的@JsonFormat
                    json = mapper.writeValueAsString(record);
                } catch (JsonProcessingException e) {
                    log.error("json解析失败" + e.getMessage(), e);
                }
                //update-begin--Author:scott -- Date:20211223 ----for：【issues/3303】restcontroller返回json数据后key顺序错乱 -----
                JSONObject item = JSONObject.parseObject(json, Feature.OrderedField);
                //update-end--Author:scott -- Date:20211223 ----for：【issues/3303】restcontroller返回json数据后key顺序错乱 -----

                //update-begin--Author:scott -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                //for (Field field : record.getClass().getDeclaredFields()) {
                // 遍历所有字段，把字典Code取出来，放到 map 里
                for (Field field : oConvertUtils.getAllFields(record)) {
                    String value = item.getString(field.getName());
                    if (oConvertUtils.isEmpty(value)) {
                        continue;
                    }
                    //update-end--Author:scott  -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                    if (field.getAnnotation(Dict.class) != null) {
                        if (!dictFieldList.contains(field)) {
                            dictFieldList.add(field);
                        }
                        String code = field.getAnnotation(Dict.class).dicCode();
                        String text = field.getAnnotation(Dict.class).dicText();
                        String table = field.getAnnotation(Dict.class).dictTable();

                        List<String> dataList;
                        String dictCode = code;
                        if (!StringUtils.isEmpty(table)) {
                            dictCode = String.format("%s,%s,%s", table, text, code);
                        }
                        dataList = dataListMap.computeIfAbsent(dictCode, k -> new ArrayList<>());
                        this.listAddAllDeduplicate(dataList, Arrays.asList(value.split(",")));
                    }
                    //date类型默认转换string格式化日期
                    if (CommonConstant.JAVA_UTIL_DATE.equals(field.getType().getName()) && field.getAnnotation(JsonFormat.class) == null && item.get(field.getName()) != null) {
                        SimpleDateFormat aDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                    }
                }
                items.add(item);
            }

            //step.2 调用翻译方法，一次性翻译
            Map<String, List<DictModel>> translText = this.translateAllDict(dataListMap);

            //step.3 将翻译结果填充到返回结果里
            for (JSONObject record : items) {
                for (Field field : dictFieldList) {
                    String code = field.getAnnotation(Dict.class).dicCode();
                    String text = field.getAnnotation(Dict.class).dicText();
                    String table = field.getAnnotation(Dict.class).dictTable();

                    String fieldDictCode = code;
                    if (!StringUtils.isEmpty(table)) {
                        fieldDictCode = String.format("%s,%s,%s", table, text, code);
                    }
                    String value = record.getString(field.getName());
                    if (oConvertUtils.isNotEmpty(value)) {
                        List<DictModel> dictModels = translText.get(fieldDictCode);
                        if (dictModels == null || dictModels.size() == 0) {
                            record.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, record.get(field.getName()));
                            continue;
                        }
                        String textValue = this.translDictText(dictModels, value);
                        record.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, textValue);
                    }
                }
            }
            ((Result) result).setResult(items);
        }
    }

    /**
     * 翻译单个对象
     *
     * @param result
     */
    private void translateObj(Object result) {
        if (result instanceof Result) {
            // 获取data数据
            Object data = ((Result) result).getResult();
            // 不为空
            if (!Objects.isNull(data)) {
                // 获取所有字段
                Field[] fields = ReflectUtil.getFields(data.getClass());
                //负责实现 Java 对象与 JSON 数据之间的相互转换（序列化和反序列化）。
                // 在这段代码中，ObjectMapper mapper = new ObjectMapper();
                // 的作用是创建一个新的 ObjectMapper 实例，用于后续的 JSON 处理
                ObjectMapper mapper = new ObjectMapper();
                String json = "{}";
                try {
                    //解决@JsonFormat注解解析不了的问题详见SysAnnouncement类的@JsonFormat
                    // 当实体类的日期字段使用 @JsonFormat 注解时，直接通过反射获取字段值可能无法正确解析格式，因此先将对象序列化为 JSON 字符串，再重新解析为 JSONObject。
                    json = mapper.writeValueAsString(data);
                } catch (JsonProcessingException e) {
                    log.error("json解析失败" + e.getMessage(), e);
                }
                //update-begin--Author:scott -- Date:20211223 ----for：【issues/3303】restcontroller返回json数据后key顺序错乱 -----
                // 通过 Feature.OrderedField 参数确保 JSON 字段顺序与 Java 对象一致，避免前端显示时字段顺序错乱。
                /* item
                * {
                        "username": "Alice",
                        "birthday": "2023-06-20",  // @JsonFormat生效
                        "sex": 1                  // 原始值，后续会被DictAspect替换为文本
                  }
                * */
                JSONObject item = JSONObject.parseObject(json, Feature.OrderedField);
                if (item != null && fields != null && fields.length > 0) {
                    // 字典数据映射
                    /**
                     *
                     * {
                     *    "sex": ["1", "2"],
                     *     "sys_dict_item,item_text,item_value": ["1001", "1002"]
                     * }
                     * */
                    Map<String, List<String>> dataListMap = new HashMap<>(5);
                    // 文件链接列表
                    List<String> links = new ArrayList<>();
                    // 文件字段映射
                    Map<String, List<String>> fileFieldMap = new HashMap<>();
                    // 遍历所有字段
                    for (Field field : fields) {
                        // 得到值
                        String value = item.getString(field.getName());
                        if (oConvertUtils.isEmpty(value)) {
                            continue;
                        }
                        // 这地方有点冗余了，前面进行了判空
                        String valueStr = value + StringPool.EMPTY;
                        // 字段获取文件注解  @File 注解用于标记实体类中存储文件链接的字段
                        File file = field.getAnnotation(File.class);
                        if (Objects.nonNull(file)) {
                            // 用逗号分割，得到文件链接字符数组
                            String[] fileArray = StringUtils.split(valueStr, StringPool.COMMA);
                            for (String s : fileArray) {
                                // 添加到list里
                                links.add(s);
                            }
                            fileFieldMap.put(valueStr, Arrays.asList(fileArray));
                        }
                        // 字段获取字典注解
                        Dict dict = field.getAnnotation(Dict.class);
                        // 详情翻译默认为false
                        if (dict != null && dict.detailTranslate()) {
                            String code = dict.dicCode();
                            String text = dict.dicText();
                            String table = dict.dictTable();
                            List<String> dataList;
                            // 先设置成code  变量名
                            String dictCode = code;
                            // 如果表字段不为空的话
                            if (!StringUtils.isEmpty(table)) {
                                // 格式化
                                dictCode = String.format("%s,%s,%s", table, text, code);
                            }
                            // 惰性加载，当 Map 中不存在指定的键时，创建一个新的 ArrayList 并与该键关联；如果键已存在，则直接返回对应的列表
                            dataList = dataListMap.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            // 去重添加，将value里的值用逗号分割，然后添加到list里
                            this.listAddAllDeduplicate(dataList, Arrays.asList(valueStr.split(",")));
                        }
                    }
                    // 查询附件列表，根据上面解析出来的地址
                    List<BizFileVO> fileVOList = commonApi.getFileByLinks(links);
                    // 业务文件vo集合
                    Map<String, List<BizFileVO>> fieldBizFileMap = new HashMap<>();
                    // 这段代码的作用是将查询到的文件详情（fileVOList）与原始字段值（fileFieldMap）进行关联匹配，
                    // 构建一个新的映射关系（fieldBizFileMap），以便将文件详情附加到最终返回的数据中
                    /**
                     * fileFieldMap
                     * {
                     *   "link1,link2": ["link1", "link2"],
                     *   "link3": ["link3"]
                     * }
                     *
                     * fileVOList
                     * [
                     *   { link: "link1", name: "图片1.jpg", size: 100 },
                     *   { link: "link2", name: "图片2.jpg", size: 200 }
                     * ]
                     *
                     * fieldBizFileMap
                     * {
                     *   "link1,link2": [
                     *     { link: "link1", name: "图片1.jpg", size: 100 },
                     *     { link: "link2", name: "图片2.jpg", size: 200 }
                     *   ],
                     *   "link3": [] // 没有匹配的文件详情
                     * }
                     * */
                    if (CollectionUtil.isNotEmpty(fileVOList) && CollectionUtil.isNotEmpty(fileFieldMap)) {
                        // fileFieldMap.put(valueStr, Arrays.asList(fileArray));
                        for (String s : fileFieldMap.keySet()) {
                            for (BizFileVO bizFileVO : fileVOList) {
                                if (s.contains(bizFileVO.getLink())) {
                                    List<BizFileVO> list = fieldBizFileMap.computeIfAbsent(s, k -> new ArrayList<>());
                                    list.add(bizFileVO);
                                }
                            }
                        }
                    }
                    //step.2 调用翻译方法，一次性翻译
                    Map<String, List<DictModel>> translText = this.translateAllDict(dataListMap);
                    List<BizFileVO> fileVOS = null;
                    for (Field field : fields) {
                        //date类型默认转换string格式化日期
                        String value = item.getString(field.getName());
                        File file = field.getAnnotation(File.class);
                        if (!Objects.isNull(file) && !Objects.isNull(value) && CollectionUtil.isNotEmpty(fileVOS = fieldBizFileMap.get(value.toString()))) {
                            item.put(field.getName() + CommonConstant.FILE_INFO, fieldBizFileMap.getOrDefault(value.toString(), new ArrayList<>()));
                        } else if (!Objects.isNull(file)) {
                            item.put(field.getName() + CommonConstant.FILE_INFO, new ArrayList<>());
                        }
                        Dict dict = field.getAnnotation(Dict.class);
                        if (dict != null) {
                            String code = dict.dicCode();
                            String text = dict.dicText();
                            String table = dict.dictTable();
                            String fieldDictCode = code;
                            if (!StringUtils.isEmpty(table)) {
                                fieldDictCode = String.format("%s,%s,%s", table, text, code);
                            }
                            if (oConvertUtils.isNotEmpty(value)) {
                                List<DictModel> dictModels = translText.get(fieldDictCode);
                                if (dictModels == null || dictModels.size() == 0) {
                                    continue;
                                }

                                String textValue = this.translDictText(dictModels, value.toString());
                                item.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, textValue);
                            }
                        }
                    }
                    ((Result) result).setResult(item);
                }
            }
        }
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (!Objects.isNull(result)) {
            this.parseDictText(result);
        }
        return result;
    }

    /**
     * 本方法针对返回对象为Result 的IPage的分页列表数据进行动态字典注入
     * 字典注入实现 通过对实体类添加注解@dict 来标识需要的字典内容,字典分为单字典code即可 ，table字典 code table text配合使用与原来jeecg的用法相同
     * 示例为SysUser   字段为sex 添加了注解@Dict(dicCode = "sex") 会在字典服务立马查出来对应的text 然后在请求list的时候将这个字典text，已字段名称加_dictText形式返回到前端
     * 例输入当前返回值的就会多出一个sex_dictText字段
     * {
     * sex:1,
     * sex_dictText:"男"
     * }
     * 前端直接取值sext_dictText在table里面无需再进行前端的字典转换了
     * customRender:function (text) {
     * if(text==1){
     * return "男";
     * }else if(text==2){
     * return "女";
     * }else{
     * return text;
     * }
     * }
     * 目前vue是这么进行字典渲染到table上的多了就很麻烦了 这个直接在服务端渲染完成前端可以直接用
     *
     * @param result
     */
    private void parseDictText(Object result) {
        if (result instanceof Result) {
            if (((Result) result).getResult() instanceof IPage) {
                List<JSONObject> items = new ArrayList<>();

                //step.1 筛选出加了 Dict 注解的字段列表
                List<Field> dictFieldList = new ArrayList<>();
                // 字典数据列表， key = 字典code，value=数据列表
                Map<String, List<String>> dataListMap = new HashMap<>(5);

                for (Object record : ((IPage) ((Result) result).getResult()).getRecords()) {
                    ObjectMapper mapper = new ObjectMapper();
                    String json = "{}";
                    try {
                        //解决@JsonFormat注解解析不了的问题详见SysAnnouncement类的@JsonFormat
                        json = mapper.writeValueAsString(record);
                    } catch (JsonProcessingException e) {
                        log.error("json解析失败" + e.getMessage(), e);
                    }
                    //update-begin--Author:scott -- Date:20211223 ----for：【issues/3303】restcontroller返回json数据后key顺序错乱 -----
                    JSONObject item = JSONObject.parseObject(json, Feature.OrderedField);
                    //update-end--Author:scott -- Date:20211223 ----for：【issues/3303】restcontroller返回json数据后key顺序错乱 -----

                    //update-begin--Author:scott -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                    //for (Field field : record.getClass().getDeclaredFields()) {
                    // 遍历所有字段，把字典Code取出来，放到 map 里
                    for (Field field : oConvertUtils.getAllFields(record)) {
                        String value = item.getString(field.getName());
                        if (oConvertUtils.isEmpty(value)) {
                            continue;
                        }
                        //update-end--Author:scott  -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                        if (field.getAnnotation(Dict.class) != null) {
                            if (!dictFieldList.contains(field)) {
                                dictFieldList.add(field);
                            }
                            String code = field.getAnnotation(Dict.class).dicCode();
                            String text = field.getAnnotation(Dict.class).dicText();
                            String table = field.getAnnotation(Dict.class).dictTable();

                            List<String> dataList;
                            String dictCode = code;
                            if (!StringUtils.isEmpty(table)) {
                                dictCode = String.format("%s,%s,%s", table, text, code);
                            }
                            dataList = dataListMap.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            this.listAddAllDeduplicate(dataList, Arrays.asList(value.split(",")));
                        }
                        //date类型默认转换string格式化日期
                        if (CommonConstant.JAVA_UTIL_DATE.equals(field.getType().getName()) && field.getAnnotation(JsonFormat.class) == null && item.get(field.getName()) != null) {
                            SimpleDateFormat aDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                        }
                    }
                    items.add(item);
                }

                //step.2 调用翻译方法，一次性翻译
                Map<String, List<DictModel>> translText = this.translateAllDict(dataListMap);

                //step.3 将翻译结果填充到返回结果里
                for (JSONObject record : items) {
                    for (Field field : dictFieldList) {
                        String code = field.getAnnotation(Dict.class).dicCode();
                        String text = field.getAnnotation(Dict.class).dicText();
                        String table = field.getAnnotation(Dict.class).dictTable();

                        String fieldDictCode = code;
                        if (!StringUtils.isEmpty(table)) {
                            fieldDictCode = String.format("%s,%s,%s", table, text, code);
                        }

                        String value = record.getString(field.getName());
                        if (oConvertUtils.isNotEmpty(value)) {
                            List<DictModel> dictModels = translText.get(fieldDictCode);
                            if (dictModels == null || dictModels.size() == 0) {
                                record.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, record.get(field.getName()));
                                continue;
                            }

                            String textValue = this.translDictText(dictModels, value);
                            record.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, textValue);
                        }
                    }
                }

                ((IPage) ((Result) result).getResult()).setRecords(items);
            }

        }
    }

    /**
     * list 去重添加
     */
    private void listAddAllDeduplicate(List<String> dataList, List<String> addList) {
        // 筛选出dataList中没有的数据
        // value值 split的数组 转成list，过滤出dataList中没有的
        List<String> filterList = addList.stream().filter(i -> !dataList.contains(i)).collect(Collectors.toList());
        // 然后再添加进去
        dataList.addAll(filterList);
    }

    /**
     * 一次性把所有的字典都翻译了
     * 1.  所有的普通数据字典的所有数据只执行一次SQL
     * 2.  表字典相同的所有数据只执行一次SQL
     *
     * @param dataListMap
     * @return
     */
    private Map<String, List<DictModel>> translateAllDict(Map<String, List<String>> dataListMap) {
        // 翻译后的字典文本，key=dictCode
        Map<String, List<DictModel>> translText = new HashMap<>(5);
        // 需要翻译的数据（有些可以从redis缓存中获取，就不走数据库查询）
        List<String> needTranslData = new ArrayList<>();
        //step.1 先通过redis中获取缓存字典数据
        /**
         *  dataListMap
         * {
         *    "sex": ["1", "2"],
         *     "sys_dict_item,item_text,item_value": ["1001", "1002"]
         * }
         * */
        for (String dictCode : dataListMap.keySet()) {
            // 获取数据列表
            List<String> dataList = dataListMap.get(dictCode);
            if (dataList.size() == 0) {
                continue;
            }
            // 表字典需要翻译的数据
            List<String> needTranslDataTable = new ArrayList<>();
            // 遍历value列表
            for (String s : dataList) {
                String data = s.trim();
                if (data.length() == 0) {
                    continue; //跳过循环
                }
                // 字符串有多个值，用逗号隔开
                if (dictCode.contains(",")) {
                    // 构建redis key
                    String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, data);
                    // 如果key存在
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            // 从redis里获取字典文本
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            // 得到value值，key是dictCode，value是List<DictModel>
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            // 往list里添加字典模型
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                        // 如果redis里没有这个key，并且，需要翻译的表字典里没有这个数据
                    } else if (!needTranslDataTable.contains(data)) {
                        // 去重添加
                        needTranslDataTable.add(data);
                    }
                } else {
                    String keyString = String.format("sys:cache:dict::%s:%s", dictCode, data);
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    } else if (!needTranslData.contains(data)) {
                        // 去重添加
                        needTranslData.add(data);
                    }
                }

            }
            //step.2 调用数据库翻译表字典
            if (needTranslDataTable.size() > 0) {
                String[] arr = dictCode.split(",");
                String table = arr[0], text = arr[1], code = arr[2];
                String values = String.join(",", needTranslDataTable);
                log.info("translateDictFromTableByKeys.dictCode:" + dictCode);
                log.info("translateDictFromTableByKeys.values:" + values);
                List<DictModel> texts = commonApi.translateDictFromTableByKeys(table, text, code, values);
                log.info("translateDictFromTableByKeys.result:" + texts);
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                list.addAll(texts);

                // 做 redis 缓存
                for (DictModel dict : texts) {
                    String redisKey = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, dict.getValue());
                    try {
                        // update-begin-author:taoyan date:20211012 for: 字典表翻译注解缓存未更新 issues/3061
                        // 保留5分钟
                        redisTemplate.opsForValue().set(redisKey, dict.getText(), 300, TimeUnit.SECONDS);
                        // update-end-author:taoyan date:20211012 for: 字典表翻译注解缓存未更新 issues/3061
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }

        //step.3 调用数据库进行翻译普通字典
        if (needTranslData.size() > 0) {
            List<String> dictCodeList = Arrays.asList(dataListMap.keySet().toArray(new String[]{}));
            // 将不包含逗号的字典code筛选出来，因为带逗号的是表字典，而不是普通的数据字典
            List<String> filterDictCodes = dictCodeList.stream().filter(key -> !key.contains(",")).collect(Collectors.toList());
            String dictCodes = String.join(",", filterDictCodes);
            String values = String.join(",", needTranslData);
            log.info("translateManyDict.dictCodes:" + dictCodes);
            log.info("translateManyDict.values:" + values);
            Map<String, List<DictModel>> manyDict = commonApi.translateManyDict(dictCodes, values);
            log.info("translateManyDict.result:" + manyDict);
            for (String dictCode : manyDict.keySet()) {
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                List<DictModel> newList = manyDict.get(dictCode);
                list.addAll(newList);

                // 做 redis 缓存
                for (DictModel dict : newList) {
                    String redisKey = String.format("sys:cache:dict::%s:%s", dictCode, dict.getValue());
                    try {
                        redisTemplate.opsForValue().set(redisKey, dict.getText());
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
        return translText;
    }

    /**
     * 字典值替换文本
     *
     * @param dictModels
     * @param values
     * @return
     */
    private String translDictText(List<DictModel> dictModels, String values) {
        List<String> result = new ArrayList<>();

        // 允许多个逗号分隔，允许传数组对象
        String[] splitVal = values.split(",");
        for (String val : splitVal) {
            String dictText = val;
            for (DictModel dict : dictModels) {
                if (val.equals(dict.getValue())) {
                    dictText = dict.getText();
                    break;
                }
            }
            result.add(dictText);
        }
        return String.join(",", result);
    }

    /**
     * 翻译字典文本
     *
     * @param code
     * @param text
     * @param table
     * @param key
     * @return
     */
    @Deprecated
    private String translateDictValue(String code, String text, String table, String key) {
        if (oConvertUtils.isEmpty(key)) {
            return null;
        }
        StringBuffer textValue = new StringBuffer();
        String[] keys = key.split(",");
        for (String k : keys) {
            String tmpValue = null;
            if (k.trim().length() == 0) {
                continue; //跳过循环
            }
            if (!StringUtils.isEmpty(table)) {
                String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s,%s,%s]", table, text, code, k.trim());
                if (redisTemplate.hasKey(keyString)) {
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                } else {
                    tmpValue = commonApi.translateDictFromTable(table, text, code, k.trim());
                }
            } else {
                String keyString = String.format("sys:cache:dict::%s:%s", code, k.trim());
                if (redisTemplate.hasKey(keyString)) {
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                } else {
                    tmpValue = commonApi.translateDict(code, k.trim());
                }
            }
            //update-end--Author:scott -- Date:20210531 ----for： !56 优化微服务应用下存在表字段需要字典翻译时加载缓慢问题-----

            if (tmpValue != null) {
                if (!"".equals(textValue.toString())) {
                    textValue.append(",");
                }
                textValue.append(tmpValue);
            }

        }
        return textValue.toString();
    }

}
