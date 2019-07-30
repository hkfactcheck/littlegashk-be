package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.District;
import io.littlegashk.webapp.entity.DistrictId;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface DistrictRepository extends CrudRepository<District, DistrictId> {
    List<District> findAllByPid(String pid);

    default List<District> getAllDistricts() {

        return findAllByPid("DISTRICT");
    }
}
