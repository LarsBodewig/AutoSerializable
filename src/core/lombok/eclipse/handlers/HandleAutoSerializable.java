package lombok.eclipse.handlers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.HandlerPriority;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAST;
import lombok.eclipse.EclipseASTAdapter;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.spi.Provides;

@Provides(EclipseASTAdapter.class)
@DeferUntilPostDiet
@HandlerPriority(65537)
public class HandleAutoSerializable extends EclipseASTAdapter {

	private static final Logger logger = Logger.getLogger("AutoSerializable");
	
	private static TypeReference serializableExp(EclipseAST ast) {
		char[][] serializableName = Eclipse.fromQualifiedName("java.io.Serializable");
		return new QualifiedTypeReference(serializableName, new long[] {0, 0, 0});
	}

	private HandleConstructor handleConstructor = new HandleConstructor();

	@Override public void endVisitType(EclipseNode typeNode, TypeDeclaration type) {
		if (typeNode.getKind() == Kind.TYPE) {
			// check if the class already implements serializable (directly)
			boolean implementsSerializable = false;
			for (TypeReference interf : type.superInterfaces) {
				implementsSerializable |=
					EclipseHandlerUtil.typeMatches(Serializable.class, typeNode, interf);
			}
			if (!implementsSerializable) {
				// implement serializable
				ListBuffer<TypeReference> implementing = new ListBuffer<TypeReference>();
				implementing.addAll(Arrays.asList(type.superInterfaces));
				implementing.add(serializableExp(typeNode.getAst()));
				type.superInterfaces = implementing.toArray(new TypeReference[implementing.size()]);

				// add protected no-arg constructor if necessary
				handleConstructor.generateConstructor(
					typeNode, 
					AccessLevel.PROTECTED, 
					List.<EclipseNode>nil(), 
					true, 
					null, 
					SkipIfConstructorExists.NO, 
					List.<Annotation>nil(),
					typeNode);
				logger.log(Level.WARNING, "Added Serializable to " + String.valueOf(type.name));
			} else {
				logger.log(Level.WARNING, String.valueOf(type.name) + " already implements Serializable");
			}
		}
	}
}
