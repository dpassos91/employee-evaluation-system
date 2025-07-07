package aor.projetofinal.entity.enums;

/**
 * Enum representing the possible categories (areas) for a course.
 * Values must match those required by the project specification.
 *
 * FRONTEND       - Frontend development
 * BACKEND        - Backend development
 * INFRAESTRUTURA - Infrastructure
 * UX_UI          - User Experience / User Interface
 *
 * Use EnumType.STRING to store the values as strings in the database.
 */
public enum CourseCategoryEnum {
    /** Frontend development */
    FRONTEND,
    /** Backend development */
    BACKEND,
    /** Infrastructure */
    INFRAESTRUTURA,
    /** User Experience / User Interface */
    UX_UI
}
