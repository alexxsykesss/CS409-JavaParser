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

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Parser {

    public static void main(String[] args) throws Exception {
        //FileInputStream in = new FileInputStream("resources/multipleBadCodeInstances.java");

        //FileInputStream in = new FileInputStream("resources/mutableInstance/safeMutableReferenceExposer.java");
        //FileInputStream in = new FileInputStream("resources/custom/Library.java");

        //FileInputStream in = new FileInputStream("resources/goodCode/squeakyClean.java");
        FileInputStream in = new FileInputStream("resources/badCode/multipleBadCodeInstances.java");

        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(in);
        } finally {
            in.close();
        }


        // Assignment Visitors
//        System.out.println("\nTesting problem 1: variable initialisation");
//        new LocalVarInitializerParser().visit(cu, null);
//
//        System.out.println("\nTesting problem 2: Keep assignments simple");
//        new AssignMultipleVarSameLine().visit(cu, null);
//
//        System.out.println("\nTesting problem 3: One variable per declaration");
//        new OneVariablePerDeclaration().visit(cu,null);
//
        System.out.println("\nTesting problem 4: Limit access to instance and class variables" );
        new InstanceClass().visit(cu, null);
//
//        System.out.println("\nTesting problem 5: Avoid local declarations that hide declarations at higher levels" );
//        new LocalDeclaredVarOverridePublic().visit(cu,new ArrayList<>());
//
//        System.out.println("\nTesting problem 6: Switch: FallThrough is commented" );
//        new FallThroughComment().visit(cu, null);
//
//        System.out.println("\nTesting problem 7: Avoid constants in code");
//        new ConstantCheck().visit(cu, null);
//
//        System.out.println("\nTesting problem 8: Don't ignore caught exceptions");
//        new CaughtExceptions().visit(cu, null);
//
//        System.out.println("\nTesting problem 9: Don't change a for loop iteration variable in the body of the loop.");
//
//        System.out.println("\nTesting problem 10: Accessors and Mutators should be named appropriately." );
//        new RelevantGetSetMethod().visit(cu, null);
//
//        System.out.println("\nTesting problem 11: Switch: default label is included" );
//
//
//        System.out.println("\nTesting problem 12: Do not return references to private mutable class members " );
//        new MutableClassMembers().visit(cu, null);
//
//        System.out.println("\nTesting problem 13: Do not expose private members of an outer class from within a nested class");
//        new ExposedPrivateFieldsFromNestedClass().visit(cu,null);
//
//        FileOutputStream out = new FileOutputStream("LibraryMODIFIED.java");
//        byte[] modfile = cu.toString().getBytes();
//        out.write(modfile);
//
//        new EnumVisitor().visit(cu, null);
//        new SwitchStatementVisitor().visit(cu, null);


    }

    /* Problem 1: local variable declarations
        specifically for local variables
        checks if initializer is present and if not then prints warning
    */
    private static class LocalVarInitializerParser extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(VariableDeclarationExpr n, Object arg) {
            n.getVariables().forEach(v -> {
                if (v.getInitializer().isEmpty()) {
                    int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                    System.out.println("line " + lineNumber + ": " + v.getType() + " " + v.getNameAsString() + "  -- Variable is not initialised with a value");
                }
            });
            super.visit(n, arg);
        }
    }

    /* Problem 2: Check for more than one assignment in one expression
        Checks assignment expression and if the value being assigned is another assignment expression then print warning
        Only checks one deep, might need more comprehensive code
        Basic implementation
     */
    private static class AssignMultipleVarSameLine extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(AssignExpr n, Object arg) {
            if (n.getValue().isAssignExpr()) {
                int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                System.out.println("line " + lineNumber + ": " + n.clone() + " -- More than one assignment in on statement");
            }
            super.visit(n, arg);
        }
    }

    /* Problem 3: Checks if more than one variable is declared in one expression unless its parent is a for loop
        I get the variable decorator and then check if the variables are > 1
        Then I get the node of the parent and the check if it is a for loop
        if not print the warning
        Only check local variables, need to expand
        basic implementation
    */
    private static class OneVariablePerDeclaration extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(VariableDeclarationExpr n, Object arg) {
            if (n.getVariables().size() > 1) {
                Node parentNode = n.getParentNode().orElse(null);
                if (!(parentNode instanceof ForStmt)) {
                    int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                    System.out.println("line " + lineNumber + ": " + n.setComment(null).clone() + " -- More than one variable declared in one declaration");
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(FieldDeclaration n, Object arg) {
            if (n.getVariables().size() > 1) {
                int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                System.out.println("line " + lineNumber + ": " + n.setComment(null).clone() + " -- More than one variable declared in one declaration");
            }
            super.visit(n, arg);
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

            n.getFields().forEach(field -> {
                if (!field.hasModifier(Modifier.Keyword.PRIVATE)) {
                    int lineNumber = field.getRange().map(r -> r.begin.line).orElse(-1);

                    field.getVariables().forEach(variable -> {
                        if (hasMethods) {
                            System.out.println("line " + lineNumber + ": " + variable +
                                    " -- Public instance/class variable detected, should be private");
                        } else {
                            System.out.println("line " + lineNumber + ": " + variable +
                                    " -- Public instance/class variable detected, but this is ok");
                        }
                    });
                }
            });
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
                        " -- This variable declaration overrides a variable declaration at a higher level");
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

    /* Problem 6: Switch: FallThrough is commented. Within a switch block, each
    statement group that does not terminate abruptly (with a break, continue, return
    or thrown exception), is marked with a comment to indicate that execution might
    continue into the next statement group. */
    private static class FallThroughComment extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(SwitchStmt n, Object arg) {
            boolean nostat = false;
            boolean statmentFound = false;
            boolean end = false;
            EmptyStmt emptyStmt = new EmptyStmt();
            for (SwitchEntry s : n.getEntries()) {
                if (s.getStatements().isEmpty() || s.isDefault()) {

                } else {

                    for (Node g : s.getStatements()) {
                        if (g instanceof BreakStmt || g instanceof ContinueStmt || g instanceof ReturnStmt || g instanceof ThrowStmt) {
                            statmentFound = true;

                        }
                    }
                    if (!statmentFound) {
                        s.getStatements().add((Statement) emptyStmt.setComment(new LineComment("Fall Through!!")));

                    }
                    statmentFound = false;
                }

            }

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
            // loops through nodes to find forStmt.
            while (parentNode != null && !forLoop.get()) {
                if (parentNode instanceof ForStmt forNode) {
                    // gets the arguments of forStmt then streams to a list and searches for node to makes ure it is the current for stmt
                    forNode.getCompare().get().stream().toList().forEach(v -> {
                        if (v == parentNode2) {
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



    /* Problem 8: Don't ignore caught exceptions. It is very rarely correct to do nothing
    in response to a caught exception, but when it truly is appropriate to take no action
    whatsoever in a catch block, the reason this is justified is explained in a comment.
    Exception: In tests, a caught exception may be ignored without comment if its name is
    or begins with expected. The example on the doc is a common idiom for ensuring that
    the code under test does throw an exception to the expected type, so a comment is
    unnecessary.*/

    public static class CaughtExceptions extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(CatchClause n, Object arg) {
            super.visit(n, arg);

            String exceptionName = n.getParameter().getNameAsString();

            // Check if the catch block is empty
            boolean isEmptyBlock = n.getBody().getStatements().isEmpty();

            // Check if the exception variable starts with "expected"
            boolean isExpected = exceptionName.startsWith("expected");

            // Get any comments associated with the catch clause
            boolean hasComment = !n.getBody().getAllContainedComments().isEmpty();

            // If the catch block is empty and the exception doesn't start with "expected"
            if (isEmptyBlock && !(isExpected) && !hasComment) {
                int lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                String catchClauseCode = n.toString().trim();
                System.out.println("line " + lineNumber + ": " + catchClauseCode + " -- Empty catch block found");
            }
        }

    }


    /* Problem 9: Don't change a for loop iteration variable in the body of the loop.
     * This leads to confusion, particularly in loops with a large scope. The for loop
     * header should contain all the information about how the loop progresses. */
    private static class IncrementLoopInLoop extends VoidVisitorAdapter<Object> {
        boolean mod = false;
        int lineNumber = 0;
        public void visit(ForStmt n, Object args) {
            for (Expression g : n.getInitialization()) {
                VariableDeclarationExpr declar = g.asVariableDeclarationExpr();
                for (int i = 0; i < (g.getChildNodes().size()); i++) {
                    String varName = declar.getVariable(i).getNameAsString();
                    if (n.getBody().toString().contains(varName + " =") || n.getBody().toString().contains(varName + "++")) {
                        mod = true;
                    }
                    if (mod) {
                        lineNumber = n.getRange().map(r -> r.begin.line).orElse(-1);
                        System.out.println("altering loop var in loop bad! Line number " + lineNumber);
                        mod = false;
                    }
                }
            }

            // check if intinilaize loop variable is on the left of any expressions if so BAD if not LIT
        }


    }


    /* Problem 10: Accessors and Mutators should be named appropriately.
           get the classes, then for each class it will get the instance variables.  Then it will
           look through all the methods inside that class, checking each if they are a getter or setter for any of the
           instance variables. The once this class is done it moves to the next, clearing the instance variables.
           finds that is not explicitly said to be an error:
           private int getVar(){
            return aVar;
           }
     */
    private static class RelevantGetSetMethod extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            HashMap<String, Type> instanceVars = new HashMap<>();

            // Get the instance variables of each class
            n.getFields().forEach(v -> {
                v.getVariables().forEach(var -> {
                    instanceVars.put(var.getNameAsString(), var.getType());
                });
            });

            // passing in methodVisitor, doing a sub traversal before continuing
            n.getMethods().forEach(m -> m.accept(new MethodDeclarationVisitor(), instanceVars));

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
                                if (assignExpr.getValue() instanceof NameExpr valueExpr) {
                                    String parameterName = n.getParameter(0).getNameAsString();
                                    // pass if assigning var directly or 'this.var'
                                    if (assignExpr.getTarget() instanceof NameExpr nameExpr) {
                                        return nameExpr.getNameAsString().equals(vName) && valueExpr.getNameAsString().equals(parameterName);
                                    } else if (assignExpr.getTarget() instanceof FieldAccessExpr fieldAccessExpr) {
                                        return fieldAccessExpr.getNameAsString().equals(vName) && valueExpr.getNameAsString().equals(parameterName);
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


    /* Problem 11: Switch: default label is included. Each switch statement includes
    a default statement group, even if it contains no code. It should also be the last
    option in the switch statement.*/

    private static class EnumVisitor extends VoidVisitorAdapter<Object> {
        static HashMap<String, ArrayList<String>> enumList = new HashMap<>();

        @Override
        public void visit(EnumDeclaration n, Object args) {
            ArrayList<String> fieldNames = new ArrayList<>();
            n.getEntries().forEach(entry -> {
                fieldNames.add(entry.getNameAsString());
            });

            enumList.put(n.getNameAsString(), fieldNames);
            super.visit(n, args);
        }
    }

    private static class SwitchStatementVisitor extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(SwitchStmt n, Object args) {

            System.out.println(EnumVisitor.enumList);

            int lineNumber = 0;

            Boolean isenum = false;
            int i = n.getEntries().size();

            for (Node sigma : n.getEntries()) {

            }
            //  if(n.getSelector() instanceof )

            if (!(n.getEntries().getLast().get().isDefault())) {
                lineNumber = n.getEntries().get(i - 1).getRange().map(r -> r.begin.line).orElse(-1);
                System.out.println("No default statment BAD! at line: " + lineNumber);
            }
            super.visit(n, args);
        }


        /* Problem 12: Do not return references to private mutable class members.
           Returning references to internal mutable members of a class can compromise an
           application's security, both by breaking encapsulation and by providing the
           opportunity to corrupt the internal state of the class (whether accidentally or
           maliciously). As a result, programs must not return references to private mutable
           classes.
         */
        public static class MutableClassMembers extends VoidVisitorAdapter<Object> {

            @Override
            public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                System.out.println("Visiting class: " + n.getNameAsString());

                // Check if the class is not final
                if (!n.isFinal()) {
                    //System.out.println("Class is not final, checking fields...");

                    // Check for non-final fields that are private
                    n.getFields().forEach(field -> {
                        if (field.hasModifier(Modifier.Keyword.PRIVATE) && !field.hasModifier(Modifier.Keyword.FINAL)) {
                            field.getVariables().forEach(variable -> {
                                String fieldName = variable.getNameAsString();
                                //System.out.println("Checking field: " + fieldName);

                                // Check for setter methods that modify the field
                                n.getMethods().forEach(method -> {
                                    //System.out.println("Checking method: " + method.getNameAsString());

                                    if (method.getNameAsString().startsWith("set") && !method.getParameters().isEmpty()) {
                                        // Check if the method modifies the private field
                                        method.getBody().ifPresent(body -> {
                                            body.findAll(AssignExpr.class).forEach(assignExpr -> {
                                                // Check if the assignment target is a field access or a simple name
                                                if (assignExpr.getTarget().isFieldAccessExpr()) {
                                                    FieldAccessExpr fieldAccess = assignExpr.getTarget().asFieldAccessExpr();
                                                    if (fieldAccess.getNameAsString().equals(fieldName)) {
                                                        int lineNumber = method.getRange().map(r -> r.begin.line).orElse(-1);
                                                        System.out.println("line " + lineNumber + ": "
                                                                + method.getNameAsString()
                                                                + " references private mutable class member '"
                                                                + fieldName + "'");
                                                    }
                                                } else if (assignExpr.getTarget().isNameExpr()) {
                                                    if (assignExpr.getTarget().asNameExpr().getNameAsString().equals(fieldName)) {
                                                        int lineNumber = method.getRange().map(r -> r.begin.line).orElse(-1);
                                                        System.out.println("line " + lineNumber + ": "
                                                                + method.getNameAsString()
                                                                + " references private mutable class member '"
                                                                + fieldName + "'");
                                                    }
                                                }
                                            });
                                        });
                                    }
                                });
                            });
                        }
                    });
                }
                super.visit(n, arg);
            }
        }


        /* Problem 13: Do not expose private members of an outer class from within a
        nested class
        */
        public static class ExposedPrivateFieldsFromNestedClass extends VoidVisitorAdapter<Object> {

            @Override
            public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                ArrayList<FieldDeclaration> outerClassFields = new ArrayList<>();

                n.getFields().forEach(v -> {
                    if (v.hasModifier(Modifier.Keyword.PRIVATE)) {
                        outerClassFields.add(v.asFieldDeclaration());
                    }
                });

                n.getMembers().forEach(member -> {
                    if (member instanceof ClassOrInterfaceDeclaration nestedClass) {
                        nestedClass.accept(new NestedClass(), outerClassFields);
                    }
                });
            }


            static class NestedClass extends VoidVisitorAdapter<ArrayList<FieldDeclaration>> {

                @Override
                public void visit(ClassOrInterfaceDeclaration n, ArrayList<FieldDeclaration> outerClassFields) {
                    ArrayList<FieldDeclaration> outerAndInner = new ArrayList<>();

                    ArrayList<String> fieldNames = new ArrayList<>();
                    outerClassFields.forEach(field -> {
                        field.getVariables().forEach(variable -> fieldNames.add(variable.getNameAsString()));
                    });

                    n.accept(new FieldAccessor(), fieldNames);

                    n.getFields().forEach(v -> {
                        if (v.hasModifier(Modifier.Keyword.PRIVATE)) {
                            outerAndInner.add(v.asFieldDeclaration());
                        }
                    });

                    n.getMembers().forEach(member -> {
                        if (member instanceof ClassOrInterfaceDeclaration nestedClass) {
                            nestedClass.accept(new NestedClass(), outerAndInner);
                        }
                    });

                }


                static class FieldAccessor extends VoidVisitorAdapter<ArrayList<String>> {

                    @Override
                    public void visit(MethodDeclaration n, ArrayList<String> outerFieldsNames) {
                        if (n.isPublic()) {
                            // with 'this.'
                            n.findAll(FieldAccessExpr.class).forEach(fieldAccess -> {
                                if (outerFieldsNames.contains(fieldAccess.getNameAsString())) {
                                    System.out.println("Exposed private parent field '" + fieldAccess.getNameAsString() + "' in method: " + n.getNameAsString() + " --- Change method to private");
                                }
                            });

                            // without 'this.'
                            n.findAll(NameExpr.class).forEach(nameExpr -> {
                                if (outerFieldsNames.contains(nameExpr.getNameAsString())) {
                                    System.out.println("Exposed private parent field '" + nameExpr.getNameAsString() + "' in method: " + n.getNameAsString() + " --- Change method to private");
                                }
                            });
                        }
                    }
                }
            }
        }

    }
}
