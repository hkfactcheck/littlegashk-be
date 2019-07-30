package io.littlegashk.webapp;

import io.littlegashk.webapp.entity.DistrictView;
import io.littlegashk.webapp.repository.DistrictRepository;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/districts")
@Api(value = "District")
@Log4j2
public class DistrictController {

    // Pattern for recognizing a URL, based off RFC 3986
    private static final Pattern urlPattern = Pattern.compile("((ht|f)tp(s?):\\/\\/|www\\.)"
                                                              + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                                                              + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                                                              Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    @Autowired
    DistrictRepository repository;

    Map<String, Map<String, Map<String, String>>> cached;

    @GetMapping
    public ResponseEntity<Map<String, Map<String, Map<String, String>>>> getDistricts() {

        if (cached == null) {
            cached = new LinkedHashMap<>();

            List<DistrictView> list = repository.getAllDistricts()
                                                .stream()
                                                .map(DistrictView::fromDistrict)
                                                .sorted(Comparator.comparing(DistrictView::getCode))
                                                .collect(toList());
            for (DistrictView dv : list) {
                cached.putIfAbsent(dv.getRegion(), new LinkedHashMap<>());
                cached.get(dv.getRegion()).putIfAbsent(dv.getDistrict(), new LinkedHashMap<>());
                cached.get(dv.getRegion()).get(dv.getDistrict()).put(dv.getConstituency(), dv.getCode());
            }
        }
        return ResponseEntity.ok(cached);
    }


}
