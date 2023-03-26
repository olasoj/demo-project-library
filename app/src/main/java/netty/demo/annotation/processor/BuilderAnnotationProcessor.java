package netty.demo.annotation.processor;

import com.google.auto.service.AutoService;
import netty.demo.annotation.Builder;
import netty.demo.annotation.exception.BadAnnotationUsageException;
import netty.demo.annotation.model.BuilderAnnotatedClass;
import netty.demo.annotation.model.descriptors.AttributeDescriptor;
import netty.demo.annotation.model.descriptors.ClassDescriptor;
import netty.demo.annotation.model.descriptors.MethodDescriptor;
import netty.demo.annotation.model.descriptors.TypeMapper;
import netty.demo.annotation.writter.BuilderClassWriter;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SupportedAnnotationTypes("netty.demo.annotation.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public final class BuilderAnnotationProcessor extends AbstractAnnotationProcessor<Builder, BuilderAnnotatedClass> {
    private static final String ANNOTATION_NAME = "@Builder";

    public BuilderAnnotationProcessor() {
        super(Builder.class);
    }

    @Override
    void validateElement(Element annotatedElement) throws BadAnnotationUsageException {
        if (!annotatedElement.getKind().isClass()) {
            throw new BadAnnotationUsageException(annotatedElement.getSimpleName().toString(),
                    ANNOTATION_NAME,
                    "Only classes may use this annotation");
        } else {
            if (annotatedElement.getModifiers().contains(Modifier.ABSTRACT)) {
                throw new BadAnnotationUsageException(annotatedElement.getSimpleName().toString(),
                        ANNOTATION_NAME,
                        "Only non-abstract classes are supported at this time");
            }
        }
    }

    @Override
    BuilderAnnotatedClass transformElementToModel(Element annotatedElement, Builder annotation) {
        TypeMapper typeMapper = annotatedElement.accept(new TypeMapper(), null);
        ClassDescriptor classDescriptor = typeMapper.getClassDescriptor();

        Map<AttributeDescriptor, Optional<MethodDescriptor>> attributeSetterMapping = new HashMap<>();

        for (AttributeDescriptor attribute : classDescriptor.getAttributes()) {
            Optional<MethodDescriptor> setter = classDescriptor.getMethods()
                    .stream()
                    .filter(method -> isMethodASetter(attribute, method))
                    .findFirst();
            attributeSetterMapping.put(attribute, setter);
        }

        return new BuilderAnnotatedClass()
                .setClassToBuild(classDescriptor.getClassName())
                .setClassToBuildPackageName(classDescriptor.getPackageName())
                .setPackageElement((PackageElement) annotatedElement.getEnclosingElement())
                .setConstructors(classDescriptor.getConstructors())
                .setUseFluentBuilder(annotation.useFluentBuilder())
                .setUseSingletonBuilder(annotation.useSingletonBuilder())
                .setAttributeSetterMapping(attributeSetterMapping);
    }

    private boolean isMethodASetter(AttributeDescriptor attribute, MethodDescriptor method) {
        return method.getName().toLowerCase().equals(String.format("set%s", attribute.name().toLowerCase()));
    }

    @Override
    void finalizeElementProcessing(BuilderAnnotatedClass model) {
        BuilderClassWriter writer = new BuilderClassWriter(super.processingEnv.getFiler());
        writer.generateBuilderClass(model);
    }
}
