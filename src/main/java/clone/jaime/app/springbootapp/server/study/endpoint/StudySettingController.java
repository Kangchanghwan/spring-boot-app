package clone.jaime.app.springbootapp.server.study.endpoint;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.CurrentUser;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.TagForm;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.ZoneForm;
import clone.jaime.app.springbootapp.server.study.application.StudyService;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyDescriptionForm;
import clone.jaime.app.springbootapp.server.study.endpoint.validator.StudyFormValidator;
import clone.jaime.app.springbootapp.server.tag.application.TagService;
import clone.jaime.app.springbootapp.server.tag.domain.entity.Tag;
import clone.jaime.app.springbootapp.server.tag.infra.repository.TagRepository;
import clone.jaime.app.springbootapp.server.zone.domain.entity.Zone;
import clone.jaime.app.springbootapp.server.zone.infra.repository.ZoneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/study/{path}/settings")
public class StudySettingController {


    private final StudyService studyService;

    private final StudyFormValidator studyFormValidator;

    private final TagRepository tagRepository;

    private final ObjectMapper objectMapper;

    private final TagService tagService;

    private final ZoneRepository zoneRepository;


    private String encode(String path){
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/study")
    public String studySettingForm(@CurrentUser Account account,
                                   @PathVariable String path,
                                   Model model) throws AccessDeniedException {
        Study study = studyService.getStudy(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }
    @PostMapping( "/study/close")
    public String closeStudy(@CurrentUser Account account,
                               @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.close(study);
        attributes.addFlashAttribute("message","스터디를 종료했습니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }
    @PostMapping( "/study/publish")
    public String publishStudy(@CurrentUser Account account,
                               @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.publish(study);
        attributes.addFlashAttribute("message","스터디를 공개했습니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }


    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account,
                               @PathVariable String path,
                               Model model,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!study.isEnableToRecruit()){
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 어러 번 변경할 수 없습니다.");
            return "redirect:/study/" + encode(path) + "/settings/study";
        }
        studyService.startRecruit(study);
        attributes.addFlashAttribute("message","인원 모집을 시작합니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account,
                               @PathVariable String path,
                               Model model,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!study.isEnableToRecruit()){
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 어러 번 변경할 수 없습니다.");
            return "redirect:/study/" + encode(path) + "/settings/study";
        }
        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message","인원 모집을 종료합니다.");
        return "redirect:/study/" + encode(path) + "settings/study";
    }

    @PostMapping("/study/path")
    public String updateStudyPath(@CurrentUser Account account,
                                  @PathVariable String path,
                                  @RequestParam String newPath,
                                  Model model,
                                  RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!studyService.isValidPath(newPath)){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError","사용할 수 없는 스터디 경로입니다.");
            return "study/settings/study";
        }
        studyService.updateStudyPath(study,newPath);
        attributes.addFlashAttribute("message","스터디 경로를 수정하였습니다.");
        return "redirect:/study/" + encode(newPath) + "/settings/study";
    }
    @PostMapping("/study/title")
    public String updateStudyTitle(@CurrentUser Account account,
                                  @PathVariable String path,
                                  @RequestParam String newTitle,
                                  Model model,
                                  RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!studyService.isValidTitle(newTitle)){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError","스터디 이름을 다시 입력하세요.");
            return "study/settings/study";
        }
        studyService.updateStudyTitle(study,newTitle);
        attributes.addFlashAttribute("message","스터디 이름을 수정하였습니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }
    @PostMapping("/study/remove")
    public String removeStudy(@CurrentUser Account account,
                              @PathVariable String path,
                              Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.remove(study);
        return "redirect:/";
    }


    @GetMapping("/zones")
    public String studyZoneForm(@CurrentUser Account account,
                                @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudy(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        List<String> zones =
                study.getZones().stream()
                        .map(Zone::toString)
                        .collect(Collectors.toList());
        List<String> allZones = zoneRepository.findAll()
                .stream()
                .map(Zone::toString)
                .collect(Collectors.toList());

        String whitelist = null;
        try {
            whitelist = objectMapper.writeValueAsString(allZones);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        model.addAttribute("zones", zones);
        model.addAttribute("whitelist", whitelist);
        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseStatus(HttpStatus.OK)
    public void addZone(@CurrentUser Account account,
                           @PathVariable String path,
                           @RequestBody ZoneForm zoneForm){
        Study study = studyService.getStudyToUpdateZone(account,path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
        studyService.addZone(study,zone);
    }

    @PostMapping("/zones/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeZone(@CurrentUser Account account,
                       @PathVariable String path,
                       @RequestBody ZoneForm zoneForm){
        Study study = studyService.getStudyToUpdateZone(account,path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
        studyService.removeZone(study,zone);
    }

    @GetMapping("/tags")
    public String studyTagForm(@CurrentUser Account account,
                             @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudy(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        List<String> tags =
                study.getTags().stream()
                        .map(t -> t.getTitle())
                        .collect(Collectors.toList());
        List<String> allTags = tagRepository.findAll()
                .stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList());

        String whitelist = null;
        try {
            whitelist = objectMapper.writeValueAsString(allTags);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        model.addAttribute("tags", tags);
        model.addAttribute("whitelist", whitelist);
        return "study/settings/tags";
    }


    @PostMapping("/tags/add")
    @ResponseStatus(HttpStatus.OK)
    public void addTag(@CurrentUser Account account,
                       @PathVariable String path,
                       @RequestBody TagForm tagForm){
        Study study = studyService.getStudyToUpdateTag(account,path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study,tag);
    }

    @PostMapping("/tags/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeTag(@CurrentUser Account account,
                       @PathVariable String path,
                       @RequestBody TagForm tagForm){
        Study study = studyService.getStudyToUpdateTag(account,path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle()).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 태그입니다."));
        studyService.removeTag(study,tag);
    }


    @GetMapping("/banner")
    public String studyImageForm(@CurrentUser Account account,
                                 @PathVariable String path, Model model){
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }
    @PostMapping("/banner")
    public String updateBanner(@CurrentUser Account account,
                               @PathVariable String path,
                               String image,
                               RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudy(account,path);
        studyService.updateStudyImage(study,image);
        attributes.addFlashAttribute("message","스터디 이미지를 수정하였습니다.");
        return "redirect:/study/"+ encode(path) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account,
                                    @PathVariable String path) throws AccessDeniedException {
        Study study = studyService.getStudy(account,path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + encode(path) + "/settings/banner";
    }
    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account,
                                    @PathVariable String path) throws AccessDeniedException {
        Study study = studyService.getStudy(account,path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + encode(path) + "/settings/banner";
    }


    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormValidator);
    }




    @GetMapping("/description")
    public String viewStudySetting(@CurrentUser Account account , @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudy(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(StudyDescriptionForm.builder()
                .shortDescription(study.getShortDescription())
                .fullDescription(study.getFullDescription())
                .build());
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudy(@CurrentUser Account account,
                              @PathVariable String path,
                              @Valid StudyDescriptionForm studyDescriptionForm,
                              Errors errors,
                              Model model,
                              RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudy(account, path);
        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }
        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message","스터디 소개를 수정했습니다.");
        return "redirect:/study/"+encode(path) + "/settings/description";
    }



}
