package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;


@Entity
@Table(name = "Course")
public class CourseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false , updatable = false, unique = true )
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "time_span", nullable = false)
    private double time_span;

    @Column (name="description", nullable = false, unique = false, length = 65535, columnDefinition = "TEXT")
    private String description;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "course_category", nullable = false)
    private String course_category;

    @Column(name = "is_active", nullable = false)
    private boolean is_active;

    // Relacionamento many to many com User
    @ManyToMany(mappedBy = "courses")  // "courses" é o nome do atributo na classe Utilizador
    private Set<UserEntity> users;  // A coleção de users associados à formação



}


