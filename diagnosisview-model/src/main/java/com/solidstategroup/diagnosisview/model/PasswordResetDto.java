package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetDto {
    private String newPassword;
    private String resetCode;
    private String username;

}
