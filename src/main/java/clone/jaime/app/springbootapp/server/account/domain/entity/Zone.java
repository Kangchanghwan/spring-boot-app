package clone.jaime.app.springbootapp.server.account.domain.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * city : 영문 도시이름
 * localNameOfCity : 한국어 도시 이름
 * province : 주(도) 이름, nullable
 */


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    private String province;

    public static Zone map(String line) {
        String[] split = line.split(",");
        Zone zone = new Zone();
        zone.city = split[0];
        zone.localNameOfCity = split[1];
        zone.province = split[2];
        return zone;
    }
    // 문자열을 받아 zone으로 매핑해주는 메서드이다.

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}
