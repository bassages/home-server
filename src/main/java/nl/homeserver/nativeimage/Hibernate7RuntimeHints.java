package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import java.util.ArrayList;
import java.util.List;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        List<String> classesToRegister = new ArrayList<>();

        // Core Internal Annotation Wrappers and their Descriptors
        // The '$Descriptor' is the "Missing Link" Hibernate 7.1 needs
        classesToRegister.addAll(List.of(
            "org.hibernate.boot.models.annotations.internal.SQLInsertAnnotation",
            "org.hibernate.boot.models.annotations.internal.SQLInsertAnnotation$Descriptor",
            "org.hibernate.boot.models.annotations.internal.OverrideInsertAnnotation",
            "org.hibernate.boot.models.annotations.internal.OverrideInsertAnnotation$Descriptor",
            "org.hibernate.boot.models.annotations.internal.SQLUpdateAnnotation",
            "org.hibernate.boot.models.annotations.internal.SQLUpdateAnnotation$Descriptor",
            "org.hibernate.boot.models.annotations.internal.OverrideUpdateAnnotation",
            "org.hibernate.boot.models.annotations.internal.OverrideUpdateAnnotation$Descriptor",
            "org.hibernate.boot.models.annotations.internal.SQLDeleteAnnotation",
            "org.hibernate.boot.models.annotations.internal.SQLDeleteAnnotation$Descriptor",
            "org.hibernate.boot.models.annotations.internal.OverrideDeleteAnnotation",
            "org.hibernate.boot.models.annotations.internal.OverrideDeleteAnnotation$Descriptor",
            "org.hibernate.boot.models.annotations.internal.EnumeratedAnnotation",
            "org.hibernate.boot.models.annotations.internal.BasicAnnotation"
        ));

        for (String className : classesToRegister) {
            hints.reflection().registerType(TypeReference.of(className), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS
                )
            );
        }

        // Public interfaces and repeatable containers
        List<String> interfaces = List.of(
            "org.hibernate.annotations.SQLInsert",
            "org.hibernate.annotations.SQLUpdate",
            "org.hibernate.annotations.SQLDelete",
            "jakarta.persistence.Enumerated",
            "org.hibernate.annotations.DialectOverride$SQLInserts",
            "org.hibernate.annotations.DialectOverride$SQLUpdates",
            "org.hibernate.annotations.DialectOverride$SQLDeletes"
        );

        for (String intrf : interfaces) {
            hints.reflection().registerType(TypeReference.of(intrf),
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
            );
        }

        // The helper class that performs the lookup
        hints.reflection().registerType(
            TypeReference.of("org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper"),
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
        );
    }
}
