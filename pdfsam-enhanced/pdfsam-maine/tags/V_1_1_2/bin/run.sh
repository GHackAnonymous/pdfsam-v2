#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  pdfsam Bootstrap Script                                                  ##
##                                                                          ##
### ====================================================================== ###

warn() {
    echo "${PROGNAME}: $*"
}

die() {
    warn $*
    exit 1
}


DIRNAME="../"
PDFSAMJAR="$DIRNAME/@PDFSAM_JAR_NAME.jar"

# Setup the classpath
if [ ! -f "$PDFSAMJAR" ]; then
    die "Missing required file: $PDFSAMJAR"
fi
PDFSAM_CLASSPATH="$PDFSAMJAR"

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

# Setup pdfsam memory properties
JAVA_OPTS="-Xmx256m"

# Display our environment
echo "========================================================================="
echo ""
echo " pdfsam"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $PDFSAM_CLASSPATH"
echo ""
echo "========================================================================="
echo ""

# Execute the JVM in the foreground
      "$JAVA" $JAVA_OPTS \
         -classpath "$PDFSAM_CLASSPATH" \
         org.pdfsam.guiclient.GuiClient "$@"