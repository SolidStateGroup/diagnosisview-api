package com.solidstategroup.diagnosisview.payloads;


import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Pavlo Maksymchuk.
 */
@Getter
@Setter
@NoArgsConstructor
public class ForgotPasswordPayload {

    @NotBlank(message = "can't be empty")
    private String captchaResponse;
    @NotBlank(message = "can't be empty")
    private String username;
}
