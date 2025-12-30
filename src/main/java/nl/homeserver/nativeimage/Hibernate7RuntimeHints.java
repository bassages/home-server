package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 1. RESOURCE HINTS (The "Missing Link" for Service Discovery)
        hints.resources().registerPattern("META-INF/services/org.hibernate.*");
        hints.resources().registerPattern("org/hibernate/boot/models/annotations/internal/*");

        // 2. The Core Helper
        hints.reflection().registerType(
            TypeReference.of("org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper"),
            builder -> builder.withMembers(
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.ACCESS_DECLARED_FIELDS
            )
        );

        // 3. The Annotation Mirror/Proxy
        List<String> annotations = List.of(
            "org.hibernate.annotations.SQLInsert",
            "org.hibernate.annotations.SQLUpdate",
            "org.hibernate.annotations.SQLDelete",
            "org.hibernate.annotations.SQLRestriction",
            "org.hibernate.annotations.Cache"
        );
        for (String ann : annotations) {
            hints.proxies().registerJdkProxy(TypeReference.of(ann));
            hints.reflection().registerType(TypeReference.of(ann), 
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        }

        // 4. The Models and DESCRIPTORS
        List<String> internalModels = List.of(
            "SQLInsertAnnotation", "OverrideInsertAnnotation",
            "SQLUpdateAnnotation", "OverrideUpdateAnnotation",
            "SQLDeleteAnnotation", "OverrideDeleteAnnotation",
            "SQLRestrictionAnnotation", "OverrideRestrictionAnnotation",
            "CacheAnnotation", "EnumeratedAnnotation", "BasicAnnotation"
        );

        for (String model : internalModels) {
            String fqn = "org.hibernate.boot.models.annotations.internal." + model;
            
            // Register the Wrapper
            hints.reflection().registerType(TypeReference.of(fqn), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, 
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS
                ));

            // Register the Descriptor
            hints.reflection().registerType(TypeReference.of(fqn + "$Descriptor"), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, 
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
                    MemberCategory.INVOKE_DECLARED_METHODS
                ));
        }
        
        // 5. Register SPI context
        hints.reflection().registerType(TypeReference.of("org.hibernate.models.spi.ModelsContext"),
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
    }
}