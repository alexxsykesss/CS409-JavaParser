import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;

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

        // Assignment Visitors
        new LocalVarInitializerParser().visit(cu, null);
        new AssignMultipleVarSameLine().visit(cu, null);
        new OneVariablePerDeclaration().visit(cu, null);
        new InstanceClass().visit(cu, null);
        new ConstantCheck().visit(cu, null);
    }

    // working on assignment
    /*
    useful links:

    https://www.javadoc.io/doc/com.github.javaparser/javaparser-core/latest/index.html
    */

    /* Problem 1: local variable declarations
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

    /* Problem 2: Check for more than one assignment in one expression
        Checks assignment expression and if the value being assigned is another assignment expression then print warning

        Only checks one deep, might need more comprehensive code

        Basic implementation
     */
    private static class AssignMultipleVarSameLine extends VoidVisitorAdapter<Object>{
        @Override
        public void visit(AssignExpr n, Object arg) {
            if(n.getValue().isAssignExpr()){
                int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                System.out.println("line " + lineNumber + ": " + n.clone()+
                        " -- More than one assignment in on expression" );
            }
        }
    }

    /* Problem 3: Checks if more than one variable is declared in one expression unless its parent is a for loop

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
                    System.out.println("line " + lineNumber + ": " + n.clone() +
                            " -- More than one variable declared in one expression");
                }
            }
        }
    }

    /* Problem 4: Limit access to Instance and Class Variables
    * Don't make any instance or class variable public without good reason.
    * This breaks encapsulation.
    * Exception - one example of appropriate public instance variables is the case where the
    * class is essentially a data structure, with no behaviour.*/

    public static class InstanceClass extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {

            boolean hasMethods = !n.getMethods().isEmpty();

            for (FieldDeclaration field : n.getFields()) {
                if (field.hasModifier(Modifier.Keyword.PUBLIC)) {
                    int lineNumber = field.getRange().map(r -> r.begin.line).orElse(-1);

                    field.getVariables().forEach(variable -> {
                        String variableName = variable.getNameAsString();

                        if (hasMethods) {
                            System.out.println("line " + lineNumber + ": " + variableName +
                                    " -- Public instance/class variable detected, should be private");
                        } else {
                            System.out.println("line " + lineNumber + ": " + variableName +
                                    " -- Public instance/class variable detected, but this is ok");
                        }
                    });
                }
            }

            super.visit(n, arg);
        }
    }


    /* Problem 7: Avoid constants in code - Numerical constants (literals) should not be coded directly.
     * The exceptions are -1, 0, and 1, which can appear in a for loop as counter values. */

    private static class ConstantCheck extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(IntegerLiteralExpr n, Object arg) {
            int value = Integer.parseInt(n.getValue());
            Node parentNode = n.getParentNode().orElse(null);

            // Only check literals that are not part of a for loop
            if (!(parentNode instanceof ForStmt)) {
                if (value != -1 && value != 0 && value != 1) {
                    int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                    System.out.println("line " + lineNumber + ": " + value + " -- Avoid using constant directly");
                }
            }
            super.visit(n, arg);
        }
    }


}
