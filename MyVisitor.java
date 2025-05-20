import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import syntaxtree.*;
import visitor.*;


class MyVisitor extends GJDepthFirst<String, Void>{
    /*Προσθέτω linked_hashmap προκειμένου να κρατήσω τα σύμβολα
     *στον symbol table(με την σειρά που εμφανίζονται).
     */
    LinkedHashMap<String, ClassSymbol> symbolTable = new LinkedHashMap<>();
    ClassSymbol currentClass = null;  //η τρέχουσα κλάση
    MethodSymbol currentMethod = null; //η τρέχουσα μέθοδος

    //δημιουργώ κλάση για να κρατάω τα σύμβολα της κλάσης
    public static class ClassSymbol {
        String name; // το όνομα της κλάσης
        String parent = null; // το όνομα της γονικής κλάσης
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        //τα πεδία της κλάσης
        LinkedHashMap<String, MethodSymbol> methods = new LinkedHashMap<>();
        //οι μέθοδοι της κλάσης
    }

    //δημιουργώ κλάση για να κρατάω τα σύμβολα της μεθόδου
    public static class MethodSymbol {
        String name; // το όνομα της μεθόδου
        String returnType; // ο τύπος επιστροφής της μεθόδου
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        //οι παράμετροι της μεθόδου
        LinkedHashMap<String, String> locals = new LinkedHashMap<>();
        //οι τοπικές μεταβλητές της μεθόδου
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
        //δημιουργώ την κλάση MainClass
        ClassSymbol classSymbol = new ClassSymbol();
        //το όνομα της κλάσης
        classSymbol.name = className; 

        //έλεγχος αν υπάρχει ήδη η κλάση
        if (symbolTable.containsKey(className)) { 
            throw new Exception("Duplicate class declaration: " + className);
        }
        //προσθέτω την κλάση στον symbol table
        symbolTable.put(className, classSymbol);
        currentClass = classSymbol; //αποθηκεύω την τρέχουσα κλάση

        //για την δημιουργία της μεθόδου
        MethodSymbol mainMethod = new MethodSymbol(); 
        mainMethod.name = "main"; // το όνομα της μεθόδου
        mainMethod.returnType = "void"; // ο τύπος επιστροφής της μεθόδου
        mainMethod.parameters.put(n.f11.accept(this, null), "String[]");
        //η παράμετρος της μεθόδου
        currentMethod = mainMethod; //αποθηκεύω την τρέχουσα μέθοδο

        n.f14.accept(this, argu);

        //προσθέτω την μέθοδο στον symbol table
        currentClass.methods.put("main", mainMethod); 
        currentMethod = null;
        currentClass = null;
        return null;
    }

    /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
    public String visit(TypeDeclaration n, Void argu) throws Exception {
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
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        String className = n.f1.accept(this, argu);
        //δημιουργώ την κλάση
        ClassSymbol classSymbol = new ClassSymbol();
        //το όνομα της κλάσης
        classSymbol.name = className;
        //έλεγχος αν υπάρχει ήδη η κλάση
        if (symbolTable.containsKey(className)) {
            throw new Exception("Duplicate class declaration: " + className);
        }
        //προσθέτω την κλάση στον symbol table
        symbolTable.put(className, classSymbol);
        currentClass = classSymbol; //αποθηκεύω την τρέχουσα κλάση

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        currentClass = null; //αδειάζω την τρέχουσα κλάση
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
        String className = n.f1.accept(this, argu);
        //έλεγχος αν υπάρχει ήδη η κλάση
        if (symbolTable.containsKey(className)) {
            throw new Exception("Duplicate class declaration: " + className);
        }
        
        String parentName = n.f3.accept(this, argu);
        //έλεγχος αν υπάρχει η γονική κλάση
        if (!symbolTable.containsKey(parentName)) {
            throw new Exception("Parent class " + parentName + " not declared before use in class " + className);
        }
        //δημιουργώ την κλάση
        ClassSymbol classSymbol = new ClassSymbol();
        //το όνομα της κλάσης
        classSymbol.name = className;
        //αποθηκεύω την γονική κλάση
        classSymbol.parent = parentName; 
        //προσθέτω την κλάση στον symbol table
        symbolTable.put(className, classSymbol); 
        currentClass = classSymbol; //αποθηκεύω την τρέχουσα κλάση

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        currentClass = null; //αδειάζω την τρέχουσα κλάση
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String varName = n.f1.accept(this, argu);
        //έλεγχος αν είμαι σε μέθοδο
        if (currentMethod != null) { 
           //έλεγχος αν υπάρχει ήδη η τοπική μεταβλητή
            if (currentMethod.locals.containsKey(varName) || currentMethod.parameters.containsKey(varName)) {
                throw new Exception("Duplicate local variable or parameter '" + varName + "' in method '" + currentMethod.name + "'");
            }
            //αποθηκεύω την τοπική μεταβλητή
            currentMethod.locals.put(varName, type); 
        //έλεγχος αν είμαι σε κλάση
        } else if (currentClass != null) {
            //έλεγχος αν υπάρχει ήδη το πεδίο
            if (currentClass.fields.containsKey(varName)) {
                throw new Exception("Duplicate field '" + varName + "' in class '" + currentClass.name + "'");
            }
            //αποθηκεύω το πεδίο
            currentClass.fields.put(varName, type);
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
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);
        //έλεγχος αν είμαι σε κλάση
        if (currentClass.methods.containsKey(methodName)) {
            throw new Exception("Duplicate method declaration '" + methodName + "' in class '" + currentClass.name + "'");
        }
        //δημιουργώ την μέθοδο
        MethodSymbol methodSymbol = new MethodSymbol();
        //το όνομα της μεθόδου
        methodSymbol.name = methodName;
        //ο τύπος επιστροφής της μεθόδου
        methodSymbol.returnType = returnType; 
        currentMethod = methodSymbol; //αποθηκεύω την τρέχουσα μέθοδο
        
        if (n.f4.present()) {//έλεγχος αν υπάρχουν παράμετροι
            n.f4.accept(this, argu);
        }

        //για overloading
        String parentName = currentClass.parent; //αποθηκεύω την γονική κλάση
        while (parentName != null) { //έλεγχος αν υπάρχει γονική κλάση
            //αποθηκεύω την γονική κλάση
            ClassSymbol superClass = symbolTable.get(parentName); 
            if (superClass == null) break; //έλεγχος αν υπάρχει η γονική κλάση
            //break για την περίπτωση του Classes-error
            if (superClass.methods.containsKey(methodName)) {//έλεγχος αν υπάρχει η μέθοδος
                //αποθηκεύω την γονική μέθοδο
                MethodSymbol inherited = superClass.methods.get(methodName);
                //έλεγχος αν η μέθοδος είναι override
                if (!methodSymbol.returnType.equals(inherited.returnType)) {
                    throw new Exception("Method '" + methodName + "' in class '" + currentClass.name +
                        "' has different return type than in superclass '" + superClass.name + "'");
                }
                
                List<String> inheritedParams = new ArrayList<>(inherited.parameters.values()); 
                List<String> currentParams = new ArrayList<>(methodSymbol.parameters.values());
                //έλεγχος αν οι παράμετροι είναι ίδιες
                if (!inheritedParams.equals(currentParams)) {
                    throw new Exception("Method '" + methodName + "' in class '" + currentClass.name +
                        "' overloads method in superclass '" + superClass.name + "' with different parameter types");
                }

                break;
            }

            parentName = superClass.parent; //αποθηκεύω την γονική κλάση
        }

        n.f7.accept(this, argu);
        n.f8.accept(this, argu); 
        n.f10.accept(this, argu); 
        //αποθηκεύω την μέθοδο
        currentClass.methods.put(methodName, methodSymbol); 
        
        currentMethod = null; //αδειάζω την τρέχουσα μέθοδο

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
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);

        if (currentMethod != null) { //έλεγχος αν είμαι σε μέθοδο
            //έλεγχος αν υπάρχει ήδη η παράμετρος
            if (currentMethod.parameters.containsKey(name)) {
                throw new Exception("Duplicate parameter '" + name + "' in method '" + currentMethod.name + "'");
            }
            //αποθηκεύω την παράμετρο
            currentMethod.parameters.put(name, type);
        }
    
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        for ( Node node: n.f0.nodes) {//έλεγχος αν υπάρχουν παράμετροι
            node.accept(this, argu);
        }
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
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
        for (Node stmt : n.f1.nodes) {//έλεγχος αν υπάρχουν δηλώσεις
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
        n.f0.accept(this, argu);  
        n.f2.accept(this, argu);
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
    public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f2.accept(this, argu); 
        n.f5.accept(this, argu); 
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
    public String visit(IfStatement n, Void argu) throws Exception {
        n.f2.accept(this, argu); 
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
    public String visit(WhileStatement n, Void argu) throws Exception {
        n.f2.accept(this, argu); 
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
    public String visit(PrintStatement n, Void argu) throws Exception {
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
        return null;
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
        return null;
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
        return null;
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
        return null;
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
        return null;
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
        if (n.f4.present()) { 
            n.f4.accept(this, argu);  
        }
    
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
        return "int";
    }

    /**
    * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, Void argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, Void argu) throws Exception {
        return "boolean";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) throws Exception {
        return n.f0.toString(); //το όνομα της μεταβλητής
    }

    /**
    * f0 -> "this"
    */
    @Override
    public String visit(ThisExpression n, Void argu) throws Exception {
        return "this";
    }

    /**
     * f0 -> BooleanArrayAllocationExpression()
    *       | IntegerArrayAllocationExpression()
    */
    @Override
    public String visit(ArrayAllocationExpression n, Void argu) throws Exception {
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
    public String  visit(BooleanArrayAllocationExpression n, Void argu) throws Exception {
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
    public String visit(IntegerArrayAllocationExpression n, Void argu) throws Exception {
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
    public String visit(AllocationExpression n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
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

    /**
     * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /*ξεχωριστή συνάρτηση για το πρώτο πέρασμα με εκτύπωση του symbol table*/
    public void printSymbolTable() { 
        for (String className : symbolTable.keySet()) {
            //εκτύπωση της κλάσης
            ClassSymbol cls = symbolTable.get(className);
            System.out.println("Class: " + cls.name + (cls.parent != null ? " extends " + cls.parent : ""));
            //εκτύπωση των πεδίων
            if (!cls.fields.isEmpty()) {
                System.out.println("  Fields:");
                for (String fieldName : cls.fields.keySet()) {//έλεγχος αν υπάρχουν πεδία
                    System.out.println("    " + fieldName + " : " + cls.fields.get(fieldName));
                }
            }
            //εκτύπωση των μεθόδων
            if (!cls.methods.isEmpty()) {
                System.out.println("  Methods:");
                for (String methodName : cls.methods.keySet()) {//έλεγχος αν υπάρχουν μέθοδοι
                    MethodSymbol method = cls.methods.get(methodName);
                    System.out.print("    " + method.name + "(");
    
                    boolean first = true; //έλεγχος αν είναι η πρώτη παράμετρος
                    for (String param : method.parameters.keySet()) {//έλεγχος αν υπάρχουν παράμετροι
                        if (!first) System.out.print(", "); 
                        System.out.print(method.parameters.get(param) + " " + param);
                        first = false; 
                    }
                    System.out.println(") : " + method.returnType);
    
                    if (!method.locals.isEmpty()) {//έλεγχος αν υπάρχουν τοπικές μεταβλητές
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

