import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import syntaxtree.*;
import visitor.*;

public class MyVisitor2 extends GJDepthFirst<String, Void> {
    LinkedHashMap<String, MyVisitor.ClassSymbol> symbolTable;
    MyVisitor.ClassSymbol currentClass = null;
    MyVisitor.MethodSymbol currentMethod = null;

    HashMap<String, Integer> fieldOffsets = new HashMap<>();  
    HashMap<String, Integer> methodOffsets = new HashMap<>();

    public MyVisitor2(LinkedHashMap<String, MyVisitor.ClassSymbol> symbolTable) {
        this.symbolTable = symbolTable;
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
        currentClass = symbolTable.get(className);

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
        n.f0.accept(this, argu);
        
        String classname = n.f1.accept(this, argu);

        MyVisitor.ClassSymbol classSymbol = symbolTable.get(classname);
        if (classSymbol == null) {
            throw new Exception("Class not found in symbol table: " + classname);
        }
        currentClass = classSymbol;

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        System.out.println("-----------");
        System.out.println("Class: " + classname);

        int fieldOffset = 0;
        for (Map.Entry<String, String> field : currentClass.fields.entrySet()) {
            String fieldName = field.getKey();
            String fieldType = field.getValue();

            System.out.println(classname + "." + fieldName + " : " + fieldOffset);
            fieldOffsets.put(classname + "." + fieldName, fieldOffset);
            fieldOffset += getSize(fieldType);
        }

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
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, argu);
        String parentname = n.f3.accept(this, argu);

        MyVisitor.ClassSymbol classSymbol = symbolTable.get(classname);
        if (classSymbol == null) {
            throw new Exception("Class not found: " + classname);
        }
        currentClass = classSymbol;

        System.out.println("-----------");
        System.out.println("Class: " + classname);
    
        
        MyVisitor.ClassSymbol parentClass = symbolTable.get(parentname);
        if (parentClass == null) {
            throw new Exception("Parent class not found: " + parentname);
        }
    
        
        int fieldOffset = 0;
        int methodOffset = 0;
    
        
        for (Map.Entry<String, String> entry : parentClass.fields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldType = entry.getValue();
            System.out.println(parentname + "." + fieldName + " : " + fieldOffset);
            fieldOffsets.put(parentname + "." + fieldName, fieldOffset);
            fieldOffset += getSize(fieldType);
        }
    
       
        Map<String, Integer> inheritedMethodOffsets = new LinkedHashMap<>();
        for (String methodName : parentClass.methods.keySet()) {
            inheritedMethodOffsets.put(methodName, methodOffset);
            System.out.println(parentname + "." + methodName + " : " + methodOffset);
            methodOffsets.put(parentname + "." + methodName, methodOffset);
            methodOffset += 8;
        }
    
        
        for (Map.Entry<String, String> field : classSymbol.fields.entrySet()) {
            String fieldName = field.getKey();
            String fieldType = field.getValue();
            System.out.println(classname + "." + fieldName + " : " + fieldOffset);
            fieldOffsets.put(classname + "." + fieldName, fieldOffset);
            fieldOffset += getSize(fieldType);
        }
    
        for (String methodName : classSymbol.methods.keySet()) {
            if (inheritedMethodOffsets.containsKey(methodName)) {
                
                int inheritedOffset = inheritedMethodOffsets.get(methodName);
                System.out.println(classname + "." + methodName + " : " + inheritedOffset);
                methodOffsets.put(classname + "." + methodName, inheritedOffset);
            } else {
                
                System.out.println(classname + "." + methodName + " : " + methodOffset);
                methodOffsets.put(classname + "." + methodName, methodOffset);
                methodOffset += 8;
            }
        }
    
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"/* */
    @Override
    public String visit(VarDeclaration n, Void argu) throws Exception {
        
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

        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {

        return null;
    }

     /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        
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
        n.f0.f0.toString(); 
    
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
        return n.f0.toString();
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
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) throws Exception {
        return n.f0.toString();
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

    //ξεχωριστή βοηθητική συνάρτηση για τα μεγέθη
    private int getSize(String type) {
        if (type.equals("int")) return 4;
        if (type.equals("boolean")) return 1;
        return 8; 
    }
    
}

