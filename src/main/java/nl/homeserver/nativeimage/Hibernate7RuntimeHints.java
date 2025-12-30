package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 1. Register the Mapping Helper
        hints.reflection().registerType(
            TypeReference.of("org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper"),
            builder -> builder.withMembers(
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.ACCESS_DECLARED_FIELDS
            )
        );

        // 2. Register Annotation Interfaces as Proxies
        // Hibernate 7 uses these to read @SQLInsert values
        List<String> annotations = List.of(
            "org.hibernate.annotations.SQLInsert",
            "org.hibernate.annotations.SQLUpdate",
            "org.hibernate.annotations.SQLDelete",
            "org.hibernate.annotations.SQLRestriction",
            "jakarta.persistence.Enumerated"
        );
        for (String ann : annotations) {
            hints.proxies().registerJdkProxy(TypeReference.of(ann));
            hints.reflection().registerType(TypeReference.of(ann), 
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        }

        // 3. Register Internal Models and their Descriptors
        List<String> models = List.of(
            "SQLInsertAnnotation", "OverrideInsertAnnotation",
            "SQLUpdateAnnotation", "OverrideUpdateAnnotation",
            "SQLDeleteAnnotation", "OverrideDeleteAnnotation",
            "SQLRestrictionAnnotation", "OverrideRestrictionAnnotation",
            "EnumeratedAnnotation", "BasicAnnotation"
        );

        for (String model : models) {
            String fqn = "org.hibernate.boot.models.annotations.internal." + model;
            // The class itself
            hints.reflection().registerType(TypeReference.of(fqn), 
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS));
            // The Descriptor inner class (The link to the annotation)
            hints.reflection().registerType(TypeReference.of(fqn + "$Descriptor"), 
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS));
        }
    }
}