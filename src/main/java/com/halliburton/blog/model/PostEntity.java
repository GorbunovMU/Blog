package com.halliburton.blog.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity(name = "Post")
@Table(name = "post")
public class PostEntity {
    @Id
    @EqualsAndHashCode.Include()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    @ToString.Exclude
    private BlogEntity blog;

    @Column(name = "post_title", nullable = false)
    private String postTitle;

    @Lob
    @Column(name = "post_body", columnDefinition = "text", nullable = false)
    private String postBody;

    @Lob
    @Column(name = "post_conclusion", columnDefinition = "text")
    private String postConclusion;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "published_on", nullable = false)
    private LocalDate publishedOn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PostEntity that = (PostEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
