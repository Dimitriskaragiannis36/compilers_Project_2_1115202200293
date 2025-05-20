import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import syntaxtree.*;
import visitor.*;

public class MyVisitor2 extends GJDepthFirst<String, MyVisitor2.Context> {
    LinkedHashMap<String, MyVisitor.ClassSymbol> symbolTable;
    MyVisitor.ClassSymbol currentClass = null;
    MyVisitor.MethodSymbol currentMethod = null;

    HashMap<String, Integer> fieldOffsets = new HashMap<>();  
    HashMap<String, Integer> methodOffsets = new HashMap<>();

    public MyVisitor2(LinkedHashMap<String, MyVisitor.ClassSymbol> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public static class Context {
        public MyVisitor.ClassSymbol currClass;
        public MyVisitor.MethodSymbol currMethod;
        public LinkedHashMap<String, MyVisitor.ClassSymbol> symbolTable;
        
        public Context(MyVisitor.ClassSymbol cls, MyVisitor.MethodSymbol mthd,
                    LinkedHashMap<String, MyVisitor.ClassSymbol> symbolTable) {
            this.currClass = cls;
            this.currMethod = mthd;
            this.symbolTable = symbolTable;
        }

        public Context(MyVisitor.ClassSymbol cls,
                    LinkedHashMap<String, MyVisitor.ClassSymbol> symbolTable) {
            this.currClass = cls;
            this.symbolTable = symbolTable;
        }

        public String lookupVariableType(String name) {
            if (currMethod != null && currMethod.locals.containsKey(name)) {
                return currMethod.locals.get(name);
            }
            if (currMethod != null && currMethod.parameters.containsKey(name)) {
                return currMethod.parameters.get(name);
            }
            //ψάχνουμε και στις υπερκλάσεις
            MyVisitor.ClassSymbol cls = currClass;
            while (cls != null) {
                if (cls.fields.containsKey(name)) {
                    return cls.fields.get(name);
                }

                if (cls.parent == null) break;
                cls = symbolTable.get(cls.parent);  
            }
            return null;
        }

    }

    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
    public String visit(Goal n, Context argu) throws Exception {
        n.f0.accept(this, argu);
        for (Node node : n.f1.nodes) {
            node.accept(this, argu);
        }
        return null;
    }    

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, Context argu) throws Exception {
        String className = n.f1.accept(this, argu);
        MyVisitor.ClassSymbol cls = symbolTable.get(className);
        if (cls == null)
            throw new Exception("Main class not found: " + className);
    
        MyVisitor.MethodSymbol mainMethod = cls.methods.get("main");
        if (mainMethod == null)
            throw new Exception("Main method not found in class: " + className);
    
        currentClass = cls;
        currentMethod = mainMethod;
    
        Context context = new Context(cls, mainMethod, symbolTable);
    
        n.f14.accept(this, context); 
        n.f15.accept(this, context); 
    
        return null;
    }

    /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
    public String visit(TypeDeclaration n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Context argu) throws Exception {
        String classname = n.f1.accept(this, argu);

        MyVisitor.ClassSymbol classSymbol = symbolTable.get(classname);
        if (classSymbol == null) {
            throw new Exception("Class not found in symbol table: " + classname);
        }
        currentClass = classSymbol;

        Context classContext = new Context(currentClass, symbolTable);
        n.f3.accept(this, classContext);
        n.f4.accept(this, classContext);

        System.out.println();
        System.out.println("-----------Class " + classname + "-----------");
        System.out.println("--Variables---");

        int fieldOffset = 0;
        for (Map.Entry<String, String> field : currentClass.fields.entrySet()) {
            String fieldName = field.getKey();
            String fieldType = field.getValue();

            System.out.println(classname + "." + fieldName + " : " + fieldOffset);
            fieldOffsets.put(classname + "." + fieldName, fieldOffset);
            fieldOffset += getSize(fieldType);
        }
        
        System.out.println("---Methods---");
        int methodOffset = 0;
        for (String methodName : currentClass.methods.keySet()) {
            System.out.println(classname + "." + methodName + " : " + methodOffset);
            methodOffsets.put(classname + "." + methodName, methodOffset);
            methodOffset += 8;
        }
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Context argu) throws Exception {
        String classname = n.f1.accept(this, argu);
        String parentname = n.f3.accept(this, argu);

        MyVisitor.ClassSymbol classSymbol = symbolTable.get(classname);
        if (classSymbol == null) {
            throw new Exception("Class not found: " + classname);
        }
        currentClass = classSymbol;

        System.out.println();
        System.out.println("-----------Class " + classname + "-----------");
        System.out.println("--Variables---");

        MyVisitor.ClassSymbol parentClass = symbolTable.get(parentname);
        if (parentClass == null) {
            throw new Exception("Parent class not found: " + parentname);
        }
        
        Context classContext = new Context(currentClass, symbolTable);
        n.f5.accept(this, classContext); 
        n.f6.accept(this, classContext); 
        
        int fieldOffset = 0;

        for (Map.Entry<String, String> parentField : parentClass.fields.entrySet()) {
            fieldOffset += getSize(parentField.getValue());
        }
    
        for (Map.Entry<String, String> field : classSymbol.fields.entrySet()) {
            String fieldName = field.getKey();
            String fieldType = field.getValue();
            System.out.println(classname + "." + fieldName + " : " + fieldOffset);
            fieldOffsets.put(classname + "." + fieldName, fieldOffset);
            fieldOffset += getSize(fieldType);
        }

        Map<String, Integer> vtableOffsets = new LinkedHashMap<>();
        int vtableOffset = 0;
        Map<String, Integer> inheritedMethodOffsets = new LinkedHashMap<>();

        System.out.println("---Methods---");

        //μεταφέρουμε τα methods του γονέα — χωρίς εκτύπωση
        for (Map.Entry<String, MyVisitor.MethodSymbol> entry : parentClass.methods.entrySet()) {
            String methodName = entry.getKey();
            int offset = methodOffsets.get(parentname + "." + methodName); // offset από τον parent

            methodOffsets.put(classname + "." + methodName, offset);
            vtableOffsets.put(methodName, offset);
            inheritedMethodOffsets.put(methodName, offset);

            vtableOffset = Math.max(vtableOffset, offset + 8);  // keep track of the next available offset
        }

        //εκτύπωση μόνο για νέα ή overridden methods
        for (Map.Entry<String, MyVisitor.MethodSymbol> entry : classSymbol.methods.entrySet()) {
            String methodName = entry.getKey();

            //αν έχει γίνει override
            if (parentClass.methods.containsKey(methodName)) {
                int inheritedOffset = methodOffsets.get(parentname + "." + methodName);
                System.out.println(classname + "." + methodName + " : " + inheritedOffset);
                methodOffsets.put(classname + "." + methodName, inheritedOffset);
            }
            //αν είναι νέο method, τότε το εκτυπώνουμε και του δίνουμε νέο offset
            else if (!vtableOffsets.containsKey(methodName)) {
                System.out.println(classname + "." + methodName + " : " + vtableOffset);
                methodOffsets.put(classname + "." + methodName, vtableOffset);
                vtableOffsets.put(methodName, vtableOffset);
                vtableOffset += 8;
            }
        }

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"/* */
    @Override
    public String visit(VarDeclaration n, Context argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String varName = n.f1.accept(this, argu);

        if (!isValidType(type)) {
            throw new Exception("Invalid type declaration: " + type + " for variable " + varName + " in class " + argu.currClass.name + (argu.currMethod != null ? " method " + argu.currMethod.name : ""));
        }

        return null;
    }
    
    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Context argu) throws Exception {
        String returnType = n.f1.accept(this, argu); 
        String methodName = n.f2.accept(this, argu);

        if (methodName.equals(argu.currClass.name) && !returnType.equals("void")) {
            throw new Exception("Constructor-like method is not allowed in class " + argu.currClass.name + ": " + methodName);
        }

        MyVisitor.MethodSymbol methodSymbol = argu.currClass.methods.get(methodName);
        if (methodSymbol == null) {
            throw new Exception("Method not found in class: " + argu.currClass.name + " -> " + methodName);
        }

        currentMethod = methodSymbol;

        Context methodContext = new Context(argu.currClass, currentMethod, symbolTable);

        if (n.f4.present()) {
            n.f4.node.accept(this, methodContext);
        }
        n.f7.accept(this, methodContext);
        n.f8.accept(this, methodContext);
        String returnedType = n.f10.accept(this, methodContext);

        if (!isTypeCompatible(returnedType, returnType)) {
            throw new Exception("Return type mismatch in method " + methodName + " of class " + argu.currClass.name + ". Expected " + returnType + ", got " + returnedType);
        }

        currentMethod = null; 

        return null;
    }

     /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Context argu) throws Exception {
        n.f0.accept(this, argu); 
        n.f1.accept(this, argu);
        return null;
    }

     /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Context argu) throws Exception{
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        if (!isValidType(type)) {
            throw new Exception("Invalid parameter type: " + type + " for parameter " + name +
                " in method " + argu.currMethod.name + " of class " + argu.currClass.name);
        }
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Context argu) throws Exception {
        for (Node node : n.f0.nodes) { 
            node.accept(this, argu);   
        }
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, Context argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return null;
    }

    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
    public String visit(Type n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> BooleanArrayType()
    *       | IntegerArrayType()
    */
    @Override
    public String visit(ArrayType n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }      

    /**
    * f0 -> "boolean"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(BooleanArrayType n, Context argu) throws Exception {
        return "boolean[]";
    }

    /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(IntegerArrayType n, Context argu) throws Exception {
        return "int[]";
    }

    /**
    * f0 -> "boolean"
    */
    @Override
    public String visit(BooleanType n, Context argu) throws Exception {
        return "boolean";
    }

    /**
    * f0 -> "int"
    */
    @Override
    public String visit(IntegerType n, Context argu) throws Exception {
        return "int";
    }  
    
    /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    @Override
    public String visit(Statement n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
    @Override
    public String visit(Block n, Context argu) throws Exception {
        for (Node stmt : n.f1.nodes) {
            stmt.accept(this, argu);
        }
        return null;
    }

    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, Context argu) throws Exception {
        String varName = n.f0.f0.toString(); 
        System.out.println("Assignment to varName = " + varName);
        String varType = argu.lookupVariableType(varName); 

        if (varType == null) {
            throw new Exception("Undefined variable: " + varName);
        }

        String exprType = n.f2.accept(this, argu); 

        if (!isTypeCompatible(exprType, varType)) {
            throw new Exception("Type mismatch in assignment to variable '" + varName +
                                "'. Expected: " + varType + ", but got: " + exprType);
        }

        return null;
    }

     /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    @Override
    public String visit(ArrayAssignmentStatement n, Context argu) throws Exception {
        String varName = n.f0.f0.toString();
        String varType = argu.lookupVariableType(varName);
    
        if (varType == null) {
            throw new Exception("Undefined array variable: " + varName);
        }
        if (!varType.equals("int[]") && !varType.equals("boolean[]")) {
            throw new Exception("Variable '" + varName + "' is not an array. Found: " + varType);
        }
    
        String indexType = n.f2.accept(this, argu);
        if (!indexType.equals("int")) {
            throw new Exception("Array index must be int. Got: " + indexType);
        }
    
        String valueType = n.f5.accept(this, argu);
        String expectedElementType = varType.equals("int[]") ? "int" : "boolean";
    
        if (!valueType.equals(expectedElementType)) {
            throw new Exception("Type mismatch in array assignment to '" + varName +
                                "'. Expected: " + expectedElementType + ", got: " + valueType);
        }
        return null;
    }

    /**
     * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    @Override
    public String visit(IfStatement n, Context argu) throws Exception {
        String condType = n.f2.accept(this, argu);
        if (!condType.equals("boolean")) {
            throw new Exception("'if' condition must be boolean. Got: " + condType);
        }
    
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
        return null;
    }

    /**
     * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    @Override
    public String visit(WhileStatement n, Context argu) throws Exception {
    String condType = n.f2.accept(this, argu);
    if (!condType.equals("boolean")) {
        throw new Exception("'while' condition must be boolean. Got: " + condType);
    }

    n.f4.accept(this, argu);
    return null;
    }

    /**
     * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    @Override
    public String visit(PrintStatement n, Context argu) throws Exception {
        String exprType = n.f2.accept(this, argu);
        if (!exprType.equals("int")) {
            throw new Exception("System.out.println only accepts int. Got: " + exprType);
        }
        return null;
    }

    /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | Clause()
    */
    @Override
    public String visit(Expression n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, Context argu) throws Exception {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
    
        if (!leftType.equals("boolean") || !rightType.equals("boolean")) {
            throw new Exception("Operator '&&' requires boolean operands. Got: " + leftType + " and " + rightType);
        }
    
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, Context argu) throws Exception {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
    
        if (!leftType.equals("int") || !rightType.equals("int")) {
            throw new Exception("Operator '<' requires int operands. Got: " + leftType + " and " + rightType);
        }
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, Context argu) throws Exception {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
    
        if (!leftType.equals("int") || !rightType.equals("int")) {
            throw new Exception("Operator '+' requires int operands. Got: " + leftType + " and " + rightType);
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, Context argu) throws Exception {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
    
        if (!leftType.equals("int") || !rightType.equals("int")) {
            throw new Exception("Operator '+' requires int operands. Got: " + leftType + " and " + rightType);
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, Context argu) throws Exception {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
    
        if (!leftType.equals("int") || !rightType.equals("int")) {
            throw new Exception("Operator '+' requires int operands. Got: " + leftType + " and " + rightType);
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, Context argu) throws Exception {
        String arrayType = n.f0.accept(this, argu);
        String indexType = n.f2.accept(this, argu);
    
        if (!indexType.equals("int")) {
            throw new Exception("Array index must be of type int, got: " + indexType);
        }
        if (arrayType.equals("int[]")) {
            return "int";
        } else if (arrayType.equals("boolean[]")) {
            return "boolean";
        } else {
            throw new Exception("Array lookup requires array type, got: " + arrayType);
        }
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, Context argu) throws Exception {
        String arrayType = n.f0.accept(this, argu);

        if (!arrayType.equals("int[]") && !arrayType.equals("boolean[]")) {
            throw new Exception("'.length' can only be applied to arrays. Got: " + arrayType);
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    @Override
    public String visit(MessageSend n, Context argu) throws Exception {
        String objectType = n.f0.accept(this, argu);

        if (objectType.equals("this")) {
            objectType = argu.currClass.name;
        }

        if (objectType.equals("int") || objectType.equals("boolean") || objectType.endsWith("[]")) {
            throw new Exception("Cannot call methods on primitive or array type: " + objectType);
        }

        MyVisitor.ClassSymbol classSymbol = symbolTable.get(objectType);
        if (classSymbol == null) {
            throw new Exception("Class not found: " + objectType);
        }

        String methodName = n.f2.f0.toString();

        MyVisitor.MethodSymbol methodSymbol = lookupMethodInClassHierarchy(classSymbol, methodName);
        if (methodSymbol == null) {
            throw new Exception("Method '" + methodName + "' not found in class '" + objectType + "' or its superclasses.");
        }

        List<String> actualArgTypes = new ArrayList<>();
        if (n.f4.present()) {
            ExpressionList exprList = (ExpressionList) n.f4.node;
            actualArgTypes.add(exprList.f0.accept(this, argu));  // πρώτο argument
            for (Node node : exprList.f1.f0.nodes) {
                ExpressionTerm term = (ExpressionTerm) node;
                actualArgTypes.add(term.f1.accept(this, argu));
            }
        }

        List<String> formalArgTypes = new ArrayList<>(methodSymbol.parameters.values());
        if (actualArgTypes.size() != formalArgTypes.size()) {
            throw new Exception("Method '" + methodName + "' in class '" + objectType +
                                "' expects " + formalArgTypes.size() + " arguments, but got " + actualArgTypes.size());
        }

        for (int i = 0; i < actualArgTypes.size(); i++) {
            String actual = actualArgTypes.get(i);
            String formal = formalArgTypes.get(i);
            if (!isTypeCompatible(actual, formal)) {
                throw new Exception("Argument " + (i + 1) + " of method '" + methodName +
                                    "' expected '" + formal + "', got '" + actual + "'");
            }
        }

        return methodSymbol.returnType;
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, Context argu) throws Exception {
        n.f0.accept(this, argu); 
        n.f1.accept(this, argu);

        return null;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, Context argu) throws Exception {
        n.f0.accept(this, argu);

        return null;
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    @Override
    public String visit(ExpressionTerm n, Context argu) throws Exception {
        n.f1.accept(this, argu);

        return null;
    }

    /**
    * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    @Override
    public String visit(Clause n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    @Override
    public String visit(PrimaryExpression n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public String visit(IntegerLiteral n, Context argu) throws Exception {
        return "int";
    }

    /**
    * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, Context argu) throws Exception {
        return "boolean";
    }

    /**
    * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, Context argu) throws Exception {
        return "boolean";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Context argu) throws Exception {
        String varName = n.f0.toString();
        System.out.println("Visiting identifier: " + varName);
        
        if (symbolTable.containsKey(varName)) {
            return varName;
        }

        if (argu == null || argu.currMethod == null || argu.currClass == null) {
            return varName;
        }
    
        String varType = argu.lookupVariableType(varName);
    
         if (varType == null) {
            throw new Exception("Undefined variableOK: '" + varName + "' in method '" +
                                argu.currMethod.name + "', class '" + argu.currClass.name + "'");
        } 
    
        return varType;
    }
    
    /**
    * f0 -> "this"
    */
    @Override
    public String visit(ThisExpression n, Context argu) throws Exception {
        if (argu == null || argu.currClass == null) {
            throw new Exception("Cannot resolve 'this' without class context");
        }
        return argu.currClass.name;
    }

    /**
     * f0 -> BooleanArrayAllocationExpression()
    *       | IntegerArrayAllocationExpression()
    */
    @Override
    public String visit(ArrayAllocationExpression n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String  visit(BooleanArrayAllocationExpression n, Context argu) throws Exception {
        n.f3.accept(this, argu);
        return "boolean[]";
    }

    /**
     * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(IntegerArrayAllocationExpression n, Context argu) throws Exception {
        n.f3.accept(this, argu); 
        return "int[]";
    }

    /**
     * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, Context argu) throws Exception {
        String className = n.f1.accept(this, null);
        if (!symbolTable.containsKey(className)) {
            throw new Exception("Cannot allocate unknown class type: " + className);
        }
        return className;
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, Context argu) throws Exception {
        String innerType = n.f1.accept(this, argu);
        if (!innerType.equals("boolean")) {
            throw new Exception("'!' operator requires boolean operand, got: " + innerType);
        }
        return "boolean";
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, Context argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    //ξεχωριστή βοηθητική συνάρτηση για τα μεγέθη
    private int getSize(String type) {
        if (type.equals("int")) return 4;
        if (type.equals("boolean")) return 1;
        return 8; 
    }
    
    //ξεχωριστή βοηθητική συνάρτηση για για την εύρεση του τύπου
    private boolean isValidType(String type) {
        //επιτρέπουμε μόνο αυτά
        if (type.equals("int") || type.equals("boolean") ||
            type.equals("int[]") || type.equals("boolean[]")) {
            return true;
        }

        //αν τελειώνει σε [], είναι array type — και αφού δεν είναι τα νόμιμα, είναι άκυρο
        if (type.endsWith("[]")) {
            return false;  
        }

        //αν είναι έγκυρο user-defined class
        return symbolTable.containsKey(type);  
    }

    //ξεχωριστή βοηθητική συνάρτηση για για το αν οι τύποι είναι συμβατοί
    private boolean isTypeCompatible(String actualType, String expectedType) {
        if (actualType.equals(expectedType)) {
            return true;
        }
    
        MyVisitor.ClassSymbol actualClass = symbolTable.get(actualType);
        MyVisitor.ClassSymbol expectedClass = symbolTable.get(expectedType);
    
        if (actualClass != null && expectedClass != null) {
            String currentParent = actualClass.parent;
            while (currentParent != null) {
                if (currentParent.equals(expectedType)) {
                    return true;
                }
                MyVisitor.ClassSymbol parentSymbol = symbolTable.get(currentParent);
                if (parentSymbol == null) {
                    break; 
                }
                currentParent = parentSymbol.parent;
            }
        }
    
        return false;
    }

    private MyVisitor.MethodSymbol lookupMethodInClassHierarchy(MyVisitor.ClassSymbol classSym, String methodName) {
        while (classSym != null) {
            if (classSym.methods.containsKey(methodName)) {
                return classSym.methods.get(methodName);
            }
            String parentName = classSym.parent;
            classSym = (parentName != null) ? symbolTable.get(parentName) : null;
            if (classSym == null && parentName != null) {
                System.err.println("Parent class not found: " + parentName);
            }
        }
        return null;
    }
    
}

