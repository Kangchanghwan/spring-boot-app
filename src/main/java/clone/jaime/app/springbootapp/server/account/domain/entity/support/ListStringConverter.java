package clone.jaime.app.springbootapp.server.account.domain.entity.support;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Convert
public class ListStringConverter implements AttributeConverter<List<String>,String> {
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return Optional.ofNullable(attribute)
                .map(a-> String.join(",", a)).orElse("");
        //,로 구분하여 list 로 가져온 데이터를 합친다.
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return Stream.of(dbData.split(","))
                .collect(Collectors.toList());
        // ,로 데이터를 구분하여 list로 출력해준다.
    }
}
