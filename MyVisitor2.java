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

        public Context(MyVisitor.ClassSymbol cls, MyVisitor.MethodSymbol mthd) {
            this.currClass = cls;
            this.currMethod = mthd;
        }

        public Context(MyVisitor.ClassSymbol cls) {
            this.currClass = cls;
            this.currMethod = null;
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
        String className = n.f1.accept(this, null);
        MyVisitor.ClassSymbol cls = symbolTable.get(className);
        if (cls == null)
            throw new Exception("Main class not found: " + className);
    
        currentClass = cls;
    
        
        Context context = new Context(cls);
    
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
        n.f0.accept(this, argu);
        
        String classname = n.f1.accept(this, argu);

        MyVisitor.ClassSymbol classSymbol = symbolTable.get(classname);
        if (classSymbol == null) {
            throw new Exception("Class not found in symbol table: " + classname);
        }
        currentClass = classSymbol;

        Context classContext = new Context(currentClass);
        n.f3.accept(this, classContext);
        n.f4.accept(this, classContext);

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
    public String visit(ClassExtendsDeclaration n, Context argu) throws Exception {
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
        
        Context classContext = new Context(currentClass);
        n.f5.accept(this, classContext); 
        n.f6.accept(this, classContext); 
        
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
    public String visit(VarDeclaration n, Context argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String varName = n.f1.accept(this, argu);
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

        String methodName = n.f2.accept(this, argu); 

        MyVisitor.MethodSymbol methodSymbol = argu.currClass.methods.get(methodName);
        if (methodSymbol == null) {
            throw new Exception("Method not found in class: " + argu.currClass.name + " -> " + methodName);
        }

        currentMethod = methodSymbol;

        Context methodContext = new Context(argu.currClass, currentMethod);

        if (n.f4.present()) {
            n.f4.node.accept(this, methodContext); 
        } 
        n.f7.accept(this, methodContext); 
        n.f8.accept(this, methodContext); 
        n.f10.accept(this, methodContext); 

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
    * f0 -> BooleanArrayType()
    *       | IntegerArrayType()
    */
    @Override
    public String visit(ArrayType n, Context argu) throws Exception {
        return n.f0.accept(this, argu);
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
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Context argu) throws Exception {
        return n.f0.toString();
    }

    //ξεχωριστή βοηθητική συνάρτηση για τα μεγέθη
    private int getSize(String type) {
        if (type.equals("int")) return 4;
        if (type.equals("boolean")) return 1;
        return 8; 
    }
    
}

