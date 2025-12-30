package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        
        // List of core internal classes that must exist for SQL Overrides and Enums
        List<String> hibernateClasses = List.of(
            "org.hibernate.boot.models.annotations.internal.SQLInsertAnnotation",
            "org.hibernate.boot.models.annotations.internal.SQLUpdateAnnotation",
            "org.hibernate.boot.models.annotations.internal.SQLDeleteAnnotation",
            "org.hibernate.boot.models.annotations.internal.OverrideInsertAnnotation",
            "org.hibernate.boot.models.annotations.internal.OverrideUpdateAnnotation",
            "org.hibernate.boot.models.annotations.internal.OverrideDeleteAnnotation",
            "org.hibernate.boot.models.annotations.internal.EnumeratedAnnotation",
            "org.hibernate.boot.models.annotations.internal.BasicAnnotation",
            "org.hibernate.boot.models.annotations.internal.CacheAnnotation",
            "org.hibernate.boot.models.annotations.internal.TableAnnotation",
            "org.hibernate.boot.models.annotations.internal.ColumnAnnotation",
            "org.hibernate.boot.models.annotations.internal.EntityAnnotation",
            "org.hibernate.boot.models.annotations.internal.IdAnnotation",
            "org.hibernate.boot.models.annotations.internal.GeneratedValueAnnotation",
            "org.hibernate.boot.models.annotations.internal.JoinColumnAnnotation"
        );

        // Register each one for reflection
        for (String className : hibernateClasses) {
            hints.reflection().registerType(TypeReference.of(className), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.DECLARED_FIELDS, // Fixed the deprecated PUBLIC_FIELDS
                    MemberCategory.INVOKE_PUBLIC_METHODS
                )
            );
        }

        // Register the Annotation interfaces themselves
        List<String> annotationInterfaces = List.of(
            "org.hibernate.annotations.SQLInsert",
            "org.hibernate.annotations.SQLUpdate",
            "org.hibernate.annotations.SQLDelete",
            "jakarta.persistence.Enumerated"
        );

        for (String ann : annotationInterfaces) {
            hints.reflection().registerType(TypeReference.of(ann),
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
            );
        }

        // Specifically register the Helper that Hibernate uses for mapping
        hints.reflection().registerType(TypeReference.of("org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper"),
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.DECLARED_FIELDS)
        );
    }
}
