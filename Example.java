class Example {
    public static void main(String[] args) {
        int x;
        x = 5;
    }
}

class A {
    int i;
    A a;

    public int foo(int i, int j) {
        int k;
        k = i+j;

        return k; 
    }
    public int bar(){ return 1; }
    public boolean compare(int x, int y, boolean flag) {
        return flag;
    }
}

class B extends A {
    int i;

    public int foo(int i, int j) { return i+j; }
    public int foobar(boolean k){ return 1; }
    public A getA(A paramA, int num) {
        return paramA;
    }
}
