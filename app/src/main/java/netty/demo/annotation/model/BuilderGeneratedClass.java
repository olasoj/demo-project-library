package netty.demo.annotation.model;

import netty.demo.annotation.exception.ClassGenerationException;
import netty.demo.annotation.model.descriptors.AttributeDescriptor;
import netty.demo.annotation.model.descriptors.ConstructorDescriptor;
import netty.demo.annotation.model.descriptors.MethodDescriptor;

import java.util.*;
import java.util.stream.Collectors;

public final class BuilderGeneratedClass extends GeneratedClass {

    private static final String BUILDER_NAME = "%sBuilder";
    private static final String INSTANCE = "instance";
    private static final String PUBLIC = "public";
    private static final String FINAL = "final";
    private static final String PRIVATE = "private";

    private final BuilderAnnotatedClass annotatedClass;

    public BuilderGeneratedClass(BuilderAnnotatedClass annotatedClass) {
        this.annotatedClass = annotatedClass;
        this.setClassName(computeBuilderName());
        this.setPackageElement(annotatedClass.getPackageElement());
        this.setPackageName(annotatedClass.getClassToBuildPackageName());
        if (annotatedClass.useSingletonBuilder()) {
            addSingletonPattern();
        } else {
            addSimpleConstructor();
        }
        addCommonFields();
        if (annotatedClass.useFluentBuilder()) {
            addFluentSetters();
        } else {
            addSimpleSetters();
        }
        addBuildMethod();
    }

    private void addCommonFields() {
        this.getAttributes().add(new AttributeDescriptor("container", "Map<String, Object>", PRIVATE));
        this.getImports().add("import java.util.Map;");
        this.getImports().add("import java.util.HashMap;");
        this.getImports().add(String.format("import %s.%s;",
                annotatedClass.getClassToBuildPackageName(),
                annotatedClass.getClassToBuild()));
        this.getConstructors().forEach(constructor ->
                constructor.addCodeLine("this.container = new HashMap<>();"));
    }

    private void addSingletonPattern() {
        this.getAttributes().add(new AttributeDescriptor(INSTANCE,
                computeBuilderName(),
                "private static"));
        MethodDescriptor descriptor = new MethodDescriptor("getInstance", computeBuilderName());
        descriptor.addModifier(PUBLIC);
        descriptor.addModifier("static");
        descriptor.addCodeLine("if (instance == null) {")
                .addCodeLine(String.format("\tinstance = new %s();", computeBuilderName()))
                .addCodeLine("}")
                .addCodeLine("return instance;");
        this.getMethods().add(descriptor);
        ConstructorDescriptor privateConstructor = new ConstructorDescriptor(computeBuilderName());
        privateConstructor.addModifier(PRIVATE);
        this.getConstructors().add(privateConstructor);
    }

    private void addSimpleConstructor() {
        ConstructorDescriptor publicConstructor = new ConstructorDescriptor(computeBuilderName());
        publicConstructor.addModifier(PUBLIC);
        this.getConstructors().add(publicConstructor);
    }

    private void addFluentSetters() {
        for (Map.Entry<AttributeDescriptor, Optional<MethodDescriptor>> attributeMethodMapping :
                this.annotatedClass.getAttributeSetterMapping().entrySet()) {
            this.getMethods().add(new MethodDescriptor(
                    computeSetterName(attributeMethodMapping.getKey().name()),
                    computeBuilderName())
                    .addModifier(PUBLIC)
                    .addModifier(FINAL)
                    .addArgument("value",
                            attributeMethodMapping.getKey().type())
                    .addCodeLine(String.format("this.container.putIfAbsent(\"%s\", value);",
                            attributeMethodMapping.getKey().name()))
                    .addCodeLine("return this;"));
        }
    }

    private void addSimpleSetters() {
        for(Map.Entry<AttributeDescriptor, Optional<MethodDescriptor>> attributeMethodMapping:
                this.annotatedClass.getAttributeSetterMapping().entrySet()) {
            this.getMethods().add(new MethodDescriptor(
                    computeSetterName(attributeMethodMapping.getKey().name()), "void")
                    .addModifier(PUBLIC)
                    .addModifier(FINAL)
                    .addArgument("value",
                            attributeMethodMapping.getKey().type())
                    .addCodeLine(String.format("this.container.putIfAbsent(\"%s\", value);",
                            attributeMethodMapping.getKey().name())));
        }
    }

    private void addBuildMethod() {
        MethodDescriptor buildMethod = new MethodDescriptor("build",
                this.annotatedClass.getClassToBuild())
                .addModifier(PUBLIC)
                .addModifier(FINAL)
                .addCodeLine("if(this.container.isEmpty()) {")
                .addCodeLine("\tthrow new IllegalStateException(\"Not enough information to build\");")
                .addCodeLine("}");
        if (this.annotatedClass.getConstructors().isEmpty()
                || this.annotatedClass.getNoArgsConstructor().isPresent()) {
            generateNoArgsConstructorPresentBuildMethod(buildMethod);
        } else {
            generateNoArgsConstructorAbsentBuildMethod(buildMethod);
        }
        buildMethod.addCodeLine("this.container.clear();");
        buildMethod.addCodeLine("return result;");
        this.getMethods().add(buildMethod);
    }

    private void generateNoArgsConstructorPresentBuildMethod (MethodDescriptor buildMethod) {
        buildMethod.addCodeLine(String.format("%s result = new %s();",
                this.annotatedClass.getClassToBuild(),
                this.annotatedClass.getClassToBuild()));
        for (Map.Entry<AttributeDescriptor, Optional<MethodDescriptor>> attributeMethodMapping:
                this.annotatedClass.getAttributeSetterMapping().entrySet()) {
            AttributeDescriptor attribute = attributeMethodMapping.getKey();
            if (attributeMethodMapping.getValue().isPresent()) {
                MethodDescriptor setter = attributeMethodMapping.getValue()
                        .orElseThrow(() ->
                                new ClassGenerationException(
                                        String.format("Can't obtain setter for %s", attribute.name())));
                buildMethod.addCodeLine(String.format("result.%s((%s)this.container.get(\"%s\"));",
                        setter.getName(),
                        attribute.type(),
                        attribute.name()));
            } else if (attribute.accessModifier().contains(PUBLIC)) {
                buildMethod.addCodeLine(String.format("result.%s = (%s)this.container.get(\"%s\");",
                        attribute.name(),
                        attribute.type(),
                        attribute.name()));
            } else {
                throw new ClassGenerationException(String.format("Non public attribute %s without a valid setter",
                        attribute.name()));
            }
        }
    }

    private void generateNoArgsConstructorAbsentBuildMethod (MethodDescriptor buildMethod) {
        ConstructorDescriptor leastArgumentsConstructor = this.annotatedClass.getConstructors()
                .stream()
                .min(Comparator.comparingInt(constructor -> constructor.getArguments().size()))
                .orElseThrow(() -> new ClassGenerationException("Can't obtain a suitable constructor"));
        for (Map.Entry<String, String> argument :
                leastArgumentsConstructor.getArguments().entrySet()) {
            buildMethod.addCodeLine(String.format("%s %s = (%s) this.container.get(\"%s\");",
                    argument.getValue(), argument.getKey(), argument.getValue(), argument.getKey()));
        }
        List<String> argList = new ArrayList<>(leastArgumentsConstructor.getArguments().keySet());
        buildMethod.addCodeLine(String.format("%s result = new %s(%s);",
                annotatedClass.getClassToBuild(),
                annotatedClass.getClassToBuild(),
                String.join(", ", argList)));
        List<Map.Entry<AttributeDescriptor, Optional<MethodDescriptor>>> missingAttributes =
                this.annotatedClass.getAttributeSetterMapping().entrySet()
                        .stream()
                        .filter(attributeSetterMapping -> !argList.contains(attributeSetterMapping.getKey().name()))
                        .collect(Collectors.toList());
        for (Map.Entry<AttributeDescriptor, Optional<MethodDescriptor>> missingAttribute : missingAttributes) {
            buildMethod.addCodeLine(String.format("%s %s = (%s)this.container.get(\"%s\");",
                    missingAttribute.getKey().type(),
                    missingAttribute.getKey().name(),
                    missingAttribute.getKey().type(),
                    missingAttribute.getKey().name()));
            MethodDescriptor setter = missingAttribute.getValue().orElseThrow(() ->
                    new ClassGenerationException(String.format(
                            "Can't obtain a suitable setter for attribute %s",
                            missingAttribute.getKey().name())));
            buildMethod.addCodeLine(String.format("result.%s(%s);",
                    setter.getName(),
                    missingAttribute.getKey().name()));
        }
    }

    private String computeBuilderName() {
        return this.getClassName()
                .orElse(String.format(BUILDER_NAME, this.annotatedClass.getClassToBuild()));
    }

    private String computeSetterName(String attributeName) {
        return "set" + attributeName.substring(0,1).toUpperCase() +
                attributeName.substring(1);
    }
}