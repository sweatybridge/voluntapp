PACKAGES = db req resp servlet listener filter exception sql chat utils

CONTEXT = ./webapps/ROOT
BIN = $(CONTEXT)/WEB-INF/classes/
LIB = $(CONTEXT)/WEB-INF/lib/
SRC = ./src/
RES = resource

EMPTY = 

JAVA_FILES = $(subst $(SRC), $(EMPTY), $(wildcard $(SRC)*.java))
PACKAGEDIRS = $(addprefix $(SRC), $(PACKAGES))
PACKAGEFILES = $(subst $(SRC), $(EMPTY), $(foreach DIR, $(PACKAGEDIRS), $(wildcard $(DIR)/*.java)))
ALL_FILES = $(PACKAGEFILES) $(JAVA_FILES)

# One of these should be the "main" class listed in Runfile
# CLASS_FILES = $(subst $(SRC), $(BIN), $(ALL_FILES:.java=.class))
CLASS_FILES = $(ALL_FILES:.java=.class)

all : $(addprefix $(BIN), $(CLASS_FILES)) properties

properties :
	cp -r $(SRC)$(RES) $(BIN)$(RES)

$(BIN)%.class : $(SRC)%.java
	javac -d $(BIN) -cp $(LIB)*:$(SRC):./lib/* $<

clean : 
	rm -rf $(BIN)*

.PHONY : clean properties

