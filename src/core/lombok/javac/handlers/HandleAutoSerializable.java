package lombok.javac.handlers;

import static lombok.javac.handlers.HandleDelegate.HANDLE_DELEGATE_PRIORITY;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAST;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.ResolutionResetNeeded;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.spi.Provides;

/**
 * Provides an JavacASTAdapter to manipulate all source files. Adds
 * {@code implements Serializable} to the type declaration and a no-arg
 * constructor if necessary.
 */
@Provides(JavacASTVisitor.class)
@HandlerPriority(HANDLE_DELEGATE_PRIORITY + 200)
@ResolutionResetNeeded
public class HandleAutoSerializable extends JavacASTAdapter {

	private static final Logger logger = Logger.getLogger("AutoSerializable");
	
	private static JCExpression serializableExp(JavacAST ast) {
		JavacTreeMaker maker = ast.getTreeMaker();
		JCExpression out = maker.Ident(ast.toName("java"));
		out = maker.Select(out, ast.toName("io"));
		out = maker.Select(out, ast.toName("Serializable"));
		return out;
	}

	private HandleConstructor handleConstructor = new HandleConstructor();

	/**
	 * Called when a type was read. Adds {@code implements Serializable} to the
	 * type declaration and a no-arg constructor unless the type already
	 * explicitly declares implementing {@code Serializable}. The no-arg
	 * constructor is created based on Lombok's default behaviour.
	 * 
	 * @param typeNode
	 *            the AST node
	 * @param type
	 *            the type delcaration
	 */
	@Override public void endVisitType(JavacNode typeNode, JCClassDecl type) {
		if (typeNode.getKind() == Kind.TYPE) {
			// check if the class already implements serializable (directly)
			boolean implementsSerializable = false;
			for (JCExpression interf : type.getImplementsClause()) {
				implementsSerializable |= 
					JavacHandlerUtil.typeMatches(Serializable.class, typeNode, interf.getTree());
			}
			if (!implementsSerializable) {
				// implement serializable
				ListBuffer<JCExpression> implementing = new ListBuffer<JCExpression>();
				implementing.addAll(type.implementing);
				implementing.add(serializableExp(typeNode.getAst()));
				type.implementing = implementing.toList();

				// add protected no-arg constructor if necessary
				handleConstructor.generateConstructor(
					typeNode, 
					AccessLevel.PROTECTED, 
					List.<JCAnnotation>nil(), 
					List.<JavacNode>nil(), 
					true, 
					null, 
					SkipIfConstructorExists.NO, 
					typeNode);
				logger.log(Level.WARNING, "Added Serializable to " + type.getSimpleName().toString());
			} else {
				logger.log(Level.WARNING, type.getSimpleName().toString() + " already implements Serializable");
			}
		}
	}
}
