package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        
        // Internal classes that provide the "Override Form" Hibernate is looking for
        List<String> hibernateInternalClasses = List.of(
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
            "org.hibernate.boot.models.annotations.internal.JoinColumnAnnotation",
            "org.hibernate.boot.models.annotations.internal.SQLRestrictionAnnotation",
            "org.hibernate.boot.models.annotations.internal.OverrideRestrictionAnnotation"
        );

        for (String className : hibernateInternalClasses) {
            hints.reflection().registerType(TypeReference.of(className), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.ACCESS_DECLARED_FIELDS, // Fixed: Modern replacement for DECLARED_FIELDS
                    MemberCategory.INVOKE_PUBLIC_METHODS
                )
            );
        }

        // The public Annotation interfaces
        List<String> annotationInterfaces = List.of(
            "org.hibernate.annotations.SQLInsert",
            "org.hibernate.annotations.SQLUpdate",
            "org.hibernate.annotations.SQLDelete",
            "org.hibernate.annotations.SQLRestriction",
            "jakarta.persistence.Enumerated",
            "org.hibernate.annotations.DialectOverride$SQLInserts",
            "org.hibernate.annotations.DialectOverride$SQLUpdates",
            "org.hibernate.annotations.DialectOverride$SQLDeletes"
        );

        for (String ann : annotationInterfaces) {
            hints.reflection().registerType(TypeReference.of(ann),
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
            );
        }

        // The core mapping helper
        hints.reflection().registerType(
            TypeReference.of("org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper"),
            builder -> builder.withMembers(
                MemberCategory.INVOKE_PUBLIC_METHODS, 
                MemberCategory.ACCESS_DECLARED_FIELDS
            )
        );
    }
}
