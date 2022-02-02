package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {

    @NotBlank
    //비어있는지 확인
    @Length(min= 3, max = 20)
    //길이 제한
    @Pattern(regexp ="^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
    // 한글, 영어ㅡ 숫자, 언더스코어, 하이픈을 포함을 포함할 수 있음.
    private String nickname;
    @Email
    //이메일인지 확인.
    @NotBlank
    private String email;
    @NotBlank
    @Length(min = 8, max = 50)
    private String password;

}
