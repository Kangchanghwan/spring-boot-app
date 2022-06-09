package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagForm {

    private String tagTitle;
}
