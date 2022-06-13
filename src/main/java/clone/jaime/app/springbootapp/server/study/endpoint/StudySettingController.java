package clone.jaime.app.springbootapp.server.study.endpoint;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.CurrentUser;
import clone.jaime.app.springbootapp.server.study.application.StudyService;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyDescriptionForm;
import clone.jaime.app.springbootapp.server.study.endpoint.validator.StudyFormValidator;
import clone.jaime.app.springbootapp.server.study.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Controller
@RequestMapping("/study/{path}/settings")
public class StudySettingController {


    private final StudyRepository studyRepository;
    private final StudyService studyService;

    private final StudyFormValidator studyFormValidator;


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
    private String encode(String path){
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }



}
