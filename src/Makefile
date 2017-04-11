
JAVAC = javac
JVM= $(JAVA_HOME)/bin/java
CPATH = ./main/java/
PATHTONODE = ./main/nodes
PATHTOCLIENT = ./main/clients
PATHNODECLASS = ../../../main/java/
PATHTOCLIENTCLASS = ../../../main/java/
CBUILD = ./build
export AKKA_CLASSPATH

#------COMPILE------

compileAll:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Client.java $(CPATH)Message.java $(CPATH)NodeApp.java $(CPATH)Item.java $(CPATH)PendingOperation.java $(CPATH)PendingRead.java  $(CPATH)PendingWrite.java $(CPATH)Utilities.java
	
compileClient:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Client.java $(CPATH)Message.java $(CPATH)Item.java
	
compileNode:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)NodeApp.java $(CPATH)Message.java $(CPATH)Item.java

compileMessage:
	@$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Message.java $(CPATH)Item.java
	
compileItem:
	 @$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Item.java
	 
compilePendingOperation:
	 @$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)PendingOperation.java

compilePendingRead:
	 @$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)PendingRead.java

compileItemPendingWrite:
	 @$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)PendingWrite.java
	 
compileUtilities:
	 @$(JAVAC) -cp $(AKKA_CLASSPATH) $(CPATH)Utilities.java
	
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
	
deletePendingOperation:
	@rm $(CPATH)PendingOperation*.class

deletePendingRead:
	@rm $(CPATH)PendingRead*.class
	
deletePendingWrite:
	@rm $(CPATH)PendingWrite*.class
	
deleteUtilities:
	@rm $(CPATH)Utilities*.class

#----RUN NODES

runNode0:
	@cd $(PATHTONODE)/0 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS) NodeApp
	
runNode1:
	@cd $(PATHTONODE)/1 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS) NodeApp

runNode2:
	@cd $(PATHTONODE)/2 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS) NodeApp

runNode3:
	@cd $(PATHTONODE)/3 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS) NodeApp

runNode7:
	@cd $(PATHTONODE)/7 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHNODECLASS) NodeApp

		
#----RUN CLIENTS

runClient0:
	@cd $(PATHTOCLIENT)/0 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHTOCLIENTCLASS) Client
	
runClient1:
	@cd $(PATHTOCLIENT)/1 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHTOCLIENTCLASS) Client
	
runClient2:
	@cd $(PATHTOCLIENT)/2 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHTOCLIENTCLASS) Client

runClient3:
	@cd $(PATHTOCLIENT)/3 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHTOCLIENTCLASS) Client
	
runClient4:
	@cd $(PATHTOCLIENT)/4 && $(JVM) -cp $(AKKA_CLASSPATH):.:$(PATHTOCLIENTCLASS) Client



