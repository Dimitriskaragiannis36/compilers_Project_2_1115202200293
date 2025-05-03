class Example {
    public static void main(String[] args) {
        int Dim_Kar293$;
        int x;
        //int x;
        int y;
        int z;
        boolean a;
        boolean b;
        boolean c;
        boolean result;
        int[] arr;
        A objA;
        B objB;
        int[] newArr;
        boolean[] boolArr;
        
        x = 5;
        y = 3;
        z = 0;
        a = true;
        b = false;
        c = true;
        arr = new int[10];

        objA = new A();
        objB = new B();

        result = a && b;            
        result = !a && c;         
        result = !(a && b);         
        result = !(!c);
        result = x < y;

        z = x + y;        
        z = x - y;         
        z = x * y;         
        

        z = arr[2];        
        System.out.println(arr.length);

        System.out.println(x);
        System.out.println(c);
        System.out.println(z);
        System.out.println(result);

        z = objA.foo(x, y);
        result = objA.compare(x, y, a);
        newArr = objA.getArray(5);
        boolArr = objA.getBoolArray();

        z = objB.foobar(true);
        objA = objB.getA(objA, 42);
        boolArr = objB.mergeBoolArrays(boolArr, boolArr);

        arr[0] = x + y; 

        if (a) { 
            System.out.println(x); 
        } else {
            arr[1] = z;
        }

        while (x < 10) { 
            System.out.println(x);
            x = x + 1;
        }
    }
}

class A {
    int i;
    //int i;
    A a;
    int[] array;
    boolean[] boolArray;

    public int foo(int i, int j) {
        int k;
        boolean comp;
        int[] localIntArray;
        boolean[] localBoolArray;
        A newObj;

        k = i+j;
        comp = (i < j);
        localIntArray = new int[10];
        localBoolArray = new boolean[5];
        newObj = new A();

        if (comp) {
            localIntArray[0] = k; 
        } else {
            System.out.println(k); 
        }
    
        while (k < 100) {
            k = k + 1;
        }

        return k; 
    }

    /*public int foo(int a, int b) {  
        return a - b;
    }*/

    public int bar(){ return 1; }
    //public int bar(){ return 2; }
    public boolean compare(int x, int y, boolean flag) {
        return flag;
    }

    public int[] getArray(int size) {
        return new int[size];
    }

    public boolean[] getBoolArray() {
        return boolArray;
    }
    public int complexOperation(int a, int b, boolean c, boolean[] d, int[] e, A obj) {
        return a + b;
    }

    public int printMultiple(int x, int y, boolean flag) {
        System.out.println(x);
        System.out.println(y);
        if (flag) {
            System.out.println(1);
        } else {
            System.out.println(2);
        }
        return 0;
    }
}

class B extends A {
    int i;
    boolean flag;

    public int foo(int i, int j) { return i+j; }
    //public int foo(int x, int y) { return x*y; }
    public int foobar(boolean k){ return 1; }
    public A getA(A paramA, int num) {
        return paramA;
    }
    
    public boolean[] mergeBoolArrays(boolean[] b1, boolean[] b2) {
        return b1;  
    }
}

/*class A {  
    int x;
}*/