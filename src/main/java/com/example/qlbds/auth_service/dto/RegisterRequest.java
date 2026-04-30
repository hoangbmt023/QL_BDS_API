package com.example.qlbds.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

                @NotBlank(message = "Tên đăng nhập không được để trống") @Size(min = 3, max = 100, message = "Tên đăng nhập phải từ 3 đến 100 ký tự") String username,

                @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email,

                @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 8, message = "Mật khẩu phải ít nhất 8 ký tự") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Mật khẩu phải có chữ hoa, chữ thường và số") String password,

                String fullName,

                String phone) {
}
