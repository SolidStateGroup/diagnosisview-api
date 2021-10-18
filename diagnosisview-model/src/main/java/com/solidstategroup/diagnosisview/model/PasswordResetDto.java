package com.solidstategroup.diagnosisview.model;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetDto {
    @NotBlank(message = "New password required.")
    private String newPassword;
    @NotBlank(message = "Missing reset code")
    private String resetCode;
    @NotBlank(message = "Missing username")
    private String username;

}
