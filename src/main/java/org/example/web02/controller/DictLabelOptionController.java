package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.DictLabelOption;
import org.example.web02.service.DictLabelOptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签字典 API
 * - 前端健康档案下拉框动态获取选项
 * - 知识库管理标签池动态获取
 * - 管理员 CRUD
 */
@RestController
@RequestMapping("/api/dict-label-options")
public class DictLabelOptionController {

    private final DictLabelOptionService dictLabelOptionService;

    public DictLabelOptionController(DictLabelOptionService dictLabelOptionService) {
        this.dictLabelOptionService = dictLabelOptionService;
    }

    /** 获取所有标签（按分类分组），供前端下拉框和知识库标签池使用 */
    @GetMapping
    public ApiResponse<Map<String, List<String>>> getAllGrouped() {
        return ApiResponse.success(dictLabelOptionService.getAllGrouped());
    }

    /** 管理端：全量列表 */
    @GetMapping("/list")
    public ApiResponse<List<DictLabelOption>> listAll() {
        return ApiResponse.success(dictLabelOptionService.listAll());
    }

    /** 管理端：查询单条 */
    @GetMapping("/{id}")
    public ApiResponse<DictLabelOption> getById(@PathVariable Long id) {
        return ApiResponse.success(dictLabelOptionService.getById(id));
    }

    /** 管理端：新增 */
    @PostMapping
    public ApiResponse<DictLabelOption> create(@RequestBody DictLabelOption option) {
        return ApiResponse.success("创建成功", dictLabelOptionService.create(option));
    }

    /** 管理端：修改 */
    @PutMapping("/{id}")
    public ApiResponse<DictLabelOption> update(@PathVariable Long id,
                                               @RequestBody DictLabelOption option) {
        return ApiResponse.success("更新成功", dictLabelOptionService.update(id, option));
    }

    /** 管理端：删除 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dictLabelOptionService.delete(id);
        return ApiResponse.success("删除成功");
    }
}