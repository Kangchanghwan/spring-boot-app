package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationForm {

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyRegistrationResultByEmail;
    private boolean studyRegistrationResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

    protected NotificationForm(Account account) {
        this.studyCreatedByEmail = account.getNotificationSetting().getStudyCreatedByEmail();
        this.studyCreatedByWeb = account.getNotificationSetting().getStudyCreatedByWeb();
        this.studyRegistrationResultByEmail = account.getNotificationSetting().getStudyRegistrationResultByEmailByEmail();
        this.studyRegistrationResultByWeb =  account.getNotificationSetting().getStudyRegistrationResultByEmailByWeb();
        this.studyUpdatedByEmail = account.getNotificationSetting().getStudyUpdatedByEmail();
        this.studyUpdatedByWeb = account.getNotificationSetting().getStudyUpdatedByWeb();
    }

    public static NotificationForm from(Account account){
        return new NotificationForm(account);
    }
}
