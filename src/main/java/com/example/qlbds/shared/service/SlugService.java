package com.example.qlbds.shared.service;

import java.text.Normalizer;

import org.springframework.stereotype.Service;

import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.shared.service.impl.SlugServiceImpl;

@Service
public class SlugService implements SlugServiceImpl{

    @Override
    public String toSlug(String input){
        if (input == null || input.isBlank()) {
            throw new InvalidResourceException("Input", "không được để trống để tạo slug");
        }

        // 1. Chuẩn hóa unicode, loại bỏ dấu tiếng Việt
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 2. Chuyển sang lowercase, thay ký tự không phải a-z,0-9 thành "-"
        String slug = normalized.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", ""); // loại bỏ "-" đầu/cuối

        return slug;
    }

}
