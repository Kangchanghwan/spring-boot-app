package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Test
    @DisplayName("회원 가입 화면 진입 확인")
    void signUpForm() throws Exception{
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                //Http status가 200 Ok 인지 확인합니다.
                .andExpect(view().name("account/sign-up"))
                //view가 제대로 이동했는지 확인합니다.
                .andExpect(model().attributeExists("signUpForm"));
                //객체로 전달했던 attribute가 존재하는지 확인합니다.
    }

}