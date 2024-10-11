
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;


public class Parser {

    public static void main(String[] args) throws Exception {
        FileInputStream in = new FileInputStream("resources/Library.java");

        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(in);
        } finally {
            in.close();
        }
//        new ClassDiagramVisitor().visit(cu, null);
//        new MethodParam().visit(cu,null);
//        new VariableVisitor().visit(cu,null);

//        VariableVisitor variableVisitor = new VariableVisitor();
//        new MethodVar(variableVisitor).visit(cu, null);

//        new LocalVarInitializerParser().visit(cu, null);
        new AssignMultipleVarSameLine().visit(cu, null);
    }
    /**
     * Simple visitor implementation for extracting class relationship information
     */
//    private static class ClassDiagramVisitor extends VoidVisitorAdapter {
//
//        @Override
//        public void visit(ClassOrInterfaceDeclaration n, Object arg){
//            System.out.println("Class Name: " + n.getName());
//
//            System.out.println("Class Implements: ");
//            for (ClassOrInterfaceType coi : n.getImplementedTypes()) {
//                System.out.println(coi.getName());
//            }
//
//            System.out.println("Extended: ");
//            if(!n.getExtendedTypes().isEmpty()){
//                System.out.println("True");
//            }else{
//                System.out.println("False");
//            }
//
//            super.visit(n, arg);
//        }
//
//        @Override
//        public void visit(FieldDeclaration n, Object a){
//            System.out.println("Field Type is: " + n.getElementType());
//            for(VariableDeclarator v : n.getVariables()){
//                System.out.println("Name: " + v.getName());
//            }
//        }
//    }
//
//    private static class VariableVisitor extends VoidVisitorAdapter<Object> {
//        @Override
//        public void visit(VariableDeclarationExpr n, Object arg) {
//            for (VariableDeclarator v : n.getVariables()) {
//                System.out.println("Variable Name:" + v.getNameAsString() + " Type:" + v.getTypeAsString() + " Value:" + v.getInitializer());
//            }
//        }
//    }
//
//    private static class MethodParam extends VoidVisitorAdapter<Object> {
//        @Override
//        public void visit(MethodDeclaration n, Object arg) {
//            System.out.println("Method Name:" + n.getName() + " Parameters:" + n.getParameters());
//        }
//    }
//
//    private static class MethodVar extends VoidVisitorAdapter<Object> {
//        private final VariableVisitor variableVisitor;
//
//        public MethodVar(VariableVisitor variableVisitor) {
//            this.variableVisitor = variableVisitor;
//        }
//
//        @Override
//        public void visit(MethodDeclaration n, Object arg) {
//            System.out.println("Method Name: " + n.getNameAsString());
//            n.getBody().ifPresent(body -> {
//                body.accept(variableVisitor, arg);
//            });
//        }
//    }
//


    // working on assignment
    /*
    useful links:

    https://www.javadoc.io/doc/com.github.javaparser/javaparser-core/latest/index.html
    */

    /* local variable declarations
        specifically for local variables
        checks if initializer is present and if not then prints warning

        basic
    */
    private static class LocalVarInitializerParser extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(VariableDeclarationExpr n, Object arg) {
            for (VariableDeclarator v: n.getVariables()){
                if (v.getInitializer().isEmpty()){
                    System.out.println(v.getNameAsString() + "  -- Variable is not initialized with a value");
                }
            }
        }
    }

    /* Check for more than one assignment in one expression
        Checks assignment expression and if the value being assigned is another assignment expression then print warning

        Only checks one deep, might need more comprehensive code

        Basic implementation
     */
    private static class AssignMultipleVarSameLine extends VoidVisitorAdapter<Object>{
        @Override
        public void visit(AssignExpr n, Object arg) {
            if(n.getValue().isAssignExpr()){
                System.out.println(n.clone()+  " -- More than one assignment in on expression" );
            }


        }
    }
}