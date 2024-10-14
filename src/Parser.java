import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;


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

        // Assignment Visitors
        new LocalVarInitializerParser().visit(cu, null);
        new AssignMultipleVarSameLine().visit(cu, null);
        new OneVariablePerDeclaration().visit(cu, null);
        new InstanceClass().visit(cu, null);
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
                    int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                    System.out.println("line " + lineNumber + ": " + v.getType() + " " + v.getNameAsString() + "  -- Variable is not initialized with a value");
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
                int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                System.out.println("line " + lineNumber + ": " + n.clone()+  " -- More than one assignment in on expression" );
            }
        }
    }

    /* Checks if more than one variable is declared in one expression unless its parent is a for loop

        I get the variable decorator and then check if the variables are > 1
        Then I get the node of the parent and the check if it is a for loop
        if not print the warning

        Only check local variables, need to expand

        basic implementation
    */
    private static class OneVariablePerDeclaration extends VoidVisitorAdapter<Object>{
        @Override
        public void visit(VariableDeclarationExpr n, Object arg) {
            if(n.getVariables().size() > 1){
                Node parentNode = n.getParentNode().orElse(null);
                if (!(parentNode instanceof ForStmt)) {
                    int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                    System.out.println("line " + lineNumber + ": " + n.clone() + " -- More than one variable declared in one expression");
                }
            }
        }
    }

    /* Limit access to Instance and Class Variables
    * Don't make any instance or class variable public without good reason.
    * This breaks encapsulation. */

    public static class InstanceClass extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {

            // Check if the class has methods, if not, it's considered a data structure
            List<MethodDeclaration> methods = n.getMethods();
            boolean hasMethods = !methods.isEmpty();

            for (FieldDeclaration field : n.getFields()) {
                if (field.hasModifier(Modifier.Keyword.PUBLIC)) {
                    int lineNumber = field.getRange().map(r -> r.begin.line).orElse(-1);

                    for (VariableDeclarator variable : field.getVariables()) {
                        String variableName = variable.getNameAsString();

                        if (hasMethods) {
                            System.out.println("line " + lineNumber + ": " + variableName + " -- Public instance/class variable detected, should be private");
                        } else {
                            System.out.println("line " + lineNumber + ": " + variableName + " -- Public instance/class variable detected, but this is ok");
                        }
                    }
                }
            }

            super.visit(n, arg);
        }
    }

}
