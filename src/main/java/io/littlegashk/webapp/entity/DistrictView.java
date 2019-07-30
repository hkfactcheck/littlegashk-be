package io.littlegashk.webapp.entity;

import com.amazonaws.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictView {

    String region;

    String district;

    String constituency;

    String code;

    public static DistrictView fromDistrict(District district) {

        String[] fields = district.getDistrictId().split("-");
        return new DistrictView(fields[0], fields[1], fields[2], district.getDistrictCode());
    }

    public static District toDistrict(DistrictView view) {

        return new District().setPid("DISTRICT")
                             .setDistrictId(StringUtils.join("-", view.region, view.district, view.constituency))
                             .setDistrictCode(view.getCode());
    }
}
