/*
 * Copyright (C) 2010-2021 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
