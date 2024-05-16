package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    protected Theme() {
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateEmpty("name", name);
        validateEmpty("description", description);
        validateEmpty("thumbnail", thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateEmpty(final String fieldName, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmptyValueNotAllowedException(fieldName);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
