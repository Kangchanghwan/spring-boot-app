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
class AccountSettingControllerTest {
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
    @DisplayName("?????? ??????")
    @WithAccount("abc")
    void addZone() throws Exception {
        Zone testZone = Zone.builder().city("test").province("test???").localNameOfCity("test???").build();
        zoneRepository.save(testZone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(AccountSettingController.SETTINGS_ZONES_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName()).orElse(null);
        assertNotNull(zone);
        assertTrue(accountRepository.findByNickname("abc").getZones().contains(zone));
    }

    @Test
    @DisplayName("?????? ??????")
    @WithAccount("abc")
    void removeZone() throws Exception {
        Zone testZone = Zone.builder().city("test").province("test???").localNameOfCity("test???").build();
        zoneRepository.save(testZone);
        Account abc = accountRepository.findByNickname("abc");
        accountService.addZone(abc, testZone);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(AccountSettingController.SETTINGS_ZONES_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(abc.getZones().contains(testZone));
    }


    @Test
    @DisplayName("?????? ?????? ???")
    @WithAccount("abc")
    void updateZoneForm() throws Exception {
        mockMvc.perform(get(AccountSettingController.SETTINGS_ZONES_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }


    @Test
    @DisplayName("?????? ??????")
    @WithAccount("abc")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("java");

        mockMvc.perform(post(AccountSettingController.SETTINGS_TAGS_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag tag = tagRepository.findByTitle("java").orElse(null);
        assertNotNull(tag);
        assertTrue(accountRepository.findByNickname("abc").getTags().contains(tag));
    }

    @Test
    @DisplayName("?????? ??????")
    @WithAccount("abc")
    void removeTag() throws Exception {
        Account abc = accountRepository.findByNickname("abc");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(abc, newTag);
        assertTrue(abc.getTags().contains(newTag));
        TagForm tagForm = new TagForm();
        String tagTitle = "newTag";
        tagForm.setTagTitle(tagTitle);
        mockMvc.perform(post(AccountSettingController.SETTINGS_TAGS_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(abc.getTags().contains(newTag));
    }


    @Test
    @DisplayName("?????? ?????? ???")
    @WithAccount("abc")
    void updateTagForm() throws Exception {
        mockMvc.perform(get(AccountSettingController.SETTINGS_TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @Test
    @DisplayName("????????? ??????: ????????? ??????")
    @WithAccount("abc")
    void updateProfile() throws Exception {
        String bio = "?????? ??????";
        mockMvc.perform(post(AccountSettingController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(AccountSettingController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));
        Account cock = accountRepository.findByNickname("abc");
        assertEquals(bio,cock.getProfile().getBio());
    }

    @Test
    @DisplayName("????????? ?????? : ????????? ??????")
    @WithAccount("abc")
    void updateProfileWithError() throws  Exception{
        String bio = "35??? ????????? ?????? 35??? ????????? ?????? 35??? ????????? ?????? 35??? ????????? ?????? 35??? ????????? ?????? 35??? ????????? ?????? 35??? ????????? ?????? ";
        mockMvc.perform(post(AccountSettingController.SETTINGS_PROFILE_URL)
                        .param("bio",bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
        Account cock = accountRepository.findByNickname("abc");
    }

    @Test
    @DisplayName("????????? ??????")
    @WithAccount("abc")
    void updateProfileForm() throws Exception {
        String bio = "?????? ??????";
        mockMvc.perform(get(AccountSettingController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }


    @Test
    @DisplayName("???????????? ?????? ???")
    @WithAccount("abc")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(AccountSettingController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }
    @Test
    @DisplayName("???????????? ??????: ????????? ??????")
    @WithAccount("abc")
    void updatePassword() throws  Exception{
        mockMvc.perform(post(AccountSettingController.SETTINGS_PASSWORD_URL)
                        .param("newPassword","12341234")
                        .param("newPasswordConfirm","12341234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(AccountSettingController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname("abc");
        assertTrue(passwordEncoder.matches("12341234",account.getPassword()));

    }

    @Test
    @DisplayName("???????????? ?????? : ????????? ?????? (??????)")
    @WithAccount("abc")
    void updatePasswordWithLengthError() throws Exception {
        mockMvc.perform(post(AccountSettingController.SETTINGS_PASSWORD_URL)
                        .param("newPassword","1")
                        .param("newPasswordConfirm","1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }
    @Test
    @DisplayName("???????????? ?????? : ????????? ?????? (?????????)")
    @WithAccount("abc")
    void updatePasswordWithNotMatchError() throws Exception {
        mockMvc.perform(post(AccountSettingController.SETTINGS_PASSWORD_URL)
                        .param("newPassword","12341234")
                        .param("newPasswordConfirm","12231234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }

    @Test
    @DisplayName("?????? ?????? ?????? ???")
    @WithAccount("abc")
    void updateNotificationForm() throws Exception{
        mockMvc.perform(get(AccountSettingController.SETTINGS_NOTIFICATION_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_NOTIFICATION_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationForm"));
    }

    @Test
    @DisplayName("?????? ?????? ?????? : ????????? ??????")
    @WithAccount("abc")
    void updateNotification() throws Exception{
        mockMvc.perform(post(AccountSettingController.SETTINGS_NOTIFICATION_URL)
                                .param("studyCreatedByEmail","true")
                                .param("studyCreatedByWeb","true")
                                .param("studyRegistrationResultByEmail","true")
                                .param("studyRegistrationResultByWeb","true")
                                .param("studyUpdatedByEmail","true")
                                .param("studyUpdatedByWeb","true")
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(AccountSettingController.SETTINGS_NOTIFICATION_URL))
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
    @DisplayName("????????? ?????? ???")
    @WithAccount("abcd")
    void updateNicknameForm() throws Exception{
        mockMvc.perform(get(AccountSettingController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @DisplayName("????????? ??????: ????????? ??????")
    @WithAccount("abcd")
    void updateNickName() throws Exception{
        String newNickname = "?????????";
        mockMvc.perform(post(AccountSettingController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(AccountSettingController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname(newNickname);
        assertEquals(newNickname,account.getNickname());
    }

    @Test
    @DisplayName("????????? ??????: ??????(??????)")
    @WithAccount("abcd")
    void updateNickNameWithShortNickname() throws Exception{
        String newNickname = "???";
        mockMvc.perform(post(AccountSettingController.SETTINGS_ACCOUNT_URL)
                        .param("nickname",newNickname)
                        .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));

    }
    @Test
    @DisplayName("????????? ??????: ??????(??????)")
    @WithAccount("abcd")
    void updateNickNameWithDuplicatedNickname() throws Exception{
        String newNickname = "abcd";
        mockMvc.perform(post(AccountSettingController.SETTINGS_ACCOUNT_URL)
                        .param("nickname",newNickname)
                        .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(view().name(AccountSettingController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }
}