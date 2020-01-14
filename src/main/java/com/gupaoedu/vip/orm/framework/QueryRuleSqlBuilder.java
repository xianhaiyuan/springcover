package com.gupaoedu.vip.orm.framework;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryRuleSqlBuilder {
    private int CURR_INDEX = 0; // 记录参数所在的位置
    private List<String> properties; // 保存列名列表
    private List<Object> values; // 保存参数值列表
    private List<Order> orders; // 保存排序规则列表

    private String whereSql = "";
    private String orderSql = "";
    private Object[] valueArr = new Object[]{};
    private Map<Object, Object> valueMap = new HashMap<Object, Object>();

    // 获得查询条件
    public String getWhereSql() {
        return this.whereSql;
    }
    // 获得排序条件
    public String getOrderSql() {
        return this.orderSql;
    }
    // 获得参数值列表
    public Object[] getValues() {
        return this.valueArr;
    }
    // 获得参数列表
    public Map<Object, Object> getValueMap() {
        return this.valueMap;
    }
    // 创建 SQL 构造器
    public QueryRuleSqlBuilder(QueryRule queryRule) {
        CURR_INDEX = 0;
        properties = new ArrayList<String>();
        values = new ArrayList<Object>();
        orders = new ArrayList<Order>();
        for (QueryRule.Rule rule : queryRule.getRuleList()) {
            switch (rule.getType()) {
                case QueryRule.BETWEEN:
                    processBetween(rule);
                    break;
                case QueryRule.EQ:
                    processEqual(rule);
                    break;
                case QueryRule.LIKE:
                    processLike(rule);
                    break;
                case QueryRule.NOTEQ:
                    processNotEqual(rule);
                    break;
                case QueryRule.GT:
                    processGreaterThan(rule);
                    break;
                case QueryRule.GE:
                    processGreaterEqual(rule);
                    break;
                case QueryRule.LT:
                    processLessThan(rule);
                    break;
                case QueryRule.LE:
                    processLessEqual(rule);
                    break;
                case QueryRule.IN:
                    processIN(rule);
                    break;
                case QueryRule.NOTIN:
                    processNotIN(rule);
                    break;
                case QueryRule.ISNULL:
                    processIsNull(rule);
                    break;
                case QueryRule.ISNOTNULL:
                    processIsNotNull(rule);
                    break;
                case QueryRule.ISEMPTY:
                    processIsEmpty(rule);
                    break;
                case QueryRule.ISNOTEMPTY:
                    processIsNotEmpty(rule);
                    break;
                case QueryRule.ASC_ORDER:
                    processOrder(rule);
                    break;
                case QueryRule.DESC_ORDER:
                    processOrder(rule);
                    break;
                default:
                    throw new IllegalArgumentException("type " + rule.getType() + "not supported.");
            }
        }
        // 拼装 where 语句
        appendWhereSql();
        // 拼装排序语句
        appendOrderSql();
        // 拼装参数值
        appendValues();
    }
    // 去掉 order
    protected String removeOrders(String sql) {
        Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }
    private void processLike(QueryRule.Rule rule) {
        if (ArrayUtils.isEmpty(rule.getValues())) {
            return;
        }
        Object obj = rule.getValues()[0];
        if (obj != null) {
            String value = obj.toString();
            if (!StringUtils.isEmpty(value)) {
                value = value.replace("*", "%");
                obj = value;
            }
        }
        add(rule.getAndOr(), rule.getPropertyName(), "like", "%"+rule.getValues()[0]+"%");
    }
    private void processBetween(QueryRule.Rule rule) {
        if ((ArrayUtils.isEmpty(rule.getValues())) || (rule.getValues().length < 2)) {
            return;
        }
        add(rule.getAndOr(), rule.getPropertyName(),"","between",rule.getValues()[0],"and");
        add(0,"","","",rule.getValues()[1],"");
    }
    private void processEqual(QueryRule.Rule rule) {
        if (ArrayUtils.isEmpty(rule.getValues())) {
            return;
        }
        add(rule.getAndOr(), rule.getPropertyName(),"=",rule.getValues()[0]);
    }
    private void processNotEqual(QueryRule.Rule rule) {
        if (ArrayUtils.isEmpty(rule.getValues())) {
            return;
        }
        add(rule.getAndOr(),rule.getPropertyName(),"<>",rule.getValues()[0]);
    }
    private void processGreaterThan(QueryRule.Rule rule) {
        if (ArrayUtils.isEmpty(rule.getValues())) {
            return;
        }
        add(rule.getAndOr(),rule.getPropertyName(),">",rule.getValues()[0]);
    }
    private void processGreaterEqual(QueryRule.Rule rule) {
        if (ArrayUtils.isEmpty(rule.getValues())) {
            return;
        }
        add(rule.getAndOr(),rule.getPropertyName(),">=",rule.getValues()[0]);
    }
    private void processLessThan(QueryRule.Rule rule) {
        if (ArrayUtils.isEmpty(rule.getValues())) {
            return;
        }
        add(rule.getAndOr(),rule.getPropertyName(),"<",rule.getValues()[0]);
    }
    private void processLessEqual(QueryRule.Rule rule) {
        if (ArrayUtils.isEmpty(rule.getValues())) {
            return;
        }
        add(rule.getAndOr(),rule.getPropertyName(),"<=",rule.getValues()[0]);
    }
}
