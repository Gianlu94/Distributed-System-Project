PATHTOSRC = ./src/

#------COMPILE------

compileAll:
	@make -C $(PATHTOSRC) -s  compileAll
	
compileClient:
	@make -C $(PATHTOSRC) -s compileClient
	
compileNode:
	@make -C $(PATHTOSRC) -s  compileNode

compileMessage:
	@make -C $(PATHTOSRC) -s  compileMessage
	
compileItem:
	@make -C $(PATHTOSRC) -s compileItem
	 
compilePendingOperation:
	@make -C $(PATHTOSRC) -s compilePendingOperation

compilePendingRead:
	@make -C $(PATHTOSRC) -s  compilePendingRead

compileItemPendingWrite:
	@make -C $(PATHTOSRC) -s  compileItemPendingWrite
	
compileUtilities:
	@make -C $(PATHTOSRC) -s compileUtilities
	
#------DELETE------

deleteAll:
	@make -C $(PATHTOSRC) -s  deleteAll

deleteClient:
	@make -C $(PATHTOSRC) -s  deleteClient

deleteNode:
	@make -C $(PATHTOSRC) -s  deleteNode
	
deleteMessage:
	@make -C $(PATHTOSRC) -s deleteMessage
	
deleteItem:
	@make -C $(PATHTOSRC) -s  deleteItem
	
deletePendingOperation:
	@make -C $(PATHTOSRC) -s  deletePendingOperation

deletePendingRead:
	@make -C $(PATHTOSRC) -s  deletePendingRead
	
deletePendingWrite:
	@make -C $(PATHTOSRC) -s  deletePendingWrite
	
deleteUtilities:
	@make -C $(PATHTOSRC) -s  deleteUtilities

#----RUN NODES

runNode0:
	@make -C $(PATHTOSRC) -s runNode0
	
runNode1:
	@make -C $(PATHTOSRC) -s runNode1

runNode2:
	@make -C $(PATHTOSRC) -s runNode2

runNode3:
	@make -C $(PATHTOSRC) -s runNode3

runNode5:
	@make -C $(PATHTOSRC) -s runNode5

runNode7:
	@make -C $(PATHTOSRC) -s runNode7

		
#----RUN CLIENTS

runClient0:
	@make -C $(PATHTOSRC) -s runClient0
	
runClient1:
	@make -C $(PATHTOSRC) -s runClient1
	
runClient2:
	@make -C $(PATHTOSRC) -s runClient2

runClient3:
	@make -C $(PATHTOSRC) -s runClient3
	
runClient4:
	@make -C $(PATHTOSRC) -s runClient4
	
#----HELP
help:
	@make -C $(PATHTOSRC) -s help



