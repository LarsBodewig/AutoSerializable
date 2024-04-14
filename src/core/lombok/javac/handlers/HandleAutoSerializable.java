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

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;

import lombok.core.AST.Kind;
import lombok.AccessLevel;
import lombok.core.HandlerPriority;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.ResolutionResetNeeded;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.spi.Provides;

@Provides(JavacASTVisitor.class)
@HandlerPriority(HANDLE_DELEGATE_PRIORITY + 200)
@ResolutionResetNeeded
public class HandleAutoSerializable extends JavacASTAdapter {

	private HandleConstructor handleConstructor = new HandleConstructor();

	@Override public void visitType(JavacNode typeNode, JCClassDecl type) {
		if (typeNode.getKind() == Kind.TYPE) {
			// check if the class already implements serializable (directly)
			boolean implementsSerializable = false;
			for (JCExpression interf : type.getImplementsClause()) {
				implementsSerializable |= 
					JavacHandlerUtil.typeMatches(Serializable.class, typeNode, interf.getTree());
			}
			if (!implementsSerializable) {
				// implement serializable
				JCExpression serializable = 
					JavacHandlerUtil.genTypeRef(typeNode, Serializable.class.getCanonicalName());
				type.getImplementsClause().add(serializable);
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
			}
		}
	}
}
