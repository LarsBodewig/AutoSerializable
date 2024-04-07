package dev.bodewig.autoserializable;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 * Adds Serializable and no-arg constructor if necessary
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_19)
public class AutoSerializableProcessor extends AbstractProcessor {

	private ProcessingEnvironment processingEnv;

	protected TypeMirror serializableTM;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.processingEnv = processingEnv;
		serializableTM = processingEnv.getElementUtils().getTypeElement(Serializable.class.getName()).asType();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		roundEnv.getRootElements().forEach(element -> {
			if (element.getKind().equals(ElementKind.CLASS)) {
				TypeElement type = (TypeElement) element;
				if (!implementsSerializable(type)) {
					type.getInterfaces().add(serializableTM);
				}
				List<ExecutableElement> constructors = getConstructors(type);
				if (!constructors.isEmpty()) {
					Optional<ExecutableElement> noArgConstructor = getNoArgConstructor(constructors);
					if (noArgConstructor.isPresent()) {
						updateNoArgConstructor(noArgConstructor.get());
					} else {
						createNoArgConstructor(type);
					}
				}
			}
		});
		return false;
	}

	protected boolean implementsSerializable(TypeElement type) {
		return processingEnv.getTypeUtils().isSubtype(type.asType(), serializableTM);
	}

	protected List<ExecutableElement> getConstructors(TypeElement type) {
		return type.getEnclosedElements()
				.stream()
				.filter(element -> element.getKind().equals(ElementKind.CONSTRUCTOR))
				.map(element -> (ExecutableElement) element)
				.toList();
	}

	protected Optional<ExecutableElement> getNoArgConstructor(List<ExecutableElement> constructors) {
		return constructors.stream().filter(c -> c.getParameters().isEmpty()).findAny();
	}

	protected void updateNoArgConstructor(ExecutableElement noArgConstructor) {
		if (noArgConstructor.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PRIVATE))) {
			noArgConstructor.getModifiers().remove(Modifier.PRIVATE);
			noArgConstructor.getModifiers().add(Modifier.PROTECTED);
		}
	}

	protected ExecutableElement createNoArgConstructor(TypeElement type) {
		ExecutableElement noArgConstructor = null;
		type.getEnclosedElements().add(noArgConstructor);
	}
}
