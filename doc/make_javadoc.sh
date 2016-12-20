#!/bin/bash
# Create the Javadoc(tm) for the Ecologia project.
# @author Daniel Vedder
# @date 13/1/2015

echo "Deleting old Javadoc..."
rm -r javadoc/*

# Set variables
DOC_TITLE='Ecologia Documentation'
HEADER='Ecologia'
PACKAGES='main model view controller'

echo "Creating Javadoc..."
javadoc -sourcepath ../src \
	-doctitle $DOC_TITLE \
	-header $HEADER \
	-d javadoc \
	-private \
	-quiet \
	$PACKAGES
	
ln -s javadoc/index.html javadoc.html

echo "Done."
