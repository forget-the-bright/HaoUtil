package org.hao.core.db;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.hao.core.auth.AuthUtil;
import org.hao.core.exception.HaoException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus SQL工具类
 * 提供数据库表字段操作、事务管理、通用启用方法、父子关系数据处理等通用数据库操作功能
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/3 14:38
 */
public class MPSqlUtil {
    //region 数据库表字段相关操作方法

    /**
     * 获取SQL字段名
     * 将Lambda表达式转换为对应的数据库字段名（下划线格式）
     *
     * @param <T> 实体类型
     * @param fieldFunction 字段函数式接口，用于指定实体类中的字段
     * @return 转换后的数据库字段名（下划线格式）
     */
    public static <T> String getSqlFieldName(SFunction<T, ?> fieldFunction) {
        String methodName = LambdaUtils.extract(fieldFunction).getImplMethodName();
        String fieldName = PropertyNamer.methodToProperty(methodName);
        String sqlFieldName = StringUtils.camelToUnderline(fieldName);
        return sqlFieldName;
    }

    /**
     * 获取实体类的主键字段
     * 通过MyBatis-Plus的TableInfo获取实体类的主键字段信息
     *
     * @param <T> 实体类型
     * @param entityClass 实体类的Class对象
     * @return 主键字段对象，如果不存在主键则返回null
     */
    public static <T> Field getId(Class<T> entityClass) {
        // 获取表信息
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo != null && tableInfo.havePK()) {
            // 返回主键字段名
            return ReflectUtil.getField(entityClass, tableInfo.getKeyProperty());
        }
        return null; // 如果没有找到主键，则返回null
    }

    /**
     * 获取实体类的主键字段名
     * 通过MyBatis-Plus的TableInfo获取实体类的主键字段名
     *
     * @param <T> 实体类型
     * @param entityClass 实体类的Class对象
     * @return 主键字段名，如果不存在主键则返回null
     */
    public static <T> String getSqlId(Class<T> entityClass) {
        // 获取表信息
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo != null && tableInfo.havePK()) {
            // 返回主键字段名
            return tableInfo.getKeyColumn();
        }
        return null; // 如果没有找到主键，则返回null
    }

    /**
     * 手动管理事务执行传入的Runnable任务
     * 该方法用于在手动控制事务的情况下执行一段代码，确保这段代码在事务性环境中运行
     * 如果在执行过程中遇到异常，将回滚事务
     *
     * @param runnable 要在事务中执行的任务，不能为空
     */
    public static void manualTransactions(Runnable runnable) {
        // 检查传入的Runnable是否为空，为空则直接返回，不执行任何操作
        if (ObjectUtil.isEmpty(runnable)) return;

        // 从Spring应用上下文中获取TransactionTemplate，用于管理事务的执行
        TransactionTemplate transactionTemplate = SpringUtil.getBean(TransactionTemplate.class);

        // 执行事务，传入一个操作无返回值的事务操作
        transactionTemplate.execute(status -> {
            try {
                // 执行传入的Runnable任务
                runnable.run();
            } catch (Exception e) {
                // 遇到异常时，设置事务为仅回滚，并重新抛出异常
                status.setRollbackOnly();
                throw e;
            }
            // 事务成功执行后，返回true表示操作成功
            return true;
        });
    }
    //endregion


    //region 通用数据库对象关系逻辑处理方法

    /**
     * 通用启用方法
     * 该方法用于启用或停用指定实体类的数据
     * 它会检查数据的当前启用状态，并递归查找所有相关子数据和父数据
     * 如果存在已启用的子数据，会提示需要先停用这些子数据
     * 最后，它会更新数据库中的数据状态
     *
     * @param <T> 实体类的类型
     * @param entityClass 实体类的Class对象，用于指定操作的实体类型
     * @param getEnableFunction 用于获取实体启用状态的函数式接口
     * @param fieldFunction 用于获取实体字段值的函数式接口
     * @param parentFieldFunction 用于获取实体父字段值的函数式接口
     * @param nameFieldFunction 用于获取实体名称字段值的函数式接口
     * @param id 要操作的数据的ID
     */
    public static <T> void CommonEnable(Class<T> entityClass, SFunction<T, ?> getEnableFunction,
                                        SFunction<T, ?> fieldFunction, SFunction<T, ?> parentFieldFunction,
                                        SFunction<T, ?> nameFieldFunction, String id) {
        // 根据ID获取工作流程数据
        T byId = Db.getById(id, entityClass);
        // 如果数据不存在，抛出异常
        HaoException.throwByFlag(byId == null, "未找到要启用的数据在数据");
        List<String> enableStatus = ListUtil.of("0", "1");
        String isEnable = Convert.toStr(getEnableFunction.apply(byId));
        // 检查数据的启用状态是否合法
        HaoException.throwByFlag(!enableStatus.contains(isEnable), "当前数据的启用状态不合法不是【0】停用或者【1】启用,来判断接下来操作是启或停,当前启用状态[ {} ]", isEnable);

        String enableStatusStr = "";
        String opStatus = "";
        String msgEnableStr = "";
        if ("1".equals(isEnable)) {
            enableStatusStr = "启用";
            msgEnableStr = "停用";
            opStatus = "0";
        } else {
            enableStatusStr = "停用";
            msgEnableStr = "启用";
            opStatus = "1";
        }
        // 递归查找所有子数据和父数据
        List<T> parentChildDataRecursively = MPSqlUtil
                .findParentChildDataRecursively(entityClass,
                        fieldFunction, parentFieldFunction,
                        Convert.toStr(fieldFunction.apply(byId)));

        // 过滤出所有已启用的子数据，并排除当前要启用的数据本身
        Field idKeyField = getId(entityClass);
        List<T> collect = parentChildDataRecursively.stream()
                .filter(item -> getEnableFunction.apply(item).equals(isEnable) && !ReflectUtil.getFieldValue(item, idKeyField).equals(id))
                .collect(Collectors.toList());
        // 如果存在已启用的子数据，抛出异常，提示需要先停用这些子数据
        HaoException.throwByFlag(collect.size() > 0, "该数据下有{}的子数据，请先{}子数据 [ {} ]",
                enableStatusStr,
                msgEnableStr,
                collect.stream()
                        .map(nameFieldFunction)
                        .map(Convert::toStr)
                        .collect(Collectors.joining(",")));

        // 更新数据库，将指定ID的数据的状态设置为启用
        UpdateChainWrapper<T> updateChainWrapper = Db.update(entityClass)
                .eq(getSqlId(entityClass), id)
                .set(MPSqlUtil.getSqlFieldName(getEnableFunction), opStatus);

        fillUpdateChainWrapper(entityClass, updateChainWrapper);

        updateChainWrapper.update();
    }

    /**
     * 通用启用方法
     * 该方法用于启用或停用指定实体类的记录
     * 它通过ID获取记录，检查其启用状态，然后更新启用状态
     *
     * @param <T> 实体类的类型
     * @param entityClass 实体类的Class对象，用于指定操作的实体类型
     * @param getEnableFunction 函数式接口，用于获取实体的启用状态字段
     * @param id 实体的ID，用于定位要操作的记录
     */
    public static <T> void CommonEnable(Class<T> entityClass,
                                        SFunction<T, ?> getEnableFunction, String id) {
        // 根据ID获取工作流程数据
        T byId = Db.getById(id, entityClass);
        // 如果数据不存在，抛出异常
        HaoException.throwByFlag(byId == null, "未找到要启用的数据在数据");
        // 定义合法的启用状态列表
        List<String> enableStatus = ListUtil.of("0", "1");
        // 获取当前数据的启用状态
        String isEnable = Convert.toStr(getEnableFunction.apply(byId));
        // 检查数据的启用状态是否合法
        HaoException.throwByFlag(!enableStatus.contains(isEnable), "当前数据的启用状态不合法不是【0】停用或者【1】启用,来判断接下来操作是启或停,当前启用状态[ {} ]", isEnable);
        // 根据当前状态确定操作后的状态
        String opStatus = "1".equals(isEnable) ? "0" : "1";
        // 更新数据库，将指定ID的数据的状态设置为启用
        UpdateChainWrapper<T> updateChainWrapper = Db.update(entityClass)
                .eq(getSqlId(entityClass), id)
                .set(MPSqlUtil.getSqlFieldName(getEnableFunction), opStatus);

        // 填充更新链包装器以完成更新操作
        fillUpdateChainWrapper(entityClass, updateChainWrapper);
        // 执行更新操作
        updateChainWrapper.update();
    }

    /**
     * 填充更新链包装器
     * 为更新操作自动设置更新时间和更新人字段
     *
     * @param <T> 实体类型
     * @param entityClass 实体类的Class对象
     * @param updateChainWrapper 更新链包装器对象
     */
    private static <T> void fillUpdateChainWrapper(Class<T> entityClass, UpdateChainWrapper<T> updateChainWrapper) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        tableInfo.getFieldList().forEach(item -> {
            String property = item.getProperty();
            String sqlField = StringUtils.camelToUnderline(property);
            if (sqlField.equals("update_by")) {
                  updateChainWrapper.set(sqlField, AuthUtil.getUserName());
            }
            if (sqlField.equals("update_time")) {
                updateChainWrapper.set(sqlField, new Date());
            }
        });
    }


    /**
     * 递归查找父子关系的数据
     *
     * @param <T> 实体类的类型
     * @param entityClass 实体类的Class对象，用于指定查询结果映射的类型
     * @param fieldFunction 函数式接口，用于指定实体类中的字段，通常代表子记录中的外键字段
     * @param parentFieldFunction 函数式接口，用于指定实体类中的字段，通常代表父记录的主键字段
     * @param value 查询的起始值，通常是一个标识符，如代码或ID
     * @return 返回查询到的父子关系数据列表，类型为T
     * <p>
     * 此方法使用递归SQL查询来获取具有父子关系的数据结构
     * 它首先构建一个递归查询，然后使用JdbcTemplate执行查询并映射结果到指定的实体类
     */
    public static <T> List<T> findParentChildDataRecursively(Class<T> entityClass,
                                                             SFunction<T, ?> fieldFunction, SFunction<T, ?> parentFieldFunction,
                                                             String value) {
        // 获取查询字段名
        String fieldName = getSqlFieldName(fieldFunction);
        // 获取父级查询字段名
        String parentFieldName = getSqlFieldName(parentFieldFunction);
        // 获取表信息
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        // 获取表名
        String tableName = tableInfo.getTableName();
        // 获取JdbcTemplate实例
        JdbcTemplate bean = SpringUtil.getBean(JdbcTemplate.class);
        // 构建递归查询SQL模板
        String sqlTemplage =
                "WITH RECURSIVE lineage AS ( " +
                        "SELECT * " +
                        "FROM  `" + tableName + "` " +
                        "WHERE code = '" + value + "' " +
                        "UNION ALL " +
                        "SELECT e.* " +
                        "FROM " + tableName + " e " +
                        "INNER JOIN lineage el ON e.`" + parentFieldName + "` = el.`" + fieldName + "` " +
                        ") " +
                        "SELECT * FROM lineage;";
        // 执行查询并映射结果到指定的实体类
        List<T> ts = bean.query(sqlTemplage, new BeanPropertyRowMapper<>(entityClass));
        //会出现字段对不齐的问题，字段名称，和字段数量如果对不上就会出现错误 Incorrect column count: expected 1, actual 10
        //List<T> ts = bean.queryForList(sqlTemplage, entityClass);
        // 返回查询结果
        return ts;
    }

    /**
     * 递归填充父级子级数据
     *
     * @param <T> 实体类类型
     * @param entityClass 实体类Class对象
     * @param fieldFunction 用于获取实体类字段值的函数式接口
     * @param parentFieldFunction 用于获取父级字段值的函数式接口
     * @param childsFunction 用于设置实体类子级列表的双消费者接口
     * @param rootParentValue 根父级值，用于筛选父级数据
     * @return 返回填充完子级数据的父级实体列表
     */
    public static <T> List<T> fillParentChildDataRecursively(Class<T> entityClass,
                                                             Function<T, ?> fieldFunction, Function<T, ?> parentFieldFunction,
                                                             BiConsumer<T, List<T>> childsFunction, String rootParentValue) {
        // 从数据库中获取所有实体数据
        List<T> list = Db.list(entityClass);
        return fillParentChildDataRecursively(list, fieldFunction, parentFieldFunction, childsFunction, rootParentValue);
    }

    /**
     * 递归填充父级子级数据
     *
     * @param <T> 实体类类型
     * @param list 数据集合
     * @param fieldFunction 用于获取主键字段值的函数式接口
     * @param parentFieldFunction 用于获取父级字段值的函数式接口
     * @param childsFunction 用于设置实体类子级列表的双消费者接口
     * @param rootParentValue 根节点id
     * @return 填充完子级数据的父级实体列表
     */
    public static <T> List<T> fillParentChildDataRecursively(List<T> list,
                                                             Function<T, ?> fieldFunction, Function<T, ?> parentFieldFunction,
                                                             BiConsumer<T, List<T>> childsFunction, String rootParentValue) {
        // 从数据库中获取所有实体数据
        if (CollUtil.isEmpty(list)) return list;
        List<T> parentLists = null;
        List<T> childLists = null;

        // 根据rootParentValue是否为空，来区分数据筛选逻辑
        if (ObjectUtil.isEmpty(rootParentValue)) {
            // 如果rootParentValue为空，区分父级和子级数据
            parentLists = list.stream().filter(item -> ObjectUtil.isEmpty(parentFieldFunction.apply(item))).collect(Collectors.toList());
            childLists = list.stream().filter(item -> ObjectUtil.isNotEmpty(parentFieldFunction.apply(item))).collect(Collectors.toList());
        } else {
            // 如果rootParentValue不为空，根据rootParentValue筛选父级数据，并区分子级数据
            parentLists = list.stream().filter(item -> rootParentValue.equals(Convert.toStr(parentFieldFunction.apply(item)))).collect(Collectors.toList());
            childLists = list.stream().filter(item -> !rootParentValue.equals(Convert.toStr(parentFieldFunction.apply(item)))).collect(Collectors.toList());
        }

        // 如果没有找到父级数据，抛出异常
        HaoException.throwByFlag(CollUtil.isEmpty(parentLists), "未找到父级数据,根据根父级值【{}】", rootParentValue);

        // 将子级数据根据父级字段值进行分组
        Map<?, List<T>> childGroups = childLists.stream().collect(Collectors.groupingBy(parentFieldFunction));

        // 为每个父级实体填充子级数据
        for (T parent : parentLists) {
            fillParentChildData(parent, childGroups, fieldFunction, childsFunction);
        }

        // 返回填充完子级数据的父级实体列表
        return parentLists;
    }

    /**
     * 填充父-child数据
     * 该方法用于将一个父对象和其潜在的子对象集合之间的关系进行填充
     * 它会根据父对象的某个属性值，从提供的子对象映射中查找对应的子对象集合，并通过BiConsumer将子对象集合设置到父对象中
     * 这个过程会递归地进行，以处理可能的多级嵌套结构
     *
     * @param parent 要填充子数据的父对象
     * @param childGroups 子对象的映射，键是子对象中的某个属性值，值是具有相同该属性值的子对象列表
     * @param fieldFunction 函数式接口，用于从父对象中获取用于查找子对象集合的键值
     * @param childsFunction 消费者接口，用于将子对象集合设置到父对象中
     * @param <T> 对象类型
     */
    public static <T> void fillParentChildData(T parent, Map<?, List<T>> childGroups,
                                               Function<T, ?> fieldFunction, BiConsumer<T, List<T>> childsFunction) {
        // 检查子对象映射是否为空，如果为空则直接返回
        if (CollUtil.isEmpty(childGroups)) return;

        // 根据父对象的某个属性值从子对象映射中获取对应的子对象列表如果获取失败，则默认使用空列表
        List<T> ts = ObjectUtil.defaultIfNull(childGroups.get(fieldFunction.apply(parent)), new ArrayList<T>());

        // 使用BiConsumer将子对象列表设置到父对象中
        childsFunction.accept(parent, ts);

        // 遍历子对象列表，对每个子对象递归执行填充操作
        for (T child : ts) {
            fillParentChildData(child, childGroups, fieldFunction, childsFunction);
        }
    }

    //endregion
}
