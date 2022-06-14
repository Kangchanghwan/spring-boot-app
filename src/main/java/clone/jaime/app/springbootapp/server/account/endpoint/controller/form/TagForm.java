package clone.jaime.app.springbootapp.server.account.endpoint.controller.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TagForm {

    private String tagTitle;
}
