package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.KnowledgeBase;
import org.example.web02.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 *
 * 提供知识库管理相关的 API 接口（管理员使用）
 * 包括知识条目的增删改查、获取合法标签池等功能
 */
@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {

    /** 知识库服务，用于处理知识条目相关的业务逻辑 */
    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 构造函数，注入知识库服务
     *
     * @param knowledgeBaseService 知识库服务实例
     */
    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 新增知识条目
     *
     * @param knowledgeBase 知识条目信息
     * @return 包含创建后的知识条目的响应
     */
    @PostMapping
    public ApiResponse<KnowledgeBase> create(@RequestBody KnowledgeBase knowledgeBase) {
        // 调用服务创建知识条目
        KnowledgeBase created = knowledgeBaseService.create(knowledgeBase);
        return ApiResponse.success("知识条目创建成功", created);
    }

    /**
     * 修改知识条目
     *
     * @param id 知识条目 ID
     * @param knowledgeBase 更新的知识条目信息
     * @return 包含更新后的知识条目的响应
     */
    @PutMapping("/{id}")
    public ApiResponse<KnowledgeBase> update(@PathVariable Long id,
                                              @RequestBody KnowledgeBase knowledgeBase) {
        // 调用服务更新知识条目
        KnowledgeBase updated = knowledgeBaseService.update(id, knowledgeBase);
        return ApiResponse.success("知识条目更新成功", updated);
    }

    /**
     * 删除知识条目（软删除）
     *
     * @param id 知识条目 ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // 调用服务删除知识条目（软删除）
        knowledgeBaseService.delete(id);
        return ApiResponse.success("删除成功");
    }

    /**
     * 查询单条知识
     *
     * @param id 知识条目 ID
     * @return 包含指定知识条目的响应
     */
    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBase> getById(@PathVariable Long id) {
        // 调用服务根据 ID 获取知识条目
        KnowledgeBase kb = knowledgeBaseService.getById(id);
        return ApiResponse.success(kb);
    }

    /**
     * 知识条目列表
     *
     * @return 包含所有知识条目列表的响应
     */
    @GetMapping("/list")
    public ApiResponse<List<KnowledgeBase>> listAll() {
        // 调用服务获取所有知识条目
        List<KnowledgeBase> list = knowledgeBaseService.listAll();
        return ApiResponse.success(list);
    }

    /**
     * 获取合法标签池（分组）
     *
     * 供管理端下拉框使用，返回按分类分组的标签列表
     *
     * @return 包含按分类分组的标签映射的响应
     */
    @GetMapping("/tags")
    public ApiResponse<Map<String, List<String>>> getValidTags() {
        // 调用服务获取所有合法标签（分组）
        Map<String, List<String>> pool = knowledgeBaseService.getAllValidTags();
        return ApiResponse.success(pool);
    }
}