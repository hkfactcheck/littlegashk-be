package io.littlegashk.webapp.rentity;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TopicMapper {

  @Mapping(target = "topicId", ignore = true)
  @Mapping(target = "oldId", source = "topicId")
  @Mapping(target = "references", ignore = true)
  @Mapping(target = "relatedTopics", ignore = true)
  @Mapping(target = "eventDate", ignore = true)
  @Mapping(target = "tags", ignore = true)
  Topic migrateToNewTopic(io.littlegashk.webapp.entity.Topic old);

  Reference migrateReference(io.littlegashk.webapp.entity.Reference old);

  @AfterMapping
  default void mapReferenceHash(io.littlegashk.webapp.entity.Reference old, @MappingTarget Reference newReference) {
    newReference.setHash(DigestUtils.md5Hex(old.getLink()));
  }

  @AfterMapping
  default void mapEventDate(io.littlegashk.webapp.entity.Topic old, @MappingTarget Topic newTopic) {
    newTopic.setEventDate(LocalDateTime.parse(old.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
  }

  @AfterMapping
  default void mapTags(io.littlegashk.webapp.entity.Topic old, @MappingTarget Topic newTopic) {
    newTopic.setTags(old.getTags().toArray(new String[]{}));
  }

  @AfterMapping
  default void mapReferences(io.littlegashk.webapp.entity.Topic old, @MappingTarget Topic newTopic) {
    if (null != old.getReferences()) {
      Set<Reference> collect = old.getReferences()
                                  .stream()
                                  .filter(r -> r != null && StringUtils.isNotBlank(r.getLink()))
                                  .map(this::migrateReference)
                                  .collect(Collectors.toSet());
      newTopic.setReferences(collect);
    }
  }


}
