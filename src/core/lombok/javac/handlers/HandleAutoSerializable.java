package lombok.javac.handlers;

import static lombok.javac.handlers.HandleDelegate.HANDLE_DELEGATE_PRIORITY;
import static lombok.javac.handlers.JavacHandlerUtil.annotationTypeMatches;
import static lombok.javac.handlers.JavacHandlerUtil.upToTypeNode;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
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

import javax.lang.model.element.Modifier;

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
		if (typeNode.getKind() == Kind.TYPE // is type
				&& type.sym != null // is not anonymous
				&& !type.sym.isAnnotationType()) { // is not annotation
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

				if (!type.sym.isInterface()) {
					// add protected no-arg constructor if necessary
					boolean hasPublicNoArgConstructor = publicNoArgsConstructorExists(typeNode);
					if (!hasPublicNoArgConstructor) {
						handleConstructor.generateConstructor(
								typeNode,
								AccessLevel.PROTECTED,
								List.<JCAnnotation>nil(),
								List.<JavacNode>nil(),
								true,
								null,
								SkipIfConstructorExists.YES,
								typeNode);
					}
				}
				logger.log(Level.WARNING, "Added Serializable to " + type.getSimpleName().toString());
			} else {
				logger.log(Level.WARNING, type.getSimpleName().toString() + " already implements Serializable");
			}
		}
	}

	private static boolean publicNoArgsConstructorExists(JavacNode typeNode) {
		for (JCTree def : ((JCClassDecl) typeNode.get()).defs) {
			if (def instanceof JCTree.JCMethodDecl) {
				JCTree.JCMethodDecl md = (JCTree.JCMethodDecl) def;
				if (md.name.contentEquals("<init>")
						&& md.params.size() == 0
						&& !md.mods.getFlags().contains(Modifier.PRIVATE)) {
					return true;
				}
			}
		}
		return false;
	}
}
