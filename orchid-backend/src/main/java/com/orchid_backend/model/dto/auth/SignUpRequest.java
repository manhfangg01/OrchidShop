package com.orchid_backend.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Email(message = "Định dạng email không đúng")
    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, message = "Mật khẩu phải hơn 6 ký tự")
    private String password;

    @NotBlank(message = "Tên tài khoản là bắt buộc")
    @Size(min = 6, message = "Tên tài khoản phải hơn 6 ký tự")
    private String fullName;

}
