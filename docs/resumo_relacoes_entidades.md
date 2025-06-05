
# Resumo das Relações Entre Entidades (Projeto AOR)

## ✅ Relações envolvendo UserEntity

| Entidade relacionada        | Tipo de relação         | Em UserEntity                      | Na outra entidade                        | Estado |
|-----------------------------|--------------------------|-------------------------------------|------------------------------------------|--------|
| RoleEntity                  | ManyToOne                | @ManyToOne + @JoinColumn            | @OneToMany(mappedBy = "role")            | ✅     |
| EvaluationEntity            | OneToMany (2x)           | mappedBy = "evaluated"/"evaluator" | @ManyToOne com JoinColumn (2x)           | ✅     |
| UserCourseEntity            | OneToMany                | mappedBy = "user"                  | @ManyToOne + @MapsId + JoinColumn        | ✅     |
| MessageEntity               | OneToMany (2x)           | mappedBy = "sender"/"receiver"     | @ManyToOne com JoinColumn (2x)           | ✅     |
| ProfileEntity               | OneToOne                 | mappedBy = "user"                  | @OneToOne + @JoinColumn(unique = true)   | ✅     |
| SessionTokenEntity          | OneToMany                | mappedBy = "user"                  | @ManyToOne + JoinColumn                  | ✅     |
| NotificationEntity          | OneToMany                | mappedBy = "user"                  | @ManyToOne + JoinColumn                  | ✅     |

---

## ✅ Avaliações

| Entidade                   | Relacionamento              | Anotação                          | Estado |
|----------------------------|-----------------------------|------------------------------------|--------|
| EvaluationEntity → Cycle   | ManyToOne                   | @JoinColumn(name = "cycle_id")     | ✅     |
| EvaluationCycleEntity → EvaluationEntity | OneToMany | mappedBy = "cycle"                | ✅     |

---

## ✅ Formações

| Entidade                 | Relacionamento              | Anotação                          | Estado |
|--------------------------|-----------------------------|------------------------------------|--------|
| UserCourseEntity → CourseEntity | ManyToOne          | @MapsId + @JoinColumn(course_id)  | ✅     |
| CourseEntity → UserCourseEntity | OneToMany          | mappedBy = "course"               | ✅     |

---

## ✅ Papéis e Permissões

| Entidade                        | Relacionamento              | Anotação                                   | Estado |
|---------------------------------|-----------------------------|---------------------------------------------|--------|
| RolePermissionEntity → Role     | ManyToOne + MapsId          | @JoinColumn(role_id)                        | ✅     |
| RolePermissionEntity → Permission | ManyToOne + MapsId        | @JoinColumn(permission_id)                  | ✅     |
| RoleEntity → RolePermissionEntity | OneToMany                 | mappedBy = "role"                           | ✅     |
| PermissionEntity → RolePermissionEntity | OneToMany           | mappedBy = "permission"                     | ✅     |

---

Todos os relacionamentos foram verificados de forma bidirecional e estão de acordo com as melhores práticas do JPA.
