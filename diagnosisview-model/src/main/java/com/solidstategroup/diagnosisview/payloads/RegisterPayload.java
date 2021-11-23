package com.solidstategroup.diagnosisview.payloads;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Pavlo Maksymchuk.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegisterPayload {

  @NotBlank(message = "can't be empty")
  private String captchaResponse;

  @Email(message = "Must be valid email address")
  @Size(min = 1, max = 255, message = "Must be between {min} and {max} characters")
  @NotNull(message = "co.sa.error.email.required")
  private String username;

  @NotBlank(message = "Password is required")
  private String password;

  @Size(min = 1, max = 255, message = "Must be between {min} and {max} characters")
  @NotBlank(message = "First Name is required")
  private String firstName;

  @Size(min = 1, max = 255, message = "Must be between {min} and {max} characters")
  @NotBlank(message = "Last Name is required")
  private String lastName;

  @NotBlank(message = "Institution is required")
  private String institution;

  @NotBlank(message = "Occupation is required")
  private String occupation;
}
