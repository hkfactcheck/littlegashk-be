package io.littlegashk.webapp.rentity;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

@Data
@Entity
@TypeDefs({
    @TypeDef(name="string",defaultForType=java.lang.String.class,typeClass=org.hibernate.type.TextType.class)
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reference {

    @Id
    @EqualsAndHashCode.Include
    private String hash;

    private String name;

    private String link;

    private String date;

    private String comment;

    private String title;

    private String imageUrl;

    @ManyToMany(mappedBy = "references", fetch = FetchType.LAZY)
    private Set<Topic> topics = new HashSet<>();
}
