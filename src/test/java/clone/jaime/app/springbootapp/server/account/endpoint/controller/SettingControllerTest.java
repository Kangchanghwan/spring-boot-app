package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.mail.EmailService;
import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.WithAccount;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.TagForm;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.ZoneForm;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import clone.jaime.app.springbootapp.server.tag.domain.entity.Tag;
import clone.jaime.app.springbootapp.server.tag.infra.repository.TagRepository;
import clone.jaime.app.springbootapp.server.zone.domain.entity.Zone;
import clone.jaime.app.springbootapp.server.zone.infra.repository.ZoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class SettingControllerTest {
    @MockBean
    EmailService emailService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AccountService accountService;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("지역 추가")
    @WithAccount("abc")
    void addZone() throws Exception {
        Zone testZone = Zone.builder().city("test").province("test주").localNameOfCity("test시").build();
        zoneRepository.save(testZone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingController.SETTINGS_ZONES_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName()).orElse(null);
        assertNotNull(zone);
        assertTrue(accountRepository.findByNickname("abc").getZones().contains(zone));
    }

    @Test
    @DisplayName("지역 삭제")
    @WithAccount("abc")
    void removeZone() throws Exception {
        Zone testZone = Zone.builder().city("test").province("test주").localNameOfCity("test시").build();
        zoneRepository.save(testZone);
        Account abc = accountRepository.findByNickname("abc");
        accountService.addZone(abc, testZone);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(SettingController.SETTINGS_ZONES_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(abc.getZones().contains(testZone));
    }


    @Test
    @DisplayName("지역 수정 폼")
    @WithAccount("abc")
    void updateZoneForm() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_ZONES_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }


    @Test
    @DisplayName("태그 추가")
    @WithAccount("abc")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("java");

        mockMvc.perform(post(SettingController.SETTINGS_TAGS_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag tag = tagRepository.findByTitle("java").orElse(null);
        assertNotNull(tag);
        assertTrue(accountRepository.findByNickname("abc").getTags().contains(tag));
    }

    @Test
    @DisplayName("태그 삭제")
    @WithAccount("abc")
    void removeTag() throws Exception {
        Account abc = accountRepository.findByNickname("abc");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(abc, newTag);
        assertTrue(abc.getTags().contains(newTag));
        TagForm tagForm = new TagForm();
        String tagTitle = "newTag";
        tagForm.setTagTitle(tagTitle);
        mockMvc.perform(post(SettingController.SETTINGS_TAGS_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(abc.getTags().contains(newTag));
    }


    @Test
    @DisplayName("태그 수정 폼")
    @WithAccount("abc")
    void updateTagForm() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @Test
    @DisplayName("프로필 수정: 입력값 정상")
    @WithAccount("abc")
    void updateProfile() throws Exception {
        String bio = "한줄 소개";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));
        Account cock = accountRepository.findByNickname("abc");
        assertEquals(bio,cock.getProfile().getBio());
    }

    @Test
    @DisplayName("프로필 수정 : 입력값 에러")
    @WithAccount("abc")
    void updateProfileWithError() throws  Exception{
        String bio = "35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 ";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                        .param("bio",bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
        Account cock = accountRepository.findByNickname("abc");
    }

    @Test
    @DisplayName("프로필 조회")
    @WithAccount("abc")
    void updateProfileForm() throws Exception {
        String bio = "한줄 소개";
        mockMvc.perform(get(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }


    @Test
    @DisplayName("패스워드 수정 폼")
    @WithAccount("abc")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }
    @Test
    @DisplayName("패스워드 수정: 입력값 정상")
    @WithAccount("abc")
    void updatePassword() throws  Exception{
        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                        .param("newPassword","12341234")
                        .param("newPasswordConfirm","12341234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname("abc");
        assertTrue(passwordEncoder.matches("12341234",account.getPassword()));

    }

    @Test
    @DisplayName("패스워드 수정 : 입력값 에러 (길이)")
    @WithAccount("abc")
    void updatePasswordWithLengthError() throws Exception {
        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                        .param("newPassword","1")
                        .param("newPasswordConfirm","1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }
    @Test
    @DisplayName("패스워드 수정 : 입력값 에러 (불일치)")
    @WithAccount("abc")
    void updatePasswordWithNotMatchError() throws Exception {
        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                        .param("newPassword","12341234")
                        .param("newPasswordConfirm","12231234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }

    @Test
    @DisplayName("알림 설정 수정 폼")
    @WithAccount("abc")
    void updateNotificationForm() throws Exception{
        mockMvc.perform(get(SettingController.SETTINGS_NOTIFICATION_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_NOTIFICATION_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationForm"));
    }

    @Test
    @DisplayName("알림 설정 수정 : 입력값 정상")
    @WithAccount("abc")
    void updateNotification() throws Exception{
        mockMvc.perform(post(SettingController.SETTINGS_NOTIFICATION_URL)
                                .param("studyCreatedByEmail","true")
                                .param("studyCreatedByWeb","true")
                                .param("studyRegistrationResultByEmail","true")
                                .param("studyRegistrationResultByWeb","true")
                                .param("studyUpdatedByEmail","true")
                                .param("studyUpdatedByWeb","true")
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_NOTIFICATION_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname("abc");
        assertTrue(account.getNotificationSetting().getStudyCreatedByEmail());
        assertTrue(account.getNotificationSetting().getStudyCreatedByWeb());
        assertTrue(account.getNotificationSetting().getStudyRegistrationResultByEmailByEmail());
        assertTrue(account.getNotificationSetting().getStudyRegistrationResultByEmailByWeb());
        assertTrue(account.getNotificationSetting().getStudyUpdatedByEmail());
        assertTrue(account.getNotificationSetting().getStudyUpdatedByWeb());
    }
    @Test
    @DisplayName("닉네임 수정 폼")
    @WithAccount("abcd")
    void updateNicknameForm() throws Exception{
        mockMvc.perform(get(SettingController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @DisplayName("닉네임 수정: 입력값 정상")
    @WithAccount("abcd")
    void updateNickName() throws Exception{
        String newNickname = "코카곰";
        mockMvc.perform(post(SettingController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname(newNickname);
        assertEquals(newNickname,account.getNickname());
    }

    @Test
    @DisplayName("닉네임 수정: 에러(길이)")
    @WithAccount("abcd")
    void updateNickNameWithShortNickname() throws Exception{
        String newNickname = "코";
        mockMvc.perform(post(SettingController.SETTINGS_ACCOUNT_URL)
                        .param("nickname",newNickname)
                        .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));

    }
    @Test
    @DisplayName("닉네임 수정: 에러(중복)")
    @WithAccount("abcd")
    void updateNickNameWithDuplicatedNickname() throws Exception{
        String newNickname = "abcd";
        mockMvc.perform(post(SettingController.SETTINGS_ACCOUNT_URL)
                        .param("nickname",newNickname)
                        .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }
}