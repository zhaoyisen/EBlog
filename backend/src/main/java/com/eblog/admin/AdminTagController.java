package com.eblog.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eblog.api.common.ApiResponse;
import com.eblog.metadata.TagEntity;
import com.eblog.metadata.TagMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tags")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminTagController {

    private final TagMapper tagMapper;

    public AdminTagController(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @GetMapping
    public ApiResponse<IPage<TagEntity>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Page<TagEntity> p = new Page<>(page, size);
        IPage<TagEntity> result = tagMapper.selectPage(p, null);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tagMapper.deleteById(id);
        return ApiResponse.ok(null);
    }
}
