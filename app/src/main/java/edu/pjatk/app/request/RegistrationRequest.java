package edu.pjatk.app.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Getter
@Setter
public class RegistrationRequest {

    @Size(min = 6, max = 12, message = "{validation.registration.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9]+$",message ="{validation.registration.username.regex}")
    private String username;
    //  https://emailregex.com/
    @NotBlank(message ="{validation.registration.email.notBlank}")
    @Pattern(regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b" +
            "\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0" +
            "-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-" +
            "9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c" +
            "\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
            message ="{validation.registration.email.regex}")
    private String email;
    //  Minimum eight and maximum 24 characters, at least one uppercase letter,
    //  one lowercase letter, one number and one special character:
    @Size(min = 8, max = 24,message ="{validation.registration.password.size}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{1,100}$",
            message ="{validation.registration.password.regex}")
    private String password;
    @NotBlank(message ="{validation.registration.password-confirmation.notBlank}")
    private String confirmPassword;

}
