package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /** 上传图片 */
    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadImage(file);
        return ApiResponse.success("上传成功", url);
    }
}
