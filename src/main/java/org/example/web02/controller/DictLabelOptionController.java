package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.DictLabelOption;
import org.example.web02.service.DictLabelOptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签字典控制器
 *
 * 提供标签字典相关的 API 接口：
 * - 前端健康档案下拉框动态获取选项
 * - 知识库管理标签池动态获取
 * - 管理员 CRUD 操作
 */
@RestController
@RequestMapping("/api/dict-label-options")
public class DictLabelOptionController {

    /** 标签字典服务，用于处理标签字典相关的业务逻辑 */
    private final DictLabelOptionService dictLabelOptionService;

    /**
     * 构造函数，注入标签字典服务
     *
     * @param dictLabelOptionService 标签字典服务实例
     */
    public DictLabelOptionController(DictLabelOptionService dictLabelOptionService) {
        this.dictLabelOptionService = dictLabelOptionService;
    }

    /**
     * 获取所有标签（按分类分组）
     *
     * 供前端下拉框和知识库标签池使用
     *
     * @return 包含按分类分组的标签映射的响应
     */
    @GetMapping
    public ApiResponse<Map<String, List<String>>> getAllGrouped() {
        return ApiResponse.success(dictLabelOptionService.getAllGrouped());
    }

    /**
     * 获取全量标签列表（管理端）
     *
     * @return 包含所有标签列表的响应
     */
    @GetMapping("/list")
    public ApiResponse<List<DictLabelOption>> listAll() {
        return ApiResponse.success(dictLabelOptionService.listAll());
    }

    /**
     * 根据 ID 查询单条标签（管理端）
     *
     * @param id 标签 ID
     * @return 包含指定标签的响应
     */
    @GetMapping("/{id}")
    public ApiResponse<DictLabelOption> getById(@PathVariable Long id) {
        return ApiResponse.success(dictLabelOptionService.getById(id));
    }

    /**
     * 新增标签（管理端）
     *
     * @param option 标签信息
     * @return 包含创建后的标签的响应
     */
    @PostMapping
    public ApiResponse<DictLabelOption> create(@RequestBody DictLabelOption option) {
        return ApiResponse.success("创建成功", dictLabelOptionService.create(option));
    }

    /**
     * 修改标签（管理端）
     *
     * @param id 标签 ID
     * @param option 更新的标签信息
     * @return 包含更新后的标签的响应
     */
    @PutMapping("/{id}")
    public ApiResponse<DictLabelOption> update(@PathVariable Long id,
                                               @RequestBody DictLabelOption option) {
        return ApiResponse.success("更新成功", dictLabelOptionService.update(id, option));
    }

    /**
     * 删除标签（管理端）
     *
     * @param id 标签 ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // 调用服务删除标签
        dictLabelOptionService.delete(id);
        return ApiResponse.success("删除成功");
    }
}