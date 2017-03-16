
JAVAC = javac
JVM= $(JAVA_HOME)/bin/java
CPATH = ./main/java/
PATHTONODE = ./main/nodes
PATHNODECLASS = ../../../ main.java.NodeApp
PATHTOCLIENTCLASS = main.java.
CBUILD = ./build
CLIENTPATH = main.java
export AKKA_CLASSPATH

#------COMPILE------

compileAll:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Client.java $(CPATH)Message.java $(CPATH)NodeApp.java $(CPATH)Item.java
	
compileClient:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Client.java $(CPATH)Message.java $(CPATH)Item.java
	
compileNode:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)NodeApp.java $(CPATH)Message.java $(CPATH)Item.java

compileMessage:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Message.java $(CPATH)Item.java
	
compileItem:
	 @$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Item.java
	
#------DELETE------

deleteAll:
	@rm $(CPATH)*.class

deleteClient:
	@rm $(CPATH)Client*.class

deleteNode:
	@rm $(CPATH)NodeApp*.class
	
deleteMessage:
	@rm $(CPATH)Message*.class
	
deleteItem:
	@rm $(CPATH)Item*.class

#----RUN NODES

runNode0:
	@cd $(PATHTONODE)/0 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS)
	
runNode1:
	@cd $(PATHTONODE)/1 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS)

runNode2:
	@cd $(PATHTONODE)/2 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS)

runNode3:
	@cd $(PATHTONODE)/3 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS)

runNode7:
	@cd $(PATHTONODE)/7 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS)

		
#----RUN CLIENT (ONLY ONE)		
runClient:
	@$(JVM) -cp $(AKKA_CLASSPATH):. $(PATHTOCLIENTCLASS)Client

