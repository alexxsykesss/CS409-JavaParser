import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.NodeList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


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
//        new LocalVarInitializerParser().visit(cu, null);
//        new AssignMultipleVarSameLine().visit(cu, null);
//        new OneVariablePerDeclaration().visit(cu,null);
//        new InstanceClass().visit(cu, null);
        new ConstantCheck().visit(cu, null);
//        new ReliventGetSetMethod().visit(cu, new HashMap<String, Type>());
//        new LocalDeclaredVarOverridePublic().visit(cu,new ArrayList<>());



//        new FallThroughComment().visit(cu, null);
//
//        FileOutputStream out = new FileOutputStream("resources/LibraryMODIFIED.java");
//        byte[] modfile = cu.toString().getBytes();
//        out.write(modfile);

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

    /* Problem 5
        Declared local variables overriding public variables;

        Does not detect in instance bellow as it sees the "if" statement as a lower level.
        The var is erased when the "if" block is traversed.
          if {
                int checkahh;
            }
          int checkahh;

        I think this is OK?
        depends on what "higher level" means
     */

    private static class LocalDeclaredVarOverridePublic extends VoidVisitorAdapter<List<String>> {
        @Override
        public void visit(VariableDeclarator n, List<String> currentVariables) {
            int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);

            if (currentVariables.contains(n.getNameAsString())) {
                System.out.println("line " + lineNumber + ": " + n.getType() + " " + n.getNameAsString() +
                        " -- This variable declaration overrides a variable declaration at a higher level" );
            }

            // When it encounters a var declaration it adds it to the List
            currentVariables.add(n.getNameAsString());
        }

        @Override
        public void visit(BlockStmt n, List<String> currentVariables) {
            int originalSize = currentVariables.size();

            // directs what to visit. can put this at the end to fix problem mentioned!!!!!!!! (i think)
            super.visit(n, currentVariables);

            // Only removes newly added var when it finishes searching block fully
            currentVariables.subList(originalSize, currentVariables.size()).clear();
        }
    }

    /* Problem 7: Avoid constants in code - Numerical constants (literals) should not be coded directly.
     * The exceptions are -1, 0, and 1, which can appear in a for loop as counter values.
     */
    private static class ConstantCheck extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(IntegerLiteralExpr n, Object arg) {

            // checks it is not any of these
            if (!(n.getParentNode().get() instanceof VariableDeclarator)
            && !(n.getParentNode().get() instanceof SwitchEntry)  // dont know if this would be excluded
            && !(n.getParentNode().get() instanceof AssignExpr)) { // dont know if this would be excluded
                    checkIntegerLiteral(n);
            }
            super.visit(n, arg);
        }

        private void checkIntegerLiteral(IntegerLiteralExpr n) {
            int value = Integer.parseInt(n.getValue());
            Node parentNode = n.getParentNode().orElse(null);
            Node parentNode2 = n.getParentNode().orElse(null);


            AtomicBoolean forLoop = new AtomicBoolean(false);
            // loops throught nodes to find forStmt.
            while (parentNode != null && !forLoop.get()) {
                if (parentNode instanceof ForStmt forNode) {
                    // gets the arguments of forStmt then streams to a list and searches for node to makes ure it is the current for stmt
                    forNode.getCompare().get().stream().toList().forEach(v -> {
                        if(v == parentNode2) {
                            forLoop.set(true);
                        }
                    });
                }
                parentNode = parentNode.getParentNode().orElse(null);
            }

            if (!forLoop.get() || value != -1 && value != 0 && value != 1) {
                int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
               System.out.println("line " + lineNumber + ": " + value + " in [" + parentNode2 + "] -- Avoid using constant directly");
            }
        }
    }

    /* Problem 10: Accessors and Mutators should be named appropriately.
           get the classes, then for each class it will get the instance variables.  Then it will
           look through all the methods inside that class, checking each if they are a getter or setter for any of the
           instance variables. The once this class is done it moves to the next, clearing the instance variables.
     */
    private static class ReliventGetSetMethod extends VoidVisitorAdapter<Map<String,Type>> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Map<String, Type> instanceVars) {
            // Clears instanceVars from previous Class
            instanceVars.clear();

            // Get the instance variables of each class
            n.getFields().forEach(v -> {
                v.getVariables().forEach(var -> {
                    instanceVars.put(var.getNameAsString(), var.getType());
                });
            });

            MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
            // passing in methodVisitor, doing a sub traversal before continuing
            n.getMethods().forEach(m -> m.accept(methodVisitor, instanceVars));

            super.visit(n, instanceVars); // Continue visiting child nodes
        }

        public static class MethodDeclarationVisitor extends VoidVisitorAdapter<Map<String, Type>> {

            @Override
            public void visit(MethodDeclaration n, Map<String, Type> instanceVars) {
                // instanceVars.entrySet() is all the HashMap values and the left hand side is assigning one of those sets to the "s" variable
                for (Map.Entry<String, Type> s : instanceVars.entrySet()) {
                    String instanceName = s.getKey();
                    Type instanceType = s.getValue();

                    boolean Getter = isGetter(n, instanceName, instanceType);
                    boolean Setter = isSetter(n, instanceName, instanceType);

                    // checks if the current method is a getter or setter
                    if (Setter || Getter) {
                        String latterString = Character.toUpperCase(instanceName.charAt(0)) + instanceName.substring(1);
                        String expectedName;
                        String getOrSet;

                        if (Getter) {
                            expectedName = "get" + latterString;
                            getOrSet = "Getter";
                        } else {
                            expectedName = "set" + latterString;
                            getOrSet = "Setter";
                        }

                        if (!Objects.equals(n.getNameAsString(), expectedName)) {
                            int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                            System.out.println("line " + lineNumber + ": " + n.getNameAsString() + " -- " + getOrSet + " method is not appropriately named, change to " + expectedName);
                        }
                        break;
                    }
                }
                super.visit(n, instanceVars);
            }

            private boolean isGetter(MethodDeclaration n, String vName, Type vType) {
                // Check if the method has no parameters and the return type matches vType
                if (n.getParameters().isEmpty() && n.getType().equals(vType)) {
                    // Check if the method body has a single return statement
                    if (n.getBody().isPresent() && n.getBody().get().getStatements().size() == 1) {
                        // checks if statement is a return statement
                        if (n.getBody().get().getStatement(0) instanceof ReturnStmt returnStmt) {
                            Expression expression = returnStmt.getExpression().orElse(null);
                            // Check if the returned expression matches vName
                            if (expression instanceof NameExpr nameExpr) {
                                return nameExpr.getNameAsString().equals(vName);
                            }
                        }
                    }
                }
                return false;
            }

            private boolean isSetter(MethodDeclaration n, String vName, Type vType) {
                // Check if the method has a single parameter and its type matches the variable type
                if (n.getParameters().size() == 1 && n.getParameter(0).getType().equals(vType)) {
                    // Check if the method body is present and has a single statement
                    if (n.getBody().isPresent() && n.getBody().get().getStatements().size() == 1) {
                        Statement statement = n.getBody().get().getStatement(0);
                        if (statement instanceof ExpressionStmt expressionStmt) {
                            // Check if statement is an assignment expression
                            if (expressionStmt.getExpression() instanceof AssignExpr assignExpr) {
                                // Checks if target is an instance variable
                                if (assignExpr.getTarget() instanceof FieldAccessExpr targetExpr) {
                                    // Check if the right-hand side matches the parameter
                                    if (assignExpr.getValue() instanceof NameExpr valueExpr) {
                                        String parameterName = n.getParameter(0).getNameAsString();
                                        return targetExpr.getNameAsString().equals(vName) && valueExpr.getNameAsString().equals(parameterName);
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        }
    }

    private static class FallThroughComment extends VoidVisitorAdapter<Object>{

        @Override
        public void visit(SwitchStmt n, Object arg){
            boolean nostat= false;
            boolean statmentFound = false;
            for(SwitchEntry s: n.getEntries()) {
                statmentFound = false;

                for (Node g : s.getStatements()) {

                    if (g instanceof BreakStmt || g instanceof ContinueStmt || g instanceof ReturnStmt || g instanceof ThrowStmt) {

                        statmentFound = true;
                        if (nostat) {
                            s.setLineComment("Fall through");
                            nostat = false;
                        }

                    }
                }

                if (!statmentFound) {
                    nostat = true;
                }
            }

        }

    }

    private static class test extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(VariableDeclarator n, Object arg) {
            System.out.println("name: " + n.getName());

        }
    }

    private static class test2 extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(FieldDeclaration n, Object arg) {
            // Loop through each variable in the field declaration
            n.getVariables().forEach(variable -> {
                // Print the variable name
                System.out.println("Class-level Variable: " + variable.getName());
            });
            super.visit(n, arg); // Call to visit other nodes
        }
    }
}
