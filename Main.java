import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import syntaxtree.*;


public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java Main <file1> <file2> ... <fileN>");
            System.exit(1);
        }

        for (String filename : args) {
            FileInputStream fis = null;
            System.out.println("=== Processing file: " + filename + " ===");
            try{
                fis = new FileInputStream(filename);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");
                System.out.println("=== Running first visitor (symbol table) ===");
                MyVisitor eval = new MyVisitor();
                root.accept(eval, null);
                System.out.println("=== Running second visitor (type checking + offsets) ===");
                MyVisitor2 eval2 = new MyVisitor2(eval.symbolTable);
                root.accept(eval2, null);
                System.out.println("====================================");
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}