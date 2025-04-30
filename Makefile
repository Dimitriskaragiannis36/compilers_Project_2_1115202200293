#Επειδή στην προηγούμενη εργασία είχα το δικό μου μονοπάτι, αποφάσισα να το βελτιώσω.
#Προστίθενται πλέον τα src, lib και τα εργαλεία εντός του αποθετηρίου.
#Σβήνουμε ολόκληρους τους φακέλους αναδρομικά για να μην μπερδεύεται το μάτι.

all: compile

compile:
	java -jar lib/javacc5.jar src/MiniJava.jj
	java -jar lib/jtb132di.jar src/MiniJava.jj
	# javac Main.java

clean:
	rm -rf *.class *.java *~
	rm -rf syntaxtree visitor
	rm -f src/MiniJava-jtb.jj

