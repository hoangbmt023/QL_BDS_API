package com.example.qlbds.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtil {
    private static final Pattern NONLATIN = Pattern.compile("[^a-z0-9-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) return null;
        
        // Chuyển về chữ thường
        String slug = input.toLowerCase(Locale.forLanguageTag("vi-VN"));
        
        // Thay thế các ký tự tiếng Việt có dấu
        slug = slug.replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a");
        slug = slug.replaceAll("[éèẻẽẹêếềểễệ]", "e");
        slug = slug.replaceAll("[íìỉĩị]", "i");
        slug = slug.replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o");
        slug = slug.replaceAll("[úùủũụưứừửữự]", "u");
        slug = slug.replaceAll("[ýỳỷỹỵ]", "y");
        slug = slug.replaceAll("đ", "d");

        // Loại bỏ các ký tự dấu phụ (diacritics) còn sót lại
        String normalized = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = normalized.replaceAll("\\p{M}", "");

        // Thay thế khoảng trắng và gạch dưới bằng gạch ngang
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("_", "-");

        // Loại bỏ các ký tự không phải chữ cái/số/gạch ngang
        slug = NONLATIN.matcher(slug).replaceAll("");
        
        // Thu gọn các gạch ngang dư thừa
        return slug.replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
}
