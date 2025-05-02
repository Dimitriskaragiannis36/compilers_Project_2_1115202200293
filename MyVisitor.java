import java.util.LinkedHashMap;

import syntaxtree.*;
import visitor.*;


class MyVisitor extends GJDepthFirst<String, Void>{
    /*Προσθέτω linked_hashmap προκειμένου να κρατήσω τα σύμβολα
     *στον symbol table.
     */
    LinkedHashMap<String, ClassSymbol> symbolTable = new LinkedHashMap<>();
    ClassSymbol currentClass = null;
    MethodSymbol currentMethod = null;

    class ClassSymbol {
        String name;
        String parent = null; 
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        LinkedHashMap<String, MethodSymbol> methods = new LinkedHashMap<>();
    }

    class MethodSymbol {
        String name;
        String returnType;
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        LinkedHashMap<String, String> locals = new LinkedHashMap<>();
    }

    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
    public String visit(Goal n, Void argu) throws Exception {
        n.f0.accept(this, argu); 
        n.f1.accept(this, argu); 
        n.f2.accept(this, argu); 

        /*για να βλέπω τι περιέχει ο symbol table*/
        printSymbolTable(); 

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
    public String visit(MainClass n, Void argu) throws Exception {
        String className = n.f1.accept(this, null);
        ClassSymbol classSymbol = new ClassSymbol();
        classSymbol.name = className;
        symbolTable.put(className, classSymbol);
        currentClass = classSymbol;

        //για την δημιουργία της μεθόδου
        MethodSymbol mainMethod = new MethodSymbol();
        mainMethod.name = "main";
        mainMethod.returnType = "void";
        mainMethod.parameters.put(n.f11.accept(this, null), "String[]");
        currentMethod = mainMethod;

        n.f14.accept(this, argu);

        currentClass.methods.put("main", mainMethod);

        currentMethod = null;
        currentClass = null;
        return null;
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
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        
        String classname = n.f1.accept(this, argu);
        //ελέγχω για duplicate
        if (symbolTable.containsKey(classname)) {
            throw new Exception("Class " + classname + " already defined.");
        }

        ClassSymbol classSymbol = new ClassSymbol();
        classSymbol.name = classname;
        symbolTable.put(classname, classSymbol);
        currentClass = classSymbol;

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        currentClass = null;
        System.out.println();

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
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);

        String classname = n.f1.accept(this, argu);
        //ελέγχω για duplicate
        if (symbolTable.containsKey(classname)) {
            throw new Exception("Class " + classname + " already defined.");
        }

        n.f2.accept(this, argu);
        String parentname = n.f3.accept(this, argu);

        ClassSymbol classSymbol = new ClassSymbol();
        classSymbol.name = classname;
        classSymbol.parent = parentname;
        symbolTable.put(classname, classSymbol);
        currentClass = classSymbol;

        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);

        currentClass = null;

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String _ret=null;
        String type = n.f0.accept(this, argu);
        String varName = n.f1.accept(this, argu);
        
        if (currentMethod != null) {
            currentMethod.locals.put(varName, type);
        } else if (currentClass != null) {
            currentClass.fields.put(varName, type);
        }

        //super.visit(n, argu);
        
        return _ret;
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
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);

        MethodSymbol methodSymbol = new MethodSymbol();
        methodSymbol.name = methodName;
        methodSymbol.returnType = returnType;
        currentMethod = methodSymbol;

        if (n.f4.present()) {
            n.f4.accept(this, argu);
        }

        n.f7.accept(this, argu);

        currentClass.methods.put(methodName, methodSymbol);
        currentMethod = null;

        //super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        n.f0.accept(this, argu); 
        n.f1.accept(this, argu);

        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        for ( Node node: n.f0.nodes) {
            node.accept(this, argu);
        }
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);

        if (currentMethod != null) {
            if (currentMethod.parameters.containsKey(name)) {
                throw new Exception("Duplicate parameter: " + name);
            }
            currentMethod.parameters.put(name, type);
        }
    
        return type + " " + name;
    }

    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
    public String visit(Type n, Void argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> BooleanArrayType()
    *       | IntegerArrayType()
    */
    @Override
    public String visit(ArrayType n, Void argu) throws Exception {
        return n.f0.accept(this, argu);
    }

       /**
    * f0 -> "boolean"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(BooleanArrayType n, Void argu) throws Exception {
        return "boolean[]";
    }

       /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(IntegerArrayType n, Void argu) throws Exception {
        return "int[]";
    }

    /**
    * f0 -> "boolean"
    */
    @Override
    public String visit(BooleanType n, Void argu) throws Exception {
        return "boolean";
    }

    /**
    * f0 -> "int"
    */
    @Override
    public String visit(IntegerType n, Void argu) throws Exception {
        return "int";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
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
    public String visit(Statement n, Void argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
    @Override
    public String visit(Block n, Void argu) throws Exception {
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
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        String varName = n.f0.accept(this, argu);  
        n.f2.accept(this, argu);
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
    public String visit(Expression n, Void argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f2.accept(this, argu);

        return null;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f2.accept(this, argu);
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f2.accept(this, argu);
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f2.accept(this, argu);
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f2.accept(this, argu);
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, Void argu) throws Exception {
        n.f0.accept(this, argu); 
        n.f2.accept(this, argu); 
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, Void argu) throws Exception {
        n.f0.accept(this, argu);
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
    public String visit(MessageSend n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f2.accept(this, argu);
        n.f4.accept(this, argu);
        return null;
    }

    /**
     * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, Void argu) throws Exception {
        n.f0.accept(this, argu); 
        n.f1.accept(this, argu);

        return null;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, Void argu) throws Exception {
        n.f0.accept(this, argu);

        return null;
    }

    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    @Override
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        n.f1.accept(this, argu);

        return null;
    }

    /**
    * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    @Override
    public String visit(Clause n, Void argu) throws Exception {
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
    public String visit(PrimaryExpression n, Void argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public String visit(IntegerLiteral n, Void argu) throws Exception {
        return null;
    }

    /**
    * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, Void argu) throws Exception {
        return null;
    }

    /**
     * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, Void argu) throws Exception {
        return null;
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, Void argu) throws Exception {
        n.f1.accept(this, argu);
        return null;
    }

    /*ξεχωριστή συνάρτηση για το πρώτο πέρασμα με εκτύπωση του symbol table*/
    public void printSymbolTable() {
        for (String className : symbolTable.keySet()) {
            ClassSymbol cls = symbolTable.get(className);
            System.out.println("Class: " + cls.name + (cls.parent != null ? " extends " + cls.parent : ""));
    
            if (!cls.fields.isEmpty()) {
                System.out.println("  Fields:");
                for (String fieldName : cls.fields.keySet()) {
                    System.out.println("    " + fieldName + " : " + cls.fields.get(fieldName));
                }
            }
    
            if (!cls.methods.isEmpty()) {
                System.out.println("  Methods:");
                for (String methodName : cls.methods.keySet()) {
                    MethodSymbol method = cls.methods.get(methodName);
                    System.out.print("    " + method.name + "(");
    
                    boolean first = true;
                    for (String param : method.parameters.keySet()) {
                        if (!first) System.out.print(", ");
                        System.out.print(method.parameters.get(param) + " " + param);
                        first = false;
                    }
                    System.out.println(") : " + method.returnType);
    
                    if (!method.locals.isEmpty()) {
                        System.out.println("      Locals:");
                        for (String localName : method.locals.keySet()) {
                            System.out.println("        " + localName + " : " + method.locals.get(localName));
                        }
                    }
                }
            }
    
            System.out.println();
        }
    }  

}

