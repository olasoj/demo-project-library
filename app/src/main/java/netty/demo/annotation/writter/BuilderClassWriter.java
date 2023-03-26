package netty.demo.annotation.writter;


import netty.demo.annotation.exception.ClassGenerationException;
import netty.demo.annotation.model.BuilderAnnotatedClass;
import netty.demo.annotation.model.BuilderGeneratedClass;

import javax.annotation.processing.Filer;

public final class BuilderClassWriter extends JavaClassWriter<BuilderGeneratedClass> {
    public BuilderClassWriter(Filer filer) {
        super(filer);
    }

    public void generateBuilderClass(BuilderAnnotatedClass builderAnnotatedClass) throws ClassGenerationException {
        this.writeFile(new BuilderGeneratedClass(builderAnnotatedClass));
    }

}
