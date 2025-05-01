#Το Makefile αποτελεί τροποποίηση του ήδη δοσμένου.
#-te για throw exception που ζητήθηκε για να αναφέρει τα τυχόν σφάλματα.
#Η αναδρομική διαγραφή των φακέλων και των αρχείων έγινε προκειμένου να βοηθήσει στο τι δημιουργήθηκε τελικά. 

all: compile

compile:
	java -jar lib/jtb132di.jar -te src/MiniJava.jj
	java -jar lib/javacc5.jar src/MiniJava-jtb.jj
	javac Main.java

clean:
	find . -name "*.class" -type f -delete
	rm -rf syntaxtree visitor
	rm -f src/MiniJava-jtb.jj

