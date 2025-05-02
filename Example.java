class Example {
    public static void main(String[] args) {
        int x;
        boolean a;
        boolean b;
        boolean c;
        boolean result;
        
        x=5;
        a = true;
        b = false;
        c = true;

        result = a && b;            
        result = !a && c;         
        result = !(a && b);         
        result = !(!c);

        System.out.println(x);
        System.out.println(c);
        System.out.println(result);
    }
}

class A {
    int i;
    A a;
    int[] array;
    boolean[] boolArray;

    public int foo(int i, int j) {
        int k;
        k = i+j;

        return k; 
    }
    public int bar(){ return 1; }
    public boolean compare(int x, int y, boolean flag) {
        return flag;
    }

    public int[] getArray(int size) {
        return new int[size];
    }

    public boolean[] getBoolArray() {
        return boolArray;
    }
}

class B extends A {
    int i;
    boolean flag;

    public int foo(int i, int j) { return i+j; }
    public int foobar(boolean k){ return 1; }
    public A getA(A paramA, int num) {
        return paramA;
    }
    
    public boolean[] mergeBoolArrays(boolean[] b1, boolean[] b2) {
        return b1;  
    }
}
