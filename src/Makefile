
JAVAC = javac
JVM= $(JAVA_HOME)/bin/java
CPATH = ./main/java/
CBUILD = ./build
CLIENTPATH = main.java
export AKKA_CLASSPATH


compileAll:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Client.java $(CPATH)Message.java $(CPATH)NodeApp.java $(CPATH)Item.java 
	
deleteAll:
	@rm $(CPATH)*.class
	
runClient:
	@$(JVM) -cp $(AKKA_CLASSPATH):. main.java.Client


