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
    * f2 -> ";"
    
   
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String _ret=null;
        String type = n.f0.accept(this, argu);
        String varName = n.f1.accept(this, argu);
        
        if (currentMethod != null) {
            if (currentMethod.locals.containsKey(varName) || currentMethod.parameters.containsKey(varName)) {
                throw new Exception("Duplicate local variable or parameter '" + varName + "' in method '" + currentMethod.name + "'");
            }
            currentMethod.locals.put(varName, type);
        } else if (currentClass != null) {
            if (currentClass.fields.containsKey(varName)) {
                throw new Exception("Duplicate field '" + varName + "' in class '" + currentClass.name + "'");
            }
            currentClass.fields.put(varName, type);
        }
        return _ret;
    } */

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) throws Exception {
        return n.f0.toString();
    }

    //ξεχωριστή βοηθητική συνάρτηση για τα μεγέθη
    private int getSize(String type) {
        if (type.equals("int")) return 4;
        if (type.equals("boolean")) return 1;
        return 8; 
    }
    
}

