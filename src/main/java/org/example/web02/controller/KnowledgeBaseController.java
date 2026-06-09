package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.KnowledgeBase;
import org.example.web02.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理 API（管理员使用）
 */
@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 新增知识条目
     */
    @PostMapping
    public ApiResponse<KnowledgeBase> create(@RequestBody KnowledgeBase knowledgeBase) {
        KnowledgeBase created = knowledgeBaseService.create(knowledgeBase);
        return ApiResponse.success("知识条目创建成功", created);
    }

    /**
     * 修改知识条目
     */
    @PutMapping("/{id}")
    public ApiResponse<KnowledgeBase> update(@PathVariable Long id,
                                              @RequestBody KnowledgeBase knowledgeBase) {
        KnowledgeBase updated = knowledgeBaseService.update(id, knowledgeBase);
        return ApiResponse.success("知识条目更新成功", updated);
    }

    /**
     * 删除知识条目（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.delete(id);
        return ApiResponse.success("删除成功");
    }

    /**
     * 查询单条知识
     */
    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBase> getById(@PathVariable Long id) {
        KnowledgeBase kb = knowledgeBaseService.getById(id);
        return ApiResponse.success(kb);
    }

    /**
     * 知识条目列表
     */
    @GetMapping("/list")
    public ApiResponse<List<KnowledgeBase>> listAll() {
        List<KnowledgeBase> list = knowledgeBaseService.listAll();
        return ApiResponse.success(list);
    }

    /**
     * 获取合法标签池（分组），供管理端下拉框使用
     */
    @GetMapping("/tags")
    public ApiResponse<Map<String, List<String>>> getValidTags() {
        Map<String, List<String>> pool = knowledgeBaseService.getAllValidTags();
        return ApiResponse.success(pool);
    }
}