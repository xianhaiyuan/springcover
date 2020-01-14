package core.common.jdbc;

import com.gupaoedu.vip.orm.framework.QueryRule;
import core.common.Page;

import java.util.List;
import java.util.Map;

public interface BaseDao<T, PK> {
    /**
     * 获取列表
     * @param queryRule 查询条件
     * @return
     * @throws Exception
     */
    List<T> select(QueryRule queryRule) throws Exception;

    /**
     * 获取分页结果
     * @param queryRule 查询条件
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return
     * @throws Exception
     */
    Page<?> select(QueryRule queryRule, int pageNo, int pageSize) throws Exception;

    /**
     * 根据 SQL 获取列表
     * @param sql SQL 语句
     * @param args 参数
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> selectBySql(String sql, Object... args) throws Exception;

    /**
     * 根据 SQL 获取分页
     * @param sql SQL 语句
     * @param param 参数
     * @param pageNo  页码
     * @param pageSize 每页条数
     * @return
     * @throws Exception
     */
    Page<Map<String, Object>> selectBySqlToPage(String sql, Object[] param, int pageNo, int pageSize) throws Exception;

    /**
     * 删除一条记录
     * @param entity entity 中的 ID 不能为空，如果 ID 为空，其他条件不能为空，都为空则不予执行
     * @return
     * @throws Exception
     */
    boolean delete(T entity) throws Exception;

    /**
     * 批量删除
     * @param list
     * @return 返回受影响的行数
     * @throws Exception
     */
    int deleteAll(List<T> list) throws Exception;

    /**
     * 插入一条记录并返回插入后的 ID
     * @param entity 只要 entity 不等于 null，就执行插入操作
     * @return
     * @throws Exception
     */
    PK insertAndReturnId(T entity) throws Exception;

    /**
     * 插入一条记录自增 ID
     * @param entity
     * @return
     * @throws Exception
     */
    boolean insert(T entity) throws Exception;

    /**
     * 批量插入
     * @param list
     * @return 返回受影响的行数
     * @throws Exception
     */
    int insertAll(List<T> list) throws Exception;

    /**
     * 修改一条记录
     * @param entity entity 中的 ID 不能为空，如果 ID 为空，其他条件不能为空，都为空则不予执行
     * @return
     * @throws Exception
     */
    boolean update(T entity) throws Exception;
}
