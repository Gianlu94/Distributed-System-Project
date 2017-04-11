
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
	
#----HELP 
help:
	@echo "\n\n--------------LIST OF COMMANDS---------------\n"
	@echo "\n ---COMPILE--- \n"
	@echo "\n   compileAll: compile project\n"
	@echo "\n   compileClient: compile client class\n"
	@echo "\n   compileNode: compile Node class\n"
	@echo "\n   compileMessage: compile Message class\n"
	@echo "\n   compileItem: compile Item class\n"
	@echo "\n   compilePendingOperation: compile PendingOperation class\n"
	@echo "\n   compilePendingRead: compile PendingRead class\n"
	@echo "\n   compilePendingWrite: compile PendingOperation class\n"
	@echo "\n   compileUtilities: compile Utilities class\n"
	
	@echo "\n ---DELETE--- \n"
	@echo "\n   deleteAll: delete class files\n"
	@echo "\n   deleteClient: delete client class\n"
	@echo "\n   deleteNode: delete Node class\n"
	@echo "\n   deleteMessage: delete Message class\n"
	@echo "\n   deleteItem: delete Item class\n"
	@echo "\n   deletePendingOperation: delete PendingOperation class\n"
	@echo "\n   deletePendingRead: delete PendingRead class\n"
	@echo "\n   deletePendingWrite: delete PendingOperation class\n"
	@echo "\n   deleteUtilities: delete Utilities class\n"
	
	@echo "\n ---RUNNODES--- \n"
	@echo "\n   runNode<i>: run Node i-th (i = {0,1,2,3,7})\n"
	
	@echo "\n ---RUNCLIENTS--- \n"
	@echo "\n   runClient<i>: run Client i-th (i = {0,1,2,3,4})\n"
	
	
	
	



