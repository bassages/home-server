package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 1. Register the Mapping Helper and SPI Context
        // ModelsContext is the parameter type Hibernate couldn't resolve in your last crash
        List<String> coreSpi = List.of(
            "org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper",
            "org.hibernate.models.spi.ModelsContext",
            "org.hibernate.models.internal.OrmAnnotationDescriptor",
            "org.hibernate.models.internal.OrmAnnotationDescriptor$DynamicCreator"
        );

        for (String spi : coreSpi) {
            hints.reflection().registerType(TypeReference.of(spi),
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.ACCESS_DECLARED_FIELDS
                )
            );
        }

        // 2. Register Annotation Interfaces as Proxies
        List<String> annotations = List.of(
            "org.hibernate.annotations.SQLInsert",
            "org.hibernate.annotations.SQLUpdate",
            "org.hibernate.annotations.SQLDelete",
            "org.hibernate.annotations.SQLRestriction",
            "org.hibernate.annotations.Cache",
            "jakarta.persistence.Enumerated"
        );
        for (String ann : annotations) {
            hints.proxies().registerJdkProxy(TypeReference.of(ann));
            hints.reflection().registerType(TypeReference.of(ann), 
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        }

        // 3. Register Internal Models and their Descriptors
        // Added CacheAnnotation here to fix your specific NoSuchMethodException
        List<String> models = List.of(
            "SQLInsertAnnotation", "OverrideInsertAnnotation",
            "SQLUpdateAnnotation", "OverrideUpdateAnnotation",
            "SQLDeleteAnnotation", "OverrideDeleteAnnotation",
            "SQLRestrictionAnnotation", "OverrideRestrictionAnnotation",
            "EnumeratedAnnotation", "BasicAnnotation", "CacheAnnotation",
            "TableAnnotation", "EntityAnnotation", "InheritanceAnnotation"
        );

        for (String model : models) {
            String fqn = "org.hibernate.boot.models.annotations.internal." + model;
            // The class itself
            hints.reflection().registerType(TypeReference.of(fqn), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, 
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS
                ));
            // The Descriptor inner class (The link to the annotation)
            hints.reflection().registerType(TypeReference.of(fqn + "$Descriptor"), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, 
                    MemberCategory.INVOKE_PUBLIC_METHODS
                ));
        }
    }
}