CONTEXT =       ../webapps/ROOT
CLSDIR  =       $(CONTEXT)/WEB-INF/classes
LIBDIR  =       $(CONTEXT)/WEB-INF/lib
CLASSES =       db/DBInterface.class \
servlet/LoginServlet.class servlet/RegisterServlet.class \
req/RegisterRequest.class req/LoginRequest.class \
resp/Response.class resp/LoginResponse.class resp/ErrorResponse.class
LIBS    =       /usr/share/java/servlet-api-3.0.jar:$(LIBDIR)/gson-2.3.1.jar:$(LIBDIR)/commons-lang3-3.4.jar:$(LIBDIR)/commons-validator-1.4.1.jar

.SUFFIXES: .java .class

all: $(CLASSES)

clean:
	/bin/rm -f $(CLASSES) */*.class

install: $(CLASSES)
	install -m600 $(CLASSES) $(CLSDIR)

.java.class:
	javac -cp $(LIBS) */*.java
