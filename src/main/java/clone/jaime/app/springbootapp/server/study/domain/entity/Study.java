package clone.jaime.app.springbootapp.server.study.domain.entity;

import clone.jaime.app.springbootapp.server.account.domain.UserAccount;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyDescriptionForm;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyForm;
import clone.jaime.app.springbootapp.server.tag.domain.entity.Tag;
import clone.jaime.app.springbootapp.server.zone.domain.entity.Zone;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NamedEntityGraph(name = "Study.withAll",
attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members"),
})
@NamedEntityGraph(
        name ="Study.withTagsAndManagers", attributeNodes = {
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("managers")
})
@NamedEntityGraph(
        name ="Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")
})
@NamedEntityGraph(
        name ="Study.withManagers", attributeNodes = {
        @NamedAttributeNode("managers")
})
@NamedEntityGraph(
        name ="Study.withMembers", attributeNodes = {
        @NamedAttributeNode("members")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;
    //스터디 페이지 경로

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;
    //긴 설명

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;
    private boolean published;
    private boolean closed;
    @Accessors(fluent = true)
    // isUseBanner 메서드 생성 방지
    private boolean useBanner;

    public static Study from(StudyForm studyForm) {
        Study study = new Study();
        study.title = studyForm.getTitle();
        study.shortDescription = studyForm.getShortDescription();
        study.fullDescription = studyForm.getFullDescription();
        study.path = studyForm.getPath();
        return study;
    }

    public void published(){
        if(this.closed || this.published){
            throw new IllegalStateException("스터디를 이미 공개했거나 종료된 스터디 입니다.");
        }
        this.published = true;
        this.publishedDateTime = LocalDateTime.now();
    }
    public void close(){
        if(!this.published || this.closed){
            throw new IllegalStateException("스터디를 공개하지 않았거나 이미 종료한 스터디 입니다.");
        }
        this.closed = true;
        this.closedDateTime = LocalDateTime.now();
    }
    public boolean isEnableToRecruit(){
        return this.published && this.recruitingUpdatedDateTime == null ||
                this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }
    public void updatePath(String newPath) {
        this.path = newPath;
    }
    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }
    public boolean isRemovable(){
        return !this.published;
    }
    public void startRecruit(){
        if(!isEnableToRecruit()){
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도 하세요.");
        }
        this.recruiting = true;
        this.recruitingUpdatedDateTime = LocalDateTime.now();
    }
    public void stopRecruit(){
        if(!isEnableToRecruit()){
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도 하세요.");
        }
        this.recruiting = false;
        this.recruitingUpdatedDateTime = LocalDateTime.now();
    }




    public void addManager(Account account) {
        managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount){
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting() && !this.members.contains(account) && !this.managers.contains(account);
    } // 가입 가능여부
    public boolean isMember(UserAccount userAccount){
        return this.members.contains(userAccount.getAccount());
    }
    // 스터디의 멤버 여부
    public boolean isManager(UserAccount userAccount){
        return !this.managers.stream()
                .filter(
                        m -> userAccount
                                .getAccount()
                                .getEmail()
                                .equals(m.getEmail()))
                .collect(Collectors.toList())
                .isEmpty();
    }

    public void updateDescription(StudyDescriptionForm studyDescriptionForm) {
        this.shortDescription = studyDescriptionForm.getShortDescription();
        this.fullDescription = studyDescriptionForm.getFullDescription();
    }

    public void updateImage(String image) {
        this.image = image;
    }

    public void setBanner(boolean useBanner) {
        this.useBanner = useBanner;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public void removeZone(Zone zone) {
        this.zones.remove(zone);
    }

    public void addMember(Account account) {
        this.members.add(account);
    }
    // 스터디의 관리자 여부

    public void removeMember(Account account) {
        this.members.remove(account);
    }

    public String getEncodePath() {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
