package org.example.web02.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 *
 * 用于封装分页查询的返回结果，包含记录列表、总数、页码、每页大小和总页数
 *
 * @param <T> 记录的类型
 */
@Data
public class PageResult<T> {

    /** 当前页的记录列表 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    /**
     * 默认构造函数
     */
    public PageResult() {
    }

    /**
     * 构造函数，自动计算总页数
     *
     * @param records 当前页的记录列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     */
    public PageResult(List<T> records, long total, long pageNum, long pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        // 计算总页数（向上取整）
        this.pages = (total + pageSize - 1) / pageSize;
    }

    /**
     * 静态工厂方法，快速构建分页结果
     *
     * @param records 当前页的记录列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }
}
