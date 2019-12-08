package io.littlegashk.webapp.rentity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
public class Tag {

  @Id
  private String tag;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name="tag_topics",joinColumns = { @JoinColumn(name="tag")}, inverseJoinColumns = {@JoinColumn(name="topic_id")})
  @JsonIgnore
  private Set<Topic> topics = new HashSet<>();
}
