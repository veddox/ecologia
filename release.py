#!/usr/bin/python3
'''
Package Ecologia ready for release:
- create an executable JAR
- create/update Javadoc
- package source and binaries
- sign tar archive
- create checksum

Note: this script is Linux (and maybe Bash) specific!

@author Daniel Vedder
@version 14/03/2015
'''

import os
import sys

'''
Extract the version number from the main file
'''
def find_version():
    main_file = open("src/main/Ecologia.java")
    main_source = main_file.read()
    main_file.close()
    version_start = main_source.find("version = ")+11
    version_end = main_source.find('"', version_start)
    return main_source[version_start:version_end]

'''
Create an executable JAR file
'''
def create_jar(package_name):
    #compile all Java source files
    if "bin" not in os.listdir(os.getcwd()): os.mkdir("bin")
    os.system("rm -r bin/*")
    source_list = ""
    for package in os.listdir("src"):
        for jsrc in os.listdir("src/"+package):
            source_list = source_list+" src/"+package+"/"+jsrc
    os.system("javac -d bin "+source_list)
    #copy the newest documentation files into the binary folder
    os.mkdir("bin/doc")
    os.system("cp doc/help bin/doc")
    os.system("cp doc/concepts bin/doc")
    os.system("cp doc/COPYING bin/doc")
    #create the JAR file
    try:
        manifest_file = open("MANIFEST.MF", "w")
        manifest_text = "Class-Path: .\nMain-Class: main.Ecologia"
        manifest_file.write(manifest_text)
        manifest_file.close()
    except IOError:
        print("Failed to create JAR manifest file!")
    jar_command = "jar cfme "+package_name+""".jar MANIFEST.MF main.Ecologia \
-C bin main -C bin model -C bin controller -C bin view -C bin doc"""
    os.system(jar_command)
    os.system("rm MANIFEST.MF")

'''
Create the release archive and package all necessary files
'''
def package_files(package_name):
    os.mkdir(package_name)
    #XXX How do I make sure the analysis folder contains only the scripts?
    files = ["src", "doc", "analysis", package_name+".jar", "README", "release.py"]
    for f in files:
        os.system("cp -r "+f+" "+package_name)
    if "--zip" in sys.argv:
        os.system("zip -r "+package_name+".zip "+package_name)
    else:
        os.system("tar czf "+package_name+".tar.gz "+package_name)
    os.system("rm -r "+package_name)

'''
Prepare the release files
'''
def release():
    print("Preparing to package release...")
    version = find_version()
    print("Identified version "+version)
    package_name = "ecologia-"+version
    if not "--no-javadoc" in sys.argv:
        print("Creating Javadoc...")
        os.chdir("doc")
        os.system("./make_javadoc.sh")
        os.chdir("..")
    print("Creating JAR file...")
    create_jar(package_name)
    print("Packaging files...")
    package_files(package_name)
    if not "--zip" in sys.argv and not "--no-sign" in sys.argv:
        print("Creating checksum...")
        os.system("sha256sum "+package_name+".tar.gz > "+package_name+".tar.gz.sha256sum")
        print("Creating signature...")
        os.system("gpg --armor --sign --detach-sig "+package_name+".tar.gz")
    print("Finishing...")
    if "releases" not in os.listdir(os.getcwd()):
        os.mkdir("releases")
    if package_name in os.listdir("releases"):
        os.system("rm -r releases/"+package_name)
    os.mkdir("releases/"+package_name)
    os.system("mv "+package_name+"* releases/"+package_name)
    print("Done.")

def print_help():
    print("This is the Ecologia release script.\n")
    print("Accepted commandline parameters:")
    print("\t--help -h")
    print("\t--zip")
    print("\t--no-sign")
    print("\t--no-javadoc")
    print("\nFor more details, please read the source.\n")

if __name__ == '__main__':
    if "--help" in sys.argv or "-h" in sys.argv:
        print_help()
    else: release()
