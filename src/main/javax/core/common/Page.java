package core.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象，包含当前页数据及分页信息，如总记录数
 * 能够支持和 JQuery EasyUI 直接对接，能够支持和 BootStrap Table 直接对接
 * @param <T>
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private int pageSize = DEFAULT_PAGE_SIZE; // 每页的记录数
    private long start; // 当前页第一条数据在 List 中的位置，从0开始
    private List<T> rows; // 当前页中存放的记录，类型一般为 List
    private long total; // 总记录数

    // 构造方法，只构造空页
    public Page() {
        this(0, 0, DEFAULT_PAGE_SIZE, new ArrayList<T>());
    }

    /**
     * 默认构造方法
     * @param start 本页数据在数据库中的起始位置
     * @param totalSize 数据库中的总记录条数
     * @param pageSize 本页容量
     * @param rows 本页包含的数据
     */
    public Page(long start, long totalSize, int pageSize, List<T> rows) {
        this.pageSize = pageSize;
        this.start = start;
        this.total = totalSize;
        this.rows = rows;
    }
    // 取总记录数
    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
    // 取总页数
    public long getTotalPageCount() {
        if (total % pageSize == 0) {
            return total / pageSize;
        } else {
            return total / pageSize + 1;
        }
    }
    // 取每页数据容量
    public int getPageSize() {
        return pageSize;
    }
    // 取当前页中的记录
    public List<T> getRows() {
        return rows;
    }
    public void setRows(List<T> rows) {
        this.rows = rows;
    }
    // 取该页的当前页码，页码从1开始
    public long getPageNo() {
        return start / pageSize + 1;
    }
    // 该页是否有下一页
    public boolean hasNextPage() {
        return this.getPageNo() < this.getTotalPageCount() - 1;
    }
    // 该页是否有上一页
    public boolean hasPreviousPage() {
        return this.getPageNo() > 1;
    }
    // 获取任意一页第一条数据在数据集中的位置，每页条数使用默认值
    protected static int getStartOfPage(int pageNo) {
        return getStartOfPage(pageNo, DEFAULT_PAGE_SIZE);
    }
    // 获取任意一页第一条数据在数据集中的位置
    public static int getStartOfPage(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
}
