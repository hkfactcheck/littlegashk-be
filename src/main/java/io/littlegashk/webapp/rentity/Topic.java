package io.littlegashk.webapp.rentity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import io.littlegashk.webapp.entity.EntryType;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

@Entity
@Data
@TypeDefs({
    @TypeDef(name = "string-array", typeClass = StringArrayType.class),
    @TypeDef(name = "int-array", typeClass = IntArrayType.class),
    @TypeDef(name = "json", typeClass = JsonStringType.class),
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
    @TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class),
    @TypeDef(name = "json-node", typeClass = JsonNodeStringType.class),
    @TypeDef(name="string",defaultForType=java.lang.String.class,typeClass=org.hibernate.type.TextType.class)
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty(position = 1, example = "2019-08-01|1565877016020")
    @EqualsAndHashCode.Include
    private UUID topicId;

    @ApiModelProperty(position = 2, example="This is topic/progress title")
    private String title;

    @ApiModelProperty(position = 3, example="This is summary")
    private String summary;

    @Enumerated(EnumType.STRING)
    @ApiModelProperty(position = 5, example="TOPIC", notes = "Not required when add or edit", allowableValues = "{TOPIC,PROGRESS,PUBLIC_RESPONSE}")
    private EntryType type;

    @Type(type = "string-array")
    @Column(columnDefinition = "VARCHAR ARRAY")
    @ApiModelProperty(position = 6)
    private List<String> relatedFiles;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name="topic_relation",joinColumns = { @JoinColumn(name="topic_id")}, inverseJoinColumns = {@JoinColumn(name="related_topic_id")})
    @JsonIgnore
    private Set<Topic> relatedTopics;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name="topic_hierarchy",joinColumns = { @JoinColumn(name="topic_id")}, inverseJoinColumns = {@JoinColumn(name="child_topic_id")})
    @JsonIgnore
    private Set<Topic> children;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "children")
    @JsonIgnore
    private Set<Topic> parents;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinTable(name="topic_reference",joinColumns = { @JoinColumn(name="topic_id")}, inverseJoinColumns = {@JoinColumn(name="reference_hash")})
    @ApiModelProperty(position = 8)
    @BatchSize(size=25)
    private Set<Reference> references;

    @Type(type = "string-array")
    @Column(columnDefinition = "VARCHAR ARRAY")
    @ApiModelProperty(position = 9)
    private String[] tags;


    @ApiModelProperty(position = 11, example="Last updated timestamp")
    private long lastUpdated;


    @ApiModelProperty(position = 12)
    private LocalDateTime eventDate;

    @ApiModelProperty(position = 13)
    private String imageUrl;

    @ApiModelProperty(position = 14)
    private String uid;

    @ApiModelProperty(position = 15)
    private Integer seq;

    private String oldId;


}
